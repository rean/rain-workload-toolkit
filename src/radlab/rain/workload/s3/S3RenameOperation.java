package radlab.rain.workload.s3;

import radlab.rain.IScoreboard;

public class S3RenameOperation extends S3Operation 
{
	public static String NAME = "Rename";
	
	public S3RenameOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = S3Generator.RENAME;

	}

	@Override
	public void execute() throws Throwable 
	{
		this.doRename();
		this.setFailed( false );
	}

}
