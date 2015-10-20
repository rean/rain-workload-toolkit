package radlab.rain.workload.redis;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

public abstract class RedisOperation extends Operation 
{
	protected String _key = "";
	protected byte[] _value = null;
	protected RedisTransport _redis = null;

	public RedisOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	@Override
	public void cleanup() 
	{
		this._key = "";
		this._value = null;	
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		RedisGenerator redisGenerator = (RedisGenerator) generator;
		
		this._redis = redisGenerator.getRedisTransport();
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
	}
	
	public byte[] doGet( String key )
	{
		return this._redis.get( key );
	}
	
	public String doSet( String key, byte[] value )
	{
		return this._redis.set( key, value );
	}
	
	//[sguazt]
	public void doDel( String key )
	{
		this._redis.del( key );
	}
	//[/sguazt]
}
