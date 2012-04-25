/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package radlab.rain;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * The ScenarioTrack abstract class represents a single workload among
 * potentially many that are simultaneously run under a single
 * Scenario.<br />
 * <br />
 * The ScenarioTrack is responsible for reading in the configuration of a
 * workload and generating the load profiles.
 */
public abstract class ScenarioTrack 
{
	public static final int VALID_LOAD_PROFILE								= 0;
	public static final int ERROR_INVALID_LOAD_PROFILE_BAD_NUM_USERS 		= 1777;
	public static final int ERROR_INVALID_LOAD_PROFILE_BAD_MIX_NAME 		= 1778;
	public static final int ERROR_INVALID_LOAD_PROFILE_BAD_BEHAVIOR_HINT 	= 1779;
	public static final int ERROR_TRACK_NOT_FOUND							= 1780;
		
	public static String CFG_TRACK_CLASS_KEY 					= "track";
	public static String CFG_OPEN_LOOP_PROBABILITY_KEY          = "pOpenLoop";
	public static String CFG_LOG_SAMPLING_PROBABILITY_KEY       = "pLogSampling";
	public static String CFG_MEAN_CYCLE_TIME_KEY                = "meanCycleTime";
	public static String CFG_MEAN_THINK_TIME_KEY                = "meanThinkTime";
	public static String CFG_INTERACTIVE_KEY                    = "interactive";
	public static String CFG_TARGET_KEY	                        = "target";
	public static String CFG_METRIC_SNAPSHOT_INTERVAL       	= "metricSnapshotInterval";
	public static String CFG_METRIC_SNAPSHOTS					= "metricSnapshots";
	public static String CFG_METRIC_SNAPSHOT_FILE_SUFFIX		= "metricSnapshotsFileSuffix";
	public static String CFG_METRIC_DB							= "metricDB";
	// Targets keys: hostname, port
	public static String CFG_TARGET_HOSTNAME_KEY                = "hostname";
	public static String CFG_TARGET_PORT_KEY                    = "port";
	public static String CFG_GENERATOR_KEY                      = "generator";
	public static String CFG_GENERATOR_PARAMS_KEY				= "generatorParameters";
	public static String CFG_LOAD_PROFILE_CLASS_KEY             = "loadProfileClass";
	public static String CFG_LOAD_PROFILE_KEY                   = "loadProfile";
	public static String CFG_LOAD_SCHEDULE_CREATOR_KEY			= "loadScheduleCreator";
	public static String CFG_LOAD_SCHEDULE_CREATOR_PARAMS_KEY	= "loadScheduleCreatorParameters";
	public static String CFG_LOAD_GENERATION_STRATEGY_KEY		= "loadGenerationStrategy";
	public static String CFG_LOAD_GENERATION_STRATEGY_PARAMS_KEY= "loadGenerationStrategyParams";
	
	// Load behavioral hints
	public static String CFG_BEHAVIOR_KEY                       = "behavior";
	public static String CFG_RESOURCE_PATH                      = "resourcePath";
	public static String CFG_OBJECT_POOL_MAX_SIZE               = "objectPoolMaxSize";
	public static String CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL = "meanResponseTimeSamplingInterval";
	public static String CFG_MAX_USERS							= "maxUsers";
		
	// Defaults
	public static long DEFAULT_OBJECT_POOL_MAX_SIZE             		= 50000;
	public static long DEFAULT_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL 		= 500;
	public static String DEFAULT_LOAD_PROFILE_CLASS             		= "radlab.rain.LoadProfile";
	public static final String DEFAULT_LOAD_GENERATION_STRATEGY_CLASS 	= "radlab.rain.PartlyOpenLoopLoadGeneration";
	
	protected Scenario _parentScenario                          = null;
	//protected Generator _generator                              = null;
	protected String _name                                      = "none";
	protected String _targetHostname                            = null;
	protected int _targetPort                                   = 80;
	public volatile LoadProfile _currentLoadProfile             = null;
	protected LinkedList<LoadProfile> _loadSchedule             = new LinkedList<LoadProfile>();
	protected Hashtable<String,MixMatrix> _mixMap               = new Hashtable<String,MixMatrix>();
	protected String _scoreboardClassName                       = "radlab.rain.Scoreboard";
	protected String _generatorClassName                        = "";
	protected JSONObject _generatorParams						= null;
	protected String _loadProfileClassName                      = "";
	protected String _loadGenerationStrategyClassName			= "";
	protected JSONObject _loadGenerationStrategyParams			= null;
	protected boolean _interactive                              = true;
	private IScoreboard _scoreboard                             = null;
	protected double _openLoopProbability                       = 0.0;
	protected String _resourcePath                              = "resources/"; // Path on local machine for files we may need (e.g. to send an image/data file as part of an operation)
	protected double _meanCycleTime                             = 0.0; // non-stop request generation
	protected double _meanThinkTime                             = 0.0; // non-stop request generation
	protected double _logSamplingProbability                    = 1.0; // Log every executed request seen by the Scoreboard
	protected double _metricSnapshotInterval                    = 60.0; // (seconds)
	protected boolean _useMetricSnapshots						= false;
	protected String _metricSnapshotFileSuffix					= "";
	protected ObjectPool _objPool                               = null;
	protected long _meanResponseTimeSamplingInterval            = DEFAULT_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL;
	protected int _maxUsersFromConfig							= 0;
	
	/**
	 * Create a new scenario track that will be benchmarked as part of the
	 * provided <code>Scenario</code>.
	 * 
	 * @param parentScenario    The Scenario under which this will be running.
	 */
	public ScenarioTrack( Scenario parentScenario )
	{
		this._parentScenario = parentScenario;
	}
	
	public abstract void start();
	public abstract void end();
	public abstract LoadProfile getCurrentLoadProfile();
	public abstract int validateLoadProfile( LoadProfile profile );
	public abstract void submitDynamicLoadProfile( LoadProfile profile );
	// public abstract LoadProfile getNextLoadProfile();
	
	public String getGeneratorClassName() { return this._generatorClassName; }
	public JSONObject getGeneratorParams() { return this._generatorParams; }
	public String getName() { return this._name; }
	public void setName( String val ) { this._name = val; }
	public long getRampUp() { return this._parentScenario.getRampUp(); }
	public long getDuration() { return this._parentScenario.getDuration(); }
	public long getRampDown() { return this._parentScenario.getRampDown(); }
	public boolean getInteractive() { return this._interactive; }
	public void setInteractive( boolean val ) { this._interactive = val; }
	public String getLoadGenerationStrategyClassName() { return this._loadGenerationStrategyClassName; }
	public JSONObject getLoadGenerationStrategyParams() { return this._loadGenerationStrategyParams; }
	
	public ObjectPool getObjectPool() { return this._objPool; };
	
	public int getMaxUsers() 
	{ 
		// Search the load profiles to figure out the maximum 
		int maxUsersFromProfile = 0;
		
		Iterator<LoadProfile> it = this._loadSchedule.iterator();
		while( it.hasNext() )
		{
			LoadProfile current = it.next(); 
			if( current.getNumberOfUsers() > maxUsersFromProfile )
				maxUsersFromProfile = current.getNumberOfUsers();
		}
		
		// In the end return the max of what's in the schedule and what was
		// put in the explicit maxUsers config setting. If the config setting
		// is larger that what's in the profile, then this allows the for a dynamic
		// load profile to create a spike of that magnitude.
		return Math.max( maxUsersFromProfile, this._maxUsersFromConfig ); 
	}
	
	public MixMatrix getMixMatrix( String name )
	{
		return this._mixMap.get( name );
	}
	
	public Scenario getParentScenario() { return this._parentScenario; }
	public void setParentScenario( Scenario val  ) { this._parentScenario = val; }
		
	public IScoreboard getScoreboard() { return this._scoreboard; }
	public void setScoreboard( IScoreboard val ) { this._scoreboard = val; val.setTargetHost( this._targetHostname ); }
	
	public double getOpenLoopProbability() { return this._openLoopProbability; }
	public void setOpenLoopProbability( double val ) { this._openLoopProbability = val; }
	
	public String getTargetHostName() { return this._targetHostname; }
	public void setTargetHostName( String val ) { this._targetHostname = val; }
	
	public int getTargetHostPort() { return this._targetPort; }
	public void setTargetHostPort( int val ) { this._targetPort = val; }
	
	public double getMeanCycleTime() { return this._meanCycleTime; }
	public void setMeanCycleTime( double val ) { this._meanCycleTime = val; }
	
	public double getMeanThinkTime() { return this._meanThinkTime; }
	public void setMeanThinkTime( double val ) { this._meanThinkTime = val; }
	
	public String getResourcePath() { return this._resourcePath; }
	public void setResourcePath( String val ) { this._resourcePath = val; }
	
	//public Generator getGenerator() { return this._generator; }
	//public void setGenerator( Generator val ) { this._generator = val; }
		
	public double getLogSamplingProbability() { return this._logSamplingProbability; }
	public void setLogSamplingProbability( double val ) { this._logSamplingProbability = val; }
	
	public double getMetricSnapshotInterval() { return this._metricSnapshotInterval; }
	public void setMetricSnapshotInterval( double val ) { this._metricSnapshotInterval = val; }
	
	/**
	 * Initializes a ScenarioTrack by reading the configurations set in the
	 * provided JSON object.
	 * 
	 * @param config    The JSON configuration object.
	 * 
	 * @throws JSONException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void initialize( JSONObject config ) throws JSONException, Exception
	{
		// 1) Open-Loop Probability
		this._openLoopProbability = config.getDouble( ScenarioTrack.CFG_OPEN_LOOP_PROBABILITY_KEY );
		// 3) Target Information
		JSONObject target = config.getJSONObject( ScenarioTrack.CFG_TARGET_KEY );
		this._targetHostname = target.getString( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY );
		this._targetPort = target.getInt( ScenarioTrack.CFG_TARGET_PORT_KEY );
		// 2) Concrete Generator
		this._generatorClassName = config.getString( ScenarioTrack.CFG_GENERATOR_KEY );
		this._generatorParams = null;
		if( config.has( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY ) )
			this._generatorParams = config.getJSONObject( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY );
		//this._generator = this.createWorkloadGenerator( this._generatorClassName, this._generatorParams );
		// 4) Log Sampling Probability
		this._logSamplingProbability = config.getDouble( ScenarioTrack.CFG_LOG_SAMPLING_PROBABILITY_KEY );
		// 5) Mean Cycle Time
		this._meanCycleTime = config.getDouble( ScenarioTrack.CFG_MEAN_CYCLE_TIME_KEY );
		// 6) Mean Think Time
		this._meanThinkTime = config.getDouble( ScenarioTrack.CFG_MEAN_THINK_TIME_KEY );
		//this._generator.setMeanCycleTime( (long) (this._meanCycleTime * 1000) );
		//this._generator.setMeanThinkTime( (long) (this._meanThinkTime * 1000) );
		// 7) Interactive?
		this._interactive = config.getBoolean( ScenarioTrack.CFG_INTERACTIVE_KEY );
		// 8) Concrete Load Profile and Load Profile Array
		if ( config.has( ScenarioTrack.CFG_LOAD_PROFILE_CLASS_KEY ) )
		{
			this._loadProfileClassName = config.getString( ScenarioTrack.CFG_LOAD_PROFILE_CLASS_KEY );
		}
		else
		{
			this._loadProfileClassName = ScenarioTrack.DEFAULT_LOAD_PROFILE_CLASS;
		}
		// Look for a load schedule OR a class that creates it, we prefer the class
		if( config.has( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_KEY ) )
		{
			// Create the load schedule creator
			String loadSchedulerClass = config.getString( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_KEY );
			LoadScheduleCreator loadScheduler = this.createLoadScheduleCreator( loadSchedulerClass );
			JSONObject loadSchedulerParams = new JSONObject();
			// Look for load scheduler parameters if any exist
			if( config.has( CFG_LOAD_SCHEDULE_CREATOR_PARAMS_KEY) )
				loadSchedulerParams = config.getJSONObject( CFG_LOAD_SCHEDULE_CREATOR_PARAMS_KEY );
			
			if( loadScheduler != null )
				this._loadSchedule = loadScheduler.createSchedule( loadSchedulerParams );
			else throw new Exception( "Error creating load scheduler class: " +  loadSchedulerClass );
		}
		else
		{
			JSONArray loadSchedule = config.getJSONArray( ScenarioTrack.CFG_LOAD_PROFILE_KEY );
			for ( int i = 0; i < loadSchedule.length(); i++ )
			{
				JSONObject profileObj = loadSchedule.getJSONObject( i );
				LoadProfile profile = this.createLoadProfile( this._loadProfileClassName, profileObj );
				
				// If the profile does NOT have a name, set one using "i" formatted as "00000N"
				if( profile._name == null || profile._name.length() == 0 )
					profile._name = new java.text.DecimalFormat("00000").format( i );
				
				this._loadSchedule.add( profile );
			}
		}
		
		if( this._loadSchedule.size() == 0 )
			throw new Exception( "Error: empty load schedule. Nothing to do." );
		
		// 9) Load Mix Matrices/Behavior Directives
		JSONObject behavior = config.getJSONObject( ScenarioTrack.CFG_BEHAVIOR_KEY );
		Iterator<String> keyIt = behavior.keys();
		// Each of the keys in the behavior section should be for some mix matrix
		while ( keyIt.hasNext() )
		{
			String mixName = keyIt.next();
			// Now we need to get this object and parse it
			JSONArray mix = behavior.getJSONArray( mixName );
			double[][] data = null;
			for ( int i = 0; i < mix.length(); i++ )
			{
				if ( i == 0 )
				{
					data = new double[mix.length()][mix.length()];
				}
				// Each row is itself an array of doubles
				JSONArray row = mix.getJSONArray( i );
				for ( int j = 0; j < row.length(); j++ )
				{
					data[i][j] = row.getDouble( j );
				}
			}
			MixMatrix m = new MixMatrix( data );
			this._mixMap.put( mixName, m );
		}
		// 10 Scoreboard snapshot interval
		if( config.has( ScenarioTrack.CFG_METRIC_SNAPSHOT_INTERVAL ) )
		{
			this._metricSnapshotInterval = config.getDouble( ScenarioTrack.CFG_METRIC_SNAPSHOT_INTERVAL );
		}
		if( config.has( ScenarioTrack.CFG_METRIC_SNAPSHOTS ) )
			this._useMetricSnapshots = config.getBoolean( ScenarioTrack.CFG_METRIC_SNAPSHOTS );	
		if( config.has( ScenarioTrack.CFG_METRIC_SNAPSHOT_FILE_SUFFIX ) )
			this._metricSnapshotFileSuffix = config.getString( ScenarioTrack.CFG_METRIC_SNAPSHOT_FILE_SUFFIX );
		// 11 Initialize the object pool - by default it remains empty unless one of the concrete operations
		// uses it.
		if( config.has( ScenarioTrack.CFG_OBJECT_POOL_MAX_SIZE ) )
		{
			this._objPool = new ObjectPool( config.getLong( ScenarioTrack.CFG_OBJECT_POOL_MAX_SIZE ) );
		}
		else
		{
			this._objPool = new ObjectPool( ScenarioTrack.DEFAULT_OBJECT_POOL_MAX_SIZE );
		}
		this._objPool.setTrackName( this._name );
		// 12 Configure the response time sampler
		if( config.has( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL ) )
			this._meanResponseTimeSamplingInterval = config.getLong( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL );
		// 13 Configure the maxUsers if specified
		if( config.has( ScenarioTrack.CFG_MAX_USERS ) )
			this._maxUsersFromConfig = config.getInt( ScenarioTrack.CFG_MAX_USERS );
		// 14 Look for a load generation strategy and optional params if they exist
		if( config.has( ScenarioTrack.CFG_LOAD_GENERATION_STRATEGY_KEY ) )
		{
			this._loadGenerationStrategyClassName = config.getString( ScenarioTrack.CFG_LOAD_GENERATION_STRATEGY_KEY );
			// Check for parameters
			if( config.has( ScenarioTrack.CFG_LOAD_GENERATION_STRATEGY_PARAMS_KEY ) )
				this._loadGenerationStrategyParams = config.getJSONObject( ScenarioTrack.CFG_LOAD_GENERATION_STRATEGY_PARAMS_KEY );
			else this._loadGenerationStrategyParams = new JSONObject();
		}
		else
		{
			this._loadGenerationStrategyClassName = ScenarioTrack.DEFAULT_LOAD_GENERATION_STRATEGY_CLASS;
			this._loadGenerationStrategyParams = new JSONObject();
		}
	}
	
	// Factory methods
	@SuppressWarnings("unchecked")
	public LoadScheduleCreator createLoadScheduleCreator( String name ) throws Exception
	{
		LoadScheduleCreator creator = null;
		Class<LoadScheduleCreator> creatorClass = (Class<LoadScheduleCreator>) Class.forName( name );
		Constructor<LoadScheduleCreator> creatorCtor = creatorClass.getConstructor( new Class[]{} );
		creator = (LoadScheduleCreator) creatorCtor.newInstance( (Object[]) null );
		return creator;
	}
	
	@SuppressWarnings("unchecked")
	public Generator createWorkloadGenerator( String name, JSONObject config ) throws Exception
	{
		Generator generator = null;
		Class<Generator> generatorClass = (Class<Generator>) Class.forName( name );
		Constructor<Generator> generatorCtor = generatorClass.getConstructor( new Class[]{ ScenarioTrack.class } );
		generator = (Generator) generatorCtor.newInstance( new Object[] { this } );
		if( config != null )
			generator.configure( config );
		return generator;
	}
	
	@SuppressWarnings("unchecked")
	public LoadProfile createLoadProfile( String name, JSONObject profileObj ) throws Exception
	{
		LoadProfile loadProfile = null;
		Class<LoadProfile> loadProfileClass = (Class<LoadProfile>) Class.forName( name );
		Constructor<LoadProfile> loadProfileCtor = loadProfileClass.getConstructor( new Class[]{ JSONObject.class } );
		loadProfile = (LoadProfile) loadProfileCtor.newInstance( new Object[] { profileObj } );
		return loadProfile;
	}
	
	@SuppressWarnings("unchecked")
	public IScoreboard createScoreboard( String name ) throws Exception
	{
		if( name == null )
			name = this._scoreboardClassName;
		
		IScoreboard scoreboard = null;
		Class<IScoreboard> scoreboardClass = (Class<IScoreboard>) Class.forName( name );
		Constructor<IScoreboard> scoreboardCtor = scoreboardClass.getConstructor( String.class );
		scoreboard = (IScoreboard) scoreboardCtor.newInstance( this._name );
		// Set the log sampling probability for the scoreboard
		scoreboard.setLogSamplingProbability( this._logSamplingProbability );
		scoreboard.setScenarioTrack( this );
		scoreboard.setUsingMetricSnapshots( this._useMetricSnapshots );
		scoreboard.setMeanResponseTimeSamplingInterval( this._meanResponseTimeSamplingInterval );
		return scoreboard;
	}
	
	@SuppressWarnings("unchecked")
	public LoadGenerationStrategy createLoadGenerationStrategy( String loadGenerationStrategyClassName, JSONObject loadGenerationStrategyParams, Generator generator, int id ) throws Exception
	{
		LoadGenerationStrategy loadGenStrategy = null;
		Class<LoadGenerationStrategy> loadGenStrategyClass = (Class<LoadGenerationStrategy>) Class.forName( loadGenerationStrategyClassName );
		Constructor<LoadGenerationStrategy> loadGenStrategyCtor = loadGenStrategyClass.getConstructor( new Class[] {Generator.class, long.class, JSONObject.class} );
		loadGenStrategy = (LoadGenerationStrategy) loadGenStrategyCtor.newInstance( new Object[] { generator, id, loadGenerationStrategyParams } );
		return loadGenStrategy;
	}
	
	public String toString()
	{
		return "[TRACK: " + this._name + "]";
	}
}
