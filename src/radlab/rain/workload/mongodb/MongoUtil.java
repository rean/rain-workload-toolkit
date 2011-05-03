package radlab.rain.workload.mongodb;

import java.util.Random;

import com.mongodb.CommandResult;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

public class MongoUtil 
{
	public static long loadDbCollection( MongoTransport mongoClient, String dbName, String collectionName, int minKey, int maxKey )
	{
		Random random = new Random();
		
		for( int i = 0; i < maxKey; i++ )
		{
			byte[] arrBytes = new byte[4096];
			BasicDBObject kv = new BasicDBObject();
			random.nextBytes( arrBytes );
			kv.put( String.valueOf( i ), arrBytes );
			WriteResult res = mongoClient.put( dbName, collectionName, kv );
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
		int maxKey = 10000;
		
		// MongoUtil <host> <port> <db> <col>
		if( args.length == 6 )
		{
			host = args[0];
			port = Integer.parseInt( args[1] );
			dbName = args[2];
			dbCollection = args[3];
			minKey = Integer.parseInt( args[4] );
			maxKey = Integer.parseInt( args[5] );
		}
		else if( args.length == 0 )
		{
			
		}
		else
		{
			System.out.println( "Usage   : MongoUtil <host> <port> <dbName> <collection name> <min key> <max key>" );
			System.out.println( "Example : MongoUtil localhost 27017 test test-ns 1 10000000" );
			System.exit( -1 );
		}
		
		MongoTransport mongoClient = new MongoTransport( host, port );
		System.out.println( "Loading: " + ((maxKey - minKey)+1) + " keys with 4K values each." );
		long start = System.currentTimeMillis();
		MongoUtil.loadDbCollection( mongoClient, dbName, dbCollection, minKey, maxKey );
		long end = System.currentTimeMillis();
		System.out.println( "Load finished: " + (end-start)/1000.0 + " seconds" );
	}
}
