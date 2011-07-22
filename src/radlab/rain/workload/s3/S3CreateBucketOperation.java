package radlab.rain.workload.s3;

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
		this.doCreateBucket();
		this.setFailed( false );
	}
}
