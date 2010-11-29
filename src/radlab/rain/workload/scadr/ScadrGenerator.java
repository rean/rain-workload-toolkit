package radlab.rain.workload.scadr;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.LinkedHashSet;
import java.util.Random;

@SuppressWarnings("unused")
public class ScadrGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY = "usePooling";
	
	// Statics URLs
	public String[] homepageStatics; 
	
	protected static final String[] HOMEPAGE_STATICS = 
	{
		"/stylesheets/base.css?1290059762",
		"http://fonts.googleapis.com/css?family=Lobster"
		// Url below considered invalid
		//"http://fonts.googleapis.com/css?family=Lobster|Nobile"
	};
	
	// Operation indices - each operation has a unique index 
	public static final int HOME_PAGE			= 0;
	public static final int CREATE_USER			= 1;
	public static final int LOGIN		 		= 2;
	public static final int LOGOUT				= 3;
	public static final int POST_THOUGHT		= 4;
	public static final int CREATE_SUBSCRIPTION = 5;
		
	private boolean _usePooling = false;
	
	private HttpTransport _http;
	private Random _rand = new Random();
	private NegativeExponential _thinkTimeGenerator  = null;
	private NegativeExponential _cycleTimeGenerator = null;
	
	// Application-specific variables
	private boolean _isLoggedIn = false;
		
	
	public String _baseUrl;
	public String _homeUrl;
	public String _loginAuthToken;
	
	public ScadrGenerator(ScenarioTrack track) 
	{
		super(track);
		this._baseUrl 	= "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();
		this._homeUrl = this._baseUrl;
		this.initializeStaticUrls();
	}

	public boolean getIsLoggedIn()
	{ return this._isLoggedIn; }
	
	@Override
	public void dispose() 
	{}

	@Override
	public long getCycleTime() {
		/* Example cycle time generator
		long nextCycleTime = (long) this._cycleTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextCycleTime, (5*this._cycleTime) );
		*/
		
		return 0; // No pause
	}

	@Override
	public long getThinkTime() {
		
		/* Example think time generator
		 long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		 // Truncate at 5 times the mean (arbitrary truncation)
		 return Math.min( nextThinkTime, (5*this._thinkTime) );
		 */	
		return 0; // No think time
	}

	@Override
	public void initialize() {
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
	}
	
	public void initializeStaticUrls()
	{
		this.homepageStatics    = joinStatics( HOMEPAGE_STATICS );
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
	
	
	/* Pass in index of the last operation */
	
	@Override
	public Operation nextRequest(int lastOperation) {
		
		// Get the current load profile if we need to look inside of it to decide
		// what to do next
		ScadrLoadProfile currentLoad = (ScadrLoadProfile) this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		
		// Pick a random number between 0 and 3
		int nextOpIndex = Math.abs( this._rand.nextInt() ) % 4;
		
		Operation op = this.getOperation( nextOpIndex );
		return op;
	}

	private ScadrOperation getOperation( int opIndex )
	{
		// We know about 4 high-level Cloudstone operations
		/*public static final int LOGIN		 		= 0;
		public static final int CREATE_SUBSCRIPTION = 1;
		public static final int CREATE_THOUGHT		= 2;
		public static final int READ_THOUGHTSTREAM	= 3;*/
		
		switch( opIndex )
		{
			case HOME_PAGE: return this.createHomePageOperation(); 
			case LOGIN: return this.createLoginOperation();
			case CREATE_SUBSCRIPTION: return this.createSubscriptionOperation();
			//case CREATE_THOUGHT: return this.createThoughtOperation();
			//case READ_THOUGHTSTREAM: return this.createReadThoughtstreamOperation();
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
		LoginOperation op = new LoginOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;		
	}

	public CreateSubscriptionOperation createSubscriptionOperation()
	{
		CreateSubscriptionOperation op = new CreateSubscriptionOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public PostThoughtOperation createThoughtOperation()
	{
		PostThoughtOperation op = new PostThoughtOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;		
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
}
