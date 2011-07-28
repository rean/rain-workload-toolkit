package radlab.rain.workload.s3;

import org.jets3t.service.model.S3Object;

import radlab.rain.IScoreboard;

public class S3ListBucketOperation extends S3Operation 
{
	public static String NAME = "ListBucket";
	
	public S3ListBucketOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.LIST_BUCKET;
	}

	@Override
	public void execute() throws Throwable
	{
		@SuppressWarnings("unused")
		S3Object[] objects = this.doListBucket( this._bucket );
		this.setFailed( false );
	}
}
