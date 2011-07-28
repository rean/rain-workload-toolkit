package radlab.rain.workload.s3;

import radlab.rain.IScoreboard;

public class S3DeleteOperation extends S3Operation 
{
	public static String NAME = "Delete";
	
	public S3DeleteOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.DELETE;
	}
	
	@Override
	public void execute() throws Throwable
	{
		this.doDelete( this._bucket, this._key );
		this.setFailed( false );
	}
}
