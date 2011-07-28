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
	//public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_MAX_WORKLOAD = "maxWorkload";
	public static String CFG_STEPS_PER_MIX = "steps";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";
	
	private int _maxWorkload = 100;
	private int _incrementSize = 30; // 30 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
	private int _steps = 10;
	
	public CapacityWalkScheduleCreator() 
	{}

	private class ReadWriteMix
	{
		ReadWriteMix( double readPct, double writePct, String name )
		{ 
			this._readPct = readPct;
			this._writePct = writePct;
			this._name = name;
		}
		
		public double _readPct;
		public double _writePct;
		public String _name;
	}
	
	//public int getInitialWorkload() { return this._initialWorkload; }
	//public void setInitialWorkload( int val ){ this._initialWorkload = val; }
	
	public int getMaxWorkload() { return this._maxWorkload; }
	public void setMaxWorkload( int val ) { this._maxWorkload = val; }
	
	public int getSteps() { return this._steps; }
	public void setSteps( int val ) { this._steps = val; }
	
	public int getIncrementSize() { return this._incrementSize; }
	public void setIncrementSize( int val ){ this._incrementSize = val; }
	
	public int getIncrementsPerInterval() { return this._incrementsPerInterval; }
	public void setIncrementsPerInterval( int val ){ this._incrementsPerInterval = val; }
	
	@Override
	public LinkedList<LoadProfile> createSchedule(JSONObject config) throws JSONException 
	{
		//if( config.has( CFG_INITIAL ) )
		//	this._initialWorkload = config.getInt( CFG_INITIAL );
		if( config.has( CFG_MAX_WORKLOAD ) )
			this._maxWorkload = config.getInt( CFG_MAX_WORKLOAD );
		
		if( config.has( CFG_STEPS_PER_MIX ) )
			this._steps = config.getInt( CFG_STEPS_PER_MIX );
		
		if( config.has(CFG_INCREMENT_SIZE) )
			this._incrementSize = config.getInt( CFG_INCREMENT_SIZE );
		
		if( config.has( CFG_INCREMENTS_PER_INTERVAL) )
			this._incrementsPerInterval = config.getInt( CFG_INCREMENTS_PER_INTERVAL );
		
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
				
		lstMix.add( new ReadWriteMix( 0.95, 0.05, "95r/5w" ) );
		lstMix.add( new ReadWriteMix( 0.90, 0.10, "90r/10w" ) );
		lstMix.add( new ReadWriteMix( 0.80, 0.20, "80r/20w" ) );
		lstMix.add( new ReadWriteMix( 0.50, 0.50, "50r/50w" ) );
		lstMix.add( new ReadWriteMix( 0.20, 0.80, "20r/80w" ) );
		lstMix.add( new ReadWriteMix( 0.10, 0.90, "10r/90w" ) );
		lstMix.add( new ReadWriteMix( 0.05, 0.95, "5r/95w" ) );
		
		int stepSize = this._maxWorkload / this._steps;
				
		for( int i = 0; i < lstMix.size(); i++ )
		{
			ReadWriteMix mix = lstMix.get( i );
			// Have a list of #user values to stride through
			long intervalLength = this._incrementSize * this._incrementsPerInterval;
						
			for( int j = stepSize; j <= this._maxWorkload; j+= stepSize )
			{
				JSONObject profileConfig = new JSONObject();
				// Set the basics: # interval, users, mix name
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_INTERVAL_KEY, intervalLength );
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_USERS_KEY, j );
				
				profileConfig.put( LoadProfile.CFG_LOAD_PROFILE_MIX_KEY, mix._name );
				// Set the Storage specific elements
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_KEY, "radlab.rain.util.storage.UniformKeyGenerator" );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY, keyGenConfig );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_REQUEST_SIZE_KEY, objectSize );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_READ_PCT_KEY, mix._readPct );
				profileConfig.put( StorageLoadProfile.CFG_LOAD_PROFILE_WRITE_PCT_KEY, mix._writePct );
				profileConfig.put( StorageLoadProfile.CFG_NUM_HOT_OBJECTS_KEY, numHotObjects );
				profileConfig.put( StorageLoadProfile.CFG_HOT_TRAFFIC_FRACTION_KEY, hotTrafficFraction );
				
				StorageLoadProfile profile = new StorageLoadProfile( profileConfig );
				profile._name = FORMATTER.format(i) + "-" + FORMATTER.format( j ) + "-" + mix._name;
				profile.setTransitionTime( 0 );
				
				loadSchedule.add( profile );
			}
		}
		
		return loadSchedule;
	}

	public static void main( String[] args ) throws JSONException
	{
		CapacityWalkScheduleCreator creator = new CapacityWalkScheduleCreator();
		
		creator.setMaxWorkload( 300 );
		creator.setSteps( 8 );
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
			System.out.println( p );
	}
}