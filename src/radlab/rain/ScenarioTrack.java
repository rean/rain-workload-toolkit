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

import org.json.*;

/**
 * The ScenarioTrack abstract class represents a single workload among
 * potentially many that are simultaneously benchmarked under a single\
 * Scenario.<br />
 * <br />
 * The ScenarioTrack is responsible for reading in the configuration of a
 * workload and generating the load profiles.
 */
public abstract class ScenarioTrack 
{
	public static String CFG_OPEN_LOOP_PROBABILITY_KEY          = "pOpenLoop";
	public static String CFG_LOG_SAMPLING_PROBABILITY_KEY       = "pLogSampling";
	public static String CFG_MEAN_CYCLE_TIME_KEY                = "meanCycleTime";
	public static String CFG_MEAN_THINK_TIME_KEY                = "meanThinkTime";
	public static String CFG_INTERACTIVE_KEY                    = "interactive";
	public static String CFG_TARGETS_KEY                        = "target";
	public static String CFG_SCOREBOARD_SNAPSHOT_INTERVAL		= "metricSnapshotInterval";
	// Targets keys: hostname, port
	public static String CFG_TARGET_HOSTNAME_KEY                = "hostname";
	public static String CFG_TARGET_PORT_KEY                    = "port";
	public static String CFG_GENERATOR_KEY                      = "generator";
	public static String CFG_LOAD_PROFILE_CLASS_KEY             = "loadProfileClass";
	public static String CFG_LOAD_PROFILE_KEY                   = "loadProfile";
	// Load behavioral hints
	public static String CFG_BEHAVIOR_KEY                       = "behavior";
	public static String CFG_RESOURCE_PATH                      = "resourcePath";
	public static String CFG_OBJECT_POOL_MAX_SIZE				= "objectPoolMaxSize";
	public static String CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL	= "meanResponseTimeSamplingInterval";
	
	// Defaults
	public static long DEFAULT_OBJECT_POOL_MAX_SIZE				= 50000;
	public static long DEFAULT_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL = 500;
	
	protected Scenario _parentScenario                          = null;
	protected Generator _generator                              = null;
	protected String _name                                      = "none";
	protected String _targetHostname                            = null;
	protected int _targetPort                                   = 80;
	public volatile LoadProfile _currentLoadProfile             = null;
	protected LinkedList<LoadProfile> _loadSchedule             = new LinkedList<LoadProfile>();
	protected Hashtable<String,MixMatrix> _mixMap               = new Hashtable<String,MixMatrix>();
	protected String _scoreboardClassName                       = "radlab.rain.Scoreboard";
	protected String _generatorClassName                        = "";
	protected String _loadProfileClassName                      = "";
	protected boolean _interactive                              = true;
	private IScoreboard _scoreboard                             = null;
	protected double _openLoopProbability                       = 0.0;
	protected String _resourcePath                              = "resources/"; // Path on local machine for files we may need (e.g. to send an image/data file as part of an operation)
	protected double _meanCycleTime                             = 0.0; // non-stop request generation
	protected double _meanThinkTime                             = 0.0; // non-stop request generation
	protected double _logSamplingProbability                    = 1.0; // Log every executed request seen by the Scoreboard
	protected double _metricSnapshotInterval					= 60.0; // (seconds)
	protected ObjectPool _objPool 								= null;
	protected long _meanResponseTimeSamplingInterval			= DEFAULT_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL;
	
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
	// public abstract LoadProfile getNextLoadProfile();
	
	public String getGeneratorClassName() { return this._generatorClassName; }
	public String getName() { return this._name; }
	public void setName( String val ) { this._name = val; }
	public long getRampUp() { return this._parentScenario.getRampUp(); }
	public long getDuration() { return this._parentScenario.getDuration(); }
	public long getRampDown() { return this._parentScenario.getRampDown(); }
	public boolean getInteractive() { return this._interactive; }
	public void setInteractive( boolean val ) { this._interactive = val; }

	public ObjectPool getObjectPool() { return this._objPool; };
	
	public int getMaxUsers() 
	{ 
		int maxUsers = 0;
		
		Iterator<LoadProfile> it = this._loadSchedule.iterator();
		while( it.hasNext() )
		{
			LoadProfile current = it.next(); 
			if( current.getNumberOfUsers() > maxUsers )
				maxUsers = current.getNumberOfUsers();
		}
		
		return maxUsers; 
	}
	
	public MixMatrix getMixMatrix( String name )
	{
		return this._mixMap.get( name );
	}
	
	public Scenario getParentScenario() { return this._parentScenario; }
	public void setParentScenario( Scenario val  ) { this._parentScenario = val; }
		
	public IScoreboard getScoreboard() { return this._scoreboard; }
	public void setScoreboard( IScoreboard val ) { this._scoreboard = val; }
	
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
	
	public Generator getGenerator() { return this._generator; }
	public void setGenerator( Generator val ) { this._generator = val; }
		
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
		// 2) Concrete Generator
		this._generatorClassName = config.getString( ScenarioTrack.CFG_GENERATOR_KEY ); 
		this._generator = this.createWorkloadGenerator( this._generatorClassName );
		// 3) Target Information
		JSONObject target = config.getJSONObject( ScenarioTrack.CFG_TARGETS_KEY );
		this._targetHostname = target.getString( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY );
		this._targetPort = target.getInt( ScenarioTrack.CFG_TARGET_PORT_KEY );
		// 4) Log Sampling Probability
		this._logSamplingProbability = config.getDouble( ScenarioTrack.CFG_LOG_SAMPLING_PROBABILITY_KEY );
		// 5) Mean Cycle Time
		this._meanCycleTime = config.getDouble( ScenarioTrack.CFG_MEAN_CYCLE_TIME_KEY );
		// 6) Mean Think Time
		this._meanThinkTime = config.getDouble( ScenarioTrack.CFG_MEAN_THINK_TIME_KEY );
		this._generator.setMeanCycleTime( (long) (this._meanCycleTime * 1000) );
		this._generator.setMeanThinkTime( (long) (this._meanThinkTime * 1000) );
		// 7) Interactive?
		this._interactive = config.getBoolean( ScenarioTrack.CFG_INTERACTIVE_KEY );
		// 8) Concrete Load Profile and Load Profile Array
		this._loadProfileClassName = config.getString( ScenarioTrack.CFG_LOAD_PROFILE_CLASS_KEY );
		JSONArray loadSchedule = config.getJSONArray( ScenarioTrack.CFG_LOAD_PROFILE_KEY );
		for ( int i = 0; i < loadSchedule.length(); i++ )
		{
			JSONObject profileObj = loadSchedule.getJSONObject( i );
			LoadProfile profile = this.createLoadProfile( this._generatorClassName, profileObj );
			
			this._loadSchedule.add( profile );
		}
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
		if( config.has( ScenarioTrack.CFG_SCOREBOARD_SNAPSHOT_INTERVAL ) )
		{
			this._metricSnapshotInterval = config.getDouble( ScenarioTrack.CFG_SCOREBOARD_SNAPSHOT_INTERVAL );
		}
		// 11 Initialize the object pool - by default it remains empty unless one of the concrete operations
		// uses it.
		if( config.has( ScenarioTrack.CFG_OBJECT_POOL_MAX_SIZE ) )
		{
			this._objPool = new ObjectPool( config.getLong( ScenarioTrack.CFG_OBJECT_POOL_MAX_SIZE ) );
		}
		else this._objPool = new ObjectPool( ScenarioTrack.DEFAULT_OBJECT_POOL_MAX_SIZE );
		this._objPool.setTrackName( this._name );
		// 12 Configure the response time sampler
		if( config.has( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL ) )
			this._meanResponseTimeSamplingInterval = config.getLong( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL );
	}
	
	// Factory methods
	@SuppressWarnings("unchecked")
	public Generator createWorkloadGenerator( String name ) throws Exception
	{
		Generator generator = null;
		Class<Generator> generatorClass = (Class<Generator>) Class.forName( name );
		Constructor<Generator> generatorCtor = generatorClass.getConstructor( new Class[]{ ScenarioTrack.class } );
		generator = (Generator) generatorCtor.newInstance( new Object[] { this } );
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
		scoreboard.setMeanResponseTimeSamplingInterval( this._meanResponseTimeSamplingInterval );
		return scoreboard;
	}
	
	public String toString()
	{
		return "[TRACK: " + this._name + "]";
	}
}
