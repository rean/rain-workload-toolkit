package radlab.rain.workload.hbase;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.util.storage.StorageLoadProfile;

public class HBaseLoadProfile extends StorageLoadProfile 
{
	public static final int READ 	= 0;
	public static final int WRITE 	= 1;
	public static final int UPDATE	= 2;
	public static final int DELETE	= 3;
	public static final int SCAN	= 4;
	public static final int MAX_OPERATIONS = 5; // supporting core operations read, write, update and delete
	
	public static String CFG_LOAD_PROFILE_SCAN_PCT_KEY			= "scanPct";
	
	public double[] _opselect 	= new double[MAX_OPERATIONS];
	protected double _scanPct = 0.0;
	
	public HBaseLoadProfile(JSONObject profileObj) throws JSONException 
	{
		super(profileObj);
	
		if( profileObj.has( CFG_LOAD_PROFILE_SCAN_PCT_KEY) )
			this._scanPct = profileObj.getDouble( CFG_LOAD_PROFILE_SCAN_PCT_KEY );
		
		double sum = this._readPct + this._writePct + this._updatePct + this._deletePct + this._scanPct;
		
		this._readPct /= sum;
		this._writePct /= sum;
		this._updatePct /= sum;
		this._deletePct /= sum;
		this._scanPct /= sum;
		
		// Create the selection vector
		this._opselect[READ] 	= this._readPct;
		this._opselect[WRITE] 	= this._opselect[READ] + this._writePct;
		this._opselect[UPDATE]	= this._opselect[WRITE] + this._updatePct;
		this._opselect[DELETE] 	= this._opselect[UPDATE] + this._deletePct;
		this._opselect[SCAN]	= this._opselect[DELETE] + this._scanPct;
	}

	public HBaseLoadProfile(long interval, int numberOfUsers, String mixName) 
	{
		super(interval, numberOfUsers, mixName);
	}

	public HBaseLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime) 
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public HBaseLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name) 
	{
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}

	public double getScanPct() { return this._scanPct; }
	public void setScanPct( double value ) { this._scanPct = value; }
}
