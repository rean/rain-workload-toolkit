package radlab.rain.util;

import java.io.PrintStream;
import java.net.Socket;

import org.json.JSONObject;

import radlab.rain.ResponseTimeStat;

public class SocketMetricWriter extends MetricWriter 
{
	private String _ipAddress = "";
	private int _port = -1;
	private PrintStream _out = null;
	
	public SocketMetricWriter(JSONObject config) throws Exception 
	{
		super(config);
		this._ipAddress = config.getString( MetricWriter.CFG_IP_ADDRESS_KEY );
		this._port = config.getInt( MetricWriter.CFG_PORT_KEY );
	}

	@Override
	public boolean write(ResponseTimeStat stat) throws Exception 
	{
		if( this._out == null )
		{
			this._out = new PrintStream( new Socket( this._ipAddress, this._port ).getOutputStream() );
		}
		
		this._out.println( stat );
		
		return true;
	}

	@Override
	public void close() throws Exception 
	{
		if( this._out != null )
		{
			this._out.flush();
			this._out.close();
		}
	}

	@Override
	public String getDetails() 
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "SOCKET " ).append( this._ipAddress ).append( ":" ).append( this._port );
		return buf.toString();
	}
}
