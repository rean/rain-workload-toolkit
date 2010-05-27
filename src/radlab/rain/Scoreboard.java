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

import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Random;
import java.io.File;
import java.io.PrintStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import radlab.rain.util.PoissonSamplingStrategy;

/**
 * The Scoreboard class implements the IScoreboard interface. Each Scoreboard
 * is specific to a single instantiation of a track (i.e. the statistical
 * results of a a scoreboard pertain to the operations executed by only the.
 * scenario track with which this scoreboard is associated).<br />
 * <br />
 * The graphs we want to show/statistics we want to record:
 * <ol>
 * <li>Offered load timeline (in ops or requests per sec in a bucket of time)</li>
 * <li>Offered load during the run (in ops or requests per sec)</li>
 * <li>Effective load during the run (in ops or requests per sec) (avg number
 *     of operations/requests that completed successfully during the run duration</li>
 * <li>Data distribution for each operation type - histogram of id's generated/used</li>
 * </ol>
 */
public class Scoreboard implements Runnable, IScoreboard
{
	public static String NO_TRACE_LABEL          	= "[NONE]";
	public static String STEADY_STATE_TRACE_LABEL 	= "[STEADY-STATE]";
	public static String LATE_LABEL               	= "[LATE]";
	public static String RAMP_UP_LABEL            	= "[RAMP-UP]";
	public static String RAMP_DOWN_LABEL          	= "[RAMP-DOWN]";
	
	/** Time in seconds to wait for worker thread to exit before interrupt. */
	public static int WORKER_EXIT_TIMEOUT 			= 60;
	/* Random number generator */
	private Random _random 							= new Random();
	/* Snapshot interval */
	private long _metricSnapshotInterval			= (1 * 60 * 1000); // Every minute
	/* Response time sampling interval */
	private long _meanResponseTimeSamplingInterval 	= 500;
	private static String NEWLINE 					= System.getProperty("line.separator");
	
	/* Time markers. */
	private long _startTime 			= 0;
	private long _endTime 				= 0;
	private long _totalDropOffWaitTime 	= 0;
	private long _maxDropOffWaitTime 	= 0;
	private long _totalDropoffs 		= 0;
	
	/* Other aggregate counters for steady state. */
	private long _totalOpsSuccessful     = 0;
	private long _totalOpsFailed         = 0;
	private long _totalActionsSuccessful = 0;
	private long _totalOpsAsync          = 0;
	private long _totalOpsSync           = 0;
	private long _totalOpsInitiated      = 0;
	
	/* Log (trace) sampling probability */
	private double _logSamplingProbability = 1.0;
	
	private String _trackName = "";
	
	/** If true, this scoreboard will refuse any new results. */
	private boolean _done = false;
	
	/** Queue that contains all results that have been dropped off. */
	private LinkedList<OperationExecution> _dropOffQ = new LinkedList<OperationExecution>();
	
	/** Queue that contains all results that need to be processed. */
	private LinkedList<OperationExecution> _processingQ = new LinkedList<OperationExecution>();
	
	/** Lock for access to _dropOffQ */
	private Object _dropOffQLock = new Object();
	
	/** A mapping of each operation with its summary. */
	private Hashtable<String,OperationSummary> _operationMap = new Hashtable<String,OperationSummary>();
	
	private Thread _workerThread = null;
	private ScenarioTrack _owner = null;
	
	//private SnapshotWriterThread _snapshotThread = null;
	
	/* How do we handle operations generated by asynchronous worker threads?
	 * - Lump all of them together.
	 * - Associate them with the thread that delegated them.
	 */
	
	private Hashtable<String,FileWriter> _logHandleMap = new Hashtable<String,FileWriter>();
	private Hashtable<String,FileWriter> _errorLogHandleMap = new Hashtable<String,FileWriter>();
	
	private NumberFormat _formatter = new DecimalFormat( "#0.0000" );
	
	public long getMeanResponseTimeSamplingInterval() { return this._meanResponseTimeSamplingInterval; }
	public void setMeanResponseTimeSamplingInterval( long val ) 
	{ 
		if( val > 0 )
			this._meanResponseTimeSamplingInterval = val; 
	}
	
	public long getStartTimestamp() { return this._startTime; }
	public void setStartTimestamp( long val ) { this._startTime = val; }
	
	public long getEndTimestamp() { return this._endTime; }
	public void setEndTimestamp( long val ) { this._endTime = val; }
	
	public String getTrackName() { return this._trackName; }
	public void setTrackName( String val ) { this._trackName = val; }
	
	public boolean getDone() { return this._done; }
	public void setDone( boolean val ) { this._done = val; }
	
	public double getLogSamplingProbability() { return this._logSamplingProbability; }
	public void setLogSamplingProbability( double val ) { this._logSamplingProbability = val; }
	
	public long getMetricSnapshotInterval() { return this._metricSnapshotInterval; }
	public void setMetricSnapshotInterval( long val ) { this._metricSnapshotInterval = val; }
	
	public void registerErrorLogHandle( String owner, FileWriter logHandle )
	{
		synchronized( this._errorLogHandleMap )
		{
			this._errorLogHandleMap.put( owner, logHandle );
		}
	}
	
	public void deRegisterErrorLogHandle( String owner )
	{
		synchronized( this._errorLogHandleMap )
		{
			this._errorLogHandleMap.remove( owner );
		}
	}
	
	public void registerLogHandle( String owner, FileWriter logHandle )
	{
		synchronized( this._logHandleMap )
		{
			this._logHandleMap.put( owner, logHandle );
		}
	}
	
	public void deRegisterLogHandle( String owner )
	{
		synchronized( this._logHandleMap )
		{
			this._logHandleMap.remove( owner );
		}
	}
	
	/**
	 * Creates a new Scoreboard with the track name specified. The Scoreboard
	 * returned must be initialized by calling <code>initialize</code>.
	 * 
	 * @param trackName     The track name to associate with this scoreboard.
	 */
	public Scoreboard( String trackName )
	{
		this._trackName = trackName;
	}
	
	public ScenarioTrack getScenarioTrack() { return this._owner; }	
	public void setScenarioTrack( ScenarioTrack owner )
	{
		this._owner = owner;
	}
	
	public void initialize( long startTime, long endTime )
	{
		this._startTime = startTime;
		this._endTime = endTime;
		
		this.reset();
	}
	
	public void reset()
	{
		this._operationMap.clear();
		synchronized( this._dropOffQLock )
		{
			this._dropOffQ.clear();
		}
		this._processingQ.clear();
		
		this._totalActionsSuccessful = 0;
		this._totalDropoffs = 0;
		this._totalDropOffWaitTime = 0;
		this._totalOpsAsync = 0;
		this._totalOpsFailed = 0;
		this._totalOpsInitiated = 0;
		this._totalOpsSuccessful = 0;
		this._totalOpsSync = 0;
		this._maxDropOffWaitTime = 0;
	}
	
	public void dropOff( OperationExecution result )
	{
		if ( this._done )
		{
			return;
		}
				
		TraceRecord traceRec = result.getOperation().getTrace();
		if ( traceRec != null )
		{
			result.getOperation().setActionsPerformed( traceRec._lstRequests.size() );
		}
		else
		{
			result.getOperation().setActionsPerformed( 1 );
		}
		
		/*
		 * Everything goes into the dropOffQ.
		 * Offered Load:   Number of requests initiated during steady state.
		 *                 Not all of them may complete within steady state.
		 * Effective Load: Number of operations that complete successfully
		 *                 within the steady state period.
		 */
		long qStart = System.currentTimeMillis();
		synchronized( this._dropOffQLock )
		{
			long qEnd = System.currentTimeMillis();
			long qTime = ( qEnd - qStart );
			
			this._totalDropOffWaitTime += qTime; 
			this._totalDropoffs++;
			
			if ( qTime > this._maxDropOffWaitTime )
			{
				this._maxDropOffWaitTime = qTime;
			}
			
			this._dropOffQ.add( result );
		}
		
		// Set the trace label accordingly.
		if ( this.isCompletedDuringSteadyState( result ) )
		{
			result.setTraceLabel( Scoreboard.STEADY_STATE_TRACE_LABEL );
		}
		else if ( this.isInitiatedDuringSteadyState( result ) )
		{
			result.setTraceLabel( Scoreboard.LATE_LABEL );
		}
		else if ( this.isInitiatedDuringRampUp( result ) )
		{
			result.setTraceLabel( Scoreboard.RAMP_UP_LABEL );
		}
		else if ( this.isInitiatedDuringRampDown( result ) )
		{
			result.setTraceLabel( Scoreboard.RAMP_DOWN_LABEL );
		}
		
		String generatedBy = result.getOperation().getGeneratedBy(); 
		
		// If this operation failed, write out the error information.
		if ( result.getOperation().isFailed() )
		{
			FileWriter errorLogger = null;
			synchronized( this._errorLogHandleMap )
			{
				errorLogger =  this._errorLogHandleMap.get( generatedBy );
			}
			
			if ( errorLogger != null )
			{
				synchronized( errorLogger )
				{
					try
					{
						Throwable failureReason = result.getOperation().getFailureReason();
						if( failureReason != null )
							errorLogger.write( "[" + generatedBy + "] " + failureReason.toString() + Scoreboard.NEWLINE );
					}
					catch( IOException ioe )
					{
						System.out.println( this + " Error writing error record: Thread name: " + Thread.currentThread().getName() + " on behalf of: " + result.getOperation().getGeneratedBy() + " Reason: " + ioe.toString() );
					}
				}
			}
		}
		
		// Flip a coin to determine whether we log or not?
		double randomVal = this._random.nextDouble();
		
		if( this._logSamplingProbability == 1.0 || randomVal <= this._logSamplingProbability )
		{	
			FileWriter logger = null;
			synchronized( this._logHandleMap )
			{
				logger = this._logHandleMap.get( generatedBy );
			}
			
			if ( logger != null )
			{
				synchronized( logger )
				{
					try
					{
						StringBuffer trace = result.getOperation().dumpTrace();
						if ( trace != null && trace.length() > 0 )
						{
							// Don't flush on every write, it kills the load performance
							logger.write( trace.toString() + Scoreboard.NEWLINE );
							// Dumping objects we persisted to disk
							result.getOperation().disposeOfTrace();
						}
					}
					catch( IOException ioe )
					{
						result.getOperation().disposeOfTrace();
						System.out.println( this + " Error writing trace record: Thread name: " + Thread.currentThread().getName() + " on behalf of: " + result.getOperation().getGeneratedBy() + " Reason: " + ioe.toString() );
					}
				}
			}
		}
		else // not logging
		{
			// Discard the trace
			result.getOperation().disposeOfTrace();
		}
		// Return operation object to pool
		if( this._owner.getObjectPool().isActive() )
			this._owner.getObjectPool().returnObject( result.getOperation() );
	}
	
	public boolean isCompletedDuringSteadyState( OperationExecution e )
	{
		long finishTime = e.getTimeFinished();
		return ( finishTime >= this._startTime && finishTime <= this._endTime );
	}
	
	public boolean isInitiatedDuringSteadyState( OperationExecution e )
	{
		long startTime = e.getTimeStarted();
		return ( startTime >= this._startTime && startTime <= this._endTime );
	}
	
	public boolean isInitiatedDuringRampUp( OperationExecution e )
	{
		long startTime = e.getTimeStarted();
		return ( startTime < this._startTime );
	}
	
	public boolean isInitiatedDuringRampDown( OperationExecution e )
	{
		long startTime = e.getTimeStarted();
		return ( startTime > this._endTime );
	}
	
	public void printStatistics( PrintStream out )
	{
		double runDuration = (double) ( this._endTime - this._startTime ) / 1000.0;
			
		long totalOperations = this._totalOpsSuccessful + this._totalOpsFailed;
			
		double offeredLoadOps = 0.0;
		if ( totalOperations > 0 )
		{
			offeredLoadOps = (double) this._totalOpsInitiated / runDuration;
		}
			
		double effectiveLoadOps = 0.0;
		if ( this._totalOpsSuccessful > 0 )
		{
			effectiveLoadOps = (double) this._totalOpsSuccessful / runDuration;
		}
			
		double effectiveLoadRequests = 0.0;
		if ( this._totalActionsSuccessful > 0 )
		{
			effectiveLoadRequests = (double) this._totalActionsSuccessful / runDuration;
		}
			
		/* Show...
		 * - average ops per second generated (load offered) - total ops/duration
		 * - average ops per second completed (effective load)- total successful ops/duration
		 * - average requests per second
		 * - async % vs. sync %
		 */ 
		out.println( this + " Total drop offs                    : " + this._totalDropoffs );
		out.println( this + " Average drop off Q time (ms)       : " + this._formatter.format( (double) this._totalDropOffWaitTime / (double) this._totalDropoffs ) );
		out.println( this + " Max drop off Q time (ms)           : " + this._maxDropOffWaitTime );
		out.println( this + " Offered load (ops/sec)             : " + this._formatter.format( offeredLoadOps ) );
		out.println( this + " Effective load (ops/sec)           : " + this._formatter.format( effectiveLoadOps ) );
		out.println( this + " Effective load (requests/sec)      : " + this._formatter.format( effectiveLoadRequests ) );
		out.println( this + " Operations initiated               : " + this._totalOpsInitiated );
		out.println( this + " Operations completed               : " + this._totalOpsSuccessful );
		out.println( this + " Async Ops                          : " + this._totalOpsAsync + " " + this._formatter.format( ( ( (double) this._totalOpsAsync / (double) totalOperations) * 100) ) + "%" );
		out.println( this + " Sync Ops                           : " + this._totalOpsSync + " " + this._formatter.format( ( ( (double) this._totalOpsSync / (double) totalOperations) * 100) ) + "%" );
		out.println( this + " Mean response time sample interval : " + this._meanResponseTimeSamplingInterval + " (using Poisson sampling)");
		
		this.printOperationStatistics( out, true );	
	}
	
	private void printOperationStatistics( PrintStream out, boolean purgePercentileData )
	{
		long totalOperations = this._totalOpsSuccessful + this._totalOpsFailed;
		
		synchronized( this._operationMap )
		{
			try
			{
				// Make this thing "prettier", using fixed width columns
				String outputFormatSpec = "|%20s|%10s|%10s|%10s|%12s|%12s|%12s|%10s|%10s|";
				
				out.println( this + String.format( outputFormatSpec, "operation", "proportion", "successes", "failures", "avg response", "min response", "max response", "90th (s)", "99th (s)" ) );
				out.println( this + String.format( outputFormatSpec, "", "", "", "", "time (s)", "time (s)", "time(s)", "", "" ) );
				//out.println( this + "| operation | proportion | successes | failures | avg response | min response | max response | 90th (s) | 99th (s) |" );
				//out.println( this + "|           |            |           |          | time (s)     | time (s)     | time (s)     |          |          |" );
				
				// Show operation proportions, response time: avg, max, min, stdev (op1 = x%, op2 = y%...)
				Enumeration<String> keys = this._operationMap.keys();
				while ( keys.hasMoreElements() )
				{
					String opName = keys.nextElement();
					OperationSummary summary = this._operationMap.get( opName );
					
					// If there were no successes, then the min and max response times would not have been set
					// so make them to 0
					if( summary.minResponseTime == Long.MAX_VALUE )
						summary.minResponseTime = 0;
					
					if( summary.maxResponseTime == Long.MIN_VALUE )
						summary.maxResponseTime = 0;
					
					// Print out the operation summary.
					out.println( this + String.format( outputFormatSpec, 
							opName, 
							this._formatter.format( ( ( (double) ( summary.succeeded + summary.failed ) / (double) totalOperations ) * 100 ) ) + "% ",
							summary.succeeded,
							summary.failed,
							this._formatter.format( summary.getAverageResponseTime() / 1000.0 ),
							this._formatter.format( summary.minResponseTime / 1000.0 ),
							this._formatter.format( summary.maxResponseTime / 1000.0 ),
							this._formatter.format( summary.getNthPercentileResponseTime( 90 ) / 1000.0 ),
							this._formatter.format( summary.getNthPercentileResponseTime( 99 ) / 1000.0 )
							) 
							+ " [Percentile estimates using " +
							summary.getSamplesCollected() + " samples collected out of " + summary.getSamplesSeen() + "]"
						);
										
					if( purgePercentileData )
						summary.resetSamples();
				}
			}
			catch( Exception e )
			{
				System.out.println( this + " Error printing operation summary. Reason: " + e.toString() );
				e.printStackTrace();
			}
		}
	}
	
	public void start()
	{
		if ( !this.isRunning() )
		{
			this._done = false;
			this._workerThread = new Thread( this );
			this._workerThread.setName( "Scoreboard-Worker" );
			this._workerThread.start();
			// Start the snapshot thread
			//this._snapshotThread = new SnapshotWriterThread( this );
			//this._snapshotThread.setName( "Scoreboard-Snapshot-Writer" );
			//this._snapshotThread.start();
		}	
	}
	
	public void stop()
	{
		if ( this.isRunning() )
		{
			this._done = true;
			try
			{
				// Check whether the thread is sleeping. If it is, then interrupt it.
				if ( this._workerThread.getState() == State.TIMED_WAITING )
				{
					this._workerThread.interrupt();
				}
				// If not give it time to exit; if it takes too long interrupt it.
				System.out.println( this + " waiting " + WORKER_EXIT_TIMEOUT + " seconds for worker thread to exit!" );
				this._workerThread.join( WORKER_EXIT_TIMEOUT * 1000 );
				if ( this._workerThread.isAlive() )
				{
					System.out.println( this + " interrupting worker thread." );
					this._workerThread.interrupt();
				}
				/*
				if( this._snapshotThread != null )
				{
					this._snapshotThread._done = true;
					if( this._snapshotThread.getState() == State.TIMED_WAITING )
					{
						this._snapshotThread.interrupt();
					}
					// If not give it time to exit; if it takes too long interrupt it.
					System.out.println( this + " waiting " + WORKER_EXIT_TIMEOUT + " seconds for snapshot thread to exit!" );
					this._snapshotThread.join( WORKER_EXIT_TIMEOUT * 1000 );
					if( this._snapshotThread.isAlive() )
					{
						System.out.println( this + " interrupting snapshot thread." );
						this._snapshotThread.interrupt();
					}
				}*/
			}
			catch( InterruptedException ie )
			{
				System.out.println( this + " Interrupted waiting on worker thread exit!" );
			}
		}
	}
	
	/**
	 * Checks whether the worker thread exists and is alive.
	 * 
	 * @return  True if the worker thread exists and is alive.
	 */
	protected boolean isRunning()
	{
		return ( this._workerThread != null && this._workerThread.isAlive() );
	}
	
	/**
	 * Implements the worker thread that periodically grabs the results from
	 * the dropOffQ and copies it over to the processingQ to be processed.
	 */
	public void run()
	{
		System.out.println( this + " worker thread started." );
		while ( !this._done )
		{
			if ( this._dropOffQ.size() > 0 )
			{
				// Queue swap
				synchronized( this._dropOffQLock )
				{
					LinkedList<OperationExecution> temp = _processingQ;
					_processingQ = _dropOffQ;
					_dropOffQ = temp;
				}
				
				while ( !this._processingQ.isEmpty() )
				{
					OperationExecution result = this._processingQ.remove();
					String traceLabel = result.getTraceLabel();
					
					if ( traceLabel.equals( Scoreboard.STEADY_STATE_TRACE_LABEL ) )
					{
						this._totalOpsInitiated++;
						this.processSteadyStateResult( result );
					}
					else if ( traceLabel.equals( Scoreboard.LATE_LABEL ) )
					{
						this._totalOpsInitiated++;
					}
				}
			}
			else
			{
				try
				{
					Thread.sleep( 1000 );
				}
				catch( InterruptedException tie )
				{ 
					System.out.println( this + " worker thread interrupted." );
					System.out.println( this + " drop off queue size: " + this._dropOffQ.size());
					System.out.println( this + " processing queue size: " + this._processingQ.size());
				}
			}
		}
		System.out.println( this + " worker thread finished!" );
	}
	
	/**
	 * Processes a result (from the processingQ) if it was received during the
	 * steady state period.
	 * 
	 * @param result    The operation execution result to process.
	 */
	private void processSteadyStateResult( OperationExecution result )
	{
		String opName = result._operationName;
		//synchronized( this._operationMap )
		//{	
			OperationSummary summary = this._operationMap.get( opName );
			
			if ( summary == null )
			{
				summary = new OperationSummary( new PoissonSamplingStrategy( this._meanResponseTimeSamplingInterval ) );
				this._operationMap.put( opName, summary );
			}
		
			if ( result.isAsynchronous() )
			{
				this._totalOpsAsync++;
			}
			else
			{
				this._totalOpsSync++;
			}
			
			if ( result.isFailed() )
			{
				summary.failed++;
				this._totalOpsFailed++;
			}
			else
			{
				this._totalOpsSuccessful++;
				this._totalActionsSuccessful += result.getActionsPerformed();
				
				summary.succeeded++;
				summary.totalActions += result.getActionsPerformed();
				if ( result.isAsynchronous() )
				{
					summary.totalAsyncInvocations++;
				}
				else
				{
					summary.totalSyncInvocations++;
				}
				
				// If interactive, look at the total response time.
				if ( result.isInteractive() )
				{
					long responseTime = result.getExecutionTime();
					// Save the response time
					summary.acceptSample( responseTime );
					// summary.responseTimes.add( responseTime );
					// Update the total response time
					summary.totalResponseTime += responseTime;
					if ( responseTime > summary.maxResponseTime )
					{
						summary.maxResponseTime = responseTime;
					}
					if ( responseTime < summary.minResponseTime )
					{
						summary.minResponseTime = responseTime;
					}
				}	
			}
		//}
	}
	
	public String toString()
	{
		return "[SCOREBOARD TRACK: " + this._trackName + "]";
	}
	
	protected class SnapshotWriterThread extends Thread
	{
		// Owning scoreboard
		private Scoreboard _owner = null;
		private boolean _done = false;
				
		public boolean getDone() { return this._done; }
		public void setDone( boolean val ) { this._done = val; }
		
		public SnapshotWriterThread( Scoreboard owner )
		{
			this._owner = owner;
		}
		
		public void run()
		{
			long now = System.currentTimeMillis();
			System.out.println( this._owner.toString() + " current time: " + now  + " metric snapshot thread started!" );
			
			PrintStream out = null; 
						
			while( !this._done )
			{
				try
				{
					// Open a log file if none is open
					if( out == null )
						out = new PrintStream( new File( "metrics-snapshots-" + this._owner._trackName + ".log" ) ); 
									
					// Sleep for a bit
					Thread.sleep( this._owner._metricSnapshotInterval );
					// See if there are any statisics available yet
					out.println( now );
					this._owner.printOperationStatistics( out, true );
				}
				catch( InterruptedException ie )
				{
					this._done = true;
				}
				catch( Exception e )
				{
					this._done = true;
				}
			}// end-while not done
			
			// Close log file if it's still open
			if( out != null )
			{
				out.flush();
				out.close();
			}
			
			System.out.println( this._owner + " snapshot-writer thread finished!" );
		}
	}
}
