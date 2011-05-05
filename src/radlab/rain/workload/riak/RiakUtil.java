package radlab.rain.workload.riak;

import java.util.Random;

import com.basho.riak.client.response.StoreResponse;

public class RiakUtil 
{
	public static long loadDbCollection( RiakTransport riakClient, String bucket, int minKey, int maxKey ) throws Exception
	{
		Random random = new Random();
		
		for( int i = 0; i < maxKey; i++ )
		{
			String key = String.valueOf( i );
			byte[] value = new byte[4096];
			random.nextBytes( value );
			StoreResponse response = riakClient.store( bucket, key, value );
			if( !response.isSuccess() )
				throw new Exception( "Store failed." );
		}
		return 0;
	}
	
	public static void main( String[] args ) throws Exception
	{
		// Need the name of a server and a bucket
		int port = RiakTransport.DEFAULT_RIAK_PORT;
		String host = "localhost";
		String bucket = "testbkt";
		
		int minKey = 1;
		int maxKey = 100000;
		
		// RiakUtil <host> <port> <db>
		if( args.length == 5 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			bucket = args[2];
			minKey = Integer.parseInt( args[3] );
			maxKey = Integer.parseInt( args[4] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : RiakUtil <host> <port> <bucket> <min key> <max key>" );
			System.out.println( "Example : RiakUtil localhost 8098 testbkt 1 100000" );
			System.exit( -1 );
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append( "http://" );
		buf.append( host ).append( ":" ).append( port );
		buf.append( "/riak" );
		
		RiakTransport riakClient = new RiakTransport( buf.toString() );
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with 4K values each." );
		long start = System.currentTimeMillis();
		RiakUtil.loadDbCollection( riakClient, bucket,minKey, maxKey );
		long end = System.currentTimeMillis();
		System.out.println( "Load finished: " + (end-start)/1000.0 + " seconds" );
	}
}
