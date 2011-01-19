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

import java.io.IOException;
import java.util.Date;
import java.util.Random;
 	
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.examples.RandomWriter;

/* Work-in-progress class for populating HDFS with x random bytes grouped into N 64mb files */
@SuppressWarnings("deprecation")
public class HdfsLoader 
{
	static enum Counters { RECORDS_WRITTEN, BYTES_WRITTEN }
	
	static class RandomInputFormat implements InputFormat<Text, Text> 
	{
	    /** 
	     * Generate the requested number of file splits, with the filename
	     * set to the filename of the output file.
	     */
	    
		public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException 
	    {
			InputSplit[] result = new InputSplit[numSplits];
			Path outDir = FileOutputFormat.getOutputPath(job);
			for(int i=0; i < result.length; ++i) 
			{
				result[i] = new FileSplit(new Path(outDir, "dummy-split-" + i), 0, 1, (String[]) null);
			}
			return result;
	    }

	    /**
	     * Return a single record (filename, "") where the filename is taken from
	     * the file split.
	     */
	    static class RandomRecordReader implements RecordReader<Text, Text> 
	    {
	    	Path name;
	      
	    	public RandomRecordReader(Path p) 
	    	{
	    		name = p;
	    	}
	      
	    	public boolean next(Text key, Text value) 
	    	{
	    		if (name != null) 
	    		{
	    			key.set(name.getName());
	    			name = null;
	    			return true;
	    		}
	    		return false;
	    	}
	      
	    	public Text createKey() 
	    	{
	    		return new Text();
	    	}
	      
	    	public Text createValue() 
	    	{
	    		return new Text();
	    	}
	      
	    	public long getPos() 
	    	{
	    		return 0;
	    	}
	    	
	    	public void close() {}
	      
	    	public float getProgress() 
	    	{
	    		return 0.0f;
	    	}
	    }
	    
		public RecordReader<Text, Text> getRecordReader(InputSplit split, JobConf job, Reporter reporter) throws IOException 
		{
			return new RandomRecordReader(((FileSplit) split).getPath());
	    }
	}
	
	@SuppressWarnings("unchecked")
	static class Map extends MapReduceBase implements Mapper<WritableComparable, Writable, BytesWritable, BytesWritable> 
	{
		private long numBytesToWrite;
		private int minKeySize;
		private int keySizeRange;
		private int minValueSize;
		private int valueSizeRange;
		private Random random = new Random();
		private BytesWritable randomKey = new BytesWritable();
		private BytesWritable randomValue = new BytesWritable();
    
		private void randomizeBytes(byte[] data, int offset, int length) 
		{
			for(int i=offset + length - 1; i >= offset; --i) 
			{
				data[i] = (byte) random.nextInt(256);
			}
		}
    
		/**
		 * Given an output filename, write a bunch of random records to it.
		 */
		public void map(WritableComparable key, Writable value, OutputCollector<BytesWritable, BytesWritable> output, Reporter reporter) throws IOException 
		{
			int itemCount = 0;
			while (numBytesToWrite > 0) 
			{
				int keyLength = minKeySize + (keySizeRange != 0 ? random.nextInt(keySizeRange) : 0);
				randomKey.setSize(keyLength);
				randomizeBytes(randomKey.getBytes(), 0, randomKey.getLength());
				int valueLength = minValueSize + (valueSizeRange != 0 ? random.nextInt(valueSizeRange) : 0);
				randomValue.setSize(valueLength);
				randomizeBytes(randomValue.getBytes(), 0, randomValue.getLength());
				output.collect(randomKey, randomValue);
				numBytesToWrite -= keyLength + valueLength;
				reporter.incrCounter(Counters.BYTES_WRITTEN, keyLength + valueLength);
				reporter.incrCounter(Counters.RECORDS_WRITTEN, 1);
				if (++itemCount % 200 == 0) 
				{
					reporter.setStatus("wrote record " + itemCount + ". " + numBytesToWrite + " bytes left.");
				}
			}
			reporter.setStatus("done with " + itemCount + " records.");
		}
	
		/**
		 * Save the values out of the configuaration that we need to write
		 * the data.
		 */
		@Override
		public void configure(JobConf job) 
		{
			numBytesToWrite = job.getLong("test.randomwrite.bytes_per_map", 64*1024*1024);
			minKeySize = job.getInt("test.randomwrite.min_key", 10);
			keySizeRange = job.getInt("test.randomwrite.max_key", 1000) - minKeySize;
			minValueSize = job.getInt("test.randomwrite.min_value", 0);
			valueSizeRange = job.getInt("test.randomwrite.max_value", 20000) - minValueSize;
		}
	}
    	
	public static int randomWrite( long totalBytes, String outputPath, String fsDefaultName, String jobTracker ) throws IOException
	{
		Path outDir = new Path(outputPath);
	    	    
		JobConf job = new JobConf(RandomWriter.class);
	    job.setJobName("random-writer");
	    FileOutputFormat.setOutputPath(job, outDir);
	    
	    job.setOutputKeyClass(BytesWritable.class);
	    job.setOutputValueClass(BytesWritable.class);
	    
	    job.setInputFormat(RandomInputFormat.class);
	    job.setMapperClass(Map.class);        
	    job.setReducerClass(IdentityReducer.class);
	    job.setOutputFormat(SequenceFileOutputFormat.class);
	    
	    //JobClient client = new JobClient(job);
	    //ClusterStatus cluster = client.getClusterStatus();
	    //int numMapsPerHost = job.getInt("test.randomwriter.maps_per_host", 1);
	    
	    // Configure the job just the way we want it. Specify as many properties as we think useful
	    
	    // Set the number of bytes to write
	    job.setLong( "test.randomwrite.bytes_per_map", 64*1024*1024 );
	    job.set( "fs.default.name", fsDefaultName );
	    job.set( "mapred.job.tracker", jobTracker ); // Set this so we can see progress on the web UI
	    job.setInt("test.randomwriter.maps_per_host", 4);
	    
	    long numBytesToWritePerMap = job.getLong("test.randomwrite.bytes_per_map", 64*1024*1024 );
	    if (numBytesToWritePerMap == 0) 
	    {
	    	System.err.println("Cannot have test.randomwrite.bytes_per_map set to 0");
	    	return -2;
	    }
	    
	    long totalBytesToWrite = totalBytes;//job.getLong( "test.randomwrite.total_bytes", numMapsPerHost*numBytesToWritePerMap*cluster.getTaskTrackers() );
	    int numMaps = (int) (totalBytesToWrite / numBytesToWritePerMap);
	    if (numMaps == 0 && totalBytesToWrite > 0) 
	    {
	    	numMaps = 1;
	    	job.setLong("test.randomwrite.bytes_per_map", totalBytesToWrite);
	    }
	    
	    job.setNumMapTasks(numMaps);
	    System.out.println("Running " + numMaps + " maps.");
	    
	    // reducer NONE
	    job.setNumReduceTasks(0);
	    // Set the default dfs path
	    String defaultfs = job.get( "fs.default.name" );
	    System.out.println( "Default fs: " + defaultfs );
	    	    
	    Date startTime = new Date();
	    System.out.println("Job started: " + startTime);
	    JobClient.runJob(job);
	    Date endTime = new Date();
	    System.out.println("Job ended: " + endTime);
	    System.out.println("The job took " + 
	                       (endTime.getTime() - startTime.getTime()) /1000 + 
	                       " seconds.");
	    
	    return 0;
	}
	
	
	public static void main( String[] args )
	{
		try 
		{
			if( args.length == 0 )
			{
				long bytesToLoad = (long) 5*1024*1024*1024;
				String hdfsPath = "hdfs://localhost:9000/user/rean/input_test";
				String fsDefaultName = "hdfs://localhost:9000";
				String jobTracker = "localhost:9001";
				System.out.println( "[HDFSLOADER]" + " Loading: " + hdfsPath + " with " + bytesToLoad + " bytes of data." );
				HdfsUtil.deletePath( hdfsPath );
				HdfsLoader.randomWrite( bytesToLoad, hdfsPath, fsDefaultName, jobTracker );
			}
			else if( args.length == 4 )
			{
				// Expect hdfs path and bytes to load
				String hdfsPath = args[0];
				long bytesToLoad = Long.parseLong( args[1] );
				String fsDefaultName = args[2];
				String jobTracker = args[3];
				System.out.println( "[HDFSLOADER]" + " Loading: " + hdfsPath + " with " + bytesToLoad + " bytes of data." );
				HdfsUtil.deletePath( hdfsPath );
				HdfsLoader.randomWrite( bytesToLoad, hdfsPath, fsDefaultName, jobTracker );
			}
			else
			{
				// Print usage
				System.out.println( "Usage: 4 parameters required: " );
				System.out.println( "<hdfs path, e.g., hdfs://localhost:9000/user/rean/input_test>" ); 
				System.out.println( "<bytes to load, e.g., 5368709120>" );
				System.out.println( "<fsdefaultname, e.g., hdfs://localhost:9000>" );
				System.out.println( "<jobtracker, e.g., localhost:9001>" );
				System.out.println( "" );
				System.out.println( "Example: hdfs://localhost:9000/user/rean/input_test 5368709120 hdfs://localhost:9000 localhost:9001" );
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
