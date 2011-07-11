package radlab.rain.workloadtraits;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;
import radlab.rain.util.storage.KeyGenerator;
import radlab.rain.util.storage.StorageLoadProfile;

/* Simple diurnal signal - 24 data points */
public class DiurnalScheduleCreator extends LoadScheduleCreator 
{
	private static NumberFormat FORMATTER = new DecimalFormat( "00000" );
	public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";
	
	private int _initialWorkload = 1;
	private double[] _relativeLoads = { 1,
			1.07,
			1.14,
			0.75,
			0.84,
			1.3,
			1.09,
			1.34,
			1.33,
			2.14,
			0.9,
			0.92,
			0.75,
			1.34,
			1.09,
			0.94,
			0.8,
			0.87,
			0.7,
			0.74,
			1.15,
			1.12,
			0.68,
			1.31,
	};
	
	private int _incrementSize = 30; // 30 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
		
	public DiurnalScheduleCreator() 
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
		long numHotObjects = 0;
		double hotTrafficFraction = 0.0;
		int minKey = 1;
		int maxKey = 100000;
		
		// See whether our config contained any overrides
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY ) )
			objectSize = config.getInt( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY );
		
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY ) )
			readPct = config.getDouble( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY );
		
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY ) )
			writePct = config.getDouble( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY );
		
		if( config.has( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY ) )
			numHotObjects = config.getInt( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY );
		
		if( config.has( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY ) )
			hotTrafficFraction = config.getDouble( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY );
		
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
				profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, numHotObjects );
				profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, hotTrafficFraction );
				
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
				profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, numHotObjects );
				profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, hotTrafficFraction );
				
				StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
				profile._name = FORMATTER.format(i);
				profile.setTransitionTime( 0 );
				
				loadSchedule.add( profile );
			}
		}
		
		int padIntervals = 3;
		
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
			profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, numHotObjects );
			profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, hotTrafficFraction );
			
			StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
			profile._name = "pad-" + FORMATTER.format(i);
			profile.setTransitionTime( 0 );
			
			loadSchedule.add( profile );
		}
		
		return loadSchedule;
	}
	
	public static void main( String[] args ) throws JSONException
	{
		DiurnalScheduleCreator creator = new DiurnalScheduleCreator();
		
		creator.setInitialWorkload( 800 );
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
			System.out.println( p.getNumberOfUsers() );
	}
}
