package radlab.rain.workload.redis;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.util.storage.StorageLoadProfile;

public class RedisLoadProfile extends StorageLoadProfile 
{
	public static final int GET 			= 0; // Read
	public static final int SET 			= 1; // Write
	public static final int DEL 			= 2; // Delete
	public static final int MAX_OPERATIONS 	= 3; // supporting core operations read, write
	
	/*
	public static String CFG_LOAD_PROFILE_REQUEST_SIZE_KEY			= "size"; 
	public static String CFG_LOAD_PROFILE_READ_PCT_KEY				= "readPct";
	public static String CFG_LOAD_PROFILE_WRITE_PCT_KEY				= "writePct";
	*/
	
	/*
	// Default request size
	private int _size					= 4096;
	private double _readPct 			= 0.9;
	private double _writePct 			= 0.1;
	*/
	
	public double[] _opselect 			= new double[MAX_OPERATIONS];
	
	public RedisLoadProfile(JSONObject profileObj) throws JSONException 
	{
		super(profileObj);
		/*		
		this._size = profileObj.getInt( CFG_LOAD_PROFILE_REQUEST_SIZE_KEY );
		// Read and write must be specified (even if 0)
		this._readPct = profileObj.getDouble( CFG_LOAD_PROFILE_READ_PCT_KEY );
		this._writePct = profileObj.getDouble( CFG_LOAD_PROFILE_WRITE_PCT_KEY );
		*/
		
		// Normalize the read/write/update/delete
		double sum = this._readPct + this._writePct + this._deletePct;
		
		this._readPct /= sum;
		this._writePct /= sum;
		this._deletePct /= sum;
		
		// Create the selection vector
		this._opselect[GET] 	= this._readPct;
		this._opselect[SET] 	= this._opselect[GET] + this._writePct;
		this._opselect[DEL] 	= this._opselect[SET] + this._deletePct;
	}

	public RedisLoadProfile(long interval, int numberOfUsers, String mixName) 
	{
		super(interval, numberOfUsers, mixName);
	}

	public RedisLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime) 
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public RedisLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name) 
	{
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}
	
	/*
	public int getSize() { return this._size; }
	public void setSize( int value ) { this._size = value; };
	
	public double getReadPct() { return this._readPct; }
	public void setReadPct( double value ) { this._readPct = value; }
	
	public double getWritePct() { return this._writePct; }
	public void setWritePct( double value ) { this._writePct = value; }
	
	public double getDeletePct() { return this._deletePct; }
	public void setDeletePct( double value ) { this._deletePct = value; }
	*/
}
