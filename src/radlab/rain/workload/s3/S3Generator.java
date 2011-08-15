package radlab.rain.workload.s3;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;

public class S3Generator extends Generator 
{
	public static String AWS_PROPERTIES_FILE			= "aws.properties";
	public static final String AWS_ACCESS_KEY_PROPERTY 	= "awsAccessKey";
    public static final String AWS_SECRET_KEY_PROPERTY 	= "awsSecretKey";
    	
	public static String CFG_USE_POOLING_KEY 		= "usePooling";
	public static String CFG_DEBUG_KEY		 		= "debug";
	public static String CFG_RNG_SEED_KEY	 		= "rngSeed";
	public static String CFG_OBJECT_KEYS			= "objectKeys";
	public static String CFG_OBJECT_KEY_PREFIXES	= "objectKeyPrefixes";
	
	public static final int GET 					= 0;
	public static final int PUT 					= 1;
	public static final int HEAD					= 2;
	public static final int DELETE					= 3;
	public static final int CREATE_BUCKET			= 4;
	public static final int LIST_BUCKET				= 5;
	public static final int DELETE_BUCKET			= 6;
	public static final int LIST_ALL_BUCKETS		= 7;
	public static final int RENAME					= 8;
	public static final int MOVE					= 9;
	public static final int MAX_OPERATIONS 			= 10;
		
	public static int DEFAULT_OBJECT_SIZE			= 4096;
	
	public static String DEFAULT_LEVEL1_PREFIX = "level1zdc";
	public static String DEFAULT_LEVEL2_PREFIX = "level2uhy";
	public static String DEFAULT_LEVEL3_PREFIX = "level3pfg";
	public static String DEFAULT_LEVEL_SEPARATOR = "/";
	
	// By default we expect 10 million object keys organized in 3 level pseudo-hierarchy with distribution as follows
	// <10 level 1 folders>/<1000 level 2 folders per level 1>/<1000 level 3 objects per level 2>
	public static int[] OBJECT_KEYS = new int[]{10, 1000, 1000};
	// We need to do some bookeeping to track object renames or moves so that we can
	// deterministically generate the object we want to touch but then check whether it's been
	// moved somewhere else. Multiple Generator threads will have to co-ordinate to prevent
	// concurrent moves of the same objects
	public static HashMap<String, String> _keyAliasMap = new HashMap<String,String>(); 
		
	private boolean _usePooling							= true;
	
	private boolean _debug 								= false;
	private Random _random								= null;
	private S3Transport _s3Client						= null;
	private int[] _objectKeys							= null;						
	private HashMap<Integer,String> _objectKeyPrefixes 	= null;
	private NumberFormat _formatter 					= new DecimalFormat( "00000" );
	
	@SuppressWarnings("unused")
	private S3Request<String> _lastRequest 	= null;
	
	public S3Generator(ScenarioTrack track) 
	{
		super(track);
	}

	@Override
	public void dispose() {}

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
	public void initialize() {}

	public void setUsePooling( boolean value ) { this._usePooling = value; }
	public boolean getUsePooling() { return this._usePooling; }
	
	public S3Transport getS3Transport() { return this._s3Client; }
	
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
		
		// Configure the s3 transport with the credentials we need to connect etc. 
		// - load from a local properties file.
		// If we don't find any credentials then throw a JSONException to that effect
		Properties aws = new Properties();
		try
		{
			InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream( AWS_PROPERTIES_FILE ); 
			aws.load( stream );			
			this._s3Client = new S3Transport( aws.getProperty( AWS_ACCESS_KEY_PROPERTY ), aws.getProperty( AWS_SECRET_KEY_PROPERTY ) );
		}
		catch( Exception e )
		{
			throw new JSONException( "Error initializing S3Transport. Make sure the properties file: " + AWS_PROPERTIES_FILE + " is on the classpath!" );
		}
		
		if( config.has( CFG_OBJECT_KEYS ) )
		{
			// Copy the object key hierarchy information from the JSON array
			JSONArray objectKeys = config.getJSONArray( CFG_OBJECT_KEYS );
			this._objectKeys = new int[objectKeys.length()];
			
			for( int i = 0; i < objectKeys.length(); i++ )
				this._objectKeys[i] = objectKeys.getInt( i );
		}
		else
		{
			this._objectKeys = S3Generator.OBJECT_KEYS;
		}
		
		// Get the list of prefixes
		if( config.has( CFG_OBJECT_KEY_PREFIXES ) )
		{
			JSONArray objectKeyPrefixes = config.getJSONArray( CFG_OBJECT_KEY_PREFIXES );
			this._objectKeyPrefixes = new HashMap<Integer,String>();
			
			// The number of object key prefixes must equal the number of object key hierarchy levels
			if( objectKeyPrefixes.length() != this._objectKeys.length )
				throw new JSONException( "The number of object key prefixes (" + objectKeyPrefixes.length() + ") is not equal to the number of object key hierarchy levels (" + this._objectKeys.length + ")" );
			
			for( int i = 0; i < objectKeyPrefixes.length(); i++ )
				this._objectKeyPrefixes.put( i, objectKeyPrefixes.getString( i ) );
		}
		else
		{
			this._objectKeyPrefixes = new HashMap<Integer,String>();
			this._objectKeyPrefixes.put( 0, S3Generator.DEFAULT_LEVEL1_PREFIX );
			this._objectKeyPrefixes.put( 1, S3Generator.DEFAULT_LEVEL2_PREFIX );
			this._objectKeyPrefixes.put( 2, S3Generator.DEFAULT_LEVEL3_PREFIX );
		}
	}
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		
		S3LoadProfile s3Profile = (S3LoadProfile) this._latestLoadProfile;
		
		S3Request<String> nextRequest = new S3Request<String>();
		
		StringBuffer bucket = new StringBuffer();
		StringBuffer key = new StringBuffer();
		for( int i = 0; i < this._objectKeys.length; i++ )
		{
			// Pick a random number between 0 and the number of items at this level of 
			// the object key hierarchy
			int val = this._random.nextInt( this._objectKeys[i] );
			if( i == 0 )
			{
				bucket.append( this._objectKeyPrefixes.get( i ) );
				bucket.append( this._formatter.format( val ) );
				nextRequest.bucket = bucket.toString();
			}
			else 
			{
				key.append( this._objectKeyPrefixes.get( i ) );
				// Add the suffix - the formatted random number we generated
				key.append( this._formatter.format( val ) );
				if( i+1 < this._objectKeys.length )
					key.append( DEFAULT_LEVEL_SEPARATOR );
			}
		}
		
		nextRequest.key = key.toString();
		// Check the alias map here if necessary to see whether the key we want has been renamed or moved
						
		double rndVal = this._random.nextDouble();
		int i = 0;
		
		// If we cared about access sequences we could check whether we just did a read or write
		// before picking the next operation
		for( i = 0; i < MAX_OPERATIONS; i++ )
		{
			if( rndVal <= s3Profile._opselect[i] )
				break;
		}
		nextRequest.op = i;
		
		// If we're writing then we need to set the size
		if( nextRequest.op == PUT )
		{
			// Use the size CDF to select the object size
			rndVal = this._random.nextDouble();
			int j = 0;
			for( j = 0; j < s3Profile._sizeMix.length; j++ )
			{
				if( rndVal <= s3Profile._sizeMix[j])
					break;
			}
			nextRequest.size = s3Profile._sizes[j];
		}
		else if( nextRequest.op == MOVE )
		{
			// Pick a new bucket that is not the same as the current one
			String newBucket = nextRequest.bucket;
			while( newBucket.equals( nextRequest.bucket ) )
			{
				StringBuffer buf = new StringBuffer();
				int val = this._random.nextInt( this._objectKeys[0] );
				buf.append( this._objectKeyPrefixes.get( 0 ) );
				buf.append( this._formatter.format( val ) );
				newBucket = buf.toString();
			}
			// Keep the same name, just change the bucket
			nextRequest.newKey = nextRequest.key;
			nextRequest.newBucket = newBucket;
		}
		else if( nextRequest.op == RENAME )
		{
			// Pick a new name for the key
			String newKey = nextRequest.key;
			while( newKey.equals( nextRequest.key ) )
			{
				StringBuffer buf = new StringBuffer();
				for( int j = 1; j < this._objectKeys.length; j++ )
				{
					int val = this._random.nextInt( this._objectKeys[j] );
					buf.append( this._objectKeyPrefixes.get( j ) );
					// Add the suffix - the formatted random number we generated
					buf.append( this._formatter.format( val ) );
					if( j+1 < this._objectKeys.length )
						buf.append( DEFAULT_LEVEL_SEPARATOR );
				}
				
				newKey = buf.toString();
			}
			
			nextRequest.newKey = newKey;
		}
		
		if( this._debug )
			System.out.println( this + " " + nextRequest );
				
		// Update the last request
		this._lastRequest = nextRequest;
		return this.getS3Operation( nextRequest );
	}
	
	private S3Operation getS3Operation( S3Request<String> request )
	{
		//if( true )
			//return null; // short circuit for testing
		
		if( request.op == GET )
			return this.createGetOperation( request );
		else if( request.op == PUT )
			return this.createPutOperation( request );
		else if( request.op == HEAD )
			return this.createHeadOperation( request );
		else if( request.op == DELETE )
			return this.createDeleteOperation( request );
		else if( request.op == CREATE_BUCKET )
			return this.createCreateBucketOperation( request );
		else if( request.op == LIST_BUCKET )
			return this.createListBucketOperation( request );
		else if( request.op == DELETE_BUCKET )
			return this.createDeleteBucketOperation( request );
		else if( request.op == LIST_ALL_BUCKETS )
			return this.createListAllBucketsOperation( request );
		else if( request.op == RENAME )
			return this.createRenameOperation( request );
		else if( request.op == MOVE )
			return this.createMoveOperation( request );
		else return null; 
	}
	
	public S3GetOperation createGetOperation( S3Request<String> request )
	{
		S3GetOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3GetOperation) pool.rentObject( S3GetOperation.NAME );	
		}
		
		if( op == null )
			op = new S3GetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
	
	public S3PutOperation createPutOperation( S3Request<String> request )
	{
		S3PutOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3PutOperation) pool.rentObject( S3PutOperation.NAME );	
		}
		
		if( op == null )
			op = new S3PutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
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
	
	public S3HeadOperation createHeadOperation( S3Request<String> request )
	{
		S3HeadOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3HeadOperation) pool.rentObject( S3HeadOperation.NAME );	
		}
		
		if( op == null )
			op = new S3HeadOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
	
	public S3DeleteOperation createDeleteOperation( S3Request<String> request )
	{
		S3DeleteOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3DeleteOperation) pool.rentObject( S3DeleteOperation.NAME );	
		}
		
		if( op == null )
			op = new S3DeleteOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
		op._key = request.key;
		
		op.prepare( this );
		return op;
	}
	
	public S3CreateBucketOperation createCreateBucketOperation( S3Request<String> request )
	{
		S3CreateBucketOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3CreateBucketOperation) pool.rentObject( S3CreateBucketOperation.NAME );	
		}
		
		if( op == null )
			op = new S3CreateBucketOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
				
		op.prepare( this );
		return op;
	}
	
	public S3ListBucketOperation createListBucketOperation( S3Request<String> request )
	{
		S3ListBucketOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3ListBucketOperation) pool.rentObject( S3ListBucketOperation.NAME );	
		}
		
		if( op == null )
			op = new S3ListBucketOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
				
		op.prepare( this );
		return op;
	}
	
	public S3DeleteBucketOperation createDeleteBucketOperation( S3Request<String> request )
	{
		S3DeleteBucketOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3DeleteBucketOperation) pool.rentObject( S3DeleteBucketOperation.NAME );	
		}
		
		if( op == null )
			op = new S3DeleteBucketOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
		
		op.prepare( this );
		return op;
	}
	
	public S3ListAllBucketsOperation createListAllBucketsOperation( S3Request<String> request )
	{
		S3ListAllBucketsOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3ListAllBucketsOperation) pool.rentObject( S3ListAllBucketsOperation.NAME );	
		}
		
		if( op == null )
			op = new S3ListAllBucketsOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// All we need are the AWS credentials to query S3
		
		op.prepare( this );
		return op;
	}
	
	public S3MoveOperation createMoveOperation( S3Request<String> request )
	{
		S3MoveOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3MoveOperation) pool.rentObject( S3MoveOperation.NAME );	
		}
		
		if( op == null )
			op = new S3MoveOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
		op._key = request.key;
		op._newBucket = request.newBucket;
		op._newKey = request.newKey;
		
		op.prepare( this );
		return op;
	}
	
	public S3RenameOperation createRenameOperation( S3Request<String> request )
	{
		S3RenameOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (S3RenameOperation) pool.rentObject( S3RenameOperation.NAME );	
		}
		
		if( op == null )
			op = new S3RenameOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		// Set the specific fields
		op._bucket = request.bucket;
		op._key = request.key;
		op._newKey = request.newKey;
		
		op.prepare( this );
		return op;
	}
	
	@Override
	public String toString()
	{
		return this._name;
	}
}
