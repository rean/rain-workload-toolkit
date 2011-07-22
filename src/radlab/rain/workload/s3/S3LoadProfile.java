package radlab.rain.workload.s3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;

public class S3LoadProfile extends LoadProfile 
{
	public static String CFG_LOAD_PROFILE_REQUEST_SIZES_KEY			= "sizes"; // list of obj size
	public static String CFG_LOAD_PROFILE_REQUEST_SIZE_MIX_KEY		= "sizeMix"; // proportions of obj size
	
	public static String CFG_LOAD_PROFILE_READ_PCT_KEY				= "readPct";
	public static String CFG_LOAD_PROFILE_WRITE_PCT_KEY				= "writePct";
	public static String CFG_LOAD_PROFILE_HEAD_PCT_KEY				= "headPct";
	public static String CFG_LOAD_PROFILE_DELETE_PCT_KEY			= "deletePct";
	
	public static String CFG_LOAD_PROFILE_CREATE_BUCKET_PCT_KEY		= "createBucketPct";
	public static String CFG_LOAD_PROFILE_LIST_BUCKET_PCT_KEY		= "listBucketPct";
	public static String CFG_LOAD_PROFILE_DELETE_BUCKET_PCT_KEY		= "deleteBucketPct";
	public static String CFG_LOAD_PROFILE_LIST_ALL_BUCKETS_PCT_KEY	= "listAllBucketsPct";
	
	public static String CFG_LOAD_PROFILE_RENAME_PCT_KEY			= "renamePct";
	public static String CFG_LOAD_PROFILE_MOVE_PCT_KEY 				= "movePct";
	
	public int[] _sizes					= { 4096 };
	public double[] _sizeMix			= { 1.0 };
	
	protected double _readPct 			= 0.9;
	protected double _writePct 			= 0.1;
	protected double _headPct 			= 0.0;
	protected double _deletePct 		= 0.0;
	
	protected double _createBucketPct 	= 0.0;
	protected double _listBucketPct 	= 0.0;
	protected double _deleteBucketPct 	= 0.0;
	protected double _listAllBucketsPct = 0.0;
	
	protected double _renamePct			= 0.0;
	protected double _movePct			= 0.0;
	
	public double[] _opselect 	= new double[S3Generator.MAX_OPERATIONS];
	
	public S3LoadProfile(JSONObject profileObj) throws JSONException 
	{
		super(profileObj);
		
		// Get the sizes
		if( profileObj.has( CFG_LOAD_PROFILE_REQUEST_SIZES_KEY ) )
		{
			JSONArray sizes = profileObj.getJSONArray( CFG_LOAD_PROFILE_REQUEST_SIZES_KEY );
			this._sizes = null;
			this._sizes = new int[sizes.length()];
			for( int i = 0; i < sizes.length(); i++ )
				this._sizes[i] = sizes.getInt( i );
			
			// If sizes are specified then the size mix must be specified
			JSONArray sizeMix = profileObj.getJSONArray( CFG_LOAD_PROFILE_REQUEST_SIZE_MIX_KEY );
			if( sizeMix.length() != sizes.length() )
				throw new JSONException( "The list of sizes and the size mix arrays must have the same length! len(sizes): " + sizes.length() + " len(sizeMix): " + sizeMix.length() );
			
			double sizeMixSum = 0;
			this._sizeMix = null;
			this._sizeMix = new double[sizeMix.length()];
			for( int i = 0; i < sizeMix.length(); i++ )
			{	
				double mixVal = sizeMix.getDouble(i);
				this._sizeMix[i] = mixVal;
				sizeMixSum += mixVal;
			}
			
			// Normalize and convert into a selection vector
			for( int i = 0; i < this._sizeMix.length; i++ )
			{
				// Normalize all the size mix entries
				this._sizeMix[i] /= sizeMixSum;
				// Compute the cumulative sum so we have a selection vector (CDF)
				if( i > 0 )
					this._sizeMix[i] += this._sizeMix[i-1];
			}
		}
		
		// Read and write must be specified (even if 0)
		this._readPct = profileObj.getDouble( CFG_LOAD_PROFILE_READ_PCT_KEY );
		this._writePct = profileObj.getDouble( CFG_LOAD_PROFILE_WRITE_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_HEAD_PCT_KEY) )
			this._headPct = profileObj.getDouble( CFG_LOAD_PROFILE_HEAD_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_DELETE_PCT_KEY) )
			this._deletePct = profileObj.getDouble( CFG_LOAD_PROFILE_DELETE_PCT_KEY );
		
		if( profileObj.has( CFG_LOAD_PROFILE_CREATE_BUCKET_PCT_KEY ) )
			this._createBucketPct = profileObj.getDouble( CFG_LOAD_PROFILE_CREATE_BUCKET_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_LIST_BUCKET_PCT_KEY ) )
			this._listBucketPct = profileObj.getDouble( CFG_LOAD_PROFILE_LIST_BUCKET_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_DELETE_BUCKET_PCT_KEY ) )
			this._deleteBucketPct = profileObj.getDouble( CFG_LOAD_PROFILE_DELETE_BUCKET_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_LIST_ALL_BUCKETS_PCT_KEY ) )
			this._listAllBucketsPct = profileObj.getDouble( CFG_LOAD_PROFILE_LIST_ALL_BUCKETS_PCT_KEY );
	
		if( profileObj.has( CFG_LOAD_PROFILE_RENAME_PCT_KEY) )
			this._renamePct = profileObj.getDouble( CFG_LOAD_PROFILE_RENAME_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_MOVE_PCT_KEY) )
			this._movePct = profileObj.getDouble( CFG_LOAD_PROFILE_MOVE_PCT_KEY );
		
		double sum = this._readPct + this._writePct + this._headPct + this._deletePct + this._createBucketPct + this._listBucketPct + this._deleteBucketPct + this._listAllBucketsPct + this._renamePct + this._movePct;
		
		this._readPct /= sum;
		this._writePct /= sum;
		this._headPct /= sum;
		this._deletePct /= sum;
		
		this._createBucketPct /= sum;
		this._listBucketPct /= sum;
		this._deleteBucketPct /= sum;
		this._listAllBucketsPct /= sum;
		
		this._renamePct /= sum;
		this._movePct /= sum;
		
		// Create the selection vector
		this._opselect[S3Generator.GET] 	= this._readPct;
		this._opselect[S3Generator.PUT] 	= this._opselect[S3Generator.GET] + this._writePct;
		this._opselect[S3Generator.HEAD]	= this._opselect[S3Generator.PUT] + this._headPct;
		this._opselect[S3Generator.DELETE] 	= this._opselect[S3Generator.HEAD] + this._deletePct;
		
		this._opselect[S3Generator.CREATE_BUCKET]	= this._opselect[S3Generator.DELETE] + this._createBucketPct;
		this._opselect[S3Generator.LIST_BUCKET] = this._opselect[S3Generator.CREATE_BUCKET] + this._listBucketPct;
		this._opselect[S3Generator.DELETE_BUCKET] = this._opselect[S3Generator.LIST_BUCKET] + this._deleteBucketPct;
		this._opselect[S3Generator.LIST_ALL_BUCKETS] =  this._opselect[S3Generator.DELETE_BUCKET] + this._listAllBucketsPct;
		
		this._opselect[S3Generator.RENAME] = this._opselect[S3Generator.LIST_ALL_BUCKETS] + this._renamePct;
		this._opselect[S3Generator.MOVE] = this._opselect[S3Generator.RENAME] + this._movePct;
	}

	public S3LoadProfile(long interval, int numberOfUsers, String mixName) 
	{
		super(interval, numberOfUsers, mixName);
	}

	public S3LoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime) 
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public S3LoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name) 
	{
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}
}
