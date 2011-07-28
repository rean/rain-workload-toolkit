package radlab.rain.workload.s3;

import org.jets3t.service.model.S3Bucket;

import radlab.rain.IScoreboard;

public class S3CreateBucketOperation extends S3Operation 
{
	public static String NAME = "CreateBucket";
	
	public S3CreateBucketOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.CREATE_BUCKET;
	}

	@Override
	public void execute() throws Throwable
	{
		S3Bucket bucket = this.doCreateBucket( this._bucket );
		if( bucket == null )
			throw new Exception( "Null object returned for create bucket: " + this._bucket );
		this.setFailed( false );
	}
}
