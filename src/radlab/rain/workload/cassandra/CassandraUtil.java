package radlab.rain.workload.cassandra;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CassandraUtil 
{
	public static NumberFormat KEY_FORMATTER = new DecimalFormat( "000000000" );
	
	public static void main(String[] args) throws Exception
	{
		int port = CassandraTransport.DEFAULT_PORT;
		String host = "localhost";
		String clusterName = "rainclstr";
		String keyspaceName = "rainks";
		String columnFamilyName = "raincf";
		int replicationFactor = 1;
		int minKey = 1;
		int maxKey = 100000;
		int size = 4096;
		
		if( args.length == 8 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			clusterName = args[2];
			keyspaceName = args[3];
			columnFamilyName = args[4];
			minKey = Integer.parseInt( args[5] );
			maxKey = Integer.parseInt( args[6] );
			size = Integer.parseInt( args[7] );
		}
		else if( args.length == 9 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			clusterName = args[2];
			keyspaceName = args[3];
			columnFamilyName = args[4];
			minKey = Integer.parseInt( args[5] );
			maxKey = Integer.parseInt( args[6] );
			size = Integer.parseInt( args[7] );
			replicationFactor = Integer.parseInt( args[8] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : CassandraUtil <host> <port> <clustername> <keyspace> <columnfamily> <min key> <max key> <size>" );
			System.out.println( "Usage   : CassandraUtil <host> <port> <clustername> <keyspace> <columnfamily> <min key> <max key> <size> <replication factor>" );
			System.out.println( "Example : CassandraUtil localhost 9160 rainclstr rainks raincf 1 100000 4096" );
			System.out.println( "Example : CassandraUtil localhost 9160 rainclstr rainks raincf 1 100000 4096 2" );
			System.exit( -1 );
		}
	
		// Do data loads in parallel, shoot for using 10 threads
		int keyCount = (maxKey - minKey) + 1;
		int keyBlockSize = 10000;
		int loaderThreads = new Double( Math.ceil( (double) keyCount / (double) keyBlockSize ) ).intValue();
				
		CassandraTransport adminClient = new CassandraTransport( clusterName, host, port, loaderThreads );
		adminClient.deleteKeyspace( keyspaceName );
		//if( true )
		//	return;
		
		// Set the replication factor explicitly
		adminClient.setReplicationFactor( replicationFactor );
		adminClient.initialize( keyspaceName, true, columnFamilyName, true );
				
		ArrayList<CassandraLoaderThread> threads = new ArrayList<CassandraLoaderThread>();
		for( int i = 0; i < loaderThreads; i++ )
		{
			CassandraTransport client = new CassandraTransport( clusterName, host, port, loaderThreads );
			// Set the timeouts
			//client.setConnectionTimeout( 60000 );
			//client.setSocketIdleTimeout( 60000 );
			// Explicitly initialize
			client.initialize( keyspaceName, false, columnFamilyName, false );
			int startKey = (i * keyBlockSize) + 1;
			int endKey = Math.min( (startKey + keyCount) - 1 , (startKey + keyBlockSize) - 1 );
			System.out.println( "Start key: " + KEY_FORMATTER.format( startKey ) + " end key: " + KEY_FORMATTER.format( endKey ) );
			CassandraLoaderThread thread = new CassandraLoaderThread(  columnFamilyName, startKey, endKey, size, client );
			threads.add( thread );
		}
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with " + size + " byte(s) values each." );
		long start = System.currentTimeMillis();
		// Start all the loader threads
		for( CassandraLoaderThread thread : threads )
			thread.start();
		
		// Wait on them to finish
		for( CassandraLoaderThread thread : threads )
			thread.join();
		
		long end = System.currentTimeMillis();
		double durationSecs = (end - start)/1000.0;
		double rate = keyCount/durationSecs;
		System.out.println( "Load finished: " + durationSecs + " seconds. Rate: " + rate + " requests/sec" );
				
		adminClient.dispose();
	}
}
