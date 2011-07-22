package radlab.rain.workload.s3;

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
		this.doListBucket();
		this.setFailed( false );
	}
}
