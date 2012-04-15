package radlab.rain.workload.riak;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.basho.riak.client.IRiakObject;

public class RiakUtil 
{
	public static long loadDbCollection( RiakTransport riakClient, String bucket, int minKey, int maxKey, int size ) throws Exception
	{
		Random random = new Random();
		int count = (maxKey - minKey) + 1;
		//System.out.println( "Key count: " + count );
		
		for( int i = 0; i < count; i++ )
		{
			String key = String.valueOf( i + minKey );
			byte[] value = new byte[size];
			random.nextBytes( value );
			
			//if( key.equals( "79899" ) )
			//	System.out.println( "\nVal written: " + new sun.misc.BASE64Encoder().encode( value ) );
			
			IRiakObject response = riakClient.store( bucket, key, value ); 
			//if( response == null )
			//	throw new Exception( "Store failed." );
		}
		return 0;
	}
	
	public static void main( String[] args ) throws Exception
	{
		// Need the name of a server and a bucket
		int port = RiakTransport.DEFAULT_PROTOBUF_PORT;//RiakTransport.DEFAULT_RIAK_PORT;
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
			System.out.println( "Example : RiakUtil localhost 8087 testbkt 1 100000 4096" );
			System.exit( -1 );
		}
		
		//StringBuffer buf = new StringBuffer();
		//buf.append( "http://" );
		//buf.append( host ).append( ":" ).append( port );
		//buf.append( "/riak" );
		
		RiakTransport riakClient = new RiakTransport( host, port );
		Set<String> buckets = riakClient.listBuckets();
		Iterator<String> bucketIt = buckets.iterator();
		while( bucketIt.hasNext() )
		{
			System.out.println( "Bucket: " + bucketIt.next() );
		}
		
		/**/
		// Quick sanity check
		//String val1 = "";
		int count = 0;
		Iterator<String> keyIt = riakClient.listBucket( bucket ).iterator();
		while( keyIt.hasNext() )
		{
			String key = keyIt.next();
			/*
			String val = new sun.misc.BASE64Encoder().encode( riakClient.fetch( bucket, key ).getValue() );
			
			if( key.equals( "79899" ) )
				val1 = val;
			//System.out.println( key + " " + val );
			*/
			count++;
			//if( count % 10000 == 0 )
			//	System.out.println( key );
		}
		System.out.println( "Key scan count: " + count );
		//System.out.println( "Val1          : " + val1 );
		/**/
		
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with " + size + " byte(s) values each." );
		long start = System.currentTimeMillis();
		RiakUtil.loadDbCollection( riakClient, bucket,minKey, maxKey, size );
		long end = System.currentTimeMillis();
		System.out.println( "Load finished: " + (end-start)/1000.0 + " seconds" );
		
		/*
		String val2 = "";
		keyIt = riakClient.listBucket( bucket ).iterator();
		count = 0;
		while( keyIt.hasNext() )
		{
			String key = keyIt.next();
			String val = new sun.misc.BASE64Encoder().encode( riakClient.fetch( bucket, key ).getValue() );
			
			if( key.equals( "79899" ) )
				val2 = val;
			
			//System.out.println( key + " " + val );
			//System.out.println( key );
			count++;
			if( count % 10000 == 0 )
				System.out.println( key );
		}
		System.out.println( "Key scan check: " + count );
		
		System.out.println( "\nVal1: " + val1 );
		System.out.println( "\nVal2: " + val2 );
		*/
		// Must dispose of the client to close the connection and allow this thread to exit
		riakClient.dispose();
		
		
		
		/*
		int count = 0;
		Iterator<String> keyIt = riakClient.listBucket( bucket ).iterator();
		while( keyIt.hasNext() )
		{
			System.out.println( keyIt.next() );
			count++;
		}
		
		System.out.println( "key count: " + count );
		riakClient.close();
		System.out.println( "Closed " );
		// Do a quick set of gets
		for( int i = minKey; i < maxKey; i++ )
		{
			IRiakObject result = riakClient.fetch( bucket, String.valueOf( i ) );
			System.out.println( result.getKey() + " " + result.getValue() );
		}*/
	}
}
