package radlab.rain.workload.s3;

import radlab.rain.IScoreboard;

public class S3PutOperation extends S3Operation 
{
	public static String NAME = "Put";
	
	public S3PutOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.PUT;
	}

	@Override
	public void execute() throws Throwable
	{
		this.doPut();
		this.setFailed( false );
	}
}
