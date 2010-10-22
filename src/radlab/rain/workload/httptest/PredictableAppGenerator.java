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

package radlab.rain.workload.httptest;

import java.io.PrintStream;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.Scenario;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.ConfigUtil;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

public class PredictableAppGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY 	= "usePooling";
	public static String CFG_MAX_POOL_SIZE_KEY 	= "maxPoolSize";
	
	public static String CFG_OPERATION_WORK_DONE_KEY= "operationWorkDone";
	public static String CFG_OPERATION_MIX_KEY		= "operationMix";
	public static String CFG_OPERATION_BUSY_PCT_KEY	= "operationBusyPct";
	public static String CFG_MEMORY_SIZES_KEY		= "memorySizes";
	public static String CFG_MEMORY_MIX_KEY			= "memoryMix";
	
	// Operation indices used in the mix matrix.
	public static final int PREDICTABLE_OP = 0;
		
	public String _baseUrl;

	private HttpTransport _http;
	private boolean _usePooling = false;
	private boolean _debug = false;
	private Random _random = new Random();
	private NegativeExponential _thinkTimeGenerator  = null;
	
	/* Example (all the operation* arrays MUST be the same size.  
	   The memory* arrays must be the same size: 
	 operationWorkDone (ms): [50, 100, 200]
	 operationMix:           [30, 30, 40]
	 operationBusyPct:     	 [50, 75, 75] | 0 <= x <= 100
	 
	 memorySizes:            [small,med,large]
	 memoryMix:             [10,20,70]
	 */
	
	private int[] _operationWorkDone	= null; // Values interpreted as msecs
	private float[] _operationMix 		= null;
	private String[] _memorySizes 		= null;
	private float[] _memoryMix 			= null;
	private int[] _operationBusyPct 	= null; 
		
	public PredictableAppGenerator(ScenarioTrack track) 
	{
		super(track);
		this._baseUrl 	= "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();
	}

	/**
	 * Initialize this generator.
	 */
	@Override
	public void initialize()
	{
		this._http = new HttpTransport();
		this._thinkTimeGenerator = new NegativeExponential( this._thinkTime );
	}

	/*
	 * Configure this generator. 
	 */
	@Override
    public void configure( JSONObject config ) throws JSONException
    {
		if( config.has(CFG_USE_POOLING_KEY) )
			this._usePooling = config.getBoolean( CFG_USE_POOLING_KEY );
		
		/* Example (all the operation* arrays MUST be the same size.  
		   The memory* arrays must be the same size: 
		 operationWorkDone (ms): [50, 100, 200]
		 operationMix:           [30, 30, 40]
		 operationBusyPct:     	 [50, 75, 75] | 0 <= x <= 100
		 
		 memorySizes:            [small,med,large]
		 memoryMix:             [10,20,70]
		 */
		
		// Get the operation work done choices
		JSONArray operationWorkDone = config.getJSONArray( CFG_OPERATION_WORK_DONE_KEY );
		JSONArray operationMix = config.getJSONArray( CFG_OPERATION_MIX_KEY );
		JSONArray operationBusyPct = config.getJSONArray( CFG_OPERATION_BUSY_PCT_KEY );
		
		// Check that the operation* arrays are the same size - use the size of operationWorkDone array as the reference 
		if( (operationWorkDone.length() != operationMix.length() ) || (operationWorkDone.length() != operationBusyPct.length() ) )
				throw new JSONException( "Invalid generator configuration: Operation* arrays must be the same size." );
				
		JSONArray memorySizes = config.getJSONArray(CFG_MEMORY_SIZES_KEY);
		JSONArray memoryMix = config.getJSONArray( CFG_MEMORY_MIX_KEY );
		
		// Check that the memory* arrays are the same size - use the size of memorySizes array as the reference
		if( memorySizes.length() != memoryMix.length() )
			throw new JSONException( "Invalid generator configuration: Memory* arrays must be the same size." );
		
		// Everything is sized as expected
		
		// Fill in operation*
		int numOperations = operationWorkDone.length();
		this._operationWorkDone = new int[numOperations];
		this._operationMix = new float[numOperations];
		this._operationBusyPct = new int[numOperations];
		
		int operationMixSum = 0;
		
		for( int i = 0; i < numOperations; i++ )
		{
			this._operationWorkDone[i] = operationWorkDone.getInt( i );
			this._operationBusyPct[i] = operationBusyPct.getInt( i );
			this._operationMix[i] = operationMix.getInt( i );
			operationMixSum += this._operationMix[i]; 
		}
		
		// Normalize the operationMix so it doesn't necessarily have to sum to 100 when it's specified.
		// Create the selection vector here as well.
		for( int i = 0; i < numOperations; i++ )
		{
			// Calculate the right percentage based on normalizing by the sum
			this._operationMix[i] /= (float) operationMixSum;
			// Compute selection vector here by adding the proportion of the previous element 
			if( i > 0 )
				this._operationMix[i] += this._operationMix[i-1];
		}
		
		// Fill in memory*
		int numMemorySizes = memorySizes.length();
		int memoryMixSum = 0;
		this._memorySizes = new String[numMemorySizes];
		this._memoryMix = new float[numMemorySizes];
				
		for( int i = 0; i < numMemorySizes; i++ )
		{
			this._memorySizes[i] = memorySizes.getString( i );
			this._memoryMix[i] = memoryMix.getInt( i );
			memoryMixSum += this._memoryMix[i]; 
		}
		
		// Normalize the memoryMix so it doesn't necessarily have to sum to 100 when it's specified.
		// Create the selection vector here as well.
		for( int i = 0; i < numMemorySizes; i++ )
		{
			// Calculate the right percentage based on normalizing by the sum
			this._memoryMix[i] /= (float) memoryMixSum;
			// Compute selection vector here by adding the proportion of the previous element 
			if( i > 0 )
				this._memoryMix[i] += this._memoryMix[i-1];
		}
		
		// Print out all the config params we have derived
		if( this._debug )
			this.printConfig( System.out );
    }
	
	private void printConfig( PrintStream out )
	{
		out.print( "[Predictable Op Generator] Operation work done      : " );
		for( int i = 0; i < this._operationWorkDone.length; i++ )
		{
			out.print( this._operationWorkDone[i] );
			out.print( " " );
		}
		out.println( "" );
		
		out.print( "[Predictable Op Generator] Operation selection mix  : " );
		for( int i = 0; i < this._operationMix.length; i++ )
		{
			out.print( this._operationMix[i] );
			out.print( " " );
		}
		out.println( "" );
		
		out.print( "[Predictable Op Generator] Operation busy pct       : " );
		for( int i = 0; i < this._operationBusyPct.length; i++ )
		{
			out.print( this._operationBusyPct[i] );
			out.print( " " );
		}
		out.println( "" );
		
		out.print( "[Predictable Op Generator] Memory sizes             : " );
		for( int i = 0; i < this._memorySizes.length; i++ )
		{
			out.print( this._memorySizes[i] );
			out.print( " " );
		}
		out.println( "" );
		
		out.print( "[Predictable Op Generator] Memory size selection mix: " );
		for( int i = 0; i < this._memoryMix.length; i++ )
		{
			out.print( this._memoryMix[i] );
			out.print( " " );
		}
		out.println( "" );
	}
	
	@Override
	public void dispose() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public long getCycleTime() 
	{
		return 0;
	}

	@Override
	public long getThinkTime() 
	{
		if( this._thinkTimeGenerator == null )
			return 0;
		
		long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextThinkTime, (5*this._thinkTime) );
	}
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		// We must save the latest loadprofile if we want the little's law calculation to be done.
		// Latest profile stores the number of users
		this._latestLoadProfile = currentLoad;
		
		// Process for generating the next operation - we ignore what happened before (lastOperation)
		
		// 1) Pick the work done
		// 2) Pick server side activity
		// 3) Pick memory size
		
		float workDoneRand = this._random.nextFloat();
		
		int i = 0;
		for( i = 0; i < this._operationMix.length; i++ )
		{
			if( workDoneRand <= this._operationMix[i] )
				break;
		}
		int workDone = this._operationWorkDone[i];
		// Busy pct is linked to the amount of workDone
		int busyPct = this._operationBusyPct[i];
		
		float memorySizeRand = this._random.nextFloat();
		i = 0;
		for( i = 0; i < this._memoryMix.length; i++ )
		{
			if( memorySizeRand <= this._memoryMix[i] )
				break;
		}
		String memorySize = this._memorySizes[i];
		
		return this.createPredictableOperation( workDone, memorySize, busyPct );
	}

	private PredictableAppOperation createPredictableOperation( int workDone, String memorySize, int busyPct )
	{
		PredictableAppOperation op = null;
		StringBuilder opName = new StringBuilder( PredictableAppOperation.NAME_PREFIX ).append( workDone );
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (PredictableAppOperation) pool.rentObject( opName.toString() );	
		}
		
		if( op == null )
			op = new PredictableAppOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Fill in the parameters
		op._workDone = workDone;
		op._memorySize = memorySize;
		op._busyPct = busyPct;
		// Set the name so that the different workDone operations can be separated on the scoreboard
		op.setName( opName.toString() );
		
		op.prepare( this );
		return op;
	}
	
	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}
	
	public static void main( String[] args ) throws JSONException, Throwable
	{
		String generatorClassName = "radlab.rain.workload.httptest.PredictableTestGenerator";
		String hostname = null;
		int port = -1;
		
		JSONObject generatorParams = null;
		
		try
		{
			String filename = "config/profiles.config.ac_predictable.json";
			String fileContents = ConfigUtil.readFileAsString( filename );
			JSONObject jsonConfigRoot = new JSONObject( fileContents );
			JSONObject jsonTrack = jsonConfigRoot.getJSONObject( "predictable-001" );
			JSONObject jsonConfig = jsonTrack.getJSONObject( "target" );
			generatorParams = jsonTrack.getJSONObject( "generatorParameters" );
			
			hostname = jsonConfig.getString( "hostname" );
			port = jsonConfig.getInt( "port" );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
		
		Scenario testScenario = new Scenario();
		testScenario.setRampUp( 10 );
		testScenario.setDuration( 600 );
		testScenario.setRampDown( 10 );
		DefaultScenarioTrack track = new DefaultScenarioTrack( testScenario );
		track.initialize( generatorClassName, hostname, port );
				
		PredictableAppGenerator gen = new PredictableAppGenerator( track );
		gen.initialize();
		gen.configure( generatorParams );
		
		/*
		Operation pop = null;
		for( int i = 0; i < 20; i++ )
		{
			pop = gen.nextRequest(1);
			pop.execute();
			pop.cleanup();
		}*/	
	}
}
