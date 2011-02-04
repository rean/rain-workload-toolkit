package radlab.rain.workload.scadr;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.RainConfig;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.LinkedHashSet;
import java.util.Random;

public class ScadrGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY = "usePooling";
	public static String CFG_DEBUG_KEY		 = "debug";
	public static String CFG_ZOOKEEPER_CONN_STRING		= "zookeeperConnString";
	public static String CFG_ZOOKEEPER_APP_SERVER_PATH	= "zookeeperAppServerPath";
	public static int DEFAULT_APP_SERVER_PORT = 8080;
	
	protected static final String[] US_CITIES = 
	{
		"New York",
		"Los Angeles",
		"Chicago",
		"Houston",
		"Phoenix",
		"Philadelphia",
		"San Antonio",
		"San Diego",
		"Dallas",
		"San Jose",
		"Detroit",
		"San Francisco",
		"Jacksonville",
		"Indianapolis",
		"Austin",
		"Columbus",
		"Fort Worth",
		"Charlotte",
		"Memphis",
		"Boston",
		"Baltimore",
		"El Paso",
		"Seattle",
		"Denver",
		"Nashville",
		"Milwaukee",
		"Washington",
		"Las Vegas",
		"Louisville",
		"Portland",
		"Oklahoma City",
		"Tucson",
		"Atlanta",
		"Albuquerque",
		"Kansas City",
		"Fresno",
		"Mesa",
		"Sacramento",
		"Long Beach",
		"Omaha",
		"Virginia Beach",
		"Miami",
		"Cleveland",
		"Oakland",
		"Raleigh",
		"Colorado Springs",
		"Tulsa",
		"Minneapolis",
		"Arlington",
		"Honolulu",
		"Wichita",
		"St. Louis",
		"New Orleans",
		"Tampa",
		"Santa Ana",
		"Anaheim",
		"Cincinnati",
		"Bakersfield",
		"Aurora",
		"Toledo",
		"Pittsburgh",
		"Riverside",
		"Lexington",
		"Stockton",
		"Corpus Christi",
		"Anchorage",
		"St. Paul",
		"Newark",
		"Plano",
		"Buffalo",
		"Henderson",
		"Fort Wayne",
		"Greensboro",
		"Lincoln",
		"Glendale",
		"Chandler",
		"St. Petersburg",
		"Jersey City",
		"Scottsdale",
		"Orlando",
		"Madison",
		"Norfolk",
		"Birmingham",
		"Winston-Salem",
		"Durham",
		"Laredo",
		"Lubbock",
		"Baton Rouge",
		"North Las Vegas",
		"Chula Vista",
		"Chesapeake",
		"Gilbert",
		"Garland",
		"Reno",
		"Hialeah",
		"Arlington",
		"Irvine",
		"Rochester",
		"Akron",
		"Boise",
		"Irving",
		"Fremont",
		"Richmond",
		"Spokane",
		"Modesto",
		"Montgomery",
		"Yonkers",
		"Des Moines",
		"Tacoma",
		"Shreveport",
		"San Bernardino",
		"Fayetteville",
		"Glendale",
		"Augusta",
		"Grand Rapids",
		"Huntington Beach",
		"Mobile",
		"Newport News",
		"Little Rock",
		"Moreno Valley",
		"Columbus",
		"Amarillo",
		"Fontana",
		"Oxnard",
		"Knoxville",
		"Fort Lauderdale",
		"Salt Lake City",
		"Worcester",
		"Huntsville",
		"Tempe",
		"Brownsville",
		"Jackson",
		"Overland Park",
		"Aurora",
		"Oceanside",
		"Tallahassee",
		"Providence",
		"Rancho Cucamonga",
		"Ontario",
		"Chattanooga",
		"Santa Clarita",
		"Garden Grove",
		"Vancouver",
		"Grand Prairie",
		"Peoria",
		"Sioux Falls",
		"Springfield",
		"Santa Rosa",
		"Rockford",
		"Springfield",
		"Salem",
		"Port St. Lucie",
		"Cape Coral",
		"Dayton",
		"Eugene",
		"Pomona",
		"Corona",
		"Alexandria",
		"Joliet",
		"Pembroke Pines",
		"Paterson",
		"Pasadena",
		"Lancaster",
		"Hayward",
		"Salinas",
		"Hampton ",
		"Palmdale",
		"Pasadena",
		"Naperville",
		"Kansas City",
		"Hollywood",
		"Lakewood",
		"Torrance",
		"Escondido",
		"Fort Collins",
		"Syracuse",
		"Bridgeport",
		"Orange",
		"Cary",
		"Elk Grove",
		"Savannah",
		"Sunnyvale",
		"Warren",
		"Mesquite",
		"Fullerton",
		"McAllen",
		"Columbia",
		"Carrollton",
		"Cedar Rapids",
		"McKinney",
		"Sterling Heights",
		"Bellevue",
		"Coral Springs",
		"Waco",
		"Elizabeth",
		"West Valley City",
		"Clarksville",
		"Topeka",
		"Hartford",
		"Thousand Oaks",
		"New Haven",
		"Denton",
		"Concord",
		"Visalia",
		"Olathe",
		"El Monte",
		"Independence",
		"Stamford",
		"Simi Valley",
		"Provo",
		"Killeen",
		"Springfield",
		"Thornton",
		"Abilene",
		"Gainesville",
		"Evansville",
		"Roseville",
		"Charleston",
		"Peoria",
		"Athens ",
		"Lafayette",
		"Vallejo",
		"Lansing",
		"Ann Arbor",
		"Inglewood",
		"Santa Clara",
		"Flint",
		"Victorville",
		"Costa Mesa",
		"Beaumont",
		"Miami Gardens",
		"Manchester",
		"Westminster",
		"Miramar",
		"Norman",
		"Cambridge",
		"Midland",
		"Arvada",
		"Allentown",
		"Elgin",
		"Waterbury",
		"Downey",
		"Clearwater",
		"Billings",
		"West Covina",
		"Round Rock",
		"Murfreesboro",
		"Lewisville",
		"West Jordan",
		"Pueblo",
		"San Buenaventura (Ventura)",
		"Lowell",
		"South Bend",
		"Fairfield",
		"Erie",
		"Rochester",
		"High Point",
		"Richardson",
		"Richmond",
		"Burbank",
		"Berkeley",
		"Pompano Beach",
		"Norwalk",
		"Frisco",
		"Columbia",
		"Gresham",
		"Daly City",
		"Green Bay",
		"Wilmington",
		"Davenport",
		"Wichita Falls",
		"Antioch",
		"Palm Bay",
		"Odessa",
		"Centennial",
		"Boulder",
	};
	
	protected static final String[] HOMEPAGE_STATICS = 
	{
		"/stylesheets/base.css?1296794014",
		"/javascripts/jquery.js?1296577051",
		"/javascripts/jquery-ui.js?1296577051",
		"/javascripts/jrails.js?1296577051",
		"/javascripts/application.js?1296577051"
	};

	protected static final String[] LOGINPAGE_STATICS =
	{
		"/stylesheets/base.css?1296794014",
		"/javascripts/jquery.js?1296577051",
		"/javascripts/jquery-ui.js?1296577051",
		"/javascripts/jrails.js?1296577051",
		"/javascripts/application.js?1296577051"
	};
	
	protected static final String[] CREATEUSERPAGE_STATICS =
	{
		"/stylesheets/base.css?1296794014",
		"/javascripts/jquery.js?1296577051",
		"/javascripts/jquery-ui.js?1296577051",
		"/javascripts/jrails.js?1296577051",
		"/javascripts/application.js?1296577051"
	};

	protected static final String[] POSTTHOUGHTPAGE_STATICS =
	{
		"/stylesheets/base.css?1296794014",
		"/javascripts/jquery.js?1296577051",
		"/javascripts/jquery-ui.js?1296577051",
		"/javascripts/jrails.js?1296577051",
		"/javascripts/application.js?1296577051"
	};
	
	// Statics URLs
	public String[] homepageStatics; 
	public String[] loginpageStatics;
	public String[] createuserpageStatics;
	public String[] postthoughtpageStatics;
		
	// Operation indices - each operation has a unique index 
	/* Test matrix
	25    75     0     0     0     0 Home
     0     0   100     0     0     0 Create user
     0     0    20     5    55    20 Login
    10     0    50     0    20    20 Logout
     0     0     0    10    40    50 Post thought
     0     0     0    20    50    30 Subscribe
     
     Should give us the following steady-state results:
     0.015555301383965 Home (~2%)
     0.011666476037974 Create user (~1%)
     0.087498570284802 Login (~9%)
     0.116664760379736 Logout (~12%)
     0.414331465172151 Post thought (~41%)
     0.354283426741404 Subscribe (~35%)
	 */
	
	public static final int HOME_PAGE			= 0;
	public static final int CREATE_USER			= 1;
	public static final int LOGIN		 		= 2;
	public static final int LOGOUT				= 3;
	public static final int POST_THOUGHT		= 4;
	public static final int CREATE_SUBSCRIPTION = 5;
	
	public static final String HOSTNAME_PORT_SEPARATOR	= ":";
	
	private boolean _usePooling = false;
	private boolean _debug = false;
	
	private HttpTransport _http;
	private Random _rand;
	private NegativeExponential _thinkTimeGenerator  = null;
	private NegativeExponential _cycleTimeGenerator = null;
	
	// App urls
	public String _baseUrl;
	public String _homeUrl;
	public String _createUserUrl;
	public String _createUserResultUrl;
	public String _loginUrl;
	public String _loginResultUrl;
	
	public String _postThoughtUrlTemplate;
	public String _postThoughtResultUrlTemplate;

	public String _createSubscriptionResultUrlTemplate;
	
	public String _logoutUrl;
	
	// Application-specific variables
	private boolean _isLoggedIn = false;
	private boolean _usingZookeeper = false;
	//private ZooKeeper _zconn = null;
	//private boolean _appServerListChanged = false;
	private String[] _appServers = null;
	private int _currentAppServer = 0;
	//private String _zkConnString = "";
	//private String _zkPath = "";
	
	public String _loginAuthToken;
	public String _username;
	
	
	public ScadrGenerator(ScenarioTrack track) 
	{
		super(track);
		this._rand = new Random();
	}

	public void initializeUrls( String targetHost, int port )
	{
		this._baseUrl 	= "http://" + targetHost + ":" + port;
		this._homeUrl = this._baseUrl;
		
		this._createUserUrl = this._baseUrl + "/users/new";
		this._createUserResultUrl = this._baseUrl + "/users";
		
		this._loginUrl = this._baseUrl;
		this._loginResultUrl = this._baseUrl + "/user_session";
				
		this._postThoughtUrlTemplate = this._baseUrl + "/users/%s"; 
		this._postThoughtResultUrlTemplate = this._baseUrl + "/users/%s/thoughts";
	
		this._createSubscriptionResultUrlTemplate = this._baseUrl + "/users/%s/subscriptions";
		
		this._logoutUrl = this._baseUrl + "/logout";
		
		this.initializeStaticUrls();
	}
	
	public boolean getIsLoggedIn()
	{ return this._isLoggedIn; }

	public void setIsLoggedIn( boolean val )
	{ this._isLoggedIn = val; }
	
	public boolean getIsDebugMode()
	{ return this._debug; }
	
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
		
		ScadrScenarioTrack scadrTrack = null;
		
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
			if( this._loadTrack instanceof ScadrScenarioTrack )
			{
				scadrTrack = (ScadrScenarioTrack) this._loadTrack;
				scadrTrack.configureZooKeeper( zkConnString, zkPath );
				if( scadrTrack.isConfigured() )
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
			this._appServers = scadrTrack.getAppServers();
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
	
	public void initializeStaticUrls()
	{
		this.homepageStatics    = joinStatics( HOMEPAGE_STATICS );
		this.loginpageStatics 	= joinStatics( LOGINPAGE_STATICS );
		this.createuserpageStatics = joinStatics( CREATEUSERPAGE_STATICS );
		this.postthoughtpageStatics = joinStatics( POSTTHOUGHTPAGE_STATICS );
	}
	
	/* Pass in index of the last operation */
	
	@Override
	public Operation nextRequest(int lastOperation) {
		
		// Get the current load profile if we need to look inside of it to decide
		// what to do next
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		
		//if( true )
		//	return getOperation( 0 );
		
		int nextOperation = -1;
		
		if( lastOperation == -1 )
		{
			nextOperation = 0;
		}
		else
		{
			if( this._usingZookeeper )
			{
				ScadrScenarioTrack scadrTrack = (ScadrScenarioTrack) this._loadTrack;
				if( scadrTrack.getAppServerListChanged() )
				{
					// Get new data
					if( scadrTrack.updateAppServerList() )
						this._currentAppServer = 0; // Reset the list
				}
				// Always get the list of app servers cached in the track - this doesn't cause a query to
				// ZooKeeper
				this._appServers = scadrTrack.getAppServers();
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
			String nextAppServerHostPort[] = null;
			if( this._currentAppServer >= this._appServers.length )
				this._currentAppServer = 0; // Reset the current application server
			
			if( this._appServers.length == 0 )
			{
				System.out.println( "No app servers available to target. Executing no-op." );
				return null; // no-op
			}
			
			nextAppServerHostPort = this._appServers[this._currentAppServer].split( HOSTNAME_PORT_SEPARATOR );
						
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

	private ScadrOperation getOperation( int opIndex )
	{		
		switch( opIndex )
		{
			case HOME_PAGE: return this.createHomePageOperation();
			case CREATE_USER: return this.createNewUserOperation();
			case LOGIN: return this.createLoginOperation();
			case LOGOUT: return this.createLogoutOperation();
			case CREATE_SUBSCRIPTION: return this.createSubscriptionOperation();
			case POST_THOUGHT: return this.createPostThoughtOperation();
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
	
	public LoginOperation createLoginOperation()
	{
		LoginOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (LoginOperation) pool.rentObject( LoginOperation.NAME );	
		}
		
		if( op == null )
			op = new LoginOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;		
	}

	public CreateUserOperation createNewUserOperation()
	{
		CreateUserOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (CreateUserOperation) pool.rentObject( CreateUserOperation.NAME );	
		}
		
		if( op == null )
			op = new CreateUserOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;		
	}
	
	public CreateSubscriptionOperation createSubscriptionOperation()
	{
		CreateSubscriptionOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (CreateSubscriptionOperation) pool.rentObject( CreateSubscriptionOperation.NAME );	
		}
		
		if( op == null )
			op = new CreateSubscriptionOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	
	public PostThoughtOperation createPostThoughtOperation()
	{
		PostThoughtOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (PostThoughtOperation) pool.rentObject( PostThoughtOperation.NAME );	
		}
		
		if( op == null )
			op = new PostThoughtOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}

	public LogoutOperation createLogoutOperation()
	{
		LogoutOperation op = null;
		
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (LogoutOperation) pool.rentObject( LogoutOperation.NAME );	
		}
		
		if( op == null )
			op = new LogoutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
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
