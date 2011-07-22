package radlab.rain.workload.s3;

import radlab.rain.IScoreboard;

public class S3MoveOperation extends S3Operation 
{
	public static String NAME = "Move";
	
	public S3MoveOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.MOVE;
	}

	@Override
	public void execute() throws Throwable 
	{
		this.doMove();
		this.setFailed( false );
	}

}
