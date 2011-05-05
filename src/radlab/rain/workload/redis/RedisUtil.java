package radlab.rain.workload.redis;

import java.util.Random;

public class RedisUtil 
{
	public static long loadDbCollection( RedisTransport redisClient, int minKey, int maxKey )
	{
		Random random = new Random();
		int successes = 0;
		int failures = 0;
		
		for( int i = 0; i < maxKey; i++ )
		{
			String key = String.valueOf( i );
			byte[] value = new byte[4096];
			random.nextBytes( value );
			
			String response = redisClient.set( key, value );
			//System.out.println( "Set response: " + response );
			if( response.equalsIgnoreCase( "ok" ) )
				successes++;
			else failures++;
		}
		
		System.out.println( "Successes : " + successes );
		System.out.println( "Failures  : " + failures );
		
		return 0;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// Need the name of a server and a bucket
		int port = RedisTransport.DEFAULT_REDIS_PORT;
		String host = "localhost";
				
		int minKey = 1;
		int maxKey = 100000;
		
		// RiakUtil <host> <port> <min key> <max key>
		if( args.length == 4 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			minKey = Integer.parseInt( args[2] );
			maxKey = Integer.parseInt( args[3] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : RedisUtil <host> <port> <min key> <max key>" );
			System.out.println( "Example : RedisUtil localhost 6379 1 100000" );
			System.exit( -1 );
		}
		
		RedisTransport redisClient = new RedisTransport( host, port );
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with 4K values each." );
		long start = System.currentTimeMillis();
		RedisUtil.loadDbCollection( redisClient, minKey, maxKey );
		long end = System.currentTimeMillis();
		System.out.println( "Load finished: " + (end-start)/1000.0 + " seconds" );
	}

}
