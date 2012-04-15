package radlab.rain.workload.riak;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.basho.riak.client.RiakException;

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
	// Debug hot object popularity
	Histogram<String> _hotObjHist				= new Histogram<String>();
	
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
			//System.out.println( this._keyHist.toString() );
			System.out.println( "Generator: " + this._name  + " hot obj stats: " + this._hotObjHist.getTotalObservations() + " observations" );
			System.out.println( this._hotObjHist.toString() );
		}
		// Very important to shut down this client so the thread can exit
		this._riak.dispose();
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
		
		// Get the bucket to use - we can support a list of buckets at some later point
		if( config.has( CFG_BUCKET_KEY ) )
			this._bucket = config.getString( CFG_BUCKET_KEY );
		
		try
		{
			this._riak = new RiakTransport( this._loadTrack.getTargetHostName(), this._loadTrack.getTargetHostPort() );
		}
		catch( RiakException re )
		{
			throw new JSONException( re );
		}
	}
	
	@Override
	public Operation nextRequest( int lastOperation ) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		int key = -1;
		
		RiakLoadProfile riakProfile = (RiakLoadProfile) this._latestLoadProfile;
		
		// Check whether we're sending traffic to hot objects or not
		double rndVal = this._random.nextDouble();
		ArrayList<Integer> hotObjectList = riakProfile.getHotObjectList();
		HashSet<Integer> hotObjectSet = riakProfile.getHotObjectSet();
		
		int numHotObjects = hotObjectList.size(); 
		
		if( rndVal < riakProfile.getHotTrafficFraction() &&  numHotObjects > 0 )
		{
			// Choose a key from the hot set uniformly at random.
			// Later we can use add skew within the hot object set
			key = hotObjectList.get( this._random.nextInt( numHotObjects ) );
			this._hotObjHist.addObservation( String.valueOf( key ) );
		}
		else
		{	
			// Pick a key using the regular keygen strategy
			KeyGenerator keyGen = riakProfile.getKeyGenerator();
			key = keyGen.generateKey();
			// Check whether we picked a key that's in the hot set - if we did, try again
			while( hotObjectSet.contains( key ) ) 
				key = keyGen.generateKey();
			
			// Do some stats checking for non-hot objects
			this._keyHist.addObservation( String.valueOf( key ) );
		}
		
		// Assume raw keys for now - here's where we could use the raw key to index into some 
		// other structure to produce the "real" key
		
		// All riak requests have string keys
		RiakRequest<String> nextRequest = new RiakRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = String.valueOf( key );
		
		rndVal = this._random.nextDouble();
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
}
