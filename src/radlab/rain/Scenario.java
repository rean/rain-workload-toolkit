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

import java.io.IOException;
import java.lang.reflect.Constructor;
//import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Iterator;
//import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.communication.RainPipe;
import radlab.rain.util.ConfigUtil;

/**
 * The Scenario class contains the specifications for a benchmark scenario,
 * which includes the timings (i.e. ramp up, duration, ramp down) and the
 * different scenario tracks.
 */
public class Scenario 
{
	public static String CFG_PROFILES_KEY    					= "profiles";
	public static String CFG_PROFILES_CREATOR_CLASS_KEY			= "profilesCreatorClass";
	public static String CFG_PROFILES_CREATOR_CLASS_PARAMS_KEY 	= "profilesCreatorClassParams";
	public static String CFG_TIMING_KEY      					= "timing";
	public static String CFG_RAMP_UP_KEY     					= "rampUp";
	public static String CFG_DURATION_KEY    					= "duration";
	public static String CFG_RAMP_DOWN_KEY   					= "rampDown";
	public static String CFG_VERBOSE_ERRORS_KEY					= "verboseErrors";
	public static String CFG_USE_PIPE							= "usePipe";
	public static String CFG_PIPE_PORT							= "pipePort";
	public static String CFG_PIPE_THREADS						= "pipeThreads";
	public static String CFG_WAIT_FOR_START_SIGNAL				= "waitForStartSignal";
	public static String CFG_MAX_SHARED_THREADS					= "maxSharedThreads";
	public static String CFG_AGGREGATE_STATS					= "aggregateStats";
	
	public static final int DEFAULT_MAX_SHARED_THREADS			= 10;
	public static final boolean DEFAULT_AGGREGATE_STATS			= false;
	
	/** Ramp up time in seconds. */
	private long _rampUp;
	
	/** Duration of the run in seconds. */
	private long _duration;

	/** Ramp down time in seconds. */
	private long _rampDown;
	
	/** Max number of threads to keep in the shared threadpool */
	private int _maxSharedThreads = DEFAULT_MAX_SHARED_THREADS;
	
	private boolean _aggregateStats = DEFAULT_AGGREGATE_STATS;
	
	/** The instantiated tracks specified by the JSON configuration. */
	// Use Hashtable instead of flat list
	//private LinkedList<ScenarioTrack> _tracks = new LinkedList<ScenarioTrack>();
	private TreeMap<String,ScenarioTrack> _tracks = new TreeMap<String,ScenarioTrack>();
	
	public long getRampUp() { return this._rampUp; }
	public void setRampUp( long val ) { this._rampUp = val; }
	
	public long getRampDown() { return this._rampDown; }
	public void setRampDown( long val ) { this._rampDown = val; }
	
	public long getDuration() { return this._duration; }
	public void setDuration( long val ) { this._duration = val; }
	
	public int getMaxSharedThreads() { return this._maxSharedThreads; }
	public void setMaxSharedThreads( int val ) { this._maxSharedThreads = val; }
	
	public boolean getAggregateStats() { return this._aggregateStats; }
	public void setAggregateStats( boolean val ) { this._aggregateStats = val; }
	
	public TreeMap<String,ScenarioTrack> getTracks() { return this._tracks; }
	
	/** Create a new and uninitialized <code>Scenario</code>. */
	public Scenario()
	{}
	
	/**
	 * Create a new Scenario and load the profile specified in the given JSON
	 * configuration object.
	 * 
	 * @param jsonConfig    The JSON object containing load specifications.
	 */
	public Scenario( JSONObject jsonConfig ) throws Exception
	{
		this.loadProfile( jsonConfig );
	}
	
	/**
	 * Ask each scenario track to start.
	 */
	public void start()
	{
		for ( ScenarioTrack track : this._tracks.values() )
		{
			track.start();
		}
	}
	
	/**
	 * Ask each scenario track to end.
	 */
	public void end()
	{
		for ( ScenarioTrack track : this._tracks.values() )
		{
			track.end();
		}
	}
	
	/**
	 * Reads the run specifications from the provided JSON configuration
	 * object. The timings (i.e. ramp up, duration, and ramp down) are set and
	 * the scenario tracks are created.
	 * 
	 * @param jsonConfig    The JSON object containing load specifications.
	 */
	public void loadProfile( JSONObject jsonConfig ) throws Exception
	{
		JSONObject tracksConfig = null;
		try
		{
			JSONObject timing = jsonConfig.getJSONObject( CFG_TIMING_KEY );
			setRampUp( timing.getLong( Scenario.CFG_RAMP_UP_KEY ) );
			setDuration( timing.getLong( Scenario.CFG_DURATION_KEY ) );
			setRampDown( timing.getLong( Scenario.CFG_RAMP_DOWN_KEY ) );
			
			// Set up Rain configuration params (if they've been provided)
			if( jsonConfig.has( Scenario.CFG_VERBOSE_ERRORS_KEY ) )
			{
				boolean val = jsonConfig.getBoolean( Scenario.CFG_VERBOSE_ERRORS_KEY );
				RainConfig.getInstance()._verboseErrors = val;
			}
			// Figure out whether we're using communication pipes
			
			// Figure out whether we're waiting for a start signal from an external controller
			if( jsonConfig.has( Scenario.CFG_PIPE_PORT ) )
			{
				RainConfig.getInstance()._pipePort = jsonConfig.getInt( Scenario.CFG_PIPE_PORT );
				RainPipe.getInstance().setPort( RainConfig.getInstance()._pipePort );
			}
			
			if( jsonConfig.has( Scenario.CFG_PIPE_THREADS ) )
			{
				RainConfig.getInstance()._pipeThreads = jsonConfig.getInt( Scenario.CFG_PIPE_THREADS );
				RainPipe.getInstance().setNumThreads( RainConfig.getInstance()._pipeThreads );
			}
			
			boolean usePipe = false;
			if( jsonConfig.has( Scenario.CFG_USE_PIPE) )
				usePipe = jsonConfig.getBoolean( Scenario.CFG_USE_PIPE );
			
			// We can only wait for start signal if we're using a pipe to the outside world.
			// If we're not using a pipe to the outside world then just launch the run.
			if( usePipe )
			{
				// Set in the config that we're using pipes
				RainConfig.getInstance()._usePipe = usePipe;
				// Check whether we're supposed to wait for a start signal
				if( jsonConfig.has( Scenario.CFG_WAIT_FOR_START_SIGNAL ) )
				{
					RainConfig.getInstance()._waitForStartSignal = jsonConfig.getBoolean( Scenario.CFG_WAIT_FOR_START_SIGNAL );
				}
			}
			
			// Look for the profiles key OR the name of a class that generates the
			// profiles.
			if( jsonConfig.has( CFG_PROFILES_CREATOR_CLASS_KEY ) )
			{
				// Programmatic generation class takes precedence
				// Create profile creator class by reflection
				String profileCreatorClass = jsonConfig.getString( CFG_PROFILES_CREATOR_CLASS_KEY);
				ProfileCreator creator = this.createLoadProfileCreator( profileCreatorClass );
				JSONObject params = null;
				// Look for profile creator params - if we find some then pass them
				if( jsonConfig.has( CFG_PROFILES_CREATOR_CLASS_PARAMS_KEY ) )
					params = jsonConfig.getJSONObject( CFG_PROFILES_CREATOR_CLASS_PARAMS_KEY );
				
				tracksConfig = creator.createProfile( params );
			}
			else // Otherwise there MUST be a profiles key in the config file
			{
				String filename = jsonConfig.getString( CFG_PROFILES_KEY );
				String fileContents = ConfigUtil.readFileAsString( filename );
				tracksConfig = new JSONObject( fileContents );
			}
			
			if( jsonConfig.has( CFG_MAX_SHARED_THREADS ) )
			{
				int sharedThreads = jsonConfig.getInt( CFG_MAX_SHARED_THREADS );
				if( sharedThreads > 0 )
					this._maxSharedThreads = sharedThreads;
			}
			
			if( jsonConfig.has( CFG_AGGREGATE_STATS ) )
				this._aggregateStats = jsonConfig.getBoolean( CFG_AGGREGATE_STATS );
		}
		catch ( JSONException e )
		{
			System.out.println( "[SCENARIO] ERROR reading JSON configuration object. Reason: " + e.toString() );
			System.exit( 1 );
		}
		catch ( IOException e )
		{
			System.out.println( "[SCENARIO] ERROR loading tracks configuration file. Reason: " + e.toString() );
			System.exit( 1 );
		}
		
		this.loadTracks( tracksConfig );
	}
	
	@SuppressWarnings("unchecked")
	public ProfileCreator createLoadProfileCreator( String name ) throws Exception
	{
		ProfileCreator creator = null;
		Class<ProfileCreator> creatorClass = (Class<ProfileCreator>) Class.forName( name );
		Constructor<ProfileCreator> creatorCtor = creatorClass.getConstructor( new Class[]{} );
		creator = (ProfileCreator) creatorCtor.newInstance( (Object[]) null );
		return creator;
	}
	
	/**
	 * Reads the track configuration from the provided JSON configuration
	 * object and creates each scenario track.
	 * 
	 * @param jsonConfig    The JSON object containing load specifications.
	 */
	@SuppressWarnings("unchecked")
	protected void loadTracks( JSONObject jsonConfig )
	{
		try
		{
			Iterator<String> i = jsonConfig.keys();
			while ( i.hasNext() )
			{
				String trackName = i.next();
				JSONObject trackConfig = jsonConfig.getJSONObject( trackName );
				
				String trackClassName = trackConfig.getString( ScenarioTrack.CFG_TRACK_CLASS_KEY );
				ScenarioTrack track = this.createTrack( trackClassName, trackName );
				track.setName( trackName );
				track.initialize( trackConfig );
				
				this._tracks.put( track._name, track );
			}
		}
		catch ( JSONException e )
		{
			System.out.println( "[SCENARIO] ERROR parsing tracks in JSON configuration file/object. Reason: " + e.toString() );
			e.printStackTrace();
			System.exit( 1 );
		}
		catch( Exception e )
		{
			System.out.println( "[SCENARIO] ERROR initializing tracks. Reason: " + e.toString() );
			e.printStackTrace();
			System.exit( 1 );
		}
	}
	
	/**
	 * Factory method for creating scenario tracks.
	 * 
	 * @param trackClassName    The class of the scenario track to create.
	 * @param trackName         The name of the instantiated track.
	 * @return                  A newly instantiated scenario track.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ScenarioTrack createTrack( String trackClassName, String trackName ) throws Exception
	{
		ScenarioTrack track = null;
		Class<ScenarioTrack> trackClass = (Class<ScenarioTrack>) Class.forName( trackClassName );
		Constructor<ScenarioTrack> trackCtor = trackClass.getConstructor( new Class[] { String.class, Scenario.class } );
		track = (ScenarioTrack) trackCtor.newInstance( new Object[] { trackName, this } );
		return track;
	}
}
