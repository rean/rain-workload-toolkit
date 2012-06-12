package radlab.rain.util;

import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.JSONObject;

import radlab.rain.ResponseTimeStat;

public class SocketMetricObjectWriter extends SocketMetricWriter 
{
	private ObjectOutputStream _out = null;
	
	public SocketMetricObjectWriter( JSONObject config ) throws Exception 
	{
		super( config );
	}

	@Override
	public boolean write( ResponseTimeStat stat ) throws Exception 
	{
		if( this._out == null )
		{
			this._out = new ObjectOutputStream( new Socket( this._ipAddress, this._port ).getOutputStream() );
		}
		
		this._out.writeObject( stat );
		this._out.reset();
		
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
		buf.append( "SOCKET [object writer]" ).append( this._ipAddress ).append( ":" ).append( this._port );
		return buf.toString();
	}
}
