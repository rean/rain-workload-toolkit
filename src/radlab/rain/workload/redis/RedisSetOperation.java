package radlab.rain.workload.redis;

import radlab.rain.IScoreboard;

public class RedisSetOperation extends RedisOperation 
{
	public static final String NAME = "Set";
	
	public RedisSetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = RedisGenerator.SET;
	}

	@Override
	public void execute() throws Throwable 
	{
		String result = this.doSet( this._key, this._value );
		if( result != null && result.equalsIgnoreCase( "ok" ) )
			this.setFailed( false );
	}
}
