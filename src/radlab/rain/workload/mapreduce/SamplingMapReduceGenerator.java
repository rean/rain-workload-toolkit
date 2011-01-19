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

package radlab.rain.workload.mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Random;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.Scenario;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.ConfigUtil;
import radlab.rain.util.EmpiricalCDF;

public class SamplingMapReduceGenerator extends Generator 
{
	public static String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	// Configuration parameters
	public static String CFG_HDFS_ROOT 				= "hdfsRoot";
	public static String CFG_HDFS_INPUT_PATH 		= "hdfsInputPath";
	public static String CFG_HDFS_OUTPUT_PATH 		= "hdfsOutputPath";
	public static String CFG_JOB_TRACKER			= "jobTracker";
	public static String CFG_BYTES_IN_HDFS_INPUT	= "bytesInHdfsInput";
	public static String CFG_HDFS_FILE_SIZE			= "hdfsFileSize";
	public static String CFG_TRACE_SUMMARY_FILE		= "traceSummaryFile";
	public static String CFG_DEBUG_MODE				= "debug";
	public static String CFG_MAX_MAP_OUTPUT			= "maxMapOutput";
	public static String CFG_MAX_HDFS_BYTES			= "maxHdfsBytes";
		
	/*
	 jobid, jobname, map_input_bytes, map_output_bytes, hdfs_bytes_written, submit_time, interarrival gap, duration, total_map_time, total_reduce_time, total_time
	 */
	public static long MB = 1024*1024;
	public static long UNLIMITED_SIZE = -1;
	public static int WORK_GEN = 0;
	
	private EmpiricalCDF _interJobArrivalCdf;
	private EmpiricalCDF _mapInputCdf;
	private EmpiricalCDF _shuffleInputRatioCdf;
	private EmpiricalCDF _outputShuffleRatioCdf;
	private EmpiricalCDF _runningTimeCdf;
	
	private Random _random = new Random();
	private NumberFormat _formatter = new DecimalFormat( "00000" );
	private boolean _debug = false;
	private long _maxMapOutput = UNLIMITED_SIZE;
	private long _maxHdfsBytes = UNLIMITED_SIZE;
		
	// Generator parameters - all configurable
	private String _hdfsRoot 			= "hdfs://localhost:9000";
	private String _hdfsInputPath 		= this._hdfsRoot + "/user/rean/input_test";
	private String _hdfsOutputPath 		= this._hdfsRoot + "/user/rean/output";
	private String _jobTracker 			= "localhost:9001";
	private long _bytesInHdfsInput 		= 1024*1024*1024; // Default to 1 gb
	private long _hdfsFileSize 			= 64*1024*1024; // Default to 64 mb
	private String _traceSummaryFile 	= "";
	private BufferedReader _fileReader 	= null;
	private long _nextThinkTime 		= 0;
	private long _nextCycleTime			= 0;
	
	public SamplingMapReduceGenerator(ScenarioTrack track) 
	{
		super(track);
	}

	@Override
	public void dispose() 
	{
		// Close the trace summary file
		this.closeTraceSummaryFile();
	}

	@Override
	public long getCycleTime() 
	{
		// Sample from interarrival cdf
		return this._nextCycleTime;
	}

	@Override
	public long getThinkTime() 
	{
		// Sample from interarrival cdf
		return this._nextThinkTime;
	}

	@Override
	public void initialize() 
	{
		// Load up 5-pt summaries [1,25,50,75,99]
		// Map Input sizes
		TreeMap<Double,Double> mapInputCdfSummary = new TreeMap<Double,Double>();
		mapInputCdfSummary.put( new Double(0.01), new Double( 0 ) );
		mapInputCdfSummary.put( new Double(0.25), new Double( 288 ) );
		mapInputCdfSummary.put( new Double(0.50), new Double( 21828 ) );
		mapInputCdfSummary.put( new Double(0.75), new Double( 673573.499999998 ) );
		mapInputCdfSummary.put( new Double(0.99), new Double( 3976242259608.18 ) );
		this._mapInputCdf = new EmpiricalCDF( mapInputCdfSummary );
		
		// Interarrival times in seconds
		TreeMap<Double,Double> interarrivalCdfSummary = new TreeMap<Double,Double>();
		interarrivalCdfSummary.put( new Double(0.01), new Double( 0 ) );
		interarrivalCdfSummary.put( new Double(0.25), new Double( 2.75 ));
		interarrivalCdfSummary.put( new Double(0.50), new Double( 7 ));
		interarrivalCdfSummary.put( new Double(0.75), new Double( 15.25 ));
		interarrivalCdfSummary.put( new Double(0.99), new Double( 122642.24 ));
		this._interJobArrivalCdf = new EmpiricalCDF( interarrivalCdfSummary );
		
		// Map output (shuffle): map input ratios
		// Output (hdfs bytes written) : Map Input ratios
		TreeMap<Double,Double> shuffleInputRatioCdfSummary = new TreeMap<Double,Double>();
		shuffleInputRatioCdfSummary.put( new Double(0.01), new Double( 0 ) );
		shuffleInputRatioCdfSummary.put( new Double(0.25), new Double( 0 ) );
		shuffleInputRatioCdfSummary.put( new Double(0.50), new Double( 0 ) );
		shuffleInputRatioCdfSummary.put( new Double(0.75), new Double( 2.86700194347304e-08 ) );
		shuffleInputRatioCdfSummary.put( new Double(0.99), new Double( 395.549387824282 ) );
		this._shuffleInputRatioCdf = new EmpiricalCDF( shuffleInputRatioCdfSummary );
	
		//Final output (hdfs bytes written):Map output (shuffle input) ratios 
		TreeMap<Double,Double> outputShuffleRatioCdfSummary = new TreeMap<Double,Double>();
		outputShuffleRatioCdfSummary.put( new Double(0.01), new Double( 0 ) );
		outputShuffleRatioCdfSummary.put( new Double(0.25), new Double( 0 ) );
		outputShuffleRatioCdfSummary.put( new Double(0.50), new Double( 0 ) );
		outputShuffleRatioCdfSummary.put( new Double(0.75), new Double( 1.13902515918428e-07 ) );
		outputShuffleRatioCdfSummary.put( new Double(0.99), new Double( 817.092310606068 ) );
		this._outputShuffleRatioCdf = new EmpiricalCDF( outputShuffleRatioCdfSummary );
		// Running time
		TreeMap<Double,Double> runningTimeCdfSummary = new TreeMap<Double,Double>();
		runningTimeCdfSummary.put( new Double( 0.01 ), new Double( 1.02 ) );
		runningTimeCdfSummary.put( new Double( 0.25 ), new Double( 10 ) );
		runningTimeCdfSummary.put( new Double( 0.50 ), new Double( 35 ) );
		runningTimeCdfSummary.put( new Double( 0.75 ), new Double( 108.5 ) );
		runningTimeCdfSummary.put( new Double( 0.99 ), new Double( 46520.0800000004 ) );
		this._runningTimeCdf = new EmpiricalCDF( runningTimeCdfSummary );		
	}

	public String getRandomString( int length )
	{
		StringBuffer buf = new StringBuffer();
		int count = 0;
		while( count < length )
		{
			char rndChar = SamplingMapReduceGenerator.ALPHABET.charAt( this._random.nextInt( SamplingMapReduceGenerator.ALPHABET.length() ) );
			buf.append( rndChar );
			count++;
		}
		
		return buf.toString();
	}
		
	@Override
    public void configure( JSONObject config ) throws JSONException
    {
		this._hdfsRoot = config.getString( CFG_HDFS_ROOT );
		this._hdfsInputPath = this._hdfsRoot + "/" + config.getString( CFG_HDFS_INPUT_PATH );
		this._hdfsOutputPath = this._hdfsRoot + "/" + config.getString( CFG_HDFS_OUTPUT_PATH );
		this._jobTracker = config.getString( CFG_JOB_TRACKER );
		this._bytesInHdfsInput = config.getLong( CFG_BYTES_IN_HDFS_INPUT );
		// HDFS files default to 64mb, but other file sizes can be used
		if( config.has( CFG_HDFS_FILE_SIZE ) )
			this._hdfsFileSize = config.getLong( CFG_HDFS_FILE_SIZE );
		// We expect a trace summary file to guide generation
		this._traceSummaryFile = config.getString( CFG_TRACE_SUMMARY_FILE );
		
		// Parse/pre-process the traceSummary file
		try
		{
			this.preprocessTraceSummary( this._traceSummaryFile );
		}
		catch( IOException ioe )
		{
			throw new JSONException( ioe );
		}
		
		// Look at whether we're in debug mode
		if( config.has( CFG_DEBUG_MODE ) )
			this._debug = config.getBoolean( CFG_DEBUG_MODE );
		// Any limits on map output specified?
		if( config.has( CFG_MAX_MAP_OUTPUT ) )
			this._maxMapOutput = config.getLong( CFG_MAX_MAP_OUTPUT );
		// Any limits on hdfs bytes written specified?
		if( config.has( CFG_MAX_HDFS_BYTES ) )
			this._maxHdfsBytes = config.getLong( CFG_MAX_HDFS_BYTES );
    }
	
	private void preprocessTraceSummary( String traceSummaryFile ) throws IOException
	{
		// Close the file if it's already open
		if( this._fileReader != null )
			this.closeTraceSummaryFile();
		
		this._fileReader = new BufferedReader( new FileReader( this._traceSummaryFile ) );
	}
	
	private void closeTraceSummaryFile()
	{
		if( this._fileReader != null )
		{
			try
			{
				this._fileReader.close();
			}
			catch( IOException ioe )
			{
				System.out.println( this + " error closing trace summary file: " + this._traceSummaryFile );
			}
		}	
	}
	
	public String getHdfsRoot() { return this._hdfsRoot; }
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		
		// Sample job name first
		// Get vector of empirical cdfs from that
		// Then sample from those empirical cdfs to produce the next operation
		
		// Describe everything from the input size (map input) and use
		// the input:mapoutput ratios and input:output (hdfs bytes written) ratios
		// to determine the bytes in and out of each phase.
				
		// Sample from input size cdf - pick input size
		// Sample from inputsize:map_output_ratio, compute map_output size
		// Sample from inputsize:output ratio, compute output size
		// Sample from running time cdf, compute running time
		
		float shuffleInputRatio = 1.0f;
		float outputShuffleRatio = 1.0f;
		
		long mapInputSize = 0;
		while( mapInputSize == 0 )
			mapInputSize = (long) this._mapInputCdf.nextDouble();

		// Figure out how many files we need to get this amount of input bytes
		int filesNeeded = Math.round( mapInputSize/this._hdfsFileSize );
		if( filesNeeded == 0 )
			filesNeeded = 1;
		
		// Figure out how many files are in hdfs
		int filesInHdfs = Math.round( this._bytesInHdfsInput/this._hdfsFileSize );
		// Pick filesNeeded out of filesInHdfs part-xxxxx files without replacement as inputs
		if( filesNeeded >= filesInHdfs )
			filesNeeded = filesInHdfs;	 // read all the data
		
		int filesFound = 0;
		Hashtable<Integer,Integer> fileParts = new Hashtable<Integer,Integer>();
		if( filesNeeded == filesInHdfs )
		{
			// Add all
			for( int i = 0; i < filesInHdfs; i++ )
				fileParts.put( i, i );
		}
		else
		{
			while( filesFound < filesNeeded )
			{
				int partNum = this._random.nextInt( filesInHdfs );
				if( !fileParts.contains( partNum ) )
				{
					fileParts.put( partNum, partNum );
					filesFound++;
				}	
			}
		}
		
		// Convert the fileParts hashtable into a comma separated list of file names
		StringBuffer inputFiles = new StringBuffer();
		Iterator<Integer> valIt = fileParts.values().iterator();
		while( valIt.hasNext() )
		{
			inputFiles.append( this._hdfsInputPath );
			inputFiles.append( "/" );
			inputFiles.append( "part-" );
			inputFiles.append( this._formatter.format( valIt.next() ) );
			if( valIt.hasNext() )
				inputFiles.append( "," );
		}
		
		long mapOutputSize = 0;
		while(mapOutputSize == 0 )
		{
			shuffleInputRatio = (float) this._shuffleInputRatioCdf.nextDouble();
			mapOutputSize = (long) (mapInputSize * shuffleInputRatio);
		}
		// Pick the output bytes written on the map input bytes or the shuffle input bytes (mapOutputSize)
		long hdfsBytesWritten = 0;
		while( hdfsBytesWritten == 0 )
		{
			outputShuffleRatio = (float) this._outputShuffleRatioCdf.nextDouble(); 
			hdfsBytesWritten = (long) ( mapOutputSize * outputShuffleRatio );//(mapInputSize * this._outputInputRatioCdf.nextDouble() );
		}
		long runningTime = 0;
		while( runningTime == 0 )
			runningTime = (long) this._runningTimeCdf.nextDouble();
		long interArrivalTime = (long) this._interJobArrivalCdf.nextDouble() * 1000; // Convert from secs to msecs
		
		// Last minute adjustments:
		if( this._debug )
		{
			System.out.println( 
					this + " (before adjustment) map input: " + mapInputSize + 
					" map output: " + mapOutputSize + " hdfs bytes: " + hdfsBytesWritten );	
		}
		
		// Make sure we have non-zero values
		if( mapInputSize < 1000 )
			mapInputSize = 1000;
		
		if( mapOutputSize < 1000 )
			mapOutputSize = 1000;
		
		if( hdfsBytesWritten < 1000 )
			hdfsBytesWritten = 1000;
									
		// Check whether there's a max shuffle limit specified in the configuration file
		if( this._maxMapOutput != UNLIMITED_SIZE )
		{
			System.out.println( this + " Configuration file limits map output to: " + this._maxMapOutput );
			mapOutputSize = this._maxMapOutput;
		}
		
		// Check whether there's a max output limit specified in the configuration file
		if( this._maxHdfsBytes != UNLIMITED_SIZE )
		{
			System.out.println( this + " Configuration file limits bytes written to HDFS to: " + this._maxHdfsBytes );
			hdfsBytesWritten = this._maxHdfsBytes;
		}
		
		if( this._debug )
		{
			System.out.println( this + " (after adjustment) map input: " + mapInputSize + 
					" map output: " + mapOutputSize + " hdfs bytes: " + hdfsBytesWritten );
		}
				
		/*
		System.out.println( "-----------------------" );
		System.out.println( "mapInput (bytes)  : " + mapInputSize );
		System.out.println( "mapOutput (bytes) : " + mapOutputSize );
		System.out.println( "hdfs bytes written: " + hdfsBytesWritten );
		System.out.println( "running time      : " + runningTime );
		System.out.println( "interarrival (s)  : " + interArrivalTime );
		System.out.println( "-----------------------" );
		*/
		
		this._nextCycleTime = interArrivalTime;
		this._nextThinkTime = interArrivalTime;
		
		// Create A MapReduce Operation
		MapReduceOperation op = new WorkGenMapReduceOperation( true, this._scoreboard );
		// Set specific fields on the new operation
		op.setJobTracker( this._jobTracker ); // Set the job tracker so we can get status updates
		op.setInputPath( inputFiles.toString() );
		// Give each job a unique output file/path to write to, not the root output directory
		op.setOutputPath( this._hdfsOutputPath + "/" + this._name + "." + this.getRandomString( 10 ) );
		// We might have to create this output path before we start
		op.setShuffleInputRatio( shuffleInputRatio );
		op.setOutputShuffleRatio( outputShuffleRatio );
		op.setInterarrival( interArrivalTime );
		// Let the operation extract any specific info it needs from the generator
		op.prepare( this );
		// Delete the output file from hdfs (if it already exists)
		op.cleanup();
		return op;
	}

	@Override
	public String toString()
	{
		return "[SamplingMapReduceGenerator]";
	}
	
	public static void main( String[] args ) throws Throwable
	{
		String generatorClassName = "radlab.rain.workload.mapreduce.SamplingMapReduceGenerator";
		String hostname = null;
		int port = -1;
		
		try
		{
			String filename = "config/profiles.config.mapreduce.json";
			String fileContents = ConfigUtil.readFileAsString( filename );
			JSONObject jsonConfigRoot = new JSONObject( fileContents );
			JSONObject jsonTrack = jsonConfigRoot.getJSONObject( "mapreduce-001" );
			JSONObject jsonConfig = jsonTrack.getJSONObject( "target" );
			
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
				
		SamplingMapReduceGenerator gen = new SamplingMapReduceGenerator( track );
		gen.initialize();
		
		Operation mro = null;
		
		for( int i = 0; i < 5; i++ )
		{
			mro = gen.nextRequest(1);
			mro.execute();
			mro.cleanup();
		}
	}
	
}
