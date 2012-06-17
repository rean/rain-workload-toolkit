package radlab.rain.workload.hbase;

import java.io.IOException;
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

public class HBaseGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY 		= "usePooling";
	public static String CFG_DEBUG_KEY		 		= "debug";
	public static String CFG_RNG_SEED_KEY	 		= "rngSeed";
	public static String CFG_TABLE_NAME_KEY	 		= "tableName";
	public static String CFG_COLUMN_FAMILY_NAME_KEY	= "columnFamily";
	
	//public static String CFG_WRITE_SEQUENTIAL_BLOCK = "writeSequentialBlock";
	
	public static final String DEFAULT_TABLE_NAME 			= "raintbl";
	public static final String DEFAULT_COLUMN_FAMILY_NAME 	= "raincf";
		
	public static int READ 					= HBaseLoadProfile.READ;
	public static int WRITE 				= HBaseLoadProfile.WRITE;
	public static int SCAN					= HBaseLoadProfile.SCAN;
	public static int DEFAULT_OBJECT_SIZE	= 4096;
	
	
	@SuppressWarnings("unused")
	private HBaseRequest<String> _lastRequest 	= null;
	private HBaseTransport _hbaseClient 		= null;
	private boolean _usePooling					= true;
	private boolean _debug 						= false;
	// Fixed work per thread/generator debugging
	//private boolean _writeSequentialBlock		= false;
	//private boolean _done 						= false; // for block writes
	//private int _currentKeyToWrite				= -1;
	//private long _blockWriteStart				= -1;
	//private long _blockWriteEnd					= -1;
	
	private Random _random						= null;
	String _tableName							= DEFAULT_TABLE_NAME;
	String _columnFamilyName					= DEFAULT_COLUMN_FAMILY_NAME;
	// Debug key popularity
	Histogram<String> _keyHist					= new Histogram<String>();
	// Debug hot object popularity
	Histogram<String> _hotObjHist				= new Histogram<String>();
	
	
	public HBaseGenerator(ScenarioTrack track) 
	{
		super(track);
	}
	
	public HBaseTransport getHBaseTransport()
	{ return this._hbaseClient; }
	
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
			this._random = new Random( config.getLong(CFG_RNG_SEED_KEY) );
		else this._random = new Random();
	
		if( config.has( CFG_TABLE_NAME_KEY ) )
			this._tableName = config.getString( CFG_TABLE_NAME_KEY );
		
		if( config.has( CFG_COLUMN_FAMILY_NAME_KEY ) )
			this._columnFamilyName = config.getString( CFG_COLUMN_FAMILY_NAME_KEY );
		
		//if( config.has( CFG_WRITE_SEQUENTIAL_BLOCK ) )
		//	this._writeSequentialBlock = config.getBoolean( CFG_WRITE_SEQUENTIAL_BLOCK );
		
		try
		{
			this._hbaseClient = new HBaseTransport( this._loadTrack.getTargetHostName(), this._loadTrack.getTargetHostPort() );
			// Initialize the hbase transport, creating the table if it does not exist
			this._hbaseClient.initialize( this._tableName, this._columnFamilyName, true );
		}
		catch( IOException ioe )
		{
			throw new JSONException( ioe );
		}
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
		this._hbaseClient.dispose();
	}
	
	@Override
	public Operation nextRequest( int lastOperation ) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		int key = -1;
		
		HBaseLoadProfile hbaseProfile = (HBaseLoadProfile) this._latestLoadProfile; 
		
		// Check whether we're sending traffic to hot objects or not
		double rndVal = this._random.nextDouble();
		ArrayList<Integer> hotObjectList = hbaseProfile.getHotObjectList();
		HashSet<Integer> hotObjectSet = hbaseProfile.getHotObjectSet();
		
		int numHotObjects = hotObjectList.size(); 
		
		/*
		int minKey = hbaseProfile.getKeyGenerator().getMinKey();
		int maxKey = hbaseProfile.getKeyGenerator().getMaxKey();
		int keyCount = (maxKey - minKey) + 1;
		int maxThreads = this.getTrack().getMaxUsers();
		// Compute our block boundaries, e.g., our block size
		int keyBlockSize = (int) Math.ceil( keyCount/maxThreads );
		// Get our thread id and use that to determine where our block starts
		int startKey = (int)((Thread.currentThread().getId()%maxThreads) * keyBlockSize) + 1;
		int endKey = (startKey + keyBlockSize) - 1;
		*/
		if( rndVal < hbaseProfile.getHotTrafficFraction() &&  numHotObjects > 0 )
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
			KeyGenerator keyGen = hbaseProfile.getKeyGenerator();
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
				
		// All mongo requests have string keys (see Mongo's BasicDBObject)
		HBaseRequest<String> nextRequest = new HBaseRequest<String>();
		// Turn the integer key into a string
		nextRequest.key = String.valueOf( key );
		
		rndVal = this._random.nextDouble();
		int i = 0;
		
		// If we cared about access sequences we could check whether we just did a read or write
		// before picking the next operation
		for( i = 0; i < HBaseLoadProfile.MAX_OPERATIONS; i++ )
		{
			if( rndVal <= hbaseProfile._opselect[i] )
				break;
		}
		nextRequest.op = i;
		
		// If we're writing then we need to set the size
		if( nextRequest.op == HBaseLoadProfile.WRITE || nextRequest.op == HBaseLoadProfile.UPDATE )
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
			nextRequest.size = hbaseProfile.getSize();
		}

		if( nextRequest.op == HBaseLoadProfile.SCAN )
			nextRequest.maxScanRows = 1000; // Use a fixed value for now, but expand it later with a distribution, randomized or deterministic value
		
		// Update the last request
		this._lastRequest = nextRequest;
		return this.getHBaseOperation( nextRequest );
	}
	
	private HBaseOperation getHBaseOperation( HBaseRequest<String> request )
	{
		if( request.op == READ )
			return this.createGetOperation( request );
		else if( request.op == SCAN )
			return this.createScanOperation( request );
		else if( request.op == WRITE )
			return this.createPutOperation( request );
		else return null; // We don't support updates/deletes explicitly, if an existing key gets re-written then so be it
	}
	
	public HBaseGetOperation createGetOperation( HBaseRequest<String> request )
	{
		HBaseGetOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (HBaseGetOperation) pool.rentObject( HBaseGetOperation.NAME );	
		}
		
		if( op == null )
			op = new HBaseGetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
	
	public HBaseScanOperation createScanOperation( HBaseRequest<String> request )
	{
		HBaseScanOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (HBaseScanOperation) pool.rentObject( HBaseScanOperation.NAME );	
		}
		
		if( op == null )
			op = new HBaseScanOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._key = request.key;
		op._maxScanRows = request.maxScanRows;
		
		op.prepare( this );
		return op;
	}
		
	public HBasePutOperation createPutOperation( HBaseRequest<String> request )
	{
		HBasePutOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (HBasePutOperation) pool.rentObject( HBasePutOperation.NAME );	
		}
		
		if( op == null )
			op = new HBasePutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
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
