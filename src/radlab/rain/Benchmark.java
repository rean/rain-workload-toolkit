/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package radlab.rain;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.*;

import radlab.rain.util.ConfigUtil;

/**
 * The Benchmark class provides a framework to initialize and run a benchmark
 * specified by a provided scenario.
 */
public class Benchmark
{
	/** Name of the main benchmark thread. */
	public String threadName = "Benchmark-thread";
	
	/**
	 * Amount of time (in milliseconds) to wait before threads start issuing
	 * requests. This allows all of the threads to start synchronously.
	 */
	public long timeToStart = 5000;
	
	/**
	 * Initializes the benchmark as specified by the provided scenario. This
	 * includes creating the n-threads needed, giving them the scenario, and
	 * starting them.
	 *  
	 * @param scenario    The scenario specifying parameters for the benchmark.
	 */
	public void start( Scenario scenario ) throws Exception
	{
		Thread.currentThread().setName( threadName );
		
		// Create a new thread pool -- either a fixed or an unbounded thread pool.
		// (In practice, we should put a limit on the size of the thread pool).
		// The thread pool will be used to hand off asynchronous requests.
		ExecutorService pool = Executors.newCachedThreadPool(); // Executors.newFixedThreadPool( maxThreads );
		
		LinkedList<LoadGenerationStrategy> threads = new LinkedList<LoadGenerationStrategy>();
		
		// Calculate the run timings that will be used for all threads.
		//     start     startS.S.              endS.S.     end
		//     | ramp up |------ duration ------| ramp down |
		long start = System.currentTimeMillis() + timeToStart;
		long startSteadyState = start + (scenario.getRampUp() * 1000);
		long endSteadyState   = startSteadyState + (scenario.getDuration() * 1000);
		
		for ( ScenarioTrack track : scenario.getTracks() )
		{
			// Start the scoreboard. It needs to know the timings because we only
			// want to retain metrics generated during the steady state interval.
			IScoreboard scoreboard = track.createScoreboard( null ); 
			if( scoreboard != null )
			{
				scoreboard.initialize( startSteadyState, endSteadyState );
				scoreboard.setMetricSnapshotInterval( (long) (track.getMetricSnapshotInterval() * 1000) );
				scoreboard.start();
			}
			track.setScoreboard(scoreboard);
			
			// Create enough threads for maximum users needed by the scenario.
			for( int i = 0; i < track.getMaxUsers(); i++ )
			{
				Generator generator = track.createWorkloadGenerator( track.getGeneratorClassName(), track.getGeneratorParams() );
				generator.setScoreboard( scoreboard );
				generator.setMeanCycleTime( (long)(track.getMeanCycleTime() * 1000) );
				generator.setMeanThinkTime( (long)(track.getMeanThinkTime() * 1000) );
				LoadGenerationStrategy lgThread = new PartlyOpenLoopLoadGeneration( generator, i );
				generator.setName( lgThread.getName() );
				generator.initialize();
				lgThread.setInteractive( track.getInteractive() );
				lgThread.setSharedWorkPool( pool );
				lgThread.setTimeStarted( start );
				
				threads.add( lgThread );
				
				lgThread.start();
			}
		}
		
		// Wait for all of the threads to finish.
		for( LoadGenerationStrategy lgThread : threads )
		{
			try
			{
				lgThread.join();
			}
			catch( InterruptedException ie )
			{
				System.out.println( "[BENCHMARK] Main thread interrupted... exiting!" );
			}
			finally
			{
				lgThread.dispose();
			}
		}
		
		// Purge threads.
		System.out.println( "[BENCHMARK] Purging threads and shutting down... exiting!" );
		threads.clear();
		
		// Shutdown the scoreboards and tally up the results.
		for ( ScenarioTrack track : scenario.getTracks() )
		{
			track.getScoreboard().stop();
			track.getScoreboard().printStatistics( System.out );
			// Collect scoreboard results
			// Collect object pool results
			track.getObjectPool().shutdown();
		}
		
		// Write out scoreboard results
				
		// Shutdown the shared threadpool.
		pool.shutdown();
		try
		{
			System.out.println( "[BENCHMARK] waiting up to 10 seconds for shared threadpool to shutdown!" );
			pool.awaitTermination( 10000, TimeUnit.MILLISECONDS );
			if( !pool.isTerminated() )
			{
				pool.shutdownNow();
			}
		}
		catch( InterruptedException ie )
		{
			System.out.println( "[BENCHMARK] INTERRUPTED while waiting for shared threadpool to shutdown!" );
		}
		System.out.println( "[BENCHMARK] finished!" );
	}
	
	/**
	 * Runs the benchmark. The only required argument is the configuration
	 * file path (e.g. config/rain.config.sample.json).
	 */
	public static void main( String[] args ) throws Exception
	{
		if ( args.length < 1 ) {
			System.out.println( "Unspecified path to configuration file!" );
			System.exit( 1 );
		}
		
		String filename = args[0];
		JSONObject jsonConfig = null;
		try
		{
			String fileContents = ConfigUtil.readFileAsString( filename );
			jsonConfig = new JSONObject( fileContents );
		}
		catch ( IOException e )
		{
			System.out.println( "ERROR loading configuration file " + filename + ". Reason: " + e.toString() );
			System.exit( 1 );
		}
		catch ( JSONException e )
		{
			System.out.println( "ERROR parsing configuration file " + filename + " as JSON. Reason: " + e.toString() );
			System.exit( 1 );
		}
		
		Scenario scenario = new Scenario( jsonConfig );
		scenario.start();
		
		Benchmark benchmark = new Benchmark();
		benchmark.start( scenario );
		
		scenario.end();
	}
}
