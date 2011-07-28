package radlab.rain.workload.s3;

import org.jets3t.service.model.S3Bucket;

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
		@SuppressWarnings("unused")
		S3Bucket[] buckets = this.doListAllBuckets();
		this.setFailed( false );
	}
}
