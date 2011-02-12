package radlab.rain.workload.scadr;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Random;
//import java.util.regex.Pattern;
//import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.IScoreboard;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

import java.security.MessageDigest;
import java.math.BigInteger;

public abstract class ScadrOperation extends Operation 
{
	public static String AUTH_TOKEN_PATTERN = "(<input name=\"authenticity_token\" (type=\"hidden\") value=\"(\\S*)\" />)";
	public static String ALPHABET = "abcdefghijklmnopqrstuvwxyz01234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final int MAX_THOUGHT_LENGTH = 140;
	
	// These references will be set by the Generator.
	protected HttpTransport _http;
	protected HashSet<String> _cachedURLs = new HashSet<String>();
	private Random _random = new Random();	
	
	public ScadrOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	public ScadrGenerator getGenerator()
	{
		return (ScadrGenerator) this._generator;
	}
	
	@Override
	public void cleanup() 
	{}

	@Override
	public void prepare(Generator generator) {
		this._generator = generator;
		ScadrGenerator scadrGenerator = (ScadrGenerator) generator;
		
		// Refresh the cache to simulate real-world browsing.
		this.refreshCache();
		
		this._http = scadrGenerator.getHttpTransport();
		LoadProfile currentLoadProfile = scadrGenerator.getLatestLoadProfile();
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
	
	/*public String parseAuthTokenRegex( StringBuilder buffer ) throws IOException
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
	}*/
	
	/**
	 * Parses an HTML document for an authenticity token used by the Ruby on
	 * Rails framework to authenticate forms.
	 * 
	 * @param buffer    The HTTP response; expected to be an HTML document.
	 * @return          The authenticity token if found; otherwise, null.
	 * 
	 * @throws IOException
	 */
	public String parseAuthTokenSAX( StringBuilder buffer ) throws IOException 
	{
		String token = "";
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			factory.setValidating(false);
			factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
			
			Document document = factory.newDocumentBuilder().parse( new InputSource( new StringReader( buffer.toString() ) ) );
			
			NodeList inputList = document.getElementsByTagName("input");
			for ( int i = 0; i < inputList.getLength(); i++ )
			{
				Element input = (Element) inputList.item(i);
				String name = input.getAttribute("name");
				if ( name.equals("authenticity_token") )
				{
					token = input.getAttribute("value");
					break;
				}
			}
		}
		catch ( Exception e )
		{
			//e.printStackTrace();
		}
		
		return token;
	}
	
	/*
	public static void main( String[] args )
	{
		String input = "<form action=\"/user_session\" class=\"new_user_session\" id=\"user_session_3558\" method=\"post\"><div style=\"margin:0;padding:0;display:inline\"><input name=\"authenticity_token\" type=\"hidden\" value=\"XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=\" /></div>";
		Pattern authTokenPattern = Pattern.compile( "<input name=\"authenticity_token\" (.)* value=\"(.*)\"", Pattern.CASE_INSENSITIVE  );
		Matcher match = authTokenPattern.matcher( input );
		match.find();
		System.out.println( "Groups: " + match.groupCount() );
		System.out.println( match.group(2) );
		
	}*/
	
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
		
		// Get the authenticity token for login and pass it to the generator so that it can
		// be used to log in if necessary
		authToken = "";//this.parseAuthTokenRegex( response ); 
				
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

	public void doCreateUser( String username ) throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		//System.out.println( "Starting CreateUser" );
		// Load the create user page to get the auth token
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._createUserUrl );
		this.trace( this.getGenerator()._createUserUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "Create user page GET ERROR - Received an empty/error response";
			throw new IOException( errorMessage );
		}
		
		// Get the authToken
		/*String authToken = this.parseAuthTokenRegex( response );
		if( authToken == null || authToken.trim().length() == 0 )
			throw new Exception( "Authenticity token not found." );*/
		
		// Load the other statics
		this.loadStatics( this.getGenerator().createuserpageStatics );
		this.trace( this.getGenerator().createuserpageStatics );
		
		// Pick a random city (hometown)- we could implement a hometown hotspot here if we want
		int rndCity = this._random.nextInt( ScadrGenerator.US_CITIES.length );
		String hometown = ScadrGenerator.US_CITIES[rndCity];
		
		String commitAction = "Submit";
				
		/*
		 authenticity_token	XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=
	 	 commit	Save changes
	     user[home_town]	somewhere's ville
		 user[username]	testuser
		 user[password] foo
		 confirm_password foo
		 */
		
		// Post the to create user results url
		HttpPost httpPost = new HttpPost( this.getGenerator()._createUserResultUrl );
		// Weird things happen if we don't specify HttpMultipartMode.BROWSER_COMPATIBLE.
		// Scadr rejects the auth token as invalid without it. Not sure if this is a 
		// Scadr-specific issue or not. HTTP POSTs by the Olio (Ruby web-app) driver 
		// worked without it. 
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "user[home_town]", new StringBody( hometown ) );
		entity.addPart( "user[username]", new StringBody( username ) );
		entity.addPart( "user[plain_password]", new StringBody( username ) );
		entity.addPart( "user[confirm_password]", new StringBody( username ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		response = this._http.fetch( httpPost );
		
		//System.out.println( response );
		
		// Look at the response for the string 'Your account "<username>" has been created!'
		StringBuilder successMessage = new StringBuilder();
		successMessage.append( "Your account \"" );
		successMessage.append( username.toString() );
		successMessage.append( "\" has been created!" );
		
		if(! (response.toString().contains( successMessage.toString() ) ) )
			throw new Exception( "Creating new user: " + username.toString() + " failed!" );
		
		this.trace( this.getGenerator()._createUserResultUrl );
		//System.out.println( "CreateUser worked" );
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "CreateUser (s): " + (end - start)/1000.0 );
		}
	}
	
	public void doCreateUser() throws Exception
	{
		// If we're already logged in then skip the creation step
		if( this.getGenerator().getIsLoggedIn() )
		{
			//System.out.println( "**************Already logged in**********." );
			//System.out.println( "Skipping user creation since we're already logged in." );
			return;
		}
		//else System.out.println( "Not logged in." );
		
		// Make up a username - just user-<generatedBy>
		String username = this.getUsername(); 
		this.doCreateUser( username );
		// Save the username in the generator
		this.getGenerator()._username = username;
	}
	
	public boolean doLogin() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		//System.out.println( "Starting Login" );
						
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._loginUrl );
		this.trace( this.getGenerator()._loginUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "";
			if( response != null )
				errorMessage = "Login page GET ERROR - Received an empty/error response. URL: " + this.getGenerator()._loginUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString();
			else errorMessage = "Login page GET ERROR - Received an empty/error response. URL: " + this.getGenerator()._loginUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: NULL";
			throw new IOException( errorMessage );
		}
		
		// Get the authToken
		/*String authToken = this.parseAuthTokenRegex( response );
		if( authToken == null || authToken.trim().length() == 0 )
			throw new Exception( "Authenticity token not found." );*/
		
		// Load the other statics
		this.loadStatics( this.getGenerator().loginpageStatics );
		this.trace( this.getGenerator().loginpageStatics );
				
		// http://localhost:3000/user_session
		/*
		 authenticity_token	XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=
		commit	Login
		user_session[username]	user-
		user_session[password]	user-
		 */
		
		// Get the username from the generator - if it's not there then make up one
		String username = this.getGenerator()._username;
		if( username == null || username.trim().length() == 0 )
			username = this.getUsername();
		
		String commitAction = "Login";
		
		HttpPost httpPost = new HttpPost( this.getGenerator()._loginResultUrl );
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "user_session[username]", new StringBody( username ) );
		entity.addPart( "user_session[password]", new StringBody( username ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		response = this._http.fetch( httpPost );
		
		// See whether we were logged in - if not doCreateUser and try again
		StringBuilder successMessage = new StringBuilder();
		successMessage.append( "Logged in as " ).append( username.toString() );
		
		// Return true if we're able to log in
		if( response.toString().contains( successMessage.toString() ) )
		{
			//System.out.println( "Login worked" );
			
			if( debug )
			{
				end = System.currentTimeMillis();
				System.out.println( "Login (s): " + (end - start)/1000.0 );
			}
			
			return true;
		}
		else throw new Exception( "Error unable to log in." );
	}
	
	public void doPostThought() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		//System.out.println( "Starting PostThought" );
		boolean result = false;
		// See whether we're logged in - if not try to log in
		if( !this.getGenerator().getIsLoggedIn() )
			result = this.doLogin();
		else result = true;
				
		if( !result )
			throw new Exception( "Error logging in. Can't post thought." );
		
		String username = this.getGenerator()._username;
		if( username == null || username.trim().length() == 0 )
			username = this.getUsername();
		
			// Fetch the post thoughts page
		String postThoughtUrl = String.format( this.getGenerator()._postThoughtUrlTemplate, username );
		StringBuilder response = this._http.fetchUrl( postThoughtUrl );
		this.trace( postThoughtUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "PostThought page GET ERROR - Received an empty/error response";
			throw new IOException( errorMessage );
		}
		
		// Get the auth token
		/*String authToken = this.parseAuthTokenRegex( response );
		if( authToken == null || authToken.trim().length() == 0 )
			throw new Exception( "Authenticity token not found." );*/
		
		// Load the other statics
		this.loadStatics( this.getGenerator().postthoughtpageStatics );
		this.trace( this.getGenerator().postthoughtpageStatics );
		
		/*
		 authenticity_token	XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=
		commit	Think
		thought[text]	thought 1
		 */
		
		String commitAction = "Think";
		String thought = this.getThought();
		
				
		String postThoughtResultUrl = String.format( this.getGenerator()._postThoughtResultUrlTemplate, username ); 
		
		HttpPost httpPost = new HttpPost( postThoughtResultUrl );
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "thought[text]", new StringBody( thought ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		response = this._http.fetch( httpPost );
		
		//String postBody = "authenticity_token=" + authToken + "&commit=" + commitAction + "&thought[text]=" + thought; 
		//response = this._http.fetchUrl( postThoughtResultUrl, postBody );
			
		String successMessage = "New thought created.";
		
		if( !response.toString().contains( successMessage.toString() ) )
		{
			throw new Exception( "Unable to create new thought." + " Logged in status: " + this.getGenerator().getIsLoggedIn() + " URL: " + postThoughtResultUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
		}
		else 
		{
			//System.out.println( "PostThought worked." );
			if( debug )
			{
				end = System.currentTimeMillis();
				System.out.println( "PostThought (s): " + (end - start)/1000.0 );
			}
		}
	}

	public void doLogout() throws Exception
	{
		if( !this.getGenerator().getIsLoggedIn() )
			return;
		
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._logoutUrl );
		this.trace( this.getGenerator()._logoutUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "";
			if( response != null )
				errorMessage = "Logout page GET ERROR - Received an empty/error response. " + " Logged in status: " + this.getGenerator().getIsLoggedIn() + " URL: " + this.getGenerator()._logoutUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response length: " + response.length();
			else errorMessage = "Logout page GET ERROR - Received an empty/error response. " + " Logged in status: " + this.getGenerator().getIsLoggedIn() +" URL: " + this.getGenerator()._logoutUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response length: 0/NULL";
			throw new IOException( errorMessage );
		}
		
		String successMessage = "You are now logged out.";
		if( !response.toString().contains( successMessage.toString() ) )
			throw new Exception( "Unable to log out. Response: " + response.toString() );
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "Logout (s): " + (end - start)/1000.0 );
		}
		
		// Update the generator to indicate that we've logged out
		this.getGenerator().setIsLoggedIn( false );
	}
	
	public void doSubscribe( boolean createTargetUser ) throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
				
		//System.out.println( "Starting Subscribe" );
		boolean result = false;
		// If we're not logged in, try to log in
		if( !this.getGenerator().getIsLoggedIn() )
			result = this.doLogin();
		else result = true;
		
		if( !result )
			throw new Exception( "Error logging in. Can't subscribe to user." );
		
		String me = this.getGenerator()._username;
		if( me == null || me.trim().length() == 0 )
			me = this.getUsername();
		
		// Randomly pick a target based on the number of users running in this track.
		// We can implement a data hotspot here if we want some users more sought after to follow.
		String targetUser = me;
		// We can't subscribe to ourselves, so keep trying to pick someone else
		while( targetUser.equals( me ) )
			targetUser = this.subscribeToUser( this.getGenerator().getTrack().getMaxUsers() );
				
		String targetUserUrl = String.format( this.getGenerator()._postThoughtUrlTemplate, targetUser );
		// Do a get for that user - look for a subscribe button
		StringBuilder response = this._http.fetchUrl( targetUserUrl );
		this.trace( targetUserUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 || response.toString().contains( "does not exist" ) )
		{
			// Create user if they don't exist
			if( !createTargetUser )
			{
				String errorMessage = "Subscribe to user page GET ERROR - Received an empty/error response";
				throw new IOException( errorMessage );
			}
			else 
			{
				//System.out.println( "Creating target user: " + targetUser + " so we can subscribe to their thoughtstream." );
				this.doCreateUser( targetUser );
			}
		}
		
		// Get the auth token
		/*
		String authToken = this.parseAuthTokenRegex( response );
		if( authToken == null || authToken.trim().length() == 0 )
			throw new Exception( "Authenticity token not found." );*/
		
		// Load the other statics
		this.loadStatics( this.getGenerator().postthoughtpageStatics );
		this.trace( this.getGenerator().postthoughtpageStatics );
		
		/*
		 	authenticity_token	XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=
			commit	Subscribe
			subscription[target]	foo 
		*/
		
		String commitAction = "Subscribe";
				
		String createSubscriptionResultUrl = String.format( this.getGenerator()._createSubscriptionResultUrlTemplate, me ); 
		HttpPost httpPost = new HttpPost( createSubscriptionResultUrl );
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "subscription[target]", new StringBody( targetUser ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		response = this._http.fetch( httpPost );
		
		String successMessage1 = "Subscribed to " + targetUser;
		String successMessage2 = "You are subscribed to " + targetUser;
		
		if( response.toString().contains( successMessage1.toString() ) || 
			response.toString().contains( successMessage2.toString() ) )
		{
			//System.out.println( "Subscribe worked" );
			if( debug )
			{
				end = System.currentTimeMillis();
				System.out.println( "Subscribe (s): " + (end - start)/1000.0 );
			}
			return;
		}
		else 
		{
			if( response != null )
				throw new Exception( "Unable to subscribe to user: " + targetUser + " Logged in status: " + this.getGenerator().getIsLoggedIn() + " URL: " + createSubscriptionResultUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
			else throw new Exception( "Unable to subscribe to user: " + targetUser + " Logged in status: " + this.getGenerator().getIsLoggedIn() + " URL: " + createSubscriptionResultUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: NULL" );
		}
	}
	
	private String subscribeToUser( int numberOfUsers )
	{
		if( numberOfUsers == 1 )
			return "fakeUser";
		
		int rndTarget = this._random.nextInt( numberOfUsers );
		StringBuilder target = new StringBuilder();
		target.append( "user-").append( this.getGenerator().getTrack().getName() ).append( "-Generator-" ).append( rndTarget );
		return distributeUserName(target.toString());
	}
	
	private String getUsername()
	{
		// User names can't have any periods in them
	  return distributeUserName("user-" + this._generatedBy.replace( '.', '-'));
	}

  //Append a hash to the begining of usernames so they distribute better
  private String distributeUserName(String username)
  {
    //Stupid checked exceptions
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(username.getBytes(), 0, username.length());
      byte[] md5Sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5Sum);
      return bigInt.toString(16) + username;
    }
    catch (Exception e) {
      System.out.println("CANT CALCULATE HASH using undistributed username");
      return username;
    }
  }
	
	private String getThought()
	{
		StringBuilder thought = new StringBuilder();
		int maxCount = ScadrOperation.MAX_THOUGHT_LENGTH;
		
		// Pick elements randomly from the alphabet - every 3 characters flip a coin on inserting
		// a space
		while( maxCount > 0 )
		{
			char rndChar = ScadrOperation.ALPHABET.charAt( this._random.nextInt( ScadrOperation.ALPHABET.length() ) );
			if( maxCount % 3 == 0 )
			{
				if( this._random.nextDouble() < 0.3 )
					thought.append( " " );
				else thought.append( rndChar );
			}
			else thought.append( rndChar );
			
			maxCount--;
		}
		
		return thought.toString();
	}
}
