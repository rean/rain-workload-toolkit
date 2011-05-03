package radlab.rain.workload.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.CommandResult;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

/*
 * Class handles interactions with MongoDB 
 */
public class MongoTransport 
{
	public static int DEFAULT_MONGO_PORT = 27017;
	
	private Mongo _conn = null;
	
	// Configuration options
	private boolean _slaveOk 		= false;
	private int _connectTimeout 	= 10000;
	private int _socketIdleTimeout	= 10000;
	private boolean _initialized 	= false;
		
	private ArrayList<ServerAddress> _servers = new ArrayList<ServerAddress>();
	
	public MongoTransport( ArrayList<ServerAddress> servers ) throws UnknownHostException
	{
		this._servers = servers;
	}
	
	public MongoTransport( String host, int port ) throws UnknownHostException
	{
		this._servers.add( new ServerAddress( host, port ) );
	}
	
	public synchronized void initialize()
	{
		MongoOptions options = new MongoOptions();
		// Haven't figured out whether there's a way to change these post-connection creation
		// i.e. whether we can start out with slaveOK = false and sometime later change our
		// minds. The same goes for changing connection and socket idle timeouts.
		options.slaveOk = this._slaveOk;
		options.connectTimeout = this._connectTimeout;
		options.socketTimeout = this._socketIdleTimeout;
	
		this._conn = new Mongo( this._servers, options );
		this._initialized = true;
	}
	
	// Can be called repeatedly
	public void configure()
	{
		
	}
	
	public DBCursor get( String dbName, String collectionName, DBObject query )
	{
		if( !this._initialized )
			this.initialize();
		
		// Make any per-request changes
		this.configure();
		DB db = this._conn.getDB( dbName );
		DBCollection collection = db.getCollection( collectionName );
		return collection.find( query );
	}
	
	public WriteResult put( String dbName, String collectionName, DBObject obj )
	{
		if( !this._initialized )
			this.initialize();
		
		// Make any per-request changes
		this.configure();
		
		DB db = this._conn.getDB( dbName );
		DBCollection collection = db.getCollection( collectionName );
		return collection.insert( obj );
	}	
	
	public WriteResult delete( String dbName, String collectionName, DBObject match )
	{
		if( !this._initialized )
			this.initialize();
		
		// Make any per-request changes
		this.configure();
		
		DB db = this._conn.getDB( dbName );
		DBCollection collection = db.getCollection( collectionName );
		return collection.remove( match );
	}
	
	public static void main( String[] args )
	{
		try
		{
			MongoTransport xport = new MongoTransport( "localhost", DEFAULT_MONGO_PORT );
			int keys = 100000;
			String dbName = "test";
			String collectionName = "test-namespace";
			long start = System.currentTimeMillis();
			
			for( int i = 0; i < keys; i++ )
			{
				BasicDBObject obj = new BasicDBObject();
				obj.put( String.valueOf( i ), i );
				WriteResult res = xport.put( dbName, collectionName, obj );
				CommandResult cmdRes = res.getLastError();
				if( cmdRes != null )
				{
					if( !cmdRes.ok() )
						System.out.println( "Failed" );
				}
			}
			
			long end = System.currentTimeMillis();
			System.out.println( "Time: " + (end - start)/1000.0 );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
