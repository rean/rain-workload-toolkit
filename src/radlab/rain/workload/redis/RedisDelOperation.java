package radlab.rain.workload.redis;

import radlab.rain.IScoreboard;

public class RedisDelOperation extends RedisOperation 
{
	public static final String NAME = "Del";
	
	public RedisDelOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = RedisGenerator.GET;
	}

	@Override
	public void execute() throws Throwable 
	{
		this.doDel( this._key );
	}
}
