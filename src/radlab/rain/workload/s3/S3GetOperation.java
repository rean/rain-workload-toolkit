package radlab.rain.workload.s3;

import java.io.IOException;
import java.io.InputStream;

import org.jets3t.service.model.S3Object;

import radlab.rain.IScoreboard;

public class S3GetOperation extends S3Operation 
{
	public static String NAME = "Get";
	
	public S3GetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.GET;	
	}

	@Override
	public void execute() throws Throwable
	{
		S3Object object = this.doGet( this._bucket, this._key );
		byte[] buf = new byte[BUF_SIZE];
		
		InputStream input = object.getDataInputStream();
		try
		{
			while( input.read( buf ) != -1 )
				input.read( buf );
		}
		catch( IOException ioe )
		{
			throw ioe;
		}
		finally
		{
			if( input != null )
				input.close();
		}
		this.setFailed( false );
	}
}
