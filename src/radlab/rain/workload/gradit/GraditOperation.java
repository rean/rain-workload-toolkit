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

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public class GraditOperation extends Operation 
{
	public static String AVAILABLE_WORDLIST_URL_PREFIX = "/games/new_game/0?wordlist=";
	public static String UNFINISHED_GAME_URL_PREFIX = "/games/game_entry/";
	
	public static String AUTH_TOKEN_PATTERN = "(<input name=\"authenticity_token\" (type=\"hidden\") value=\"(\\S*)\" />)";
	public static String AVAILABLE_WORDLIST_PATTERN = "(<a href=\"/games/new_game/0\\?wordlist=(\\w+)\">)";
	public static String UNFINISHED_GAME_PATTERN = "(<a href=\"/games/game_entry/(\\d+)\">)";
	
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
						
		this.loadStatics( this.getGenerator().homepageStatics );
		this.trace( this.getGenerator().homepageStatics );
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "HomePage (s): " + (end - start)/1000.0 );
		}
		
		return authToken;
	}
	
	public void doRegisterUser() throws Exception
	{
		if( this.getGenerator().getIsLoggedIn() )
		{
			//System.out.println( "**************Already logged in**********." );
			//System.out.println( "Skipping user creation since we're already logged in." );
			return;
		}
		
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		// 1) Fetch the register user page
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._registerUserUrl );
		if( response == null || response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "";
			if( response != null )
				errorMessage = "RegisterUser page GET ERROR - Received an empty/error response. URL: " + this.getGenerator()._registerUserUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString();
			else errorMessage = "RegisterUser page GET ERROR - Received an empty/error response. URL: " + this.getGenerator()._registerUserUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: NULL";
			throw new IOException( errorMessage );
		}		
		
		// 1b) Get the register user page statics
		this.loadStatics( this.getGenerator().registeruserpageStatics );
		
		// 2) Post to the create user url
		// Make up a username - just user-<generatedBy>
		String username = this.getUsername(); 
		this.doCreateUser( username );
		// Save the username in the generator
		this.getGenerator()._username = username;
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "RegisterUser (s): " + (end - start)/1000.0 );
		}
	}

	private void doCreateUser( String username ) throws Exception
	{	
		// Do the post to the create user url
		/*
		commit	Sign up
		login	testuser
		name	testuser
		password	testuser
		*/

		String commitAction = "Sign up";
		// Post the to create user results url
		HttpPost httpPost = new HttpPost( this.getGenerator()._createUserUrl );
		// Weird things happen if we don't specify HttpMultipartMode.BROWSER_COMPATIBLE.
		// Scadr rejects the auth token as invalid without it. Not sure if this is a 
		// Scadr-specific issue or not. HTTP POSTs by the Olio (Ruby web-app) driver 
		// worked without it. 
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "login", new StringBody( username ) );
		entity.addPart( "name", new StringBody( username ) );
		entity.addPart( "password", new StringBody( username ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		StringBuilder response = this._http.fetch( httpPost );
		
		//System.out.println( response );
		
		// Look at the response for the string 'Your account "<username>" has been created!'
		StringBuilder successMessage = new StringBuilder();
		successMessage.append( "Successfully created user." );
		
		if( !(response.toString().contains( successMessage.toString() ) ) )
		{
			if( response.toString().contains( "There was a problem with your registration." ) )
			{
				// This may be because an account with this user name has already been created - e.g., if the
				// datastore is not reset between runs try to login - if we did create the account before then
				// the login should work
				if( !this.doLogin() )
					throw new Exception( "Creating new user: " + username.toString() + " failed! No success message found. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
			}
			else throw new Exception( "Creating new user: " + username.toString() + " failed! No success message found. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
		}
		
		// If the register user worked then we're automatically logged in to our dashboard
		// so mark that we're logged in
		this.getGenerator().setIsLoggedIn( true );
		this.trace( this.getGenerator()._dashboardUrl );
		//System.out.println( "CreateUser worked" );	
	}
	
	private String getUsername()
	{
		// User names can't have any periods in them
	  return distributeUserName("user-" + this._generatedBy.replace( '.', '-'));
	}

	//Append a hash to the beginning of usernames so they distribute better
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
	
	public StringBuilder doDashboard() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
		
		if( !this.getGenerator().getIsLoggedIn() )
		{
			if( !this.doLogin() )
				throw new Exception( "Error trying to view dashboard. Login failed!" );
		}
		
		if( debug )
			start = System.currentTimeMillis();
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._dashboardUrl );
		this.trace( this.getGenerator()._dashboardUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "Dashboard GET ERROR - Received an empty/error response. HTTP Status Code: " + this._http.getStatusCode();
			throw new IOException( errorMessage );
		}
						
		this.loadStatics( this.getGenerator().dashboardStatics );
		this.trace( this.getGenerator().dashboardStatics );
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "Dashboard (s): " + (end - start)/1000.0 );
		}
		
		return response;
	}
	
	public void doStartGame() throws Exception
	{
		// Get the dashboard, this will cause a log in of we're not logged in
		StringBuilder dashboardResponse = this.doDashboard();
		if( dashboardResponse == null || dashboardResponse.toString().trim().length() == 0 )
			throw new Exception( "Error starting game. Empty dashboard response." );
		
		System.out.println( dashboardResponse.toString() );
		
		// Once we get the dashboard response - we can either start a sample game or
		// resume an unfinished game
		int availableWordlistCount = 0;
		int unfinishedGameCount = 0;
		Vector<String> availableGames = new Vector<String>();
		Vector<String> unfinishedGames = new Vector<String>();
		// Parse the response for available wordlists "/games/new_game/0?wordlist=*"
		// Parse the response for unfinished games "/games/game_entry/*"
		Pattern availableWordlistPattern = Pattern.compile( AVAILABLE_WORDLIST_PATTERN, Pattern.CASE_INSENSITIVE );
		Matcher availableWordlistMatch = availableWordlistPattern.matcher( dashboardResponse.toString() );
		//System.out.println( "Groups: " + match.groupCount() );
		while( availableWordlistMatch.find() )
		{
			//System.out.println( buffer.substring( match.start(), match.end()) );
			System.out.println( availableWordlistMatch.group(2) );
			availableGames.add( availableWordlistMatch.group(2) );
			availableWordlistCount++;
		}
		System.out.println( "Available wordlist count: " + availableWordlistCount );
		
		// If there are any unfinished games flip a coin and restart an old game 70% of the time
		// otherwise start a brand new game
		Pattern unfinishedGamePattern = Pattern.compile( UNFINISHED_GAME_PATTERN, Pattern.CASE_INSENSITIVE );
		Matcher unfinishedGametMatch = unfinishedGamePattern.matcher( dashboardResponse.toString() );
		//System.out.println( "Groups: " + match.groupCount() );
		while( unfinishedGametMatch.find() )
		{
			//System.out.println( buffer.substring( match.start(), match.end()) );
			System.out.println( unfinishedGametMatch.group(2) );
			unfinishedGames.add( unfinishedGametMatch.group(2) );
			unfinishedGameCount++;
		}
		System.out.println( "Unfinished game count: " + unfinishedGameCount );
		
		// Pick a sample game to start - later we can allow users to resume games as well
		// We could skew the popularity of the sample games if we want to. Choose
		// randomly for now.
		if( availableWordlistCount > 0 )
		{
			StringBuffer gameUrl = new StringBuffer();
			gameUrl.append( this.getGenerator()._baseUrl );
			gameUrl.append( AVAILABLE_WORDLIST_URL_PREFIX );
			gameUrl.append( availableGames.get( this._random.nextInt( availableWordlistCount ) ) );
			System.out.println( "GameUrl: " + gameUrl.toString() );
			StringBuilder gameResponse = this._http.fetchUrl( gameUrl.toString() );
			this.trace( gameUrl.toString() );
			System.out.println( gameResponse.toString() );
			if( gameResponse == null || gameResponse.length() == 0 || this._http.getStatusCode() > 399 )
			{
				String errorMessage = "Start Game GET ERROR - Received an empty/error response. HTTP Status Code: " + this._http.getStatusCode();
				throw new IOException( errorMessage );
			}
		}
		
	}
	
	public boolean doLogin() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		//System.out.println( "Starting Login" );
						
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._homeUrl );
		this.trace( this.getGenerator()._homeUrl );
		if( response == null || response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "";
			if( response != null )
				errorMessage = "Login page GET ERROR - Received an empty/error response. URL: " + this.getGenerator()._homeUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString();
			else errorMessage = "Login page GET ERROR - Received an empty/error response. URL: " + this.getGenerator()._homeUrl + " HTTP Status Code: " + this._http.getStatusCode() + " Response: NULL";
			throw new IOException( errorMessage );
		}
		
		
		// Load the other statics
		this.loadStatics( this.getGenerator().loginpageStatics );
		this.trace( this.getGenerator().loginpageStatics );
				
		/*
		 commit Play a Game
		 login foo
		 password foo
		 */
				
		// Get the username from the generator - if it's not there then make up one
		String username = this.getGenerator()._username;
		if( username == null || username.trim().length() == 0 )
			username = this.getUsername();
		
		String commitAction = "Play a Game";
		
		HttpPost httpPost = new HttpPost( this.getGenerator()._loginUrl );
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "login", new StringBody( username ) );
		entity.addPart( "password", new StringBody( username ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		response = this._http.fetch( httpPost );
		
		// See whether we were logged in - if not doCreateUser and try again
		StringBuilder successMessage = new StringBuilder();
		successMessage.append( "You've been successfully logged in, " ).append( username.toString() );
		
		// Return true if we're able to log in
		if( response.toString().contains( successMessage.toString() ) )
		{
			//System.out.println( "Login worked" );
			
			if( debug )
			{
				end = System.currentTimeMillis();
				System.out.println( "Login (s): " + (end - start)/1000.0 );
			}
		
			this.getGenerator().setIsLoggedIn( true );
			return true;
		}
		else throw new Exception( "Error unable to log in. No success message found. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
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
		
		String successMessage = "You've been logged out.";
		if( !response.toString().contains( successMessage.toString() ) )
			throw new Exception( "Unable to log out. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "Logout (s): " + (end - start)/1000.0 );
		}
		
		// Update the generator to indicate that we've logged out
		this.getGenerator().setIsLoggedIn( false );
	}
}
