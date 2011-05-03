package radlab.rain.workload.mongodb;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;

public abstract class MongoOperation extends Operation 
{
	protected String _dbName = "";
	protected String _collectionName = "";
	protected String _key = "";
	protected byte[] _value = null;
	protected MongoTransport _mongoClient = null;
	
	public MongoOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	@Override
	public void cleanup() 
	{
		this._dbName = "";
		this._collectionName = "";
		this._key = "";
		this._value = null;
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		MongoGenerator mongoGenerator = (MongoGenerator) generator;
		
		this._mongoClient = mongoGenerator.getMongoTransport();
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
		
		this._dbName = mongoGenerator._dbName;
		this._collectionName = mongoGenerator._collectionName;
	}

	public int doGet( String key ) throws Exception
	{
		BasicDBObject query = new BasicDBObject();
		query.put( key, null );
		DBCursor cursor = this._mongoClient.get( this._dbName, this._collectionName, query );
		int count = cursor.count();
		// Close the db cursor
		cursor.close();
		return count;
	}
	
	public void doPut( String key, byte[] value ) throws Exception
	{
		BasicDBObject obj = new BasicDBObject();
		obj.put( key, value );
		
		WriteResult res = this._mongoClient.put( this._dbName, this._collectionName, obj );
		CommandResult cmdRes = res.getLastError();
		if( cmdRes == null )
			throw new Exception( "Error getting command result after write." );
		
		if( !cmdRes.ok() )
			throw new Exception( "Write failed!" );
	}
}
