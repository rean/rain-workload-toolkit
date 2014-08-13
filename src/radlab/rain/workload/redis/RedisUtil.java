package radlab.rain.workload.redis;

import java.util.Random;

public class RedisUtil 
{
	public static long loadDbCollection( RedisTransport redisClient, int minKey, int maxKey, int size )
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
		
		return 0;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// Need the name of a server and a bucket
		
		//int port = RedisTransport.DEFAULT_REDIS_PORT;
		/*String host = "localhost";
		*/
		
		int port = RedisTransport.DEFAULT_REDIS_PORT;
		String host = "localhost";
		//String host = "localhost:7000,localhost:7001,localhost:7002";
		
		int minKey = 1;
		int maxKey = 100000;
		int size = 1024;//16384;//32768;//16384;//4096;
		
		// RiakUtil <host> <port> <min key> <max key>
		if( args.length == 5 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			minKey = Integer.parseInt( args[2] );
			maxKey = Integer.parseInt( args[3] );
			size = Integer.parseInt( args[4] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : RedisUtil <host> <port> <min key> <max key> <size>" );
			System.out.println( "Example : RedisUtil localhost 6379 1 100000 4096" );
			System.exit( -1 );
		}
		
		RedisTransport redisClient = new RedisTransport( host, port );
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with " + size + " byte(s) values each." );
		long start = System.currentTimeMillis();
		RedisUtil.loadDbCollection( redisClient, minKey, maxKey, size );
		long end = System.currentTimeMillis();
		long duration = end - start;
		double rate = ((maxKey - minKey) + 1)/((double)duration/1000.0);
		System.out.println( "Load finished: " + (duration)/1000.0 + " seconds. Rate: " + rate + " request/sec" );
	}

}
