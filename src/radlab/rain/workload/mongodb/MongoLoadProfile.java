package radlab.rain.workload.mongodb;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.util.storage.StorageLoadProfile;

public class MongoLoadProfile extends StorageLoadProfile 
{	
	public static final int READ 	= 0;
	public static final int WRITE 	= 1;
	public static final int UPDATE	= 2;
	public static final int DELETE	= 3;
	public static final int MAX_OPERATIONS = 4; // supporting core operations read, write, update and delete
	
	public static String CFG_LOAD_PROFILE_REQUEST_SIZE_KEY			= "size";
	public static String CFG_LOAD_PROFILE_READ_PCT_KEY				= "readPct";
	public static String CFG_LOAD_PROFILE_WRITE_PCT_KEY				= "writePct";
	public static String CFG_LOAD_PROFILE_UPDATE_PCT_KEY			= "updatePct";
	public static String CFG_LOAD_PROFILE_DELETE_PCT_KEY			= "deletePct";
	
	// Default request size
	private int _size			= 4096;
	private double _readPct 	= 0.9;
	private double _writePct 	= 0.1;
	private double _updatePct 	= 0.0;
	private double _deletePct 	= 0.0;
	public double[] _opselect 	= new double[MAX_OPERATIONS];
	
	public MongoLoadProfile(JSONObject profileObj) throws JSONException 
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
		
		// Normalize the read/write/update/delete
		double sum = this._readPct + this._writePct + this._updatePct + this._deletePct;
		
		this._readPct /= sum;
		this._writePct /= sum;
		this._updatePct /= sum;
		this._deletePct /= sum;
		
		// Create the selection vector
		this._opselect[READ] 	= this._readPct;
		this._opselect[WRITE] 	= this._opselect[READ] + this._writePct;
		this._opselect[UPDATE]	= this._opselect[WRITE] + this._updatePct;
		this._opselect[DELETE] 	= this._opselect[UPDATE] + this._deletePct;
	}

	public MongoLoadProfile(long interval, int numberOfUsers, String mixName) 
	{
		super(interval, numberOfUsers, mixName);
	}

	public MongoLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime) 
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public MongoLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name) 
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
