package radlab.rain.workload.hbase;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class HBaseUtil 
{
	public static void main( String[] args ) throws Exception
	{
		int port = HBaseTransport.DEFAULT_HBASE_PORT;
		String host = "aqua";
		String tableName = "raintbl";
		String columnFamilyName = "raincf";
		int minKey = 1;
		int maxKey = 100000;
		int size = 4096;
		
		// HBaseUtil <host> <port> <table> <column family> <min key> <max key> <size>
		if( args.length == 7 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			tableName = args[2];
			columnFamilyName = args[3];
			minKey = Integer.parseInt( args[4] );
			maxKey = Integer.parseInt( args[5] );
			size = Integer.parseInt( args[6] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : HBaseUtil <host> <port> <tableName> <column family> <min key> <max key> <size>" );
			System.out.println( "Example : HBaseUtil localhost 60000 raintbl raincf 1 100000 4096" );
			System.exit( -1 );
		}
	
		// Do data loads in parallel, shoot for using 10 threads
		int keyCount = (maxKey - minKey) + 1;
		int loaderThreads = 1; //new Double( Math.ceil( (double) keyCount / (double) keyBlockSize ) ).intValue();
		
		int keyBlockSize = (int) Math.ceil( keyCount/loaderThreads );//10000;
		
		HBaseTransport adminClient = new HBaseTransport( host, port );
		// Before we start the load, delete the table and then re-create it
		adminClient.deleteTable( tableName );
		adminClient.initialize( tableName, columnFamilyName, true );
		
		ArrayList<HBaseLoaderThread> threads = new ArrayList<HBaseLoaderThread>();
		for( int i = 0; i < loaderThreads; i++ )
		{
			HBaseTransport client = new HBaseTransport( host, port );
			// Set the timeouts
			//client.setConnectionTimeout( 60000 );
			//client.setSocketIdleTimeout( 60000 );
			// Explicitly initialize
			client.initialize( tableName, columnFamilyName, false );
			int startKey = (i * keyBlockSize) + 1;
			int endKey = (startKey + keyBlockSize) - 1;
			System.out.println( "Start key: " + startKey + " end key: " + endKey );
			HBaseLoaderThread thread = new HBaseLoaderThread( tableName, columnFamilyName, startKey, endKey, size, client );
			threads.add( thread );
		}
		
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with " + size + " byte(s) values each." );
		long start = System.currentTimeMillis();
		// Start all the loader threads
		for( HBaseLoaderThread thread : threads )
			thread.start();
		
		// Wait on them to finish
		for( HBaseLoaderThread thread : threads )
			thread.join();
		
		long end = System.currentTimeMillis();
		double durationSecs = (end-start)/1000.0; 
		double avgResponseTimeSecs = durationSecs/keyCount;
		
		// Dispose of the client
		adminClient.dispose();
		
		System.out.println( "Load finished : " +  durationSecs + " seconds" );
		System.out.println( "Rate [" + size + "]    : " +  keyCount/durationSecs + " puts/sec" );
		NumberFormat formatter = new DecimalFormat( "#0.0000" );
		System.out.println( "Avg resp time [" + size + "]: " +  formatter.format( avgResponseTimeSecs ) + "secs" );
	}
}
