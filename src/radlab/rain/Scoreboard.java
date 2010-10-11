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

//import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Iterator;
//import java.util.Enumeration;
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
	
	// Scorecards - per-interval scorecards plus the final scorecard
	TreeMap<String,Scorecard> _intervalScorecards = new TreeMap<String,Scorecard>();
	Scorecard finalCard 							= null;
	
	/* Other aggregate counters for steady state. */
	/*private long _totalOpsSuccessful     = 0;
	private long _totalOpsFailed         = 0;
	private long _totalActionsSuccessful = 0;
	private long _totalOpsAsync          = 0;
	private long _totalOpsSync           = 0;
	private long _totalOpsInitiated      = 0;
	private long _totalOpsLate			 = 0;
	private long _totalOpResponseTime	 = 0;
	*/
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
	
	/** Lock for access to waitTime table */
	private Object _waitTimeDropOffLock = new Object();
	
	/** A mapping of each operation with its summary. */
	//private Hashtable<String,OperationSummary> _operationMap = new Hashtable<String,OperationSummary>();
	
	/** A mapping of each operation with its wait/cycle time. */
	//private Hashtable<String,WaitTimeSummary> _waitTimeMap = new Hashtable<String,WaitTimeSummary>();
	private TreeMap<String,WaitTimeSummary> _waitTimeMap = new TreeMap<String,WaitTimeSummary>();
	
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
		
		double runDuration = (double) ( this._endTime - this._startTime ) / 1000.0;
		this.finalCard = new Scorecard( "final", runDuration, this._trackName );
		
		this.reset();
	}
	
	public void reset()
	{
		// Clear the operation map		
		this.finalCard._operationMap.clear();
		synchronized( this._dropOffQLock )
		{
			this._dropOffQ.clear();
		}
		this._processingQ.clear();
		
		// Clear the wait/cycle time map
		synchronized( this._waitTimeDropOffLock )
		{
			this._waitTimeMap.clear();
		}
		
		this.finalCard._totalActionsSuccessful = 0;
		this._totalDropoffs = 0;
		this._totalDropOffWaitTime = 0;
		this.finalCard._totalOpsAsync = 0;
		this.finalCard._totalOpsFailed = 0;
		this.finalCard._totalOpsInitiated = 0;
		this.finalCard._totalOpsSuccessful = 0;
		this.finalCard._totalOpsSync = 0;
		this._maxDropOffWaitTime = 0;
		this.finalCard._totalOpsLate = 0;
		this.finalCard._totalOpResponseTime = 0;
	}
	
	public void dropOffWaitTime( long time, String opName, long waitTime )
	{
		if( this._done )
			return;
		
		if( !this.isSteadyState( time ) )
			return;
		
		synchronized( this._waitTimeDropOffLock )
		{
			WaitTimeSummary waitTimeSummary = this._waitTimeMap.get( opName );
			if( waitTimeSummary == null )
			{
				waitTimeSummary = new WaitTimeSummary( new PoissonSamplingStrategy( this._meanResponseTimeSamplingInterval ) );
				this._waitTimeMap.put( opName, waitTimeSummary );
			}
			
			waitTimeSummary.count++;
			waitTimeSummary.totalWaitTime += waitTime;
			if( waitTime < waitTimeSummary.minWaitTime )
				waitTimeSummary.minWaitTime = waitTime;
			if( waitTime > waitTimeSummary.maxWaitTime )
				waitTimeSummary.maxWaitTime = waitTime;
			waitTimeSummary.acceptSample( waitTime );
		}
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
		
		boolean isSteadyState = false;
		
		// Set the trace label accordingly.
		if ( this.isRampUp( result.getTimeStarted() ) ) // Initiated during ramp up
		{
			result.setTraceLabel( Scoreboard.RAMP_UP_LABEL );
		}
		if ( this.isSteadyState( result.getTimeFinished() ) ) // Finished in steady state
		{
			result.setTraceLabel( Scoreboard.STEADY_STATE_TRACE_LABEL );
			isSteadyState = true;
		}
		else if ( this.isSteadyState( result.getTimeStarted() ) ) // Initiated in steady state BUT did not complete until after steady state
		{
			result.setTraceLabel( Scoreboard.LATE_LABEL );
		}
		/*else if ( this.isRampUp( result.getTimeStarted() ) ) // Initiated during ramp up
		{
			result.setTraceLabel( Scoreboard.RAMP_UP_LABEL );
		}*/
		else if ( this.isRampDown( result.getTimeStarted() ) ) // Initiated during ramp down
		{
			result.setTraceLabel( Scoreboard.RAMP_DOWN_LABEL );
		}
		
		String generatedBy = result.getOperation().getGeneratedBy(); 
		
		// If this operation failed, write out the error information.
		if ( isSteadyState && result.getOperation().isFailed() )
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
						{
							errorLogger.write( "[" + generatedBy + "] " + failureReason.toString() + Scoreboard.NEWLINE );
							RainConfig config = RainConfig.getInstance();
							if( config._verboseErrors )
							{
								// If we're doing verbose error reporting then dump the stack trace
								for( StackTraceElement frame : failureReason.getStackTrace() )
									errorLogger.write( "at [" + generatedBy + "] " + frame.toString() + Scoreboard.NEWLINE );			
							}
							errorLogger.write( Scoreboard.NEWLINE );
						}
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
	
	public boolean isSteadyState( long time )
	{
		return ( time >= this._startTime && time <= this._endTime );
	}
	
	public boolean isRampUp( long time )
	{
		return ( time < this._startTime );
	}
	
	public boolean isRampDown( long time )
	{
		return ( time > this._endTime );
	}
	
	/*
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
	*/
	
	public void printStatistics( PrintStream out )
	{
		double runDuration = (double) ( this._endTime - this._startTime ) / 1000.0;
			
		long totalOperations = this.finalCard._totalOpsSuccessful + this.finalCard._totalOpsFailed;
			
		double offeredLoadOps = 0.0;
		if ( totalOperations > 0 )
		{
			offeredLoadOps = (double) this.finalCard._totalOpsInitiated / runDuration;
		}
			
		double effectiveLoadOps = 0.0;
		if ( this.finalCard._totalOpsSuccessful > 0 )
		{
			effectiveLoadOps = (double) this.finalCard._totalOpsSuccessful / runDuration;
		}
			
		double effectiveLoadRequests = 0.0;
		if ( this.finalCard._totalActionsSuccessful > 0 )
		{
			effectiveLoadRequests = (double) this.finalCard._totalActionsSuccessful / runDuration;
		}
		
		
		/* Show...
		 * - average ops per second generated (load offered) - total ops/duration
		 * - average ops per second completed (effective load)- total successful ops/duration
		 * - average requests per second
		 * - async % vs. sync %
		 */ 
		
		long totalUsers = 0;
		long totalIntervalActivations = 0;
		out.println( this + " Interval results-------------------: " );
		// Print out per-interval stats?
		for( Scorecard card : this._intervalScorecards.values() )
		{
			totalUsers += card._numberOfUsers * card._activeCount;
			totalIntervalActivations += card._activeCount;
			card.printStatistics( out );
		}
		
		double averageOpResponseTime = 0.0;
		
		if( this.finalCard._totalOpsSuccessful > 0 )
			averageOpResponseTime = ((double)this.finalCard._totalOpResponseTime/(double)this.finalCard._totalOpsSuccessful)/1000.0;
		
		double averageNumberOfUsers = 0.0;
		if( totalIntervalActivations != 0 )
			averageNumberOfUsers = (double) totalUsers/ (double) totalIntervalActivations;
		
		out.println( this + " Final results----------------------: " );
		out.println( this + " Total drop offs                    : " + this._totalDropoffs );
		out.println( this + " Average drop off Q time (ms)       : " + this._formatter.format( (double) this._totalDropOffWaitTime / (double) this._totalDropoffs ) );
		out.println( this + " Max drop off Q time (ms)           : " + this._maxDropOffWaitTime );
		out.println( this + " Total interval activations         : " + totalIntervalActivations );
		out.println( this + " Average number of users            : " + this._formatter.format( averageNumberOfUsers ) );
		out.println( this + " Offered load (ops/sec)             : " + this._formatter.format( offeredLoadOps ) );
		out.println( this + " Effective load (ops/sec)           : " + this._formatter.format( effectiveLoadOps ) );
		// Still a rough estimate, need to compute the bounds on this estimate
		if( averageOpResponseTime > 0.0 )
			out.println( this + " Little's Law Estimate (ops/sec)    : " + this._formatter.format( averageNumberOfUsers / averageOpResponseTime ) );
		else
			out.println( this + " Little's Law Estimate (ops/sec)    : 0" );
		out.println( this + " Effective load (requests/sec)      : " + this._formatter.format( effectiveLoadRequests ) );
		out.println( this + " Operations initiated               : " + this.finalCard._totalOpsInitiated );
		out.println( this + " Operations successfully completed  : " + this.finalCard._totalOpsSuccessful );
		// Avg response time per operation
		out.println( this + " Average operation response time (s): " + this._formatter.format( averageOpResponseTime ) );
		out.println( this + " Operations late                    : " + this.finalCard._totalOpsLate );
		out.println( this + " Operations failed                  : " + this.finalCard._totalOpsFailed );
		out.println( this + " Async Ops                          : " + this.finalCard._totalOpsAsync + " " + this._formatter.format( ( ( (double) this.finalCard._totalOpsAsync / (double) totalOperations) * 100) ) + "%" );
		out.println( this + " Sync Ops                           : " + this.finalCard._totalOpsSync + " " + this._formatter.format( ( ( (double) this.finalCard._totalOpsSync / (double) totalOperations) * 100) ) + "%" );
		
		out.println( this + " Mean response time sample interval : " + this._meanResponseTimeSamplingInterval + " (using Poisson sampling)");
		
		this.printOperationStatistics( out, true );
		out.println( "" );
		this.printWaitTimeStatistics( out, true );
	}
	
	private void printWaitTimeStatistics( PrintStream out, boolean purgePercentileData )
	{
		synchronized( this.finalCard._operationMap )
		{
			try
			{
				// Make this thing "prettier", using fixed width columns
				String outputFormatSpec = "|%20s|%12s|%12s|%12s|%10s|%10s|%20s|";
				
				out.println( this + String.format( outputFormatSpec, "operation", "avg wait", "min wait", "max wait", "90th (s)", "99th (s)", "pctile" ) );
				out.println( this + String.format( outputFormatSpec, "", "time (s)", "time (s)", "time (s)", "", "", "samples" ) );
				//out.println( this + "| operation | proportion | successes | failures | avg response | min response | max response | 90th (s) | 99th (s) | pctile  |" );
				//out.println( this + "|           |            |           |          | time (s)     | time (s)     | time (s)     |          |          | samples |" );
				
				// Show operation proportions, response time: avg, max, min, stdev (op1 = x%, op2 = y%...)
				//Enumeration<String> keys = this.finalCard._operationMap.keys();
				Iterator<String> keys = this.finalCard._operationMap.keySet().iterator();
				while ( keys.hasNext() )
				{
					String opName = keys.next();
					WaitTimeSummary summary = this._waitTimeMap.get( opName );
					
					// If there were no values, then the min and max wait times would not have been set
					// so make them to 0
					if( summary.minWaitTime == Long.MAX_VALUE )
						summary.minWaitTime = 0;
					
					if( summary.maxWaitTime == Long.MIN_VALUE )
						summary.maxWaitTime = 0;
					
					// Print out the operation summary.
					out.println( this + String.format( outputFormatSpec, 
							opName, 
							//this._formatter.format( ( ( (double) ( summary.succeeded + summary.failed ) / (double) totalOperations ) * 100 ) ) + "% ",
							//summary.succeeded,
							//summary.failed,
							this._formatter.format( summary.getAverageWaitTime() / 1000.0 ),
							this._formatter.format( summary.minWaitTime / 1000.0 ),
							this._formatter.format( summary.maxWaitTime / 1000.0 ),
							this._formatter.format( summary.getNthPercentileResponseTime( 90 ) / 1000.0 ),
							this._formatter.format( summary.getNthPercentileResponseTime( 99 ) / 1000.0 ),
							summary.getSamplesCollected() + "/" + summary.getSamplesSeen()
							) 
						);
										
					if( purgePercentileData )
						summary.resetSamples();
				}
			}
			catch( Exception e )
			{
				System.out.println( this + " Error printing think/cycle time summary. Reason: " + e.toString() );
				e.printStackTrace();
			}
		}
	}
	
	private void printOperationStatistics( PrintStream out, boolean purgePercentileData )
	{
		long totalOperations = this.finalCard._totalOpsSuccessful + this.finalCard._totalOpsFailed;
		double totalAvgResponseTime = 0.0;
		double totalResponseTime = 0.0;
		long totalSuccesses = 0;
		
		synchronized( this.finalCard._operationMap )
		{
			try
			{
				// Make this thing "prettier", using fixed width columns
				String outputFormatSpec = "|%20s|%10s|%10s|%10s|%12s|%12s|%12s|%10s|%10s|%10s|";
				
				out.println( this + String.format( outputFormatSpec, "operation", "proportion", "successes", "failures", "avg response", "min response", "max response", "90th (s)", "99th (s)", "pctile" ) );
				out.println( this + String.format( outputFormatSpec, "", "", "", "", "time (s)", "time (s)", "time(s)", "", "", "samples" ) );
				//out.println( this + "| operation | proportion | successes | failures | avg response | min response | max response | 90th (s) | 99th (s) | pctile  |" );
				//out.println( this + "|           |            |           |          | time (s)     | time (s)     | time (s)     |          |          | samples |" );
				
				// Show operation proportions, response time: avg, max, min, stdev (op1 = x%, op2 = y%...)
				//Enumeration<String> keys = this.finalCard._operationMap.keys();
				Iterator<String> keys = this.finalCard._operationMap.keySet().iterator();
				while ( keys.hasNext() )
				{
					String opName = keys.next();
					OperationSummary summary = this.finalCard._operationMap.get( opName );
					
					totalAvgResponseTime += summary.getAverageResponseTime();
					totalResponseTime += summary.totalResponseTime;
					totalSuccesses += summary.succeeded;
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
							this._formatter.format( summary.getNthPercentileResponseTime( 99 ) / 1000.0 ),
							summary.getSamplesCollected() + "/" + summary.getSamplesSeen()
							) 
						);
										
					if( purgePercentileData )
						summary.resetSamples();
				}
				
				/*if( this._operationMap.size() > 0 )
				{
					out.println( "" );
					//out.println( this + " average response time (agg)        : " + this._formatter.format( ( totalAvgResponseTime/this._operationMap.size())/1000.0 ) );
					out.println( this + " average response time (s)          : " + this._formatter.format( ( totalResponseTime/totalSuccesses)/1000.0 ) );
				}*/
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
				//if ( this._workerThread.getState() == State.TIMED_WAITING )
				//{
				//	this._workerThread.interrupt();
				//}
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
				
				//System.out.println( this + " processingQ contains: " + this._processingQ.size() + " unprocessed records." );
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
		while ( !this._done || this._dropOffQ.size() > 0 )
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
						this.finalCard._totalOpsInitiated++;
						this.processSteadyStateResult( result );
					}
					else if ( traceLabel.equals( Scoreboard.LATE_LABEL ) )
					{
						this.finalCard._totalOpsInitiated++;
						this.finalCard._totalOpsLate++;
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
					//System.out.println( this + " drop off queue size: " + this._dropOffQ.size());
					//System.out.println( this + " processing queue size: " + this._processingQ.size());
				}
			}
		}
		System.out.println( this + " drop off queue size: " + this._dropOffQ.size());
		System.out.println( this + " processing queue size: " + this._processingQ.size());
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
		// By default we don't save per-interval metrics
		LoadProfile activeProfile = result._generatedDuring;
		if( activeProfile != null && (activeProfile._name != null && activeProfile._name.length() > 0 ) )
		{
			String intervalName = activeProfile._name;
			Scorecard intervalScorecard = this._intervalScorecards.get( intervalName );
			if( intervalScorecard == null )
			{
				intervalScorecard = new Scorecard( intervalName, activeProfile._interval, this._trackName );
				intervalScorecard._numberOfUsers = activeProfile._numberOfUsers;
				this._intervalScorecards.put( intervalName, intervalScorecard );
			}
			intervalScorecard._activeCount = activeProfile._activeCount;
			intervalScorecard._totalOpsInitiated += 1;
			
			// Do accounting for this interval's scorecard
			OperationSummary intervalSummary = intervalScorecard._operationMap.get( opName );
			if( intervalSummary == null )
			{
				intervalSummary = new OperationSummary( new PoissonSamplingStrategy( this._meanResponseTimeSamplingInterval ) );
				intervalScorecard._operationMap.put( opName, intervalSummary );
			}
			
			if ( result.isAsynchronous() )
				intervalScorecard._totalOpsAsync++;
			else intervalScorecard._totalOpsSync++;
			
			if ( result.isFailed() )
			{
				intervalSummary.failed++;
				intervalScorecard._totalOpsFailed++;
			}
			else // Result was successful
			{
				intervalScorecard._totalOpsSuccessful++;
				intervalScorecard._totalActionsSuccessful += result.getActionsPerformed();
				intervalSummary.succeeded++;
				intervalSummary.totalActions += result.getActionsPerformed();
				
				if ( result.isAsynchronous() )
					intervalSummary.totalAsyncInvocations++;
				else intervalSummary.totalSyncInvocations++;
				
				// If interactive, look at the total response time.
				if ( result.isInteractive() )
				{
					long responseTime = result.getExecutionTime();
					intervalSummary.acceptSample( responseTime );
					
					intervalSummary.totalResponseTime += responseTime;
					intervalScorecard._totalOpResponseTime += responseTime;
					if( responseTime > intervalSummary.maxResponseTime )
						intervalSummary.maxResponseTime = responseTime;
					if( responseTime < intervalSummary.minResponseTime )
						intervalSummary.minResponseTime = responseTime;
				}
			}
		}
				
		// Do the accounting for the final score card
		OperationSummary summary = this.finalCard._operationMap.get( opName );
		if ( summary == null )
		{
			summary = new OperationSummary( new PoissonSamplingStrategy( this._meanResponseTimeSamplingInterval ) );
			this.finalCard._operationMap.put( opName, summary );
		}
					
		if ( result.isAsynchronous() )
		{
			this.finalCard._totalOpsAsync++;
		}
		else
		{
			this.finalCard._totalOpsSync++;
		}
		
		if ( result.isFailed() )
		{
			summary.failed++;
			this.finalCard._totalOpsFailed++;
		}
		else
		{
			this.finalCard._totalOpsSuccessful++;
			this.finalCard._totalActionsSuccessful += result.getActionsPerformed();
			
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
				// Update the total response time
				summary.totalResponseTime += responseTime;
								
				this.finalCard._totalOpResponseTime += responseTime;
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
