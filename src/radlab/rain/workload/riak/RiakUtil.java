package radlab.rain.workload.riak;

import java.util.Random;

import com.basho.riak.client.response.StoreResponse;

public class RiakUtil 
{
	public static long loadDbCollection( RiakTransport riakClient, String bucket, int minKey, int maxKey, int size ) throws Exception
	{
		Random random = new Random();
		int count = (maxKey - minKey) + 1;
		
		for( int i = 0; i < count; i++ )
		{
			String key = String.valueOf( i + minKey );
			byte[] value = new byte[size];
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
		int size = 4096;
		
		int minKey = 1;
		int maxKey = 100000;
		
		// RiakUtil <host> <port> <db>
		if( args.length == 6 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			bucket = args[2];
			minKey = Integer.parseInt( args[3] );
			maxKey = Integer.parseInt( args[4] );
			size = Integer.parseInt( args[5] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : RiakUtil <host> <port> <bucket> <min key> <max key> <size>" );
			System.out.println( "Example : RiakUtil localhost 8098 testbkt 1 100000 4096" );
			System.exit( -1 );
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append( "http://" );
		buf.append( host ).append( ":" ).append( port );
		buf.append( "/riak" );
		
		RiakTransport riakClient = new RiakTransport( buf.toString() );
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with " + size + " byte(s) values each." );
		long start = System.currentTimeMillis();
		RiakUtil.loadDbCollection( riakClient, bucket,minKey, maxKey, size );
		long end = System.currentTimeMillis();
		System.out.println( "Load finished: " + (end-start)/1000.0 + " seconds" );
	}
}
