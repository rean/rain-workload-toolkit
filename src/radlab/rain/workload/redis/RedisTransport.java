package radlab.rain.workload.redis;

import redis.clients.jedis.Jedis;
// We'll use the pool later, for now just use the non-threadsafe Jedis (no open-loop/partly-open loop workloads)
//import org.apache.commons.pool.impl.GenericObjectPool.Config;

public class RedisTransport 
{
	public static final int DEFAULT_REDIS_PORT = 6379;

	private int _timeout					= 10000;
	private boolean _debug					= false;
		
	private Jedis _redis = null;
	
	public RedisTransport( String host, int port )
	{
		this._redis = new Jedis( host, port, this._timeout );
	}
	
	// Can be used to modify the timeouts on the fly (ideally) if supported
	public void configureRedisClient()
	{
		// We could try to set the timeout parameter here
	}
	
	public Jedis getRedisClient()
	{ return this._redis; }
	
	/**
	 * Returns the time to wait for a response from Redis.
	 * 
	 * @return  Time to wait for a response.
	 */
	public int getTimeout()
	{
		return this._timeout;
	}
	
	/**
	 * Sets the time to wait for a response from Redis 
	 * 
	 * @param val   The new configuration.
	 */
	public void setTimeout( int val )
	{
		this._timeout = val;
	}
	
	public boolean getDebug() { return this._debug; }
	public void setDebug( boolean val ) { this._debug = val; }
	
	public String set( String key, byte[] value )
	{
		return this._redis.set( key.getBytes(), value );
	}
	
	public byte[] get( String key )
	{
		return this._redis.get( key.getBytes() );
	}
}
