package radlab.rain.workload.mongodb;

import java.util.ArrayList;
import java.util.Random;

import com.mongodb.CommandResult;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

public class MongoUtil 
{
	public static void createIndex( MongoTransport mongoClient, String dbName, String collectionName, int keyField )
	{
		mongoClient.createIndex( dbName, collectionName, keyField );
	}
	
	public static boolean dropCollection( MongoTransport mongoClient, String dbName, String collectionName )
	{
		return mongoClient.dropCollection( dbName, collectionName );
	}
	
	public static long loadDbCollection( MongoTransport mongoClient, String dbName, String collectionName, int minKey, int maxKey, int size )
	{
		Random random = new Random();
		int count = (maxKey - minKey) + 1;
		
		for( int i = 0; i < count; i++ )
		{
			byte[] arrBytes = new byte[size];
			BasicDBObject kv = new BasicDBObject();
			random.nextBytes( arrBytes );
			
			kv.put( "key", String.valueOf( i + minKey ) );
			kv.put( "value", arrBytes );
			
			WriteResult res = mongoClient.insert( dbName, collectionName, kv );
			CommandResult cmdRes = res.getLastError();
			if( !cmdRes.ok() )
				throw cmdRes.getException();
		}
		return 0;
	}
	
	public static void main( String[] args ) throws Exception
	{
		// Need the name of a server, a db to use and a collection
		int port = MongoTransport.DEFAULT_MONGO_PORT;
		String host = "localhost";
		String dbName = "test";
		String dbCollection = "test-ns";
		int minKey = 1;
		int maxKey = 100000;
		int size = 4096;
		
		// MongoUtil <host> <port> <db> <col>
		if( args.length == 7 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			dbName = args[2];
			dbCollection = args[3];
			minKey = Integer.parseInt( args[4] );
			maxKey = Integer.parseInt( args[5] );
			size = Integer.parseInt( args[6] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : MongoUtil <host> <port> <dbName> <collection name> <min key> <max key> <size>" );
			System.out.println( "Example : MongoUtil localhost 27017 test test-ns 1 100000 4096" );
			System.exit( -1 );
		}
	
		// Do data loads in parallel, shoot for using 10 threads
		int keyCount = (maxKey - minKey) + 1;
		int keyBlockSize = 10000;
		int loaderThreads = new Double( Math.ceil( (double) keyCount / (double) keyBlockSize ) ).intValue();
				
		ArrayList<MongoLoaderThread> threads = new ArrayList<MongoLoaderThread>();
		for( int i = 0; i < loaderThreads; i++ )
		{
			MongoTransport client = MongoTransport.getInstance( host, port );//new MongoTransport( host, port );
			// String dbName, String collectionName, int minKey, int maxKey, int size, MongoTransport client )
			// Set the timeouts
			client.setConnectionTimeout( 60000 );
			client.setSocketIdleTimeout( 60000 );
			// Explicitly initialize
			client.initialize();
			int startKey = (i * keyBlockSize) + 1;
			int endKey = (startKey + keyBlockSize) - 1;
			System.out.println( "Start key: " + startKey + " end key: " + endKey );
			MongoLoaderThread thread = new MongoLoaderThread( dbName, dbCollection, startKey, endKey, size, client );
			threads.add( thread );
		}
		
		MongoTransport mongoClient = MongoTransport.getInstance( host, port );//new MongoTransport( host, port );
		// Set the timeouts
		mongoClient.setConnectionTimeout( 60000 );
		mongoClient.setSocketIdleTimeout( 60000 );
		// Explicitly initialize
		mongoClient.initialize();
		int indexField = 1; // create an index on the "first" field of the preloaded key-value pairs i.e., our integer keys
		System.out.println( "Dropping index on collection: " + dbCollection + " index field: " + indexField );
		mongoClient.dropIndex( dbName, dbCollection, indexField );
		// Drop the database first (if it exists)
		System.out.println( "Dropping collection: " + dbCollection );
		mongoClient.dropCollection( dbName, dbCollection );
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with " + size + " byte(s) values each." );
		long start = System.currentTimeMillis();
		// Start all the loader threads
		for( MongoLoaderThread thread : threads )
			thread.start();
		
		// Wait on them to finish
		for( MongoLoaderThread thread : threads )
			thread.join();
		
		//MongoUtil.loadDbCollection( mongoClient, dbName, dbCollection, minKey, maxKey, size );
		long end = System.currentTimeMillis();
		System.out.println( "Load finished: " + (end-start)/1000.0 + " seconds" );
		System.out.println( "Creating index on collection: " + dbCollection + " index field: " + indexField );
		// Create the index
		mongoClient.createIndex( dbName, dbCollection, indexField );
		System.out.println( "Finished creating index on collection: " + dbCollection + " index field: " + indexField );
	}
}
