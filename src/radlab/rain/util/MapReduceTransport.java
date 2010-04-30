package radlab.rain.util;

import java.net.InetSocketAddress;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.RunningJob;

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
		client.submitJob( (JobConf) jobConf );
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main( String[] args ) 
	{
		

	}
}
