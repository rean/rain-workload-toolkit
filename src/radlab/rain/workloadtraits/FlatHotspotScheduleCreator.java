package radlab.rain.workloadtraits;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;
import radlab.rain.util.storage.KeyGenerator;
import radlab.rain.util.storage.StorageLoadProfile;

/* Flat/steady hotspot workload
 */
public class FlatHotspotScheduleCreator extends LoadScheduleCreator 
{
	private static NumberFormat FORMATTER = new DecimalFormat( "00000" );
	public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";
	
	private int _initialWorkload = 1;
	private double[] _relativeLoads = { 1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
	};
	
	private int[] _numHotObjects = { 100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100,
			100			
	};
	
	private double[] _hotTrafficFraction = { 0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64,
			0.64			
	};
	
	private int _incrementSize = 30; // 30 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
		
	public FlatHotspotScheduleCreator() 
	{}

	public int getInitialWorkload() { return this._initialWorkload; }
	public void setInitialWorkload( int val ){ this._initialWorkload = val; }
	
	public int getIncrementSize() { return this._incrementSize; }
	public void setIncrementSize( int val ){ this._incrementSize = val; }
	
	public int getIncrementsPerInterval() { return this._incrementsPerInterval; }
	public void setIncrementsPerInterval( int val ){ this._incrementsPerInterval = val; }
	
	@Override
	public LinkedList<LoadProfile> createSchedule(JSONObject config) throws JSONException 
	{
		// The relative load, num hot object and hot traffic fraction arrays must all be the same size
		if( this._relativeLoads.length != this._numHotObjects.length )
			throw new JSONException( "Length of Number of hot objects array: " + this._numHotObjects.length + " is not the same as relative load array: " + this._relativeLoads.length );
		
		if( this._relativeLoads.length != this._hotTrafficFraction.length )
			throw new JSONException( "Length of hot traffic fraction array: " + this._hotTrafficFraction.length + " is not the same as relative load array: " + this._relativeLoads.length );
		
		// Pull out the base offset
		if( config.has( CFG_INITIAL ) )
			this._initialWorkload = config.getInt( CFG_INITIAL );
		
		if( config.has(CFG_INCREMENT_SIZE) )
			this._incrementSize = config.getInt( CFG_INCREMENT_SIZE );
		
		if( config.has( CFG_INCREMENTS_PER_INTERVAL) )
			this._incrementsPerInterval = config.getInt( CFG_INCREMENTS_PER_INTERVAL );

		// Accept parameters for the number min and max key and the object size
		
		/* Example storage load profile that we're creating programmatically
		 
		 "interval": 20,
			"users": 100,
			"mix": "uniform50r/50w",
			
			"keyGenerator": "radlab.rain.util.storage.UniformKeyGenerator",
			"keyGeneratorConfig": {
				"rngSeed": 1,
				"minKey": 1,
				"maxKey": 100000,
				"a": 1.001,
				"r": 3.456
			},
			"size": 4096,
			"readPct": 0.50,
			"writePct": 0.50,
			"numHotObjects" : 10,
			"hotTrafficFraction": 0.0
		 */
		
		// Here are some default parameters
		String mixName = "uniform50r/50w";
		long objectSize = 4096;
		double readPct = 0.5;
		double writePct = 0.5;
		//long numHotObjects = 0;
		//double hotTrafficFraction = 0.0;
		JSONArray hotSet = null;
		int minKey = 1;
		int maxKey = 100000;
		
		// See whether our config contained any overrides
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY ) )
			objectSize = config.getInt( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY );
		
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY ) )
			readPct = config.getDouble( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY );
		
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY ) )
			writePct = config.getDouble( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY );
		
		// Override the number of hot objects with a flat/steady signal based on the config file param
		if( config.has( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY ) )
		{
			int numHotObjects = config.getInt( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY );
			for( int i = 0; i < this._relativeLoads.length; i++ )
				this._numHotObjects[i] = numHotObjects; 
		}
		
		// Override the hot traffic fraction with a flat/steady signal based on the config file param
		if( config.has( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY ) )
		{
			double hotTrafficFraction = config.getDouble( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY );
			for( int i = 0; i < this._relativeLoads.length; i++ )
				this._hotTrafficFraction[i] = hotTrafficFraction;
		}
		
		// Use a hot set if it's been pre-specified
		//if( config.has( StorageLoadProfile.CFG_HOT_SET_KEY ) )
		//{
		hotSet = config.getJSONArray( StorageLoadProfile.CFG_HOT_SET_KEY );
		//}
				
		if( config.has( KeyGenerator.MIN_KEY_CONFIG_KEY ) )
			minKey = config.getInt( KeyGenerator.MIN_KEY_CONFIG_KEY );
		
		if( config.has( KeyGenerator.MAX_KEY_CONFIG_KEY ) )
			maxKey = config.getInt( KeyGenerator.MAX_KEY_CONFIG_KEY );		
		
		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		// Specify the key generator parameters
		JSONObject keyGenConfig = new JSONObject();
		keyGenConfig.put( KeyGenerator.RNG_SEED_KEY, 1 );
		keyGenConfig.put( KeyGenerator.MIN_KEY_CONFIG_KEY, minKey );
		keyGenConfig.put( KeyGenerator.MAX_KEY_CONFIG_KEY, maxKey );
		keyGenConfig.put( KeyGenerator.A_CONFIG_KEY, 1.001 );
		keyGenConfig.put( KeyGenerator.R_CONFIG_KEY, 3.456 );
		
		
		for( int i = 0; i < this._relativeLoads.length; i++ )
		{
			long intervalLength = this._incrementSize * this._incrementsPerInterval;
			if( i == 0 )
			{
				JSONObject profileConfig = new JSONObject();
				// Set the basics: # interval, users, mix name
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_INTERVAL_KEY, intervalLength );
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_USERS_KEY, this._initialWorkload );
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_MIX_KEY, mixName );
				// Set the Storage specific elements
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_KEY, "radlab.rain.util.storage.UniformKeyGenerator" );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY, keyGenConfig );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY, objectSize );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY, readPct );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY, writePct );
				// We're going to let the first interval be hotspot free
				//profileConfig.put( StorageLoadProfile.CFG_HOT_SET_KEY, hotSet );
				profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, 0.0 );
				
				StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
				profile._name = FORMATTER.format(i);
				profile.setTransitionTime( 0 );
				
				loadSchedule.add( profile );
			}
			else 
			{	
				int users = 0;
				users = (int) Math.round( loadSchedule.getLast().getNumberOfUsers() * this._relativeLoads[i] );
				
				JSONObject profileConfig = new JSONObject();
				// Set the basics: # interval, users, mix name
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_INTERVAL_KEY, intervalLength );
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_USERS_KEY, users );
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_MIX_KEY, mixName );
				// Set the Storage specific elements
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_KEY, "radlab.rain.util.storage.UniformKeyGenerator" );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY, keyGenConfig );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY, objectSize );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY, readPct );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY, writePct );
				// Specify the hot set directly
				profileConfig.put( StorageLoadProfile.CFG_HOT_SET_KEY, hotSet );
				profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, this._hotTrafficFraction[i] );
				
				StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
				profile._name = FORMATTER.format(i);
				profile.setTransitionTime( 0 );
				
				loadSchedule.add( profile );
			}
		}
		
		int padIntervals = 5;
		
		// Pad the workload with 3 intervals back at the initial workload level
		for( int i = 0; i < padIntervals; i++ )
		{
			long intervalLength = this._incrementSize * this._incrementsPerInterval;
			JSONObject profileConfig = new JSONObject();
			// Set the basics: # interval, users, mix name
			profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_INTERVAL_KEY, intervalLength );
			profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_USERS_KEY, this._initialWorkload );
			profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_MIX_KEY, mixName );
			// Set the Storage specific elements
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_KEY, "radlab.rain.util.storage.UniformKeyGenerator" );
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY, keyGenConfig );
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY, objectSize );
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY, readPct );
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY, writePct );
			profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, this._numHotObjects[0] );
			profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, this._hotTrafficFraction[0] );	
			//profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, numHotObjects );
			//profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, hotTrafficFraction );
			
			StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
			profile._name = "pad-" + FORMATTER.format(i);
			profile.setTransitionTime( 0 );
			
			loadSchedule.add( profile );
		}
		
		return loadSchedule;
	}
	
	public static void main( String[] args ) throws JSONException
	{
		HotspotScheduleCreator creator = new HotspotScheduleCreator();
		
		creator.setInitialWorkload( 800 );
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
		{
			if( p instanceof StorageLoadProfile )
			{
				StorageLoadProfile slp = (StorageLoadProfile) p;
				System.out.println( slp.getNumberOfUsers() + " " + slp.getNumHotObjects() + " " + slp.getHotTrafficFraction() );
			}
			else System.out.println( p.getNumberOfUsers() );
		}
	}
}
