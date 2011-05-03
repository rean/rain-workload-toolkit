package radlab.rain.workload.riak;

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

public class RiakGenerator extends Generator 
{
	public static final String CFG_USE_POOLING_KEY 			= "usePooling";
	public static final String CFG_DEBUG_KEY		 		= "debug";
	public static final String CFG_RNG_SEED_KEY	 			= "rngSeed";
	public static final String CFG_BUCKET_KEY				= "bucket";
	// Configuration parameters for hotspotting
	public static final String CFG_NUM_HOT_OBJECTS_KEY		= "numHotObjects";
	public static final String CFG_HOT_TRAFFIC_FRACTION_KEY	= "hotTrafficFraction";
	public static final String CFG_HOT_SET_ENTROPY_KEY		= "hotSetEntropy";
	// Let users specify the specific hot items?
	public static final String CFG_HOT_SET_KEY				= "hotSet";
	
	public static final String DEFAULT_BUCKET	= "testbkt";
	public static int DEFAULT_OBJECT_SIZE		= 4096;
	
	// Main operations GET/PUT
	public static final int FETCH 			= RiakLoadProfile.FETCH; // Read
	public static final int STORE 			= RiakLoadProfile.STORE; // Write
	// Additional operations
	public static final int MODIFY 			= RiakLoadProfile.UPDATE; // Write (update)
	public static final int DELETE 			= RiakLoadProfile.DELETE; // Write
	public static final int LIST_BUCKET		= RiakLoadProfile.LIST_BUCKET; // Read
	public static final int FETCH_STREAM 	= RiakLoadProfile.FETCH_STREAM; // Read
	
	@SuppressWarnings("unused")
	private RiakRequest<String> _lastRequest 	= null;
	private RiakTransport _riak 				= null;
	private boolean _usePooling					= true;
	private boolean _debug 						= false;
	private Random _random						= null;
	// Debug key popularity
	Histogram<String> _keyHist					= new Histogram<String>();
		
	// Bucket and key next request should go to
	String _bucket = DEFAULT_BUCKET; 
		
	public RiakGenerator(ScenarioTrack track) 
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

	public RiakTransport getRiakTransport()
	{
		return this._riak;
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
		
		// Get the bucket to use - we can support a list of buckets at some later point
		if( config.has( CFG_BUCKET_KEY ) )
			this._bucket = config.getString( CFG_BUCKET_KEY );
		
		// "http://localhost:8098/riak"
		StringBuffer riakUrl = new StringBuffer();
		riakUrl.append( "http://" );
		riakUrl.append( this._loadTrack.getTargetHostName() ).append( ":" ).append( this._loadTrack.getTargetHostPort() );
		riakUrl.append( "/riak" );
		this._riak = new RiakTransport( riakUrl.toString() );
	}
	
	@Override
	public Operation nextRequest( int lastOperation ) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
	
		RiakLoadProfile riakProfile = (RiakLoadProfile) this._latestLoadProfile;
		
		// Pick a key using the keygen
		KeyGenerator keyGen = riakProfile.getKeyGenerator();
		int key = keyGen.generateKey();		
		// Assume raw keys for now - here's where we could use the raw key to index into some 
		// other structure to produce the "real" key
		
		// All riak requests have string keys
		RiakRequest<String> nextRequest = new RiakRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = String.valueOf( key );
		
		// Do some stats checking
		this._keyHist.addObservation( nextRequest.key );
		
		double rndVal = this._random.nextDouble();
		int i = 0;
		
		// If we cared about access sequences we could check whether we just did a read or write
		// before picking the next operation
		for( i = 0; i < RiakLoadProfile.MAX_OPERATIONS; i++ )
		{
			if( rndVal <= riakProfile._opselect[i] )
				break;
		}
		nextRequest.op = i;
		
		// If we're writing then we need to set the size
		if( nextRequest.op == RiakLoadProfile.STORE || nextRequest.op == RiakLoadProfile.UPDATE )
			// We could also get the size cdf if we want to support size histograms
			nextRequest.size = riakProfile.getSize();

		// Update the last request
		this._lastRequest = nextRequest;
		return this.getRiakOperation( nextRequest );
	}
	
	private RiakOperation getRiakOperation( RiakRequest<String> request )
	{
		if( request.op == FETCH )
			return this.createFetchOperation( request );
		else if( request.op == STORE )
			return this.createStoreOperation( request );
		else return null; // We don't support updates/deletes explicitly, if an existing key gets re-written then so be it
	}
	
	public RiakFetchOperation createFetchOperation( RiakRequest<String> request )
	{
		RiakFetchOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (RiakFetchOperation) pool.rentObject( RiakFetchOperation.NAME );	
		}
		
		if( op == null )
			op = new RiakFetchOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
		
	public RiakStoreOperation createStoreOperation( RiakRequest<String> request )
	{
		RiakStoreOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (RiakStoreOperation) pool.rentObject( RiakStoreOperation.NAME );	
		}
		
		if( op == null )
			op = new RiakStoreOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
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
