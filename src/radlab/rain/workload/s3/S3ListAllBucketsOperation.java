package radlab.rain.workload.s3;

import radlab.rain.IScoreboard;

public class S3ListAllBucketsOperation extends S3Operation 
{
	public static String NAME = "ListAllBuckets";
	
	public S3ListAllBucketsOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.LIST_ALL_BUCKETS;
	}

	@Override
	public void execute() throws Throwable
	{
		this.doListAllBuckets();
		this.setFailed( false );
	}
}
