package radlab.rain.workload.httptest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.ProfileCreator;
import radlab.rain.Scenario;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.ConfigUtil;
import radlab.rain.workloadtraits.WikipediaScheduleCreator;

public class BurstUrlProfileCreator extends ProfileCreator 
{
	public static String CFG_HOST_LIST_FILE					= "hostListFile";
	public static String CFG_NUM_HOST_TARGETS_KEY			= "numHostTargets";
	public static String CFG_POPULAR_HOST_FRACTION_KEY		= "popularHostFraction";
	public static String CFG_POPULAR_HOST_LOAD_FRACTION_KEY = "popularHostLoadFraction";
	public static String CFG_MEAN_THINK_TIME_KEY			= "meanThinkTime";
	public static String CFG_USER_POPULATION_KEY			= "userPopulation";
		
	// Allow specification of users per popular app and users per less-popular app.
	// If these values are specified then the userPopulation and popularHostLoadFraction
	// parameters aren't necessary
	public static String CFG_USERS_PER_POPULAR_HOST			 = "usersPerPopularHost";
	public static String CFG_USERS_PER_LESS_POPULAR_HOST	 = "usersPerLessPopularHost";
	// Allow specification of burst size per popular app and burst size per less-popular app.
	public static String CFG_BURST_SIZE_PER_POPULAR_HOST	  = "burstSizePerPopularHost";
	public static String CFG_BURST_SIZE_PER_LESS_POPULAR_HOST = "burstSizePerLessPopularHost";
	
	private int _numHostTargets = 1;
	private float _popularHostFraction = 0.1f; // 10% of them
	private float _popularHostLoadFraction = 0.1f; // 10% of the traffic goes to the popular hosts
	private double _meanThinkTime = 0.0;
	private int _userPopulation = this._numHostTargets;
	private int _usersPerPopularHost  			= 10;
	private int _usersPerLessPopularHost 		= 1;
	private int _burstSizePerPopularHost		= 0;
	private int _burstSizePerLessPopularHost	= 0;
	private NumberFormat _formatter = new DecimalFormat( "#0.0000" );
	private int _responseTimeSamplingInterval = 100;
	
	public BurstUrlProfileCreator() 
	{}

	@Override
	public JSONObject createProfile(JSONObject params) throws JSONException 
	{
		// Pull out all the parameters we need:
		Vector<String> hostList = new Vector<String>();
		// Find the file with the list of hosts - it's not optional
		String hostListFile = params.getString(CFG_HOST_LIST_FILE);
		BufferedReader reader = null;
		// Open the file for reading and get the list of hosts
		try
		{
			reader = new BufferedReader( new FileReader( hostListFile ) );
			String hostSpec = "";
			while( (hostSpec = reader.readLine()) != null )
			{
				if( hostSpec.trim().length() > 0 )
					hostList.add( hostSpec );
			}
		}
		catch( IOException ioe )
		{
			throw new JSONException( ioe );
		}
		finally
		{
			if( reader != null )
			{
				try{ reader.close(); }
				catch( IOException oioe ){}
			}
		}
	
		
		if( params.has( CFG_POPULAR_HOST_FRACTION_KEY ) );
			this._popularHostFraction = (float) params.getDouble( CFG_POPULAR_HOST_FRACTION_KEY );
				
		if( params.has( CFG_MEAN_THINK_TIME_KEY ) )
			this._meanThinkTime = params.getDouble( CFG_MEAN_THINK_TIME_KEY );
		
		this._numHostTargets = hostList.size();		
		if( this._numHostTargets == 0 )
			throw new JSONException( "Expected at least 1 host/url to target." );
		
		// Get the burst sizes
		if( params.has( CFG_BURST_SIZE_PER_POPULAR_HOST ) )
			this._burstSizePerPopularHost = params.getInt( CFG_BURST_SIZE_PER_POPULAR_HOST );
		if( params.has( CFG_BURST_SIZE_PER_LESS_POPULAR_HOST ) )
			this._burstSizePerLessPopularHost = params.getInt( CFG_BURST_SIZE_PER_LESS_POPULAR_HOST );

		if( params.has( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL ) )
			this._responseTimeSamplingInterval = params.getInt( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL );
			
		this.printConfig( System.out );
		
		// Determine the set of hosts to touch - could all be contiguous or randomly selected
		Random random = new Random();
		JSONObject trackConfig = new JSONObject();
				
		// Determine the number of popular and less popular hosts
		int numPopularHosts =  Math.min( this._numHostTargets, Math.max( 0, (int) Math.ceil( this._numHostTargets * this._popularHostFraction ) ) );
		int numLessPopularHosts = this._numHostTargets - numPopularHosts;
		
		HashSet<Integer> popularHosts = new HashSet<Integer>();
		if( numLessPopularHosts != 0 )
		{
			// Pick the popular hosts
			while( popularHosts.size() != numPopularHosts )
			{
				int nextHost = random.nextInt( this._numHostTargets );
				if( popularHosts.contains( nextHost ) )
					continue;
				else popularHosts.add( nextHost );
			}
		}
		else // If numLessPopularHosts == 0 then everything is going to be popular
		{
			for( int i = 0; i < this._numHostTargets; i++ )
				popularHosts.add( i );
		}
				
		this._usersPerPopularHost = params.getInt(CFG_USERS_PER_POPULAR_HOST);
		this._usersPerLessPopularHost = params.getInt(CFG_USERS_PER_LESS_POPULAR_HOST);
		// Set the user population
		this._userPopulation = (this._usersPerPopularHost*numPopularHosts) + (this._usersPerLessPopularHost*numLessPopularHosts); 
		// Compute the popular host load fraction
		this._popularHostLoadFraction = (float)(this._usersPerPopularHost*numPopularHosts)/(float)this._userPopulation;
		
		// Print out what we've computed
		System.out.println( this + " total host targets                    : " + this._numHostTargets );
		System.out.println( this + " popular hosts fraction                : " + this._formatter.format( this._popularHostFraction ) );
		System.out.println( this + " popular hosts                         : " + numPopularHosts );
		System.out.println( this + " less popular hosts                    : " + numLessPopularHosts );
		System.out.println( this + " total user population                 : " + this._userPopulation );
		System.out.println( this + " popular host user load fraction       : " + this._formatter.format( this._popularHostLoadFraction ) );
		System.out.println( this + " users for all popular hosts           : " + this._usersPerPopularHost*numPopularHosts );
		System.out.println( this + " users for less popular hosts          : " + this._usersPerLessPopularHost*numLessPopularHosts );
		System.out.println( this + " users per popular host                : " + this._usersPerPopularHost );
		System.out.println( this + " users per less-popular host           : " + this._usersPerLessPopularHost );
		System.out.println( this + " effective user population             : " + ( (this._usersPerPopularHost*numPopularHosts)+(this._usersPerLessPopularHost*numLessPopularHosts) ) );
		
		// Change the way we compute the target's ip address
		// Store the last ip address we received,
		// Once the current ip address is different from the last ip address accept it
		// otherwise as for another one
		
		for( int i = 0; i < hostList.size(); i++ )
		{
			JSONObject trackDetails = null;
			
			String trackName = "";
			if( i < 10 )
				trackName = "burstUrl-000" + i;
			else trackName = "burstUrl-00" + i;
		
			// Get base IP, split on . get last octet convert to int then add i
			String targetHost = hostList.get(i);
		
			JSONObject generatorParameters = null;
			if( params.has( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY ) )
				generatorParameters = new JSONObject( params.getJSONObject( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY ).toString() );
			else generatorParameters = new JSONObject();
			
			if( popularHosts.contains( i ) )
			{
				trackName = trackName + "-p";
				// Put in the burst size
				generatorParameters.put( BurstUrlGenerator.CFG_BURST_SIZE_KEY, this._burstSizePerPopularHost );
				trackDetails = this.createTrack( targetHost, this._meanThinkTime, this._usersPerPopularHost, generatorParameters );
			}
			else 
			{
				// Put in the burst size
				generatorParameters.put( BurstUrlGenerator.CFG_BURST_SIZE_KEY, this._burstSizePerLessPopularHost );
				trackDetails = this.createTrack( targetHost, this._meanThinkTime, this._usersPerLessPopularHost, generatorParameters );
			}
	
			//System.out.println( generatorParameters.toString() );
			
			trackConfig.put( trackName, trackDetails );
		}
			
		return trackConfig;	
	}

	private JSONObject createTrack( String host, double meanThinkTime, int minUsers, JSONObject generatorParameters ) throws JSONException
	{
		JSONObject trackDetails = new JSONObject();
		// Fill in details
		trackDetails.put( ScenarioTrack.CFG_GENERATOR_KEY, "radlab.rain.workload.httptest.BurstUrlGenerator" );
		// Fill in any generator parameters
		trackDetails.put( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY, generatorParameters );
		
		trackDetails.put( ScenarioTrack.CFG_TRACK_CLASS_KEY, "radlab.rain.DefaultScenarioTrack" );
		trackDetails.put( ScenarioTrack.CFG_RESOURCE_PATH, "resources/" );
		// Add in loadProfileCreatorClass
		JSONObject behaviorDetails = new JSONObject();
		// Store the behavior details in the track config
		trackDetails.put( ScenarioTrack.CFG_BEHAVIOR_KEY, behaviorDetails );
		// Specifiy the load creator class - we're going to use Wikipedia for now
		trackDetails.put( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_KEY, "radlab.rain.workloadtraits.WikipediaScheduleCreator" );
		JSONObject scheduleCreatorParams = new JSONObject();
		// Fill in the parameters for the schedule creator
		// Just set the minUsers so the workload can be scaled
		scheduleCreatorParams.put( WikipediaScheduleCreator.CFG_INITIAL, minUsers );
				
		trackDetails.put( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_PARAMS_KEY, scheduleCreatorParams );
		
		JSONObject targetDetails = new JSONObject();
				
		targetDetails.put( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY, host );
		targetDetails.put( ScenarioTrack.CFG_TARGET_PORT_KEY, 80 );
		
		trackDetails.put( ScenarioTrack.CFG_TARGET_KEY, targetDetails );
		trackDetails.put( ScenarioTrack.CFG_LOG_SAMPLING_PROBABILITY_KEY, 0.0 ); // No log sampling
		trackDetails.put( ScenarioTrack.CFG_OPEN_LOOP_PROBABILITY_KEY, 0.0 );
		trackDetails.put( ScenarioTrack.CFG_MEAN_CYCLE_TIME_KEY, 0 );
		trackDetails.put( ScenarioTrack.CFG_MEAN_THINK_TIME_KEY, meanThinkTime );
		trackDetails.put( ScenarioTrack.CFG_INTERACTIVE_KEY, true );

		// Set response time sampling interval - should be tuned based on the expected 
		// order of the expected number of operations/requests that will be issued/served
		// e.g. lower values if we're doing a short run with few operations and
		// larger values if we're doing a long run with many operations so we reduce
		// memory overhead of storing samples
		trackDetails.put( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL, this._responseTimeSamplingInterval );

		return trackDetails;
	}
	
	private void printConfig( PrintStream out )
	{
		out.println( this + " Mean think time                       : " + this._meanThinkTime );
		out.println( this + " Burst size popular hosts              : " + this._burstSizePerPopularHost );
		out.println( this + " Burst size less popular hosts         : " + this._burstSizePerLessPopularHost );
		out.println( this + " Mean response time Sampling interval  : " + this._responseTimeSamplingInterval );
	}
	
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append( "[BurstUrlProfileCreator]" );
		return buf.toString();
	}
	
	public static void main( String[] args ) throws Exception
	{
		
		// Read in an example rain.xxx.json file and see what we get
		String filename = "config/rain.config.ac_burst_url.json";
		String fileContents = ConfigUtil.readFileAsString( filename );
		JSONObject jsonConfigRoot = new JSONObject( fileContents );
		JSONObject params = jsonConfigRoot.getJSONObject( Scenario.CFG_PROFILES_CREATOR_CLASS_PARAMS_KEY );
		
		BurstUrlProfileCreator creator = new BurstUrlProfileCreator(); 
		JSONObject tracks = creator.createProfile( params );
		System.out.println( tracks.toString() );
	}
}
