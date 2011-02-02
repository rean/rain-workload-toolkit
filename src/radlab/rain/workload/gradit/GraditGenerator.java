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

package radlab.rain.workload.gradit;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.RainConfig;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

import java.util.LinkedHashSet;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class GraditGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY = "usePooling";
	public static String CFG_DEBUG_KEY		 = "debug";
	public static String CFG_ZOOKEEPER_CONN_STRING		= "zookeeperConnString";
	public static String CFG_ZOOKEEPER_APP_SERVER_PATH	= "zookeeperAppServerPath";
	public static int DEFAULT_APP_SERVER_PORT = 8080;
		
	// Operation indices used in the mix matrix.
	public static final int HOME_PAGE	= 0;
	
	public static final String HOSTNAME_PORT_SEPARATOR	= ":";
	
	
	protected static final String[] HOMEPAGE_STATICS = 
	{
		"/javascripts/application.js?1296239250",
		"/stylesheets/search_home.css?1296239250",
		"/javascripts/prototype.js?1296239250",
		"/javascripts/effects.js?1296239250",
		"/javascripts/dragdrop.js?1296239250",
		"/javascripts/controls.js?1296239250",
	};
	
	// Statics URLs
	public String[] homepageStatics; 
	
	
	// App urls
	public String _baseUrl;
	public String _homeUrl;
	
	
	private HttpTransport _http;
	private Random _rand;
	private NegativeExponential _thinkTimeGenerator  = null;
	private NegativeExponential _cycleTimeGenerator = null;
	private boolean _usePooling = false;
	private boolean _debug = false;
		
	public boolean _isLoggedIn = false;
	private boolean _usingZookeeper = false;
	
	public String _loggedInUser = "";
	public String _homePageAuthToken;
	
	private String[] _appServers = null;
	private int _currentAppServer = 0;
	
	
	public GraditGenerator(ScenarioTrack track) 
	{
		super(track);
		
		//this._baseUrl 	= "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();
		
		this._rand = new Random();
	}

	public boolean getIsLoggedIn()
	{ return this._isLoggedIn; }

	public void setIsLoggedIn( boolean val )
	{ this._isLoggedIn = val; }

	public boolean getIsDebugMode()
	{ return this._debug; }
	
	
	private void initializeUrls( String targetHost, int port )
	{
		this._baseUrl 	= "http://" + targetHost + ":" + port;
		this._homeUrl = this._baseUrl;
		
		/*
		this._createUserUrl = this._baseUrl + "/users/new";
		this._createUserResultUrl = this._baseUrl + "/users";
		
		this._loginUrl = this._baseUrl;
		this._loginResultUrl = this._baseUrl + "/user_session";
				
		this._postThoughtUrlTemplate = this._baseUrl + "/users/%s"; 
		this._postThoughtResultUrlTemplate = this._baseUrl + "/users/%s/thoughts";
	
		this._createSubscriptionResultUrlTemplate = this._baseUrl + "/users/%s/subscriptions";
		
		this._logoutUrl = this._baseUrl + "/logout";
		*/
		this.initializeStaticUrls();
	}

	public void initializeStaticUrls()
	{
		this.homepageStatics    = joinStatics( HOMEPAGE_STATICS );
		/*
		this.loginpageStatics 	= joinStatics( LOGINPAGE_STATICS );
		this.createuserpageStatics = joinStatics( CREATEUSERPAGE_STATICS );
		this.postthoughtpageStatics = joinStatics( POSTTHOUGHTPAGE_STATICS );
		*/
	}
	
	@Override
	public void dispose() 
	{}

	@Override
	public void initialize() 
	{
		this._http = new HttpTransport();
		// Initialize think/cycle time random number generators (if you need/want them)
		this._cycleTimeGenerator = new NegativeExponential( this._cycleTime );
		this._thinkTimeGenerator = new NegativeExponential( this._thinkTime );
	}

	@Override
	public void configure( JSONObject config ) throws JSONException
	{
		if( config.has(CFG_USE_POOLING_KEY) )
			this._usePooling = config.getBoolean( CFG_USE_POOLING_KEY );
		
		if( config.has( CFG_DEBUG_KEY) )
			this._debug = config.getBoolean( CFG_DEBUG_KEY );
		
		GraditScenarioTrack graditTrack = null;
		
		String zkConnString = "";			
		String zkPath = "";
		
		// Get the zookeeper parameter from the RainConfig first. If that doesn't
		// exist then get it from the generator config parameters
		try
		{
			String zkString = RainConfig.getInstance()._zooKeeper; 
			if( zkString != null && zkString.trim().length() > 0 )
				zkConnString = zkString;
			else zkConnString = config.getString( CFG_ZOOKEEPER_CONN_STRING );
				
			zkPath = RainConfig.getInstance()._zkPath;
			if( zkPath == null || zkPath.trim().length() == 0 )
				zkPath = config.getString( CFG_ZOOKEEPER_APP_SERVER_PATH );
		
			// Get the track - see whether it's the "right" kind of track
			if( this._loadTrack instanceof GraditScenarioTrack )
			{
				graditTrack = (GraditScenarioTrack) this._loadTrack;
				graditTrack.configureZooKeeper( zkConnString, zkPath );
				if( graditTrack.isConfigured() )
					this._usingZookeeper = true;
			}		
		}
		catch( JSONException e )
		{
			System.out.println( this + "Error obtaining ZooKeeper info from RainConfig instance or generator paramters. Falling back on targetHost and port." );
			this._usingZookeeper = false;
		}
		
		if( this._usingZookeeper )
		{
			this._appServers = graditTrack.getAppServers();
			// Pick an app server @ random and use that as the target host
			this._currentAppServer = this._rand.nextInt( this._appServers.length );
			
			String[] appServerNamePort = this._appServers[this._currentAppServer].split( HOSTNAME_PORT_SEPARATOR );
			
			if( appServerNamePort.length == 2 )
				this.initializeUrls( appServerNamePort[0], Integer.parseInt( appServerNamePort[1]) );
			else if( appServerNamePort.length == 1 )
				this.initializeUrls( appServerNamePort[0], DEFAULT_APP_SERVER_PORT );
		}
		else 
		{
			String appServer = this.getTrack().getTargetHostName() + HOSTNAME_PORT_SEPARATOR + this.getTrack().getTargetHostPort();
			this._appServers = new String[1];
			this._appServers[0] = appServer;
			this._currentAppServer = 0;
			this.initializeUrls( this.getTrack().getTargetHostName(), this.getTrack().getTargetHostPort() );
		}
	}
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		// Get the current load profile if we need to look inside of it to decide
		// what to do next
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
				
		int nextOperation = -1;
		
		if( lastOperation == -1 )
		{
			nextOperation = 0;
		}
		else
		{
			if( this._usingZookeeper )
			{
				GraditScenarioTrack graditTrack = (GraditScenarioTrack) this._loadTrack;
				if( graditTrack.getAppServerListChanged() )
				{
					// Get new data
					if( graditTrack.updateAppServerList() )
						this._currentAppServer = 0; // Reset the list
				}
				// Always get the list of app servers cached in the track - this doesn't cause a query to
				// ZooKeeper
				this._appServers = graditTrack.getAppServers();
			}
			
			if( this._appServers == null )
			{
				String appServer = this.getTrack().getTargetHostName() + HOSTNAME_PORT_SEPARATOR + this.getTrack().getTargetHostPort();
				this._appServers = new String[1];
				this._appServers[0] = appServer;
				this._currentAppServer = 0;
				this.initializeUrls( this.getTrack().getTargetHostName(), this.getTrack().getTargetHostPort() );
			}
			
			// Pick the new target based on the current app server value
			String nextAppServerHostPort[] = this._appServers[this._currentAppServer].split( HOSTNAME_PORT_SEPARATOR );
			
			if( nextAppServerHostPort.length == 2 )
				this.initializeUrls( nextAppServerHostPort[0], Integer.parseInt( nextAppServerHostPort[1] ) );
			else if( nextAppServerHostPort.length == 1 )
				this.initializeUrls( nextAppServerHostPort[0], DEFAULT_APP_SERVER_PORT );
			
			// Update the current app server value
			this._currentAppServer = (this._currentAppServer + 1) % this._appServers.length;
			
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix( currentLoad.getMixName() ).getSelectionMix();
			double rand = this._rand.nextDouble();
			
			int j;
			for ( j = 0; j < selectionMix.length; j++ )
			{
				if ( rand <= selectionMix[lastOperation][j] )
				{
					break;
				}
			}
			nextOperation = j;
		}
		return getOperation( nextOperation );
	}

	private GraditOperation getOperation( int opIndex )
	{
		// Set opIndex to 0 all the time, for testing until the rest of gRADit operations are specified?
		opIndex = 0;
		
		
		switch( opIndex )
		{
			case HOME_PAGE: return this.createHomePageOperation();
			default: return null;
		}
	}
	
	// Factory methods for creating operations
	public HomePageOperation createHomePageOperation()
	{
		HomePageOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (HomePageOperation) pool.rentObject( HomePageOperation.NAME );	
		}
		
		if( op == null )
			op = new HomePageOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	
	private String[] joinStatics( String[] ... staticsLists ) 
	{
		LinkedHashSet<String> urlSet = new LinkedHashSet<String>();
		
		for ( String[] staticList : staticsLists )
		{
			for ( int i = 0; i < staticList.length; i++ )
			{
				String url = "";
				if( staticList[i].trim().startsWith( "http://" ) )
					url = staticList[i].trim();
				else url = this._baseUrl + staticList[i].trim();
				
				urlSet.add( url );
			}
		}
		
		return (String[]) urlSet.toArray(new String[0]);
	}
	
	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}
	
	@Override
	public long getCycleTime() 
	{
		if( this._cycleTime == 0 )
			return 0;
		else
		{
			// Example cycle time generator
			long nextCycleTime = (long) this._cycleTimeGenerator.nextDouble(); 
			// Truncate at 5 times the mean (arbitrary truncation)
			return Math.min( nextCycleTime, (5*this._cycleTime) );
		}
	}

	@Override
	public long getThinkTime() 
	{	
		if( this._thinkTime == 0 )
			return 0;
		else
		{
			//Example think time generator
			long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
			// Truncate at 5 times the mean (arbitrary truncation)
			return Math.min( nextThinkTime, (5*this._thinkTime) );
		}
	}
}
