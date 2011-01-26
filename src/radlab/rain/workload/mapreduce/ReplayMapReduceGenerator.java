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
import java.util.Hashtable;
import java.util.Random;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;

public class ReplayMapReduceGenerator extends Generator 
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
	
	// File parsing format constants
	public static String COLUMN_SEPARATOR		= "\t";
	public static int JOB_ID_COL 				= 0;
	public static int JOB_NAME_COL 				= 1;
	public static int MAP_INPUT_BYTES_COL 		= 2;
	public static int MAP_OUTPUT_BYTES_COL 		= 3;
	public static int HDFS_BYTES_WRITTEN_COL 	= 4;
	public static int SUBMIT_TIME_COL 			= 5;
	public static int INTERARRIVAL_GAP_COL 		= 6;
	public static int DURATION_COL 				= 7;
	public static int TOTAL_MAP_TIME_COL 		= 8;
	public static int TOTAL_REDUCE_TIME_COL 	= 9;
	public static int TOTAL_TIME_COL 			= 10;
	public static int MAX_COLUMNS				= 11;
	
	/*
	 jobid, jobname, map_input_bytes, map_output_bytes, hdfs_bytes_written, 
	 submit_time, interarrival gap, duration, total_map_time, total_reduce_time, total_time
	 */
	
	public static long MB = 1024*1024;
	public static long UNLIMITED_SIZE = -1;
	
	public static int WORK_GEN = 0;
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
	
	public ReplayMapReduceGenerator(ScenarioTrack track) 
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
		return this._nextCycleTime;
	}

	@Override
	public long getThinkTime() 
	{
		return this._nextThinkTime;
	}

	@Override
	public void initialize() 
	{}

	public String getRandomString( int length )
	{
		StringBuffer buf = new StringBuffer();
		int count = 0;
		while( count < length )
		{
			char rndChar = ReplayMapReduceGenerator.ALPHABET.charAt( this._random.nextInt( ReplayMapReduceGenerator.ALPHABET.length() ) );
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
	public Operation nextRequest( int lastOperation ) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		
		try
		{
			String line = this._fileReader.readLine();
			if( line != null )
			{
				// Parse line and create a new workgen MR operation
				String[] fields = line.split( COLUMN_SEPARATOR );
				// Check on the number of columns we expect a certain number of them
				if( fields.length == MAX_COLUMNS )
				{
					float shuffleInputRatio = 1.0f;
					float outputShuffleRatio = 1.0f;
					
					String jobName = fields[ReplayMapReduceGenerator.JOB_NAME_COL];
					long mapInputSize = Long.parseLong( fields[ReplayMapReduceGenerator.MAP_INPUT_BYTES_COL] );
					
					if( mapInputSize > this._bytesInHdfsInput )
					{
						System.out.println( this + " Configuration file limits map input to: " + this._bytesInHdfsInput + " bytes, which equals all the bytes in HDFS." );
						// Don't reduce the mapInputSize from what was originally read in, instead keep it so that we 
						// can calculate the original shuffle/input ratio and use that on less input data
					}
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
					
					long mapOutputSize = Long.parseLong( fields[ReplayMapReduceGenerator.MAP_OUTPUT_BYTES_COL] );
					long hdfsBytesWritten = Long.parseLong( fields[ReplayMapReduceGenerator.HDFS_BYTES_WRITTEN_COL] );
					long interArrivalGap = Long.parseLong( fields[ReplayMapReduceGenerator.INTERARRIVAL_GAP_COL] ) * 1000; // convert secs to msecs
			
					if( this._debug )
					{
						System.out.println( this + " " + line );
						System.out.println( 
								this + " " + jobName + " (before adjustment) map input: " + mapInputSize + 
								" map output: " + mapOutputSize + " hdfs bytes: " + hdfsBytesWritten );	
					}
					
					// Make sure we have non-zero values
					// If the mapInputSize is less than 1 block in hdfs,
					// bump it up to 1 block at least.
					if( mapInputSize < this._hdfsFileSize )
						mapInputSize = this._hdfsFileSize;
					
					if( mapOutputSize < 1000 )
						mapOutputSize = 1000;
					//if( mapOutputSize < this._hdfsFileSize )
						//mapOutputSize = this._hdfsFileSize;
					
					if( hdfsBytesWritten < 1000 )
						hdfsBytesWritten = 1000;
					//if( hdfsBytesWritten < this._hdfsFileSize )
					//	hdfsBytesWritten = this._hdfsFileSize;
					
					
					// Check whether there's a max shuffle limit specified in the configuration file
					if( this._maxMapOutput != UNLIMITED_SIZE && mapOutputSize > this._maxMapOutput )
					{
						System.out.println( this + " Configuration file limits map output to: " + this._maxMapOutput );
						mapOutputSize = this._maxMapOutput;
					}
					
					// Check whether there's a max output limit specified in the configuration file
					if( this._maxHdfsBytes != UNLIMITED_SIZE && hdfsBytesWritten > this._maxHdfsBytes )
					{
						System.out.println( this + " Configuration file limits bytes written to HDFS to: " + this._maxHdfsBytes );
						hdfsBytesWritten = this._maxHdfsBytes;
					}
					
					if( this._debug )
					{
						System.out.println( this + " " + jobName + " (after adjustment)  map input: " + mapInputSize + 
								" map output: " + mapOutputSize + " hdfs bytes: " + hdfsBytesWritten );
					}
					
					// Set the next think/cycle time using the interarrival gap
					this._nextCycleTime = interArrivalGap;
					this._nextThinkTime = interArrivalGap;
					
					if( this._debug )
					{
						System.out.println( this + " next cycle time: " + interArrivalGap );
						System.out.println( this + " next think time: " + interArrivalGap );
					}
					
					// Compute the ratios
					shuffleInputRatio = (float) ( (double) mapOutputSize / (double) mapInputSize );
					outputShuffleRatio = (float) ( (double) hdfsBytesWritten / (double) mapOutputSize );
					
					if( this._debug )
						System.out.println( this + " " + jobName + " Shuffle input ratio: " + shuffleInputRatio + " output shuffle ratio: " + outputShuffleRatio );
					
					// Create A MapReduce Operation
					MapReduceOperation op = new WorkGenMapReduceOperation( true, this._scoreboard );
					// Set specific fields on the new operation
					op.setJobName( jobName );
					op.setJobTracker( this._jobTracker ); // Set the job tracker so we can get status updates
					op.setInputPath( inputFiles.toString() );
					// Give each job a unique output file/path to write gap
					this._nextCycleTime = interArrivalGap;
					this._nextThinkTime = interArrivalGap; // to, not the root output directory
					op.setOutputPath( this._hdfsOutputPath + "/" + this._name + "." + this.getRandomString( 10 ) );
					// We might have to create this output path before we start
					op.setShuffleInputRatio( shuffleInputRatio );
					op.setOutputShuffleRatio( outputShuffleRatio );
					op.setInterarrival( interArrivalGap );
					// Let the operation extract any specific info it needs from the generator
					op.prepare( this );
					// Delete the output file from hdfs (if it already exists)
					op.cleanup();
					return op;
				}
				else
				{
					// Do something appropriate if we get a line that's not 
					// formatted the way we expect it
					return null; // no-op
				}
			}
			else
			{
				// Return null or no-op MapReduce operation OR should we restart reading from the
				// start of the file again?
				return null; // no-op
			}
		}
		catch( IOException ioe )
		{
			// Log the IO exception reading from the replay file
			return null;
		}
	}

	@Override
	public String toString()
	{
		return "[MapReduceGenerator]";
	}
	
	/*
	public static void main( String[] args ) throws Throwable
	{
		String generatorClassName = "radlab.rain.workload.mapreduce.MapReduceGenerator";
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
				
		ReplayMapReduceGenerator gen = new ReplayMapReduceGenerator( track );
		gen.initialize();
		
		Operation mro = null;
		
		for( int i = 0; i < 5; i++ )
		{
			mro = gen.nextRequest(1);
			mro.execute();
			mro.cleanup();
		}
	}*/
	
}
