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
			int bytesRead = 0;
			int totalBytesRead = 0;
			
			while( ( bytesRead = input.read( buf ) ) != -1 )
			{
				totalBytesRead += bytesRead;
				bytesRead = input.read( buf );
				if( bytesRead != -1)
					totalBytesRead += bytesRead;
			}
			
			// Append the bytes read to the operation name
			this._operationName = NAME + "_" + totalBytesRead;
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
