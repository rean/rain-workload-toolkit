package radlab.rain.workload.redis;

import java.util.Random;

public class RedisLoaderThread extends Thread
{
	private int minKey = -1;
	private int maxKey = -1;
	private int size = 0;
	private RedisTransport redisClient = null;
	public int keysLoaded = 0;
	
	public RedisLoaderThread( RedisTransport redisClient, int minKey, int maxKey, int size )
	{
		this.minKey = minKey;
		this.maxKey = maxKey;
		this.size = size;
		this.redisClient = redisClient;
	}
	
	public void run()
	{
		Random random = new Random();
		int successes = 0;
		int failures = 0;
		int count = (maxKey - minKey) + 1;
		
		for( int i = 0; i < count; i++ )
		{
			String key = String.valueOf( i + minKey );
			byte[] value = new byte[size];
			random.nextBytes( value );
			
			String response = redisClient.set( key, value );
			//System.out.println( "Set response: " + response );
			if( response.equalsIgnoreCase( "ok" ) )
				successes++;
			else failures++;
		}
		
		System.out.println( "Successes : " + successes );
		System.out.println( "Failures  : " + failures );
	}
}
