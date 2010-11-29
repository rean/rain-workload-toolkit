package radlab.rain.workload.booking;

import java.lang.Exception;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

/**
 * The BookingOperation class contains common static methods for use by the
 * operations that inherit from this abstract class.
 */
public abstract class BookingOperation extends Operation 
{
    public static final int MAX_USER_ID = 1000;  // Range is 0 to 999

	// These references will be set by the Generator.
	protected java.util.Random _randomNumberGenerator;
	protected HttpTransport _http;

	/**
	 * Returns the BookingGenerator that created this operation.
	 * 
	 * @return      The BookingGenerator that created this operation.
	 */
	public BookingGenerator getGenerator()
	{
		return (BookingGenerator) this._generator;
	}
	
	public BookingOperation( boolean interactive, IScoreboard scoreboard )
	{
		super( interactive, scoreboard );
	}
	
	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		BookingGenerator bookingGenerator = (BookingGenerator) generator;
		
		this._http = bookingGenerator.getHttpTransport();
		this._randomNumberGenerator = bookingGenerator.getRandomNumberGenerator();
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
	}

	public boolean traceUser(StringBuilder response)
	{
		String currentUser = this.getGenerator().getCurrentUser();
		String loggedInUserOnPage = this.getLoggedInUserFromResponse(response);
		
		if (currentUser == null && loggedInUserOnPage == null) {
			this.debugTrace("OK - No current user.");
			return true;
		}

		if (currentUser != null && loggedInUserOnPage != null && currentUser.equals(loggedInUserOnPage)) {
			this.debugTrace("OK - Current user is " + currentUser);
			return true;
		}

		this.debugTrace("ERROR - Current user mismatch.  Page shows user: " + loggedInUserOnPage + "  Saved current user: " + currentUser);
		return false;
	}

	private String getLoggedInUserFromResponse(StringBuilder responseIn) {
        
		String response = responseIn.toString();
        //System.out.println(response);
		
		String firstKey = "Welcome, ";
		int index1 = response.indexOf(firstKey);
        if (index1 == -1) {
            //System.out.println("Getting username from response - 1st indexOf failed");
        	return null;
        }
        int firstKeyLen = firstKey.length();
        
        // Find the trailing double-quote enclosing the id.
        String secondKey = " |";
        int index2 = response.indexOf(secondKey, index1 + firstKeyLen);
        if (index2 == -1) {
            //System.out.println("Getting username from response - 2nd indexOf failed");
        	return null;
        }

        String username = response.substring(index1 + firstKeyLen, index2);
        //System.out.println("The username in the response page is: " + username);
        
        return username;
	}
	
	public String getViewStateFromResponse( StringBuilder responseIn) {
        
		String response = responseIn.toString();
        //System.out.println(response);
		
		String firstKey = "id=\"javax.faces.ViewState\" value=\"";
		int index1 = response.indexOf(firstKey);
        if (index1 == -1) {
            //System.out.println("1st indexOf failed");
        	return null;
        }
        int firstKeyLen = firstKey.length();
        
        // Find the trailing double-quote enclosing the id.
        String secondKey = "\"";
        int index2 = response.indexOf(secondKey, index1 + firstKeyLen);
        if (index2 == -1) {
            //System.out.println("2nd indexOf failed");
        	return null;
        }

        String idString = response.substring(index1 + firstKeyLen, index2);
        //System.out.println("viewState is=" + idString);
        
        return idString;
	}
	
	
	/**
	 * Load the image files specified by the image URLs if they were not
	 * previously loaded and cached.
	 * 
	 * @param imageURLs     The set of image URLs.
	 * @return              The number of images loaded.
	 * 
	 * @throws IOException
	 */
	protected long loadImages( Set<String> imageUrls ) throws IOException 
	{
		long imagesLoaded = 0;
		
		if ( imageUrls != null )
		{
			for (String imageUrl : imageUrls )
			{
				// Do not load if cached (adding returns false if present).
				this._http.fetchUrl( imageUrl );
				imagesLoaded++;
			}
		}
		return imagesLoaded;
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
	protected long loadStatics( String[] urls ) throws Exception 
	{
		long staticsLoaded = 0;
		
		// Since some of our operations, like Home Page, are loading 35 static 
		// files, let's triple the current connection and socket idle timeouts.
		// Doubling the time was not enough in some cases.
		int originalConnectTimeout = _http.getConnectTimeout();
		int newConnectTimeout = originalConnectTimeout * 3;
		int originalIdleTimeout = _http.getSocketIdleTimeout();
		int newIdleTimeout = originalIdleTimeout * 3;
		try {
			_http.setConnectTimeout(newConnectTimeout);
			_http.setSocketIdleTimeout(newIdleTimeout);
			//System.out.println("New Conn TO: " + _http.getConnectTimeout() + "  " +
			//           "New Idle TO: " +  _http.getSocketIdleTimeout());

			for ( String url : urls )
			{
				this._http.fetchUrl( url );
				staticsLoaded++;
			}
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
			// Restore the original timeout value.
			_http.setConnectTimeout(originalConnectTimeout);
			_http.setSocketIdleTimeout(originalIdleTimeout);
			//System.out.println("Orig Conn TO: " + _http.getConnectTimeout() + "  " +
			//                   "Orig Idle TO: " +  _http.getSocketIdleTimeout());

		}
		return staticsLoaded;
	}
	
	/**
	 * Parses an HTML document for an authenticity token used by the Ruby on
	 * Rails framework to authenticate forms.
	 * 
	 * NOT USED BY THIS GENERATOR.
	 * 
	 * @param buffer    The HTTP response; expected to be an HTML document.
	 * @return          The authenticity token if found; otherwise, null.
	 * 
	 * @throws IOException
	 */
	public String parseAuthToken( StringBuilder buffer ) throws IOException 
	{
		String token = "";
		
		try {
			// TODO: Share this factory.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
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
			e.printStackTrace();
		}
		
		return token;
	}
	
	@Override
	public void cleanup()
	{
		
	}

    /* This method is called when the Login form page is already been received. */
	public String processLoginForm(boolean loadStaticUrls) throws Throwable
	{
		int randomUserId = this._randomNumberGenerator.nextInt(MAX_USER_ID);
		String username = "user" + randomUserId;
		String password = "pass" + randomUserId;
	
		this.debugTrace( "Logging in as " + username + " (Password: " + password + ")" );
	
		// Make the POST request to log in.
		StringBuilder postBody = new StringBuilder();
		postBody.append( "j_username=" ).append( username );
		postBody.append( "&j_password=" ).append( password );
		postBody.append("&submit=Login");
		StringBuilder postResponse = this._http.fetchUrl( this.getGenerator().loginProcessUrl, postBody.toString() );
		this.trace( username  + " POST " + this.getGenerator().loginProcessUrl );
	
		String finalUrl = this._http.getFinalUrl();
		this.debugTrace("POST to loginProcess status: " + this._http.getStatusCode());
	    this.debugTrace("Login response final URL is: " + finalUrl);
	    //this.trace("login response: " + postResponse);
	   
		// Check that the user was successfully logged in.
		String successfulLoginMessage = "Welcome, " + username;
		if ( postResponse.indexOf( successfulLoginMessage ) < 0 ) {
			String errorMessage = "Login ERROR - No welcome message.  Login failed for " + username;
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}
		
		// Save the username of the currently logged in user.  Also save the last URL.
		this.getGenerator().setCurrentUser(username);
		this.getGenerator().setLastUrl(this._http.getFinalUrl());
	
		this.debugTrace(this.getGenerator().getCurrentUser() + " is now logged in.");
	
		// Load the static files (CSS/JS) associated with the both the Home page
		// and Login pages.  This is to emulate a new user logging in whose Web
		// browser will not have those secondary pages cached.
		//
		// Note: This normally happens after the initial GET, but we're doing this at the
		// beginning so that final URL is preserved.
		//
		// Also there is a parameter to control loading the static URLs.  In the case
		// where a login is necessary in the middle of a booking operation, we want
		// to preserve the data about the last HTTP operation, e.g. the response, 
		// URL, status, etc.
		if (loadStaticUrls && !this.getGenerator().staticHomePageUrlsLoaded) {
			loadStatics( this.getGenerator().staticHomePageUrls );
			this.trace( this.getGenerator().staticHomePageUrls );
			this.getGenerator().staticHomePageUrlsLoaded = true;
		}
		if (loadStaticUrls && !this.getGenerator().staticLoginPageUrlsLoaded) {
			loadStatics( this.getGenerator().staticLoginPageUrls );
			this.trace( this.getGenerator().staticLoginPageUrls );
			this.getGenerator().staticLoginPageUrlsLoaded = true;
		}

		return username;	
	}

	/*
     * This method is called when the View Hotel page has already been received.  We
     * will process the Book Hotel button. 
     */
	public boolean processBookHotelButton(String attempt) throws Throwable
	{
		String viewHotelFinalUrl = this._http.getFinalUrl();
		StringBuilder viewHotelResponse = this._http.getResponseBuffer();

		if (this._http.getStatusCode() != 200) {
        	String errorMessage = "BookHotel " + attempt + " ERROR - GET View Hotel result status: " + this._http.getStatusCode();
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);		
		}		

		// Bail out if the response isn't a View Hotel page.
    	if (viewHotelResponse.indexOf("value=\"Book Hotel\"") == -1) {
        	String errorMessage = "BookHotel " + attempt + " ERROR - GET did not display a View Hotel result page.";
        	this.debugTrace(errorMessage);
        	this.debugTrace(viewHotelResponse.toString());
			throw new Exception(errorMessage);		
    	}    			

    	// Create a POST request
		HttpPost bookHotelPost = new HttpPost(viewHotelFinalUrl);

		this.trace(this.getGenerator().getCurrentUser() + " POST " + viewHotelFinalUrl);

		// Get the ViewState from the form so we can specify it later in the POST data.
		String viewState = this.getViewStateFromResponse(viewHotelResponse);

		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add( new BasicNameValuePair( "hotel", "hotel" ) );
		formParams.add( new BasicNameValuePair( "hotel:book", "Book Hotel" ) );
		formParams.add( new BasicNameValuePair( "javax.faces.ViewState", viewState ) );

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
		bookHotelPost.setEntity(entity);

        StringBuilder bookHotelResponse = this._http.fetch(bookHotelPost);

        // The response could contain either a Book Hotel page or a Login page.  We 
        // have to let the caller process it. 
        if (this._http.getStatusCode() != 200)
		{
        	String errorMessage = "BookHotel " + attempt + " ERROR - POST Book Hotel status: " + this._http.getStatusCode();
        	this.debugTrace(errorMessage);
			throw new Exception(errorMessage);		
		}

		// If we are redirected to a Login URL, then that's OK.  Return success.
		if (this._http.getFinalUrl().indexOf("/login") > 0) {
        	this.debugTrace("POST Book Hotel redirected to a Login form page.");
    		return true;   		
    	}		

		// See if we can find some proof that a Book Hotel page was returned.
    	if ( bookHotelResponse.indexOf( "<legend>Book Hotel</legend>" ) > 0 ) {
    		this.debugTrace( "Success - Response contains a Book Hotel page!" );
    		return true;
    	}	

		String error = "BookHotel " + attempt + " ERROR - POST Book Hotel received an unexpected response page.";
		this.debugTrace( error );
		this.debugTrace( bookHotelResponse.toString() );
		throw new Exception(error);
	}

	/**
	 * Write a debug message to the trace log if the allowDebugToTraceLog
	 * variable is set to true.
	 * 
	 * Normally only GET and POST messages containing URLs are written to the 
	 * Rain trace log file.  However, to aid in debugging, it's very 
	 * advantageous to write debug information to the log along with the GET 
	 * and POST information.
     *
	 * @param message	The message to write to the trace log.
	 *
	 */
	public void debugTrace (String message)
	{
		if (this.getGenerator().getPrintDebugToTraceLog()) {
			this.trace ("DEBUG: " + message);
		}
	}

	/**
	 * There are two ways to fail an operation.  You can throw an exception
	 * or call setFailed(true) and setFailureReason() specifying an exception.
	 * Throwing an exception directly will cause Rain to do these two operations
	 * in its try/catch block.  This method traces the message to the trace
	 * log if debugging is enabled.
	 * 
	 * NOTE: This method isn't used, but was retained.
	 * 
	 * @param errorMessage
	 */
	void processError(String errorMessage)
	{
		this.debugTrace(errorMessage);
		this.setFailed( true );
		this.setFailureReason(new Exception(errorMessage));
		return;
	}

}
