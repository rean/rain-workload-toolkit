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
public class MapReduceTransport
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
		return client.submitJob( (JobConf) jobConf );
		
	}
	
	/**
	 * @param args
	 */
	public static void main( String[] args ) throws Exception
	{
		
		GrepRain.main(args);

	}
	
}
