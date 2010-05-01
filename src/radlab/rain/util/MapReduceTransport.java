package radlab.rain.util;

import java.net.InetSocketAddress;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.RunningJob;

import java.util.Random;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


@SuppressWarnings("deprecation")
public class MapReduceTransport implements Tool
{
	private String _jobTrackerAddress = "";
	private int _jobTrackerPort = 0;
	
	public MapReduceTransport( String jobTrackerAddress, int jobTrackerPort )
	{
		this._jobTrackerAddress = jobTrackerAddress;
		this._jobTrackerPort = jobTrackerPort;
	}
	
	public String getJobTrackerAddress() { return this._jobTrackerAddress; }
	public void setJobTrackerAddress( String val ) { this._jobTrackerAddress = val; }
	
	public int getJobTrackerPort() { return this._jobTrackerPort; }
	public void setJobTrackerPort( int val ) { this._jobTrackerPort = val; }
	
	public RunningJob submitJob( Configuration jobConf ) throws IOException
	{
		JobClient client = new JobClient( new InetSocketAddress( this._jobTrackerAddress, this._jobTrackerPort ), jobConf );
		client.submitJob( (JobConf) jobConf );
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main( String[] args ) throws Exception
	{
		int res = ToolRunner.run(new Configuration(), new MapReduceTransport("localhost", 9001), args);
	    System.exit(res);

	}
	
	public int run(String[] args) throws Exception {
	    if (args.length < 3) {
	      System.out.println("Grep <inDir> <outDir> <regex> [<group>]");
	      ToolRunner.printGenericCommandUsage(System.out);
	      return -1;
	    }

	    Path tempDir =
	      new Path("grep-temp-"+
	          Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));

	    JobConf grepJob = new JobConf(new Configuration(), MapReduceTransport.class);

	    try {

	      grepJob.setJobName("grep-search");

	      FileInputFormat.setInputPaths(grepJob, args[0]);

	      grepJob.setMapperClass(RegexMapper.class);
	      grepJob.set("mapred.mapper.regex", args[2]);
	      if (args.length == 4)
	        grepJob.set("mapred.mapper.regex.group", args[3]);

	      grepJob.setCombinerClass(LongSumReducer.class);
	      grepJob.setReducerClass(LongSumReducer.class);

	      FileOutputFormat.setOutputPath(grepJob, tempDir);
	      grepJob.setOutputFormat(SequenceFileOutputFormat.class);
	      grepJob.setOutputKeyClass(Text.class);
	      grepJob.setOutputValueClass(LongWritable.class);

//	      JobClient.runJob(grepJob);
	      this.submitJob(grepJob);

	      JobConf sortJob = new JobConf(MapReduceTransport.class);
	      sortJob.setJobName("grep-sort");

	      FileInputFormat.setInputPaths(sortJob, tempDir);
	      sortJob.setInputFormat(SequenceFileInputFormat.class);

	      sortJob.setMapperClass(InverseMapper.class);

	      sortJob.setNumReduceTasks(1);                 // write a single file                 
	      FileOutputFormat.setOutputPath(sortJob, new Path(args[1]));
	      sortJob.setOutputKeyComparatorClass           // sort by decreasing freq             
	      (LongWritable.DecreasingComparator.class);

//	      JobClient.runJob(sortJob);
	      this.submitJob(sortJob);
	    }
	    finally {
	      FileSystem.get(grepJob).delete(tempDir, true);
	    }
	    return 0;
	  }

	@Override
	public Configuration getConf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConf(Configuration arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
