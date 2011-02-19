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

public class GraditGenerator extends Generator {
	public static String CFG_USE_POOLING_KEY = "usePooling";
	public static String CFG_DEBUG_KEY = "debug";
	public static String CFG_ZOOKEEPER_CONN_STRING = "zookeeperConnString";
	public static String CFG_ZOOKEEPER_APP_SERVER_PATH = "zookeeperAppServerPath";
	public static int DEFAULT_APP_SERVER_PORT = 8080;

	protected static final String[] HOMEPAGE_STATICS = {
	/*
	 * "/stylesheets/base.css?1296794014", "/javascripts/jquery.js?1296577051",
	 * "/javascripts/jquery-ui.js?1296577051",
	 * "/javascripts/jrails.js?1296577051",
	 * "/javascripts/application.js?1296577051"
	 */
	};

	protected static final String[] LOGINPAGE_STATICS = {
	/*
	 * "/stylesheets/base.css?1296794014", "/javascripts/jquery.js?1296577051",
	 * "/javascripts/jquery-ui.js?1296577051",
	 * "/javascripts/jrails.js?1296577051",
	 * "/javascripts/application.js?1296577051"
	 */
	};

	protected static final String[] CREATEUSERPAGE_STATICS = {
	/*
	 * "/stylesheets/base.css?1296794014", "/javascripts/jquery.js?1296577051",
	 * "/javascripts/jquery-ui.js?1296577051",
	 * "/javascripts/jrails.js?1296577051",
	 * "/javascripts/application.js?1296577051"
	 */
	};

	protected static final String[] DASHBOARD_STATICS = {
	/*
	 * "/stylesheets/base.css?1296794014", "/javascripts/jquery.js?1296577051",
	 * "/javascripts/jquery-ui.js?1296577051",
	 * "/javascripts/jrails.js?1296577051",
	 * "/javascripts/application.js?1296577051"
	 */
	};

	// Statics URLs
	public String[] homepageStatics;
	public String[] loginpageStatics;
	public String[] registeruserpageStatics;
	public String[] dashboardStatics;

	// Operation indices - each operation has a unique index
	/*
	 * 50 50 0 0 0 0 HomePage 0 0 0 100 0 0 Register 0 0 40 50 0 10 Dashboard 0
	 * 0 15 80 0 5 StartGame 0 0 0 80 0 20 Login 30 0 20 10 40 0 Logout
	 * 
	 * We should get the following steady-state results 0.034460753031271
	 * HomePage (~3%) 0.017230376515635 Register (~2%) 0.188895979578818
	 * Dashboard (~19%) 0.679004467134670 StartGame (~68%) 0.022973835354181
	 * Login (~2%) 0.057434588385451 Logout (~6%)
	 */

	public static final int HOME_PAGE = 0;
	public static final int REGISTER_USER = 1;
	public static final int DASHBOARD = 2;
	public static final int START_GAME = 3;
	public static final int LOGIN = 4;
	public static final int LOGOUT = 5;

	public static final String HOSTNAME_PORT_SEPARATOR = ":";

	private boolean _usePooling = false;
	private boolean _debug = false;

	private HttpTransport _http;
	private Random _rand;
	private NegativeExponential _thinkTimeGenerator = null;
	private NegativeExponential _cycleTimeGenerator = null;

	// App urls
	public String _baseUrl;
	public String _homeUrl;
	public String _registerUserUrl;
	public String _createUserUrl;
	public String _loginUrl;
	public String _dashboardUrl;

	public String _logoutUrl;

	// Application-specific variables
	private boolean _isLoggedIn = false;
	private boolean _usingZookeeper = false;
	private String[] _appServers = null;
	private int _currentAppServer = 0;

	// public String _loginAuthToken;
	public String _username;

	// Keep track of every app server we talk to so we can check whether the
	// load
	// is being rotated like we expect
	private String _lastAppServer = "";

	public GraditGenerator(ScenarioTrack track) {
		super(track);
		this._rand = new Random();
		//System.out.println( "Think time ctor: " + track.getMeanThinkTime() );
		this._thinkTime = (long)(track.getMeanThinkTime() * 1000);
		this._cycleTime = (long)(track.getMeanCycleTime() * 1000);
	}

	public void initializeUrls(String targetHost, int port) {
		this._baseUrl = "http://" + targetHost + ":" + port;
		this._homeUrl = this._baseUrl;

		this._registerUserUrl = this._baseUrl + "/register";
		this._createUserUrl = this._baseUrl + "/users/create";

		this._dashboardUrl = this._baseUrl + "/dashboard";

		this._loginUrl = this._baseUrl + "/users/login_action";

		this._logoutUrl = this._baseUrl + "/logout";

		this.initializeStaticUrls();
	}

	public boolean getIsLoggedIn() {
		return this._isLoggedIn;
	}

	public void setIsLoggedIn(boolean val) {
		this._isLoggedIn = val;
	}

	public boolean getIsDebugMode() {
		return this._debug;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void initialize() {
		this._http = new HttpTransport();
		// Set the redirect limit to 5 for Gradit
		this._http.setRedirectLimit( 5 );
	}

	@Override
	public void configure(JSONObject config) throws JSONException {
		if (config.has(CFG_USE_POOLING_KEY))
			this._usePooling = config.getBoolean(CFG_USE_POOLING_KEY);

		if (config.has(CFG_DEBUG_KEY))
			this._debug = config.getBoolean(CFG_DEBUG_KEY);

		GraditScenarioTrack graditTrack = null;

		String zkConnString = "";
		String zkPath = "";

		// Get the zookeeper parameter from the RainConfig first. If that
		// doesn't
		// exist then get it from the generator config parameters
		try {
			String zkString = RainConfig.getInstance()._zooKeeper;
			if (zkString != null && zkString.trim().length() > 0)
				zkConnString = zkString;
			else
				zkConnString = config.getString(CFG_ZOOKEEPER_CONN_STRING);

			zkPath = RainConfig.getInstance()._zkPath;
			if (zkPath == null || zkPath.trim().length() == 0)
				zkPath = config.getString(CFG_ZOOKEEPER_APP_SERVER_PATH);

			// Get the track - see whether it's the "right" kind of track
			if (this._loadTrack instanceof GraditScenarioTrack) {
				graditTrack = (GraditScenarioTrack) this._loadTrack;
				graditTrack.configureZooKeeper(zkConnString, zkPath);
				if (graditTrack.isConfigured())
					this._usingZookeeper = true;
			}
		} catch (JSONException e) {
			System.out
					.println(this
							+ "Error obtaining ZooKeeper info from RainConfig instance or generator paramters. Falling back on targetHost and port.");
			this._usingZookeeper = false;
		}

		if (this._usingZookeeper) {
			this._appServers = graditTrack.getAppServers();
			// Pick an app server @ random and use that as the target host
			this._currentAppServer = this._rand
					.nextInt(this._appServers.length);

			String[] appServerNamePort = this._appServers[this._currentAppServer]
					.split(HOSTNAME_PORT_SEPARATOR);

			if (appServerNamePort.length == 2)
				this.initializeUrls(appServerNamePort[0], Integer
						.parseInt(appServerNamePort[1]));
			else if (appServerNamePort.length == 1)
				this.initializeUrls(appServerNamePort[0],
						DEFAULT_APP_SERVER_PORT);
		} else {
			String appServer = this.getTrack().getTargetHostName()
					+ HOSTNAME_PORT_SEPARATOR
					+ this.getTrack().getTargetHostPort();
			this._appServers = new String[1];
			this._appServers[0] = appServer;
			this._currentAppServer = 0;
			this.initializeUrls(this.getTrack().getTargetHostName(), this
					.getTrack().getTargetHostPort());
		}

		// Initialize think/cycle time random number generators (if you
		// need/want them)
		//System.out.println("Think time: " + this._thinkTime);
		this._cycleTimeGenerator = new NegativeExponential( this._cycleTime );
		this._thinkTimeGenerator = new NegativeExponential( this._thinkTime );
	}

	public void initializeStaticUrls() {
		this.homepageStatics = joinStatics(HOMEPAGE_STATICS);
		this.loginpageStatics = joinStatics(LOGINPAGE_STATICS);
		this.registeruserpageStatics = joinStatics(CREATEUSERPAGE_STATICS);
		this.dashboardStatics = joinStatics(DASHBOARD_STATICS);
	}

	/* Pass in index of the last operation */

	@Override
	public Operation nextRequest(int lastOperation) {

		// Get the current load profile if we need to look inside of it to
		// decide
		// what to do next
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;

		// if( true )
		// return getOperation( 0 );

		int nextOperation = -1;

		if (lastOperation == -1) {
			nextOperation = 0;
		} else {
			if (this._usingZookeeper) {
				GraditScenarioTrack graditTrack = (GraditScenarioTrack) this._loadTrack;
				if (graditTrack.getAppServerListChanged()) {
					// Get new data
					if (graditTrack.updateAppServerList())
						this._currentAppServer = 0; // Reset the list
				}
				// Always get the list of app servers cached in the track - this
				// doesn't cause a query to
				// ZooKeeper
				this._appServers = graditTrack.getAppServers();
			}

			if (this._appServers == null) {
				String appServer = this.getTrack().getTargetHostName()
						+ HOSTNAME_PORT_SEPARATOR
						+ this.getTrack().getTargetHostPort();
				this._appServers = new String[1];
				this._appServers[0] = appServer;
				this._currentAppServer = 0;
				this.initializeUrls(this.getTrack().getTargetHostName(), this
						.getTrack().getTargetHostPort());
			}

			// Pick the new target based on the current app server value
			String nextAppServerHostPort[] = null;
			if (this._currentAppServer >= this._appServers.length)
				this._currentAppServer = 0; // Reset the current application
											// server

			if (this._appServers.length == 0) {
				System.out
						.println("No app servers available to target. Executing no-op.");
				return null; // no-op
			}

			nextAppServerHostPort = this._appServers[this._currentAppServer]
					.split(HOSTNAME_PORT_SEPARATOR);

			if (nextAppServerHostPort.length == 2)
				this.initializeUrls(nextAppServerHostPort[0], Integer
						.parseInt(nextAppServerHostPort[1]));
			else if (nextAppServerHostPort.length == 1)
				this.initializeUrls(nextAppServerHostPort[0],
						DEFAULT_APP_SERVER_PORT);

			// Check whether the current app server is the same as the previous
			// app server
			if (this._debug) {
				// System.out.println( this + " " + this._appServers.length +
				// " app servers found." );

				if (this._appServers.length > 1
						&& nextAppServerHostPort[0]
								.equalsIgnoreCase(this._lastAppServer))
					System.out.println(this + " no app server rotation");
				/*
				 * else { System.out.println( this +
				 * " app server rotation. Prev: " + this._lastAppServer +
				 * " current: " + nextAppServerHostPort[0] ); }
				 */

				// Save the app server being targeted now
				this._lastAppServer = nextAppServerHostPort[0];
			}

			// Update the current app server value
			this._currentAppServer = (this._currentAppServer + 1)
					% this._appServers.length;

			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(
					currentLoad.getMixName()).getSelectionMix();
			double rand = this._rand.nextDouble();

			int j;
			for (j = 0; j < selectionMix.length; j++) {
				if (rand <= selectionMix[lastOperation][j]) {
					break;
				}
			}
			nextOperation = j;
		}
		return getOperation(nextOperation);
	}

	private GraditOperation getOperation(int opIndex) {
		switch (opIndex) {
		case HOME_PAGE:
			return this.createHomePageOperation();
		case REGISTER_USER:
			return this.createRegisterUserOperation();
		case LOGIN:
			return this.createLoginOperation();
		case LOGOUT:
			return this.createLogoutOperation();
		case DASHBOARD:
			return this.createDashboardOperation();
		case START_GAME:
			return this.createStartGameOperation();
		default:
			return null;
		}
	}

	// Factory methods for creating operations
	public HomePageOperation createHomePageOperation() {
		HomePageOperation op = null;

		if (this._usePooling) {
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (HomePageOperation) pool.rentObject(HomePageOperation.NAME);
		}

		if (op == null)
			op = new HomePageOperation(this.getTrack().getInteractive(), this
					.getScoreboard());

		op.prepare(this);
		return op;
	}

	public LoginOperation createLoginOperation() {
		LoginOperation op = null;

		if (this._usePooling) {
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (LoginOperation) pool.rentObject(LoginOperation.NAME);
		}

		if (op == null)
			op = new LoginOperation(this.getTrack().getInteractive(), this
					.getScoreboard());

		op.prepare(this);
		return op;
	}

	public RegisterUserOperation createRegisterUserOperation() {
		RegisterUserOperation op = null;

		if (this._usePooling) {
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (RegisterUserOperation) pool
					.rentObject(RegisterUserOperation.NAME);
		}

		if (op == null)
			op = new RegisterUserOperation(this.getTrack().getInteractive(),
					this.getScoreboard());

		op.prepare(this);
		return op;
	}

	public LogoutOperation createLogoutOperation() {
		LogoutOperation op = null;

		if (this._usePooling) {
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (LogoutOperation) pool.rentObject(LogoutOperation.NAME);
		}

		if (op == null)
			op = new LogoutOperation(this.getTrack().getInteractive(), this
					.getScoreboard());

		op.prepare(this);
		return op;
	}

	public DashboardOperation createDashboardOperation() {
		DashboardOperation op = null;

		if (this._usePooling) {
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (DashboardOperation) pool.rentObject(DashboardOperation.NAME);
		}

		if (op == null)
			op = new DashboardOperation(this.getTrack().getInteractive(), this
					.getScoreboard());

		op.prepare(this);
		return op;
	}

	public StartGameOperation createStartGameOperation() {
		StartGameOperation op = null;

		if (this._usePooling) {
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (StartGameOperation) pool.rentObject(StartGameOperation.NAME);
		}

		if (op == null)
			op = new StartGameOperation(this.getTrack().getInteractive(), this
					.getScoreboard());

		op.prepare(this);
		return op;
	}

	private String[] joinStatics(String[]... staticsLists) {
		LinkedHashSet<String> urlSet = new LinkedHashSet<String>();

		for (String[] staticList : staticsLists) {
			for (int i = 0; i < staticList.length; i++) {
				String url = "";
				if (staticList[i].trim().startsWith("http://"))
					url = staticList[i].trim();
				else
					url = this._baseUrl + staticList[i].trim();

				urlSet.add(url);
			}
		}

		return (String[]) urlSet.toArray(new String[0]);
	}

	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return An HTTP transport.
	 */
	public HttpTransport getHttpTransport() {
		return this._http;
	}

	@Override
	public long getCycleTime() {
		if (this._cycleTime == 0)
			return 0;
		else {
			// Example cycle time generator
			long nextCycleTime = (long) this._cycleTimeGenerator.nextDouble();
			// Truncate at 5 times the mean (arbitrary truncation)
			return Math.min(nextCycleTime, (5 * this._cycleTime));
		}
	}

	@Override
	public long getThinkTime() {
		if (this._thinkTime == 0)
			return 0;
		else {
			// Example think time generator
			long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble();
			// Truncate at 5 times the mean (arbitrary truncation)
			return Math.min(nextThinkTime, (5 * this._thinkTime));
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[").append(this._name).append("]");
		return buf.toString();
	}

}
