package radlab.rain.workload.redis;

import java.util.ArrayList;
import java.util.HashSet;
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
	public static final int DEL 			= RedisLoadProfile.DEL; // Delete
	
	@SuppressWarnings("unused")
	private RedisRequest<String> _lastRequest 	= null;
	private RedisTransport _redis 				= null;
	private boolean _usePooling					= true;
	private boolean _debug 						= false;
	private Random _random						= null;
	// Debug key popularity
	Histogram<String> _keyHist					= new Histogram<String>();
	// Debug hot object popularity
	Histogram<String> _hotObjHist				= new Histogram<String>();
	
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
			//System.out.println( this._keyHist.toString() );
			System.out.println( "Generator: " + this._name  + " hot obj stats: " + this._hotObjHist.getTotalObservations() + " observations" );
			System.out.println( this._hotObjHist.toString() );
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

	public void setUsePooling( boolean value ) { this._usePooling = value; }
	public boolean getUsePooling() { return this._usePooling; }
		
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
		int key = -1;
		
		RedisLoadProfile redisProfile = (RedisLoadProfile) this._latestLoadProfile;
		
		// Check whether we're sending traffic to hot objects or not
		double rndVal = this._random.nextDouble();
		ArrayList<Integer> hotObjectList = redisProfile.getHotObjectList();
		HashSet<Integer> hotObjectSet = redisProfile.getHotObjectSet();
		
		int numHotObjects = hotObjectList.size(); 
		
		if( rndVal < redisProfile.getHotTrafficFraction() &&  numHotObjects > 0 )
		{
			// Choose a key from the hot set uniformly at random.
			// Later we can use add skew within the hot object set
			key = hotObjectList.get( this._random.nextInt( numHotObjects ) );
			
			// Make collection of hot object stats configurable/optional
			//this._hotObjHist.addObservation( String.valueOf( key ) );
		}
		else
		{	
			// Pick a key using the regular keygen strategy
			KeyGenerator keyGen = redisProfile.getKeyGenerator();
			key = keyGen.generateKey();
			// Check whether we picked a key that's in the hot set - if we did, try again
			while( hotObjectSet.contains( key ) ) 
				key = keyGen.generateKey();
			
			// Make collection of non-hot object stats configurable/optional
			// Do some stats checking for non-hot objects
			//this._keyHist.addObservation( String.valueOf( key ) );
		}
		
		// Assume raw keys for now - here's where we could use the raw key to index into some 
		// other structure to produce the "real" key
		
		// All redis requests have string keys
		RedisRequest<String> nextRequest = new RedisRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = String.valueOf( key );
		
		rndVal = this._random.nextDouble();
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
		else if( request.op == DEL )
			return this.createDelOperation( request );
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
		
		// Check whether a value has been pre-set, if not then fill in random bytes
		if( request.value == null )
		{
			if( request.size < Integer.MAX_VALUE )
			{
				op._value = new byte[request.size];
			}
			else op._value = new byte[DEFAULT_OBJECT_SIZE];
			
			this._random.nextBytes( op._value );
		}
		else op._value = request.value;
		
		op.prepare( this );
		return op;
	}

	public RedisDelOperation createDelOperation( RedisRequest<String> request )
	{
		RedisDelOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (RedisDelOperation) pool.rentObject( RedisDelOperation.NAME );	
		}
		
		if( op == null )
			op = new RedisDelOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
}
