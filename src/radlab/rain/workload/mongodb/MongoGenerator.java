package radlab.rain.workload.mongodb;

import java.net.UnknownHostException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;

import radlab.rain.util.storage.KeyGenerator;
import radlab.rain.util.Histogram;

public class MongoGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY 		= "usePooling";
	public static String CFG_DEBUG_KEY		 		= "debug";
	public static String CFG_RNG_SEED_KEY	 		= "rngSeed";
	public static String CFG_DB_NAME_KEY	 		= "dbName";
	public static String CFG_COLLECTION_NAME_KEY 	= "collectionName";
		
	public static final String DEFAULT_DB_NAME 			= "test";
	public static final String DEFAULT_COLLECTION_NAME 	= "test-ns";
	
	public static int READ 					= MongoLoadProfile.READ;
	public static int WRITE 				= MongoLoadProfile.WRITE;
	public static int DEFAULT_OBJECT_SIZE	= 4096;
	
	@SuppressWarnings("unused")
	private MongoRequest<String> _lastRequest 	= null;
	private MongoTransport _mongoClient 		= null;
	private boolean _usePooling					= true;
	private boolean _debug 						= false;
	private Random _random						= null;
	String _dbName								= DEFAULT_DB_NAME;
	String _collectionName						= DEFAULT_COLLECTION_NAME;
	// Debug key popularity
	Histogram<String> _keyHist					= new Histogram<String>();
	
	public MongoGenerator(ScenarioTrack track) 
	{
		super(track);
	}

	public MongoTransport getMongoTransport()
	{ return this._mongoClient; }
	
	@Override
	public void dispose() 
	{
		// Dump generator stats
		if( this._debug )
		{
			System.out.println( "Generator: " + this._name  + " key stats: " + this._keyHist.getTotalObservations() + " observations" );
			System.out.println( this._keyHist.toString() );
		}
	}

	@Override
	public long getCycleTime() 
	{
		return 0;
	}

	@Override
	public long getThinkTime() 
	{
		return 0;
	}

	@Override
	public void initialize() 
	{}

	@Override
	public void configure( JSONObject config ) throws JSONException
	{
		if( config.has(CFG_USE_POOLING_KEY) )
			this._usePooling = config.getBoolean( CFG_USE_POOLING_KEY );
		
		if( config.has( CFG_DEBUG_KEY) )
			this._debug = config.getBoolean( CFG_DEBUG_KEY );
		
		// Look for a random number seed
		if( config.has( CFG_RNG_SEED_KEY ) )
			this._random = new Random( config.getLong(CFG_RNG_SEED_KEY) );
		else this._random = new Random();
	
		if( config.has( CFG_DB_NAME_KEY ) )
			this._dbName = config.getString( CFG_DB_NAME_KEY );
		
		if( config.has( CFG_COLLECTION_NAME_KEY ) )
			this._collectionName = config.getString( CFG_COLLECTION_NAME_KEY );
		
		// Look for a mongo connection string otherwise just use the target host and port info
		try
		{
			this._mongoClient = new MongoTransport( this._loadTrack.getTargetHostName(), this._loadTrack.getTargetHostPort() );
		}
		catch( UnknownHostException uhe )
		{
			throw new JSONException( uhe );
		}
	}
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
	
		MongoLoadProfile mongoProfile = (MongoLoadProfile) this._latestLoadProfile; 
		
		// Pick a key using the keygen
		KeyGenerator keyGen = mongoProfile.getKeyGenerator();
		int key = keyGen.generateKey();
		// Assume raw keys for now - we could use this to index into some other structure
		// to produce the "real" key
				
		// All mongo requests have string keys (see Mongo's BasicDBObject)
		MongoRequest<String> nextRequest = new MongoRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = String.valueOf( key );
		
		// Do some stats checking
		this._keyHist.addObservation( nextRequest.key );
		
		double rndVal = this._random.nextDouble();
		int i = 0;
		
		// If we cared about access sequences we could check whether we just did a read or write
		// before picking the next operation
		for( i = 0; i < MongoLoadProfile.MAX_OPERATIONS; i++ )
		{
			if( rndVal <= mongoProfile._opselect[i] )
				break;
		}
		nextRequest.op = i;
		
		// If we're writing then we need to set the size
		if( nextRequest.op == MongoLoadProfile.WRITE || nextRequest.op == MongoLoadProfile.UPDATE )
			// We could also get the size cdf if we want to support size histograms
			nextRequest.size = mongoProfile.getSize();

		// Update the last request
		this._lastRequest = nextRequest;
		return this.getMongoOperation( nextRequest );
	}
		
	private MongoOperation getMongoOperation( MongoRequest<String> request )
	{
		if( request.op == READ )
			return this.createGetOperation( request );
		else if( request.op == WRITE )
			return this.createPutOperation( request );
		else return null; // We don't support updates/deletes explicitly, if an existing key gets re-written then so be it
	}
	
	public MongoGetOperation createGetOperation( MongoRequest<String> request )
	{
		MongoGetOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (MongoGetOperation) pool.rentObject( MongoGetOperation.NAME );	
		}
		
		if( op == null )
			op = new MongoGetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
		
	public MongoPutOperation createPutOperation( MongoRequest<String> request )
	{
		MongoPutOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (MongoPutOperation) pool.rentObject( MongoPutOperation.NAME );	
		}
		
		if( op == null )
			op = new MongoPutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		if( request.size < Integer.MAX_VALUE )
		{
			op._value = new byte[request.size];
		}
		else op._value = new byte[DEFAULT_OBJECT_SIZE];
		this._random.nextBytes( op._value );
		
		op.prepare( this );
		return op;
	}
}
