package radlab.rain.workload.s3;

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
		this.doHead();
		this.setFailed( false );
	}
}
