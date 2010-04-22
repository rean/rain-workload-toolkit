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

import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The PartlyOpenLoopLoadGeneration class is a thread that supports partly
 * open loop load generation.
 */
public class PartlyOpenLoopLoadGeneration extends LoadGenerationStrategy 
{
	/** Minimum increments of intervals of inactivity in seconds. */
	public static int INACTIVE_DURATION = 1000;
	
	/** The probability of using open loop vs. closed loop. */
	protected double _openLoopProbability;
	
	/** The random number generator used to decide which loop to use. */
	protected Random _random = new Random();
	
	/** Log writer registered to the scoreboard. */
	protected FileWriter _logWriter;
	
	/** Error log writer registered to the scoreboard. */
	protected FileWriter _errorLogWriter;
	
	/** Statistic: number of synchronous operations run. */
	protected long _synchOperations = 0;
	
	/** Statistic: number of asynchronous operations run. */
	protected long _asynchOperations = 0;
	
	/**
	 * Creates a load generation thread that supports partly open loop.
	 * 
	 * @param generator     The generator used to generate this thread's load.
	 * @param id            The ID of this thread; used to sleep it on demand.
	 */
	public PartlyOpenLoopLoadGeneration( Generator generator, long id )
	{
		super( generator, id );
		
		// If a thread dies for some reason (e.g. the JVM runs out of heap
		// space, which causes an Error not an Exception), use our uncaught
		// exception handler to catch it and print some useful debugging info.
		Thread.setDefaultUncaughtExceptionHandler( new UnexpectedDeathHandler() );
	}
	
	/** Resets the number of synchronous/asynchronous operations run. */
	public void resetStatistics()
	{
		this._synchOperations	= 0;
		this._asynchOperations	= 0;
	}
	
	/** Disposes of objects used by this thread. */
	public void dispose()
	{
		this._generator.dispose();
	}
	
	/** Runs this partly open loop load generation thread. */
	public void run()
	{
		String threadName = this.getName();
		this.resetStatistics();
		this.createLogWriters();
		
		this.loadTrackConfiguration( this._generator.getTrack() );
		
		try
		{
			this.sleepUntil( this._timeStarted );
			
			int lastOperationIndex = NO_OPERATION_INDEX;
			while ( System.currentTimeMillis() <= this._timeToQuit )
			{
				if ( !this.isActive() )
				{
					this._lgState = LGState.Inactive;
					Thread.sleep( INACTIVE_DURATION );
				}
				else
				{
					this._lgState = LGState.Active;
					Operation nextOperation = this._generator.nextRequest( lastOperationIndex );
					
					// Update last operation index.
					lastOperationIndex = nextOperation.getOperationIndex();
					
					// Store the thread name/ID so we can organize the traces.
					nextOperation.setGeneratedBy( threadName );
					nextOperation.setGeneratorThreadID( this._id );
					
					// Decide whether to do things open or closed
					double randomDouble = this._random.nextDouble();
					if ( randomDouble <= this._openLoopProbability )
					{
						this.doAsyncOperation( nextOperation );
					}
					else
					{
						this.doSyncOperation( nextOperation );
					}
				}
			}	
		}
		catch( InterruptedException ie )
		{
			System.out.println( "[" + threadName + "] load generation thread interrupted exiting!" );
		}
		catch( Exception e )
		{
			System.out.println( "[" + threadName + "] load generation thread died by exception! Reason: " + e.toString() );
			e.printStackTrace();
		}
		finally
		{
			this.closeLogWriters();
		}
	}
	
	/**
	 * Runs the provided operation asynchronously and sleeps this thread on
	 * the cycle time.
	 * 
	 * @param operation     The operation to run asynchronously.
	 * 
	 * @throws InterruptedException
	 */
	protected void doAsyncOperation( Operation operation ) throws InterruptedException
	{
		this._asynchOperations++;
		
		long wakeUpTime = System.currentTimeMillis() + this._generator.getCycleTime();
		
		operation.setAsync( true );
		this.doOperation( operation );
		
		this.sleepUntil( wakeUpTime );
	}
	
	/**
	 * Runs the provided operation synchronously and sleeps this thread on the
	 * think time.
	 * 
	 * @param operation     The operation to run synchronously.
	 * 
	 * @throws InterruptedException
	 */
	protected void doSyncOperation( Operation operation ) throws InterruptedException
	{
		this._synchOperations++;
		
		operation.setAsync( false );
		this.doOperation( operation );
		
		this.sleepUntil( System.currentTimeMillis() + this._generator.getThinkTime() );
	}
	
	/**
	 * Loads the configuration from the provided scenario track. This sets the
	 * open loop probability as well as time markers for when this thread
	 * starts, when steady state should begin (i.e. when metrics start
	 * recording), and when steady state ends.
	 * 
	 * @param track     The track from which to load the configuration.
	 */
	protected void loadTrackConfiguration( ScenarioTrack track )
	{
		this._openLoopProbability = this._generator.getTrack().getOpenLoopProbability();
		
		if ( this._timeStarted == TIME_NOT_SET )
		{
			this._timeStarted = System.currentTimeMillis();
		}
		
		// Configuration is specified in seconds; convert to milliseconds.
		long rampUp   = track.getRampUp()   * 1000;
		long duration = track.getDuration() * 1000;
		long rampDown = track.getRampDown() * 1000;
		
		this._startSteadyState = this._timeStarted + rampUp;
		this._endSteadyState   = this._startSteadyState + duration;
		this._timeToQuit       = this._endSteadyState + rampDown;
	}
	
	/**
	 * Creates log and error writers and registers them with the scoreboard. 
	 */
	protected void createLogWriters()
	{
		String threadName = this.getName();
		
		try
		{
			this._logWriter = new FileWriter( new File( "thread-" + threadName + ".log" ) );
			if ( this._logWriter != null )
			{
				this._generator.getScoreboard().registerLogHandle( threadName, this._logWriter );
			}
			
			this._errorLogWriter = new FileWriter( new File( "error-thread-" + threadName + ".log" ) );
			if ( this._errorLogWriter != null )
			{
				this._generator.getScoreboard().registerErrorLogHandle( threadName, this._errorLogWriter );
			}
		}
		catch( IOException ioe )
		{
			System.out.println( "[" + threadName + "] could not create trace log. Reason: " + ioe.toString() );
		}
	}
	
	/**
	 * Closes the log writer and ensures that no other thread can write to it.
	 */
	protected void closeLogWriters()
	{
		String threadName = this.getName();
		// Close trace logging writer
		if ( this._logWriter != null )
		{
			this._generator.getScoreboard().deRegisterLogHandle( threadName );
			
			synchronized( this._logWriter )
			{
				try
				{
					this._logWriter.flush();
					this._logWriter.close();
					this._logWriter = null;
				}
				catch( IOException ioe )
				{
					System.out.println( "[" + threadName + "] failed to close trace log Reason: " + ioe.toString() );
				}
			}
		}
		// Do the same for this._errorLogWriter
		if( this._errorLogWriter != null )
		{
			this._generator.getScoreboard().deRegisterErrorLogHandle( threadName );
			synchronized( this._errorLogWriter )
			{
				try
				{
					this._errorLogWriter.flush();
					this._errorLogWriter.close();
					this._errorLogWriter = null;
				}
				catch( IOException ioe )
				{
					System.out.println( "[" + threadName + "] failed to close error trace log Reason: " + ioe.toString() );
				}
			}
		}
	}
	
	/**
	 * Sleep this thread until the provided time if this thread is being run
	 * in interactive mode. No point in sleeping if we are simply generating
	 * a trace. 
	 * 
	 * @param time  The time to wake up.
	 * 
	 * @throws InterruptedException 
	 */
	protected void sleepUntil( long time ) throws InterruptedException
	{
		if ( this._interactive )
		{
			long preRunSleep = time - System.currentTimeMillis();
			if ( preRunSleep > 0 )
			{
				Thread.sleep( preRunSleep );
			}
		}
	}
	
	/**
	 * Checks whether this thread should be active or not based on the number
	 * of active users specified by the current load profile and this thread's
	 * ID number.
	 * 
	 * @return      True if this thread should be active; otherwise false.
	 */
	protected boolean isActive()
	{
		LoadProfile loadProfile = this._generator.getTrack().getCurrentLoadProfile();
		return ( this._id <= loadProfile.getNumberOfUsers() );
	}
}
