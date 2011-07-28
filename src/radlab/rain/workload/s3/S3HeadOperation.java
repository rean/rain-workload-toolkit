package radlab.rain.workload.s3;

import org.jets3t.service.model.S3Object;

import radlab.rain.IScoreboard;

public class S3HeadOperation extends S3Operation 
{
	public static String NAME = "Head";
	
	public S3HeadOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.HEAD;
	}

	@Override
	public void execute() throws Throwable
	{
		S3Object object = this.doHead( this._bucket, this._key );
		if( object == null )
			throw new Exception( "Null S3Object returned for HEAD. Bucket: " + this._bucket + ", Key: " + this._key );
		this.setFailed( false );
	}
}
