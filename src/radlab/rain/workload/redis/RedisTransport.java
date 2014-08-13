package radlab.rain.workload.redis;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
// We'll use the pool later, for now just use the non-threadsafe Jedis (no open-loop/partly-open loop workloads)
//import org.apache.commons.pool.impl.GenericObjectPool.Config;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

public class RedisTransport 
{
	public static final int DEFAULT_REDIS_PORT = 6379;

	private int _timeout					= 10000;
	private boolean _debug					= false;
	private boolean _usingCluster			= false;
	
	private Jedis _redis 					= null;
	private JedisCluster _redisCluster 		= null;
		
	public RedisTransport( String host, int port )
	{
		Set<HostAndPort> clusterNodes = new HashSet<HostAndPort>();
		// Look at the host string and decide whether we're using a cluster or not
		if( host.contains(",") )
		{
			// Try to parse
			String[] nodes = host.split(",");
			for(String node : nodes)
			{
				// See whether there's a port otherwise assume passed in port is identical for all nodes
				if(node.contains(":"))
				{
					String[] address = node.split(":");
					clusterNodes.add(new HostAndPort(address[0], Integer.parseInt(address[1])));
				}
				else
				{
					// Use the hostname and the passed in port
					clusterNodes.add(new HostAndPort(node, port));
				}
			}
			
			// Decide whether we're using a cluster or not
			if(clusterNodes.size() > 0)
			{
				this._redisCluster = new JedisCluster(clusterNodes);
				this._usingCluster = true;
				//System.out.println("Using cluster");
			}
		}
		
		if(!this._usingCluster)
			this._redis = new Jedis( host, port, this._timeout );
	}
	
	// Can be used to modify the timeouts on the fly (ideally) if supported
	public void configureRedisClient()
	{
		// We could try to set the timeout parameter here
	}
	
	public JedisCommands getRedisClient()
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
		if( this._usingCluster )
			return this._redisCluster.set( key, String.valueOf(value) );
		else return this._redis.set( key.getBytes(), value );
	}
	
	public byte[] get( String key )
	{
		if( this._usingCluster )
			return this._redisCluster.get( key ).getBytes();
		else return this._redis.get( key.getBytes() );
	}
}
