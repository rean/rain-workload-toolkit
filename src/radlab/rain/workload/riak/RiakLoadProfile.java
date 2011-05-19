package radlab.rain.workload.riak;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.util.storage.StorageLoadProfile;

public class RiakLoadProfile extends StorageLoadProfile 
{
	public static final int FETCH 			= 0; // Read
	public static final int STORE 			= 1; // Write
	public static final int UPDATE			= 2; // Write
	public static final int DELETE			= 3; // Write
	public static final int LIST_BUCKET 	= 4; // Read
	public static final int FETCH_STREAM	= 5; // Read
	public static final int MAX_OPERATIONS 	= 6; // supporting core operations read, write, update and delete
	
	public static String CFG_LOAD_PROFILE_REQUEST_SIZE_KEY			= "size";
	public static String CFG_LOAD_PROFILE_READ_PCT_KEY				= "readPct";
	public static String CFG_LOAD_PROFILE_WRITE_PCT_KEY				= "writePct";
	public static String CFG_LOAD_PROFILE_UPDATE_PCT_KEY			= "updatePct";
	public static String CFG_LOAD_PROFILE_DELETE_PCT_KEY			= "deletePct";
	public static String CFG_LOAD_PROFILE_LIST_BUCKET_PCT_KEY		= "listBucketPct";
	public static String CFG_LOAD_PROFILE_READ_STREAM_PCT_KEY		= "readStreamPct";

	// Default request size
	private int _size				= 4096;
	private double _readPct 		= 0.9;
	private double _writePct 		= 0.1;
	private double _updatePct 		= 0.0;
	private double _deletePct 		= 0.0;
	private double _listBucketPct 	= 0.0;
	private double _readStreamPct 	= 0.0;
	
	public double[] _opselect 	= new double[MAX_OPERATIONS];
	
	public RiakLoadProfile(JSONObject profileObj) throws JSONException 
	{
		super(profileObj);
		
		this._size = profileObj.getInt( CFG_LOAD_PROFILE_REQUEST_SIZE_KEY );
		// Read and write must be specified (even if 0)
		this._readPct = profileObj.getDouble( CFG_LOAD_PROFILE_READ_PCT_KEY );
		this._writePct = profileObj.getDouble( CFG_LOAD_PROFILE_WRITE_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_UPDATE_PCT_KEY) )
			this._updatePct = profileObj.getDouble( CFG_LOAD_PROFILE_UPDATE_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_DELETE_PCT_KEY) )
			this._deletePct = profileObj.getDouble( CFG_LOAD_PROFILE_DELETE_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_LIST_BUCKET_PCT_KEY) )
			this._listBucketPct = profileObj.getDouble( CFG_LOAD_PROFILE_LIST_BUCKET_PCT_KEY );
		if( profileObj.has( CFG_LOAD_PROFILE_READ_STREAM_PCT_KEY) )
			this._readStreamPct = profileObj.getDouble( CFG_LOAD_PROFILE_READ_STREAM_PCT_KEY );
				
		// Normalize the read/write/update/delete
		double sum = this._readPct + this._writePct + this._updatePct + this._deletePct + this._listBucketPct + this._readStreamPct;
		
		this._readPct /= sum;
		this._writePct /= sum;
		this._updatePct /= sum;
		this._deletePct /= sum;
		this._listBucketPct /= sum;
		this._readStreamPct /= sum;
		
		// Create the selection vector
		this._opselect[FETCH] 	= this._readPct;
		this._opselect[STORE] 	= this._opselect[FETCH] + this._writePct;
		this._opselect[UPDATE]	= this._opselect[STORE] + this._updatePct;
		this._opselect[DELETE] 	= this._opselect[UPDATE] + this._deletePct;
		this._opselect[LIST_BUCKET] = this._opselect[DELETE] + this._listBucketPct;
		this._opselect[FETCH_STREAM] = this._opselect[LIST_BUCKET] + this._readStreamPct;		
	}

	public RiakLoadProfile(long interval, int numberOfUsers, String mixName) 
	{
		super(interval, numberOfUsers, mixName);
	}

	public RiakLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime) 
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public RiakLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name) 
	{
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}

	public int getSize() { return this._size; }
	public void setSize( int value ) { this._size = value; };
	
	public double getReadPct() { return this._readPct; }
	public void setReadPct( double value ) { this._readPct = value; }
	
	public double getWritePct() { return this._writePct; }
	public void setWritePct( double value ) { this._writePct = value; }
	
	public double getUpdatePct() { return this._updatePct; }
	public void setUpdatePct( double value ) { this._updatePct = value; }
	
	public double getDeletePct() { return this._deletePct; }
	public void setDeletePct( double value ) { this._deletePct = value; }
}
