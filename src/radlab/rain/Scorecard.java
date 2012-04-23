package radlab.rain;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
//import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
//import java.util.Hashtable;

import radlab.rain.util.NullSamplingStrategy;

// Not even going to try to make Scorecards thread-safe, the Scoreboard must do "the right thing"(tm)
public class Scorecard 
{
	// Eventually all stats reporting will be done using Scorecards. There will
	// be per-interval Scorecards as well as a final Scorecard for the entire run.
	// The Scoreboard will maintain/manage a hashtable of Scorecards.
	
	public String _name = ""; // All scorecards are named with the interval they are generated in
	// What track does this scorecard belong to
	public String _trackName = "";
	
	
	// What goes on the scorecard?
	public long _totalOpsSuccessful     = 0;
	public long _totalOpsFailed         = 0;
	public long _totalActionsSuccessful = 0;
	public long _totalOpsAsync          = 0;
	public long _totalOpsSync           = 0;
	public long _totalOpsInitiated      = 0;
	public long _totalOpsLate			= 0;
	public long _totalOpResponseTime	= 0;
	
	public double _intervalDuration		= 0;
	public double _numberOfUsers		= 0.0;
	public double _activeCount			= 0.0;
	
	/** A mapping of each operation with its summary. */
	public TreeMap<String,OperationSummary> _operationMap = new TreeMap<String,OperationSummary>();
	
	/** A mapping of each operation with its wait/cycle time. */
	//public Hashtable<String,WaitTimeSummary> _waitTimeMap = new Hashtable<String,WaitTimeSummary>();
	
	private NumberFormat _formatter = new DecimalFormat( "#0.0000" );
	
	public Scorecard( String name, double intervalDurationInSecs, String trackName )
	{
		this._name = name;
		this._intervalDuration = intervalDurationInSecs;
		this._trackName = trackName;
	}
	
	public void reset()
	{
		// Clear the operation map		
		this._operationMap.clear();
		// Reset aggregate counters
		this._totalActionsSuccessful = 0;
		this._totalOpsAsync = 0;
		this._totalOpsFailed = 0;
		this._totalOpsInitiated = 0;
		this._totalOpsSuccessful = 0;
		this._totalOpsSync = 0;
		this._totalOpsLate = 0;
		this._totalOpResponseTime = 0;
		this._intervalDuration = 0;
		this._activeCount = 0.0;
		this._numberOfUsers = 0.0;
	}
	
	public void printStatistics( PrintStream out )
	{
		long totalOperations = this._totalOpsSuccessful + this._totalOpsFailed;
			
		double offeredLoadOps = 0.0;
		if ( totalOperations > 0 )
		{
			offeredLoadOps = (double) this._totalOpsInitiated / this._intervalDuration;
		}
			
		double effectiveLoadOps = 0.0;
		if ( this._totalOpsSuccessful > 0 )
		{
			effectiveLoadOps = (double) this._totalOpsSuccessful / this._intervalDuration;
		}
			
		double effectiveLoadRequests = 0.0;
		if ( this._totalActionsSuccessful > 0 )
		{
			effectiveLoadRequests = (double) this._totalActionsSuccessful / this._intervalDuration;
		}
			
		/* Show...
		 * - average ops per second generated (load offered) - total ops/duration
		 * - average ops per second completed (effective load)- total successful ops/duration
		 * - average requests per second
		 * - async % vs. sync %
		 */ 
		out.println( this + " Interval name                      : " + this._name );
		out.println( this + " Active users                       : " + this._formatter.format( this._numberOfUsers ) );
		out.println( this + " Activation count                   : " + this._formatter.format( this._activeCount ) );
		out.println( this + " Offered load (ops/sec)             : " + this._formatter.format( offeredLoadOps/(double) this._activeCount ) );
		out.println( this + " Effective load (ops/sec)           : " + this._formatter.format( effectiveLoadOps/(double) this._activeCount ) );
		out.println( this + " Effective load (requests/sec)      : " + this._formatter.format( effectiveLoadRequests/(double) this._activeCount ) );
		out.println( this + " Operations initiated               : " + this._totalOpsInitiated );
		out.println( this + " Operations successfully completed  : " + this._totalOpsSuccessful );
		// Avg response time per operation
		if( this._totalOpsSuccessful > 0 )
			out.println( this + " Average operation response time (s): " + this._formatter.format( ( (double)this._totalOpResponseTime/(double)this._totalOpsSuccessful)/1000.0 ) );
		else out.println( this + " Average operation response time (s): 0.0000" );
		out.println( this + " Operations late                    : " + this._totalOpsLate );
		out.println( this + " Operations failed                  : " + this._totalOpsFailed );
		out.println( this + " Async Ops                          : " + this._totalOpsAsync + " " + this._formatter.format( ( ( (double) this._totalOpsAsync / (double) totalOperations) * 100) ) + "%" );
		out.println( this + " Sync Ops                           : " + this._totalOpsSync + " " + this._formatter.format( ( ( (double) this._totalOpsSync / (double) totalOperations) * 100) ) + "%" );
		
		//out.println( this + " Mean response time sample interval : " + this._meanResponseTimeSamplingInterval + " (using Poisson sampling)");
		
		this.printOperationStatistics( out, true );
		out.println( "" );
		//this.printWaitTimeStatistics( out, true );
		
	}
	
	@SuppressWarnings("unused")
	private void printOperationStatistics( PrintStream out, boolean purgePercentileData )
	{
		long totalOperations = this._totalOpsSuccessful + this._totalOpsFailed;
		double totalAvgResponseTime = 0.0;
		double totalResponseTime = 0.0;
		long totalSuccesses = 0;
		
		synchronized( this._operationMap )
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
				//Enumeration<String> keys = this._operationMap.keys();
				Iterator<String> keys = this._operationMap.keySet().iterator();
				while ( keys.hasNext() )
				{
					String opName = keys.next();
					OperationSummary summary = this._operationMap.get( opName );
					
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
	
	public void merge( Scorecard rhs )
	{
		// We expect to merge only "final" scorecards
		
		// For merges the activeCount is always set to 1
		this._activeCount = 1;
		// Merge another scorecard with "me"
		// Let's compute total operations
		this._totalOpsSuccessful += rhs._totalOpsSuccessful;
		this._totalOpsFailed += rhs._totalOpsFailed;
		this._totalActionsSuccessful += rhs._totalActionsSuccessful;
		this._totalOpsAsync += rhs._totalOpsAsync;
		this._totalOpsSync += rhs._totalOpsSync;
		this._totalOpsInitiated += rhs._totalOpsInitiated;
		this._totalOpsLate += rhs._totalOpsLate;
		this._totalOpResponseTime += rhs._totalOpResponseTime;
		this._numberOfUsers += rhs._numberOfUsers;
		
		// Merge operation maps
		for( String opName : rhs._operationMap.keySet() )
		{
			OperationSummary lhsOpSummary = null;
			OperationSummary rhsOpSummary = rhs._operationMap.get( opName );
			// Do we have an operationSummary for this operation yet?
			// If we don't have one, initialize an OperationSummary with a Null/dummy sampler that will
			// simply accept all of the samples from the rhs' sampler
			if( this._operationMap.containsKey( opName ) )
				lhsOpSummary = this._operationMap.get( opName );
			else lhsOpSummary = new OperationSummary( new NullSamplingStrategy() );
			lhsOpSummary.merge( rhsOpSummary );
			this._operationMap.put( opName, lhsOpSummary );
		}
	}
	
	public String toString()
	{
		return "[SCOREBOARD TRACK: " + this._trackName + "]";
	}
}
