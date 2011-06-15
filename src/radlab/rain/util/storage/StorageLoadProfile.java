package radlab.rain.util.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;

public class StorageLoadProfile extends LoadProfile 
{
	public static String CFG_LOAD_PROFILE_KEY_GENERATOR_KEY        	= "keyGenerator";
	public static String CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY 	= "keyGeneratorConfig";
	
	// Hotspot related configuration
	// The hotSet parameter allows the user to specify which objects are hot, this takes precedence
	// over the numHotObjects parameter. If hotSet is not specified, then we will randomly select
	// <numHotObjects> objects to represent the hot set.
	public static String CFG_HOT_SET_KEY							= "hotSet";
	public static String CFG_NUM_HOT_OBJECTS_KEY					= "numHotObjects";
	public static String CFG_HOT_TRAFFIC_FRACTION_KEY				= "hotTrafficFraction";
	
	public static String CFG_LOAD_PROFILE_REQUEST_SIZE_KEY			= "size";
	public static String CFG_LOAD_PROFILE_READ_PCT_KEY				= "readPct";
	public static String CFG_LOAD_PROFILE_WRITE_PCT_KEY				= "writePct";
	public static String CFG_LOAD_PROFILE_UPDATE_PCT_KEY			= "updatePct";
	public static String CFG_LOAD_PROFILE_DELETE_PCT_KEY			= "deletePct";
	
	protected String _keyGeneratorClass				= "";
	protected JSONObject _keyGeneratorConfig			= null;
	protected KeyGenerator _keyGenerator				= null;
		
	protected int _numHotObjects 					= 0;
	protected double _hotTrafficFraction 			= 0.0;
	protected ArrayList<Integer> _hotObjectList		= new ArrayList<Integer>();
	protected HashSet<Integer> _hotObjectSet		= new HashSet<Integer>();
	// We'll eventually add support for hotSet skew, in the short term all hot objects
	// will be equally popular, later we'll let some be more popular than others
	
	protected int _size					= 4096;
	protected double _readPct 			= 0.9;
	protected double _writePct 			= 0.1;
	protected double _updatePct 		= 0.0;
	protected double _deletePct 		= 0.0;
		
	public StorageLoadProfile(JSONObject profileObj) throws JSONException 
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
				
		// Key gen initialization
		this._keyGeneratorClass = profileObj.getString( CFG_LOAD_PROFILE_KEY_GENERATOR_KEY );
	
		try
		{
			this._keyGeneratorConfig = profileObj.getJSONObject( CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY );
			this._keyGenerator = KeyGenerator.createKeyGenerator( this._keyGeneratorClass, this._keyGeneratorConfig );
		}
		catch( Exception e )
		{
			throw new JSONException( e );
		}
		
		// If the hot traffic fraction is there, then we're most likely doing hotspots
		// Next we should check whether we have to pick hotspots OR they're specified for us already
		if( profileObj.has( CFG_HOT_TRAFFIC_FRACTION_KEY ) )
		{
			double hotTrafficFraction = profileObj.getDouble( CFG_HOT_TRAFFIC_FRACTION_KEY );
			if( hotTrafficFraction < 0.0 || hotTrafficFraction >= 1.0 )
				throw new JSONException( "Invalid hot traffic fraction: " + hotTrafficFraction + " expected a value > 0 and < 1" );
			
			// Set the hot traffic fraction
			this._hotTrafficFraction = hotTrafficFraction;
			
			// See whether we have to select the hot objects or whether they're already specified
			if( profileObj.has( CFG_HOT_SET_KEY ) )
			{
				// The hot set is specified for us, if it's empty throw an exception
				JSONArray hotSet = profileObj.getJSONArray( CFG_HOT_SET_KEY );
				for( int i = 0; i < hotSet.length(); i++ )
				{
					int obj = hotSet.getInt( i );
					this._hotObjectList.add( obj );
					this._hotObjectSet.add( obj );
				}
				// Set the number of hot objects
				this._numHotObjects = hotSet.length();
			}
			else if( profileObj.has( CFG_NUM_HOT_OBJECTS_KEY ) )
			{
				this._numHotObjects = profileObj.getInt( CFG_NUM_HOT_OBJECTS_KEY );
				
				// We need to pick the set of hot objects
				this._hotObjectSet.clear();
				this._hotObjectList.clear();
				
				Random rnd = new Random( this.getKeyGenerator().getSeed() );
				int minKey = this._keyGenerator.getMinKey();
				int maxKey = this._keyGenerator.getMaxKey();
				// Pick hotspots independently
				int numObjects =  maxKey - minKey;
				
				if( this._numHotObjects >= numObjects )
					throw new JSONException( "Number of hot objects must be less than the total number of objects" );
					
				while( this._hotObjectSet.size() < this._numHotObjects )
				{
					int key = rnd.nextInt( maxKey );
					if( !this._hotObjectSet.contains( key ) )
						this._hotObjectSet.add( key );
				}
				
				// Now that we have the hot objects save them in the profile
				Iterator<Integer> it = this._hotObjectSet.iterator();
				while( it.hasNext() )
				{
					this._hotObjectList.add( it.next() );
				}
			}
		}
	}

	public StorageLoadProfile(long interval, int numberOfUsers, String mixName) 
	{
		super(interval, numberOfUsers, mixName);
	}

	public StorageLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime) 
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public StorageLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name) 
	{
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}
	
	public String getKeyGeneratorName() { return this._keyGeneratorClass; }
	public void setKeyGeneratorName( String val ) { this._keyGeneratorClass = val; }

	public JSONObject getKeyGeneratorConfig() { return this._keyGeneratorConfig; }
	public void setKeyGeneratorConfig( JSONObject val ) { this._keyGeneratorConfig = val; }
	
	public KeyGenerator getKeyGenerator(){ return this._keyGenerator; }
	
	public int getNumHotObjects() { return this._numHotObjects; }
	public void setNumHotObjects( int value ) { this._numHotObjects = value; }
	
	public double getHotTrafficFraction() { return this._hotTrafficFraction; }
	public void setHotTrafficFraction( double value ) { this._hotTrafficFraction = value; }
	
	public ArrayList<Integer> getHotObjectList() { return this._hotObjectList; }
	public HashSet<Integer> getHotObjectSet() { return this._hotObjectSet; }
	
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
