package radlab.rain.workload.httptest;

import java.io.PrintStream;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

public class FixedUrlGenerator extends Generator 
{
	public static String CFG_USE_POOLING_KEY = "usePooling";
	public static String CFG_MAX_POOL_SIZE_KEY = "maxPoolSize";
	
	// Allow setting of connection and socket timeouts
	public static String CFG_CONNECTION_TIMEOUT	= "connectionTimeoutMsecs";
	public static String CFG_SOCKET_TIMEOUT		= "socketTimeoutMsecs";
	
	// Operation indices used in the mix matrix.
	public static final int PING_URL = 0;
		
	public String _baseUrl;
	private HttpTransport _http;
	private boolean _usePooling = false;
	private boolean _debug = false;
	private NegativeExponential _thinkTimeGenerator  = null;
	private int _connectionTimeoutMsecs = 1000;
	private int _socketTimeoutMsecs = 1000;
		
	public FixedUrlGenerator(ScenarioTrack track) 
	{
		super(track);
		this._baseUrl 	= this._loadTrack.getTargetHostName();
	}

	@Override
	public long getCycleTime() 
	{
		return 0;
	}

	@Override
	public long getThinkTime() 
	{
		if( this._thinkTimeGenerator == null )
			return 0;
		
		long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextThinkTime, (5*this._thinkTime) );
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
		
	/**
	 * Initialize this generator.
	 */
	@Override
	public void initialize()
	{
		this._http = new HttpTransport();
		this._thinkTimeGenerator = new NegativeExponential( this._thinkTime );
	}

	/*
	 * Configure this generator. 
	 */
	@Override
    public void configure( JSONObject config ) throws JSONException
    {
		if( config.has(CFG_USE_POOLING_KEY) )
			this._usePooling = config.getBoolean( CFG_USE_POOLING_KEY );
		
		// Check whether we're setting connection timeouts of using our defaults
		if( config.has(CFG_CONNECTION_TIMEOUT) )
			this._connectionTimeoutMsecs = config.getInt(CFG_CONNECTION_TIMEOUT);
		
		this._http.setConnectTimeout( this._connectionTimeoutMsecs );
		//System.out.println( "Setting connection timeout (msecs): " + this._http.getConnectTimeout() );
		
		// Check whether we're setting socket (idle) timeouts or using our defaults
		if( config.has(CFG_SOCKET_TIMEOUT) )
			this._socketTimeoutMsecs = config.getInt(CFG_SOCKET_TIMEOUT);
		
		this._http.setSocketIdleTimeout( this._socketTimeoutMsecs );
		//System.out.println( "Setting socket timeout (msecs): " + this._http.getSocketIdleTimeout() );
					
		// Print out all the config params we have derived
		if( this._debug )
			this.printConfig( System.out );
    }

	private void printConfig( PrintStream out )
	{
		//out.print( "[Specific URL Op Generator]      : " );
		//out.println( "" );
	}

	@Override
	public void dispose()
	{}
	
	
	@Override
	public Operation nextRequest(int lastOperation) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		// We must save the latest loadprofile if we want the little's law calculation to be done.
		// Latest profile stores the number of users
		this._latestLoadProfile = currentLoad;
		
		return this.createFixedUrlOperation();
	}
	
	private FixedUrlOperation createFixedUrlOperation()
	{
		FixedUrlOperation op = null;
				
		if( this._usePooling )
		{
			ObjectPool pool = this.getTrack().getObjectPool();
			op = (FixedUrlOperation) pool.rentObject( FixedUrlOperation.NAME );	
		}
		
		if( op == null )
			op = new FixedUrlOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	

}
