package radlab.rain.workload.gradit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public class GraditOperation extends Operation 
{
	public static String AUTH_TOKEN_PATTERN = "(<input name=\"authenticity_token\" (type=\"hidden\") value=\"(\\S*)\" />)";
	
	// These references will be set by the Generator.
	protected HttpTransport _http;
	protected HashSet<String> _cachedURLs = new HashSet<String>();
	private Random _random = new Random();	
		
	public GraditOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	public GraditGenerator getGenerator()
	{
		return (GraditGenerator) this._generator;
	}
	
	@Override
	public void cleanup() 
	{}

	@Override
	public void execute() throws Throwable 
	{}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		GraditGenerator graditGenerator = (GraditGenerator) generator;
		
		// Refresh the cache to simulate real-world browsing.
		this.refreshCache();
		
		this._http = graditGenerator.getHttpTransport();
		LoadProfile currentLoadProfile = graditGenerator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
	}
	
	/**
	 * Load the static files specified by the URLs if the current request is
	 * not cached and the file was not previously loaded and cached.
	 * 
	 * @param urls      The set of static file URLs.
	 * @return          The number of static files loaded.
	 * 
	 * @throws IOException
	 */
	protected long loadStatics( String[] urls ) throws IOException 
	{
		long staticsLoaded = 0;
		
		for ( String url : urls )
		{
			if ( this._cachedURLs.add( url ) ) 
			{
				this._http.fetchUrl( url );
				staticsLoaded++;
			}
		}
		
		return staticsLoaded;
	}
	
	/**
	 * Refreshes the cache by resetting it 40% of the time.
	 * 
	 * @return      True if the cache was refreshed; false otherwise.
	 */
	protected boolean refreshCache()
	{
		boolean resetCache = ( this._random.nextDouble() < 0.6 ); 
		if ( resetCache )
		{
			this._cachedURLs.clear();
		}
		
		return resetCache;
	}
	
	public String parseAuthTokenRegex( StringBuilder buffer ) throws IOException
	{
		String token = "";
		//System.out.println( buffer.toString() );
		Pattern authTokenPattern = Pattern.compile( AUTH_TOKEN_PATTERN, Pattern.CASE_INSENSITIVE );
		Matcher match = authTokenPattern.matcher( buffer.toString() );
		//System.out.println( "Groups: " + match.groupCount() );
		if( match.find() )
		{
			//System.out.println( buffer.substring( match.start(), match.end()) );
			//System.out.println( match.group(3) );
			token = match.group( 3 );
		}
				
		return token;
	}
	
	// Add all the methods here for viewing the homepage, logging in etc. so that we could
	// reuse them from other operations if necessary
	public String doHomePage() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		//System.out.println( "Starting HomePage" );
		String authToken = "";
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._homeUrl );
		this.trace( this.getGenerator()._homeUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "Home page GET ERROR - Received an empty/error response";
			throw new IOException( errorMessage );
		}
		
		// Gradit doesn't use authenticity tokens
		// authToken = this.parseAuthTokenRegex( response ); 
				
		this.loadStatics( this.getGenerator().homepageStatics );
		this.trace( this.getGenerator().homepageStatics );
		//System.out.println( "HomePage worked" );
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "HomePage (s): " + (end - start)/1000.0 );
		}
		
		return authToken;
	}	
}
