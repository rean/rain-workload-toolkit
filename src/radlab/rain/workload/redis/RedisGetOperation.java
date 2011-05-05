package radlab.rain.workload.redis;

import radlab.rain.IScoreboard;

public class RedisGetOperation extends RedisOperation 
{
	public static final String NAME = "Get";
	
	public RedisGetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = RedisGenerator.GET;
	}

	@Override
	public void execute() throws Throwable 
	{
		byte[] result = this.doGet( this._key );
		// Better checking for what gets returned when attempting to get a nonexistent key
		if( result != null )
			this.setFailed( false );
	}
}
