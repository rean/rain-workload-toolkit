package radlab.rain.workload.cassandra;

//import java.io.IOException;
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

public class CassandraGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY 		= "usePooling";
	public static String CFG_DEBUG_KEY		 		= "debug";
	public static String CFG_RNG_SEED_KEY	 		= "rngSeed";
	public static String CFG_CLUSTER_NAME_KEY		= "clusterName";
	public static String CFG_KEYSPACE_NAME_KEY 		= "keyspaceName";
	public static String CFG_COLUMN_FAMILY_NAME_KEY	= "columnFamilyName";
	
	public static final String DEFAULT_CLUSTER_NAME			= "rainclstr";
	public static final String DEFAULT_KEYSPACE_NAME		= "rainks";
	public static final String DEFAULT_COLUMN_FAMILY_NAME 	= "raincf";
		
	public static int READ 					= CassandraLoadProfile.READ;
	public static int WRITE 				= CassandraLoadProfile.WRITE;
	public static int SCAN					= CassandraLoadProfile.SCAN;
	public static int DEFAULT_OBJECT_SIZE	= 4096;
	
	@SuppressWarnings("unused")
	private CassandraRequest<String> _lastRequest 	= null;
	private CassandraTransport _cassandraClient		= null;
	private boolean _usePooling						= true;
	private boolean _debug 							= false;
	
	private Random _random						= null;
	String _clusterName							= DEFAULT_CLUSTER_NAME;
	String _keyspaceName						= DEFAULT_KEYSPACE_NAME;
	String _columnFamilyName					= DEFAULT_COLUMN_FAMILY_NAME;
	// Debug key popularity
	Histogram<String> _keyHist					= new Histogram<String>();
	// Debug hot object popularity
	Histogram<String> _hotObjHist				= new Histogram<String>();
	
	
	public CassandraGenerator(ScenarioTrack track) 
	{
		super(track);
	}

	public CassandraTransport getCassandraTransport()
	{ return this._cassandraClient; }
	
	public void setUsePooling( boolean value ) { this._usePooling = value; }
	public boolean getUsePooling() { return this._usePooling; }
		
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
			this._random = new Random( config.getLong( CFG_RNG_SEED_KEY ) );
		else this._random = new Random();
	
		if( config.has( CFG_CLUSTER_NAME_KEY ) )
			this._clusterName = config.getString( CFG_CLUSTER_NAME_KEY );
		
		if( config.has( CFG_KEYSPACE_NAME_KEY ) )
			this._keyspaceName = config.getString( CFG_KEYSPACE_NAME_KEY );
		
		if( config.has( CFG_COLUMN_FAMILY_NAME_KEY ) )
			this._columnFamilyName = config.getString( CFG_COLUMN_FAMILY_NAME_KEY );
		
		/*int timeout = CassandraTransport.DEFAULT_TIMEOUT;
				
		if( config.has( CFG_TIMEOUT_KEY ) )
			timeout = config.getInt( CFG_TIMEOUT_KEY );
		*/
		
		//if( config.has( CFG_WRITE_SEQUENTIAL_BLOCK ) )
		//	this._writeSequentialBlock = config.getBoolean( CFG_WRITE_SEQUENTIAL_BLOCK );
		
		//try
		//{
			this._cassandraClient = new CassandraTransport( this._clusterName, this._loadTrack.getTargetHostName(), this._loadTrack.getTargetHostPort(), this._loadTrack.getMaxUsers() );
			// Initialize the cassandra transport, creating the keyspace and column family if they do not exist
			this._cassandraClient.initialize( this._keyspaceName, true, this._columnFamilyName, true );
		//}
		//catch( IOException ioe )
		//{
			//throw new JSONException( ioe );
		//}
	}
	
	
	@Override
	public long getThinkTime() 
	{
		return 0;
	}

	@Override
	public long getCycleTime() 
	{
		return 0;
	}

	@Override
	public void dispose() 
	{
		// Dump generator stats
		if( this._debug )
		{
			long totalObservations = this._keyHist.getTotalObservations() + this._hotObjHist.getTotalObservations();
			double hotTraffic = (this._hotObjHist.getTotalObservations() / (double) totalObservations) * 100;
			double nonHotTraffic = (this._keyHist.getTotalObservations() / (double) totalObservations) * 100;
			System.out.println( "Generator: " + this._name  + " key stats    : " + this._keyHist.getTotalObservations()    + " observations. Keys: " + this._keyHist.getKeySet().size()    + " Traffic %: " + nonHotTraffic );
			//System.out.println( this._keyHist.toString() );
			System.out.println( "Generator: " + this._name  + " hot obj stats: " + this._hotObjHist.getTotalObservations() + " observations. Keys: " + this._hotObjHist.getKeySet().size() + " Traffic %: " + hotTraffic );
			//System.out.println( this._hotObjHist.toString() );
		}
		
		// Dispose of the client
		this._cassandraClient.dispose();
	}
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		int key = -1;
		
		CassandraLoadProfile cassandraProfile = (CassandraLoadProfile) this._latestLoadProfile; 
		
		// Check whether we're sending traffic to hot objects or not
		double rndVal = this._random.nextDouble();
		ArrayList<Integer> hotObjectList = cassandraProfile.getHotObjectList();
		HashSet<Integer> hotObjectSet = cassandraProfile.getHotObjectSet();
		
		int numHotObjects = hotObjectList.size(); 
		
		/*
		int minKey = cassandraProfile.getKeyGenerator().getMinKey();
		int maxKey = cassandraProfile.getKeyGenerator().getMaxKey();
		int keyCount = (maxKey - minKey) + 1;
		int maxThreads = this.getTrack().getMaxUsers();
		// Compute our block boundaries, e.g., our block size
		int keyBlockSize = (int) Math.ceil( keyCount/maxThreads );
		// Get our thread id and use that to determine where our block starts
		int startKey = (int)((Thread.currentThread().getId()%maxThreads) * keyBlockSize) + 1;
		int endKey = (startKey + keyBlockSize) - 1;
		*/
		if( rndVal < cassandraProfile.getHotTrafficFraction() &&  numHotObjects > 0 )
		{
			// Choose a key from the hot set uniformly at random.
			// Later we can use add skew within the hot object set
			key = hotObjectList.get( this._random.nextInt( numHotObjects ) );
			if( this._debug )
				this._hotObjHist.addObservation( String.valueOf( key ) );
		}
		else
		{	
			// Pick a key using the regular keygen strategy
			KeyGenerator keyGen = cassandraProfile.getKeyGenerator();
			key = keyGen.generateKey();
			// Check whether we picked a key that's in the hot set - if we did, try again
			while( hotObjectSet.contains( key ) ) 
				key = keyGen.generateKey();
			
			// Do some stats checking for non-hot objects
			if( this._debug )
				this._keyHist.addObservation( String.valueOf( key ) );
		}
		
		// Assume raw keys for now - we could use this to index into some other structure
		// to produce the "real" key
				
		// All requests have string keys
		CassandraRequest<String> nextRequest = new CassandraRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = CassandraUtil.KEY_FORMATTER.format( key );
		
		rndVal = this._random.nextDouble();
		int i = 0;
		
		// If we cared about access sequences we could check whether we just did a read or write
		// before picking the next operation
		for( i = 0; i < CassandraLoadProfile.MAX_OPERATIONS; i++ )
		{
			if( rndVal <= cassandraProfile._opselect[i] )
				break;
		}
		nextRequest.op = i;
		
		// If we're writing then we need to set the size
		if( nextRequest.op == CassandraLoadProfile.WRITE || nextRequest.op == CassandraLoadProfile.UPDATE )
		{
			/*
			// If we're going to write and we're supposed to write sequentially, swap in the "right" key in the next request
			// We only write to keys within this range
			if( this._currentKeyToWrite == -1 )
			{
				this._blockWriteStart = System.currentTimeMillis();
				this._currentKeyToWrite = startKey;
			}
			else this._currentKeyToWrite = (this._currentKeyToWrite + 1);// % keyBlockSize;
			
			if( this._currentKeyToWrite > endKey && this._done == false )
			{
				this._blockWriteEnd = System.currentTimeMillis();
				this._done = true;
				// Print rate
				double durationSecs = (this._blockWriteEnd - this._blockWriteStart)/1000.0;
				System.out.println( "Duration (secs): " + durationSecs );
				System.out.println( "Rate (res/sec) : " + (double)keyBlockSize/durationSecs );
				return null;
			}
			
			
			//System.out.println( this + " " + this._currentKeyToWrite );
			
			if( this._writeSequentialBlock )
				nextRequest.key = String.valueOf( this._currentKeyToWrite );
			*/
			// We could also get the size cdf if we want to support size histograms
			nextRequest.size = cassandraProfile.getSize();
		}

		if( nextRequest.op == CassandraLoadProfile.SCAN )
			nextRequest.maxScanRows = 1000; // Use a fixed value for now, but expand it later with a distribution, randomized or deterministic value
		
		// Update the last request
		this._lastRequest = nextRequest;
		return this.getCassandraOperation( nextRequest );
	}

	private CassandraOperation getCassandraOperation( CassandraRequest<String> request )
	{
		if( request.op == READ )
			return this.createGetOperation( request );
		else if( request.op == SCAN )
			return this.createScanOperation( request );
		else if( request.op == WRITE )
			return this.createPutOperation( request );
		else return null; // We don't support updates/deletes explicitly, if an existing key gets re-written then so be it
	}
	
	public CassandraGetOperation createGetOperation( CassandraRequest<String> request )
	{
		CassandraGetOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (CassandraGetOperation) pool.rentObject( CassandraGetOperation.NAME );	
		}
		
		if( op == null )
			op = new CassandraGetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
	
	public CassandraScanOperation createScanOperation( CassandraRequest<String> request )
	{
		CassandraScanOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (CassandraScanOperation) pool.rentObject( CassandraScanOperation.NAME );	
		}
		
		if( op == null )
			op = new CassandraScanOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		op._maxScanRows = request.maxScanRows;
		
		op.prepare( this );
		return op;
	}
		
	public CassandraPutOperation createPutOperation( CassandraRequest<String> request )
	{
		CassandraPutOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (CassandraPutOperation) pool.rentObject( CassandraPutOperation.NAME );	
		}
		
		if( op == null )
			op = new CassandraPutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
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
