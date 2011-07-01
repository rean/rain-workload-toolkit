package radlab.rain.workload.mongodb;

import java.util.Random;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.WriteResult;

public class MongoLoaderThread extends Thread 
{
	private int minKey = -1;
	private int maxKey = -1;
	private String dbName = "";
	private String collectionName = "";
	private int size = 0;
	private MongoTransport mongoClient = null;
	
	public int keysLoaded = 0;
	
	public MongoLoaderThread( String dbName, String collectionName, int minKey, int maxKey, int size, MongoTransport client ) 
	{
		this.dbName = dbName;
		this.collectionName = collectionName;
		this.minKey = minKey;
		this.maxKey = maxKey;
		this.mongoClient = client;
	}

	public void run()
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
			
			WriteResult res = this.mongoClient.insert( dbName, collectionName, kv );
			CommandResult cmdRes = res.getLastError();
			if( !cmdRes.ok() )
				throw cmdRes.getException();
			
			this.keysLoaded++;
		}
	}
}
