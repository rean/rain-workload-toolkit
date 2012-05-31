package radlab.rain.util;

import java.io.File;
import java.io.PrintStream;

import org.json.JSONObject;

import radlab.rain.ResponseTimeStat;

public class FileMetricWriter extends MetricWriter 
{
	private String _filename = "";
	private PrintStream _out = null;
	
	public FileMetricWriter( JSONObject config ) throws Exception 
	{
		super( config );
		this._filename = config.getString( MetricWriter.CFG_FILENAME_KEY );
	}

	@Override
	public boolean write( ResponseTimeStat stat ) throws Exception 
	{
		// Try to open the file if it is not open
		if( this._out == null )
		{
			this._out = new PrintStream( new File( this._filename ) );
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
		buf.append( "FILE: ").append( this._filename );
		return buf.toString();
	}
}
