package radlab.rain.workload.redis;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.Histogram;
import radlab.rain.util.storage.KeyGenerator;

public class RedisGenerator extends Generator 
{
	public static final String CFG_USE_POOLING_KEY 			= "usePooling";
	public static final String CFG_DEBUG_KEY		 		= "debug";
	public static final String CFG_RNG_SEED_KEY	 			= "rngSeed";
	
	public static int DEFAULT_OBJECT_SIZE		= 4096;
	
	// Main operations GET/SET
	public static final int GET 			= RedisLoadProfile.GET; // Read
	public static final int SET 			= RedisLoadProfile.SET; // Write
	
	@SuppressWarnings("unused")
	private RedisRequest<String> _lastRequest 	= null;
	private RedisTransport _redis 				= null;
	private boolean _usePooling					= true;
	private boolean _debug 						= false;
	private Random _random						= null;
	// Debug key popularity
	Histogram<String> _keyHist					= new Histogram<String>();
	
	
	public RedisGenerator(ScenarioTrack track) 
	{
		super(track);
	}

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
	{
		
	}

	public RedisTransport getRedisTransport()
	{
		return this._redis;
	}

	@Override
	public void configure( JSONObject config ) throws JSONException
	{
		if( config.has(CFG_USE_POOLING_KEY) )
			this._usePooling = config.getBoolean( CFG_USE_POOLING_KEY );
		
		if( config.has( CFG_DEBUG_KEY) )
			this._debug = config.getBoolean( CFG_DEBUG_KEY );
		
		// Look for a random number seed
		if( config.has( CFG_RNG_SEED_KEY ) )
			this._random = new Random( config.getLong( CFG_RNG_SEED_KEY ) );
		else this._random = new Random();
		
		this._redis = new RedisTransport( this._loadTrack.getTargetHostName(), this._loadTrack.getTargetHostPort() );
	}
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
	
		RedisLoadProfile redisProfile = (RedisLoadProfile) this._latestLoadProfile;
		
		// Pick a key using the keygen
		KeyGenerator keyGen = redisProfile.getKeyGenerator();
		int key = keyGen.generateKey();		
		// Assume raw keys for now - here's where we could use the raw key to index into some 
		// other structure to produce the "real" key
		
		// All redis requests have string keys
		RedisRequest<String> nextRequest = new RedisRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = String.valueOf( key );
		
		// Do some stats checking
		this._keyHist.addObservation( nextRequest.key );
		
		double rndVal = this._random.nextDouble();
		int i = 0;
		
		// If we cared about access sequences we could check whether we just did a read or write
		// before picking the next operation
		for( i = 0; i < RedisLoadProfile.MAX_OPERATIONS; i++ )
		{
			if( rndVal <= redisProfile._opselect[i] )
				break;
		}
		nextRequest.op = i;
		
		// If we're writing then we need to set the size
		if( nextRequest.op == RedisLoadProfile.SET )
			// We could also get the size cdf if we want to support size histograms
			nextRequest.size = redisProfile.getSize();

		// Update the last request
		this._lastRequest = nextRequest;
		return this.getRedisOperation( nextRequest );
	}

	private RedisOperation getRedisOperation( RedisRequest<String> request )
	{
		if( request.op == GET )
			return this.createGetOperation( request );
		else if( request.op == SET )
			return this.createSetOperation( request );
		else return null; // We don't support updates/deletes explicitly, if an existing key gets re-written then so be it
	}
	
	public RedisGetOperation createGetOperation( RedisRequest<String> request )
	{
		RedisGetOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (RedisGetOperation) pool.rentObject( RedisGetOperation.NAME );	
		}
		
		if( op == null )
			op = new RedisGetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
		
	public RedisSetOperation createSetOperation( RedisRequest<String> request )
	{
		RedisSetOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (RedisSetOperation) pool.rentObject( RedisSetOperation.NAME );	
		}
		
		if( op == null )
			op = new RedisSetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
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
