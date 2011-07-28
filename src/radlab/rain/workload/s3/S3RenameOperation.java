package radlab.rain.workload.s3;

import java.util.Map;

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
		Map<String, Object> map = this.doRename( this._bucket, this._key, this._newKey );
		if( map.size() == 0 )
			throw new Exception( "Empty map returned from rename. Bucket: " + this._bucket + " old key: " + this._key + " new key: " + this._newKey );
		this.setFailed( false );
	}

}
