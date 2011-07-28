package radlab.rain.workload.s3;

import java.util.Map;

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
		Map<String, Object> map = this.doMove( this._bucket, this._key, this._newBucket, this._newKey );
		if( map.size() == 0 )
			throw new Exception( "Empty map returned from move. Old bucket: " + this._bucket + " old key: " + this._key + " new bucket: " + this._newBucket + " new key: " + this._newKey );
		this.setFailed( false );
	}
}
