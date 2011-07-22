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

// Walk through different combinations of r/w mixes to see what throughput and latency we get
public class CapacityWalkScheduleCreator extends LoadScheduleCreator 
{
	private static NumberFormat FORMATTER = new DecimalFormat( "00000" );
	public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";
	
	private int _initialWorkload = 1;
	private int _incrementSize = 30; // 30 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
		
	public CapacityWalkScheduleCreator() 
	{}

	private class ReadWriteMix
	{
		ReadWriteMix( double readPct, double writePct )
		{ 
			this._readPct = readPct;
			this._writePct = writePct;
		}
		
		public double _readPct;
		public double _writePct;
	}
	
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
		
		String mixName = "capacityWalk";
		int objectSize = 4096;
		int minKey = 1;
		int maxKey = 100000;
		long numHotObjects = 0;
		double hotTrafficFraction = 0.0;
				
		// See whether our config contained any overrides
		if( config.has( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY ) )
			objectSize = config.getInt( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY );
		
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
		
		LinkedList<ReadWriteMix> lstMix = new LinkedList<ReadWriteMix>();
		
		// We have 7 intervals with different r/w mixes
		/*
		  r : w
		 95 :  5
		 90 : 10
		 80 : 20
		 50 : 50
		 20 : 80
		 10 : 90
		  5 : 95
		 */
				
		lstMix.add( new ReadWriteMix( 0.95, 0.05 ) );
		lstMix.add( new ReadWriteMix( 0.90, 0.10 ) );
		lstMix.add( new ReadWriteMix( 0.80, 0.20 ) );
		lstMix.add( new ReadWriteMix( 0.50, 0.50 ) );
		lstMix.add( new ReadWriteMix( 0.80, 0.20 ) );
		lstMix.add( new ReadWriteMix( 0.10, 0.90 ) );
		lstMix.add( new ReadWriteMix( 0.05, 0.95 ) );
				
		for( int i = 0; i < lstMix.size(); i++ )
		{
			ReadWriteMix mix = lstMix.get( i );
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
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY, mix._readPct );
			profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY, mix._writePct );
			profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, numHotObjects );
			profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, hotTrafficFraction );
			
			StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
			profile._name = FORMATTER.format(i);
			profile.setTransitionTime( 0 );
			
			loadSchedule.add( profile );
		}
		
		return loadSchedule;
	}

	public static void main( String[] args ) throws JSONException
	{
		CapacityWalkScheduleCreator creator = new CapacityWalkScheduleCreator();
		
		creator.setInitialWorkload( 200 );
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
			System.out.println( p );
	}
}