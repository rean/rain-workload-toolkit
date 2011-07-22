package radlab.rain.workload.s3;

import radlab.rain.IScoreboard;

public class S3GetOperation extends S3Operation 
{
	public static String NAME = "Get";
	
	public S3GetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.GET;	
	}

	@Override
	public void execute() throws Throwable
	{
		this.doGet();
		this.setFailed( false );
	}
}
