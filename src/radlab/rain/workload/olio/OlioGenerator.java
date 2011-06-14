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

package radlab.rain.workload.olio;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

/**
 * The OlioGenerator class generates operations for a single user thread by
 * producing the next operation to execute given the last operation. The next
 * operation is decided through the use of a load mix matrix.<br />
 * <br />
 * This class also provides accessors to many static parameters such as the
 * HTTP client, and it generates other potentially dynamic values such as the
 * think/cycle times. 
 */
public class OlioGenerator extends Generator
{
	/** Static URLs loaded as part of the layout. */
	protected static final String[] LAYOUT_STATICS = {
		"/javascripts/prototype.js",
		"/javascripts/effects.js",
		"/javascripts/dragdrop.js",
		"/javascripts/controls.js",
		"/javascripts/application.js",
		"/stylesheets/scaffold.css",
		"/stylesheets/site.css",
		"/images/bg_main.png",
		"/images/bg_header.gif",
		"/images/main_nav_link_bg.gif",
		"/images/main_nav_link_bg.gif",
		"/images/RSS-icon-large.gif",
		"/images/main_nav_link_bg.gif",
		"/images/main_nav_link_bg.gif",
		"/images/corner_top_right.png",
		"/images/corner_top_left.png",
		"/images/corner_bottom_right.png",
		"/images/corner_bottom_left.png",
		"/images/reflec_tile.png",
		"/images/reflec_right.png",
		"/images/reflec_left.png",
		"/images/main_nav_hover_bg.gif"
	};
	
	/*
	 * @Row({  0, 11, 52, 36,  0, 1,  0 }), // Home Page
	 * @Row({  0,  0, 60, 20,  0, 0, 20 }), // Login
	 * @Row({ 21,  6, 41, 31,  0, 1,  0 }), // Tag Search
	 * @Row({ 72, 21,  0,  0,  6, 1,  0 }), // Event Detail
	 * @Row({ 52,  6,  0, 31, 11, 0,  0 }), // Person Detail
	 * @Row({  0,  0,  0,  0,100, 0,  0 }), // Add Person
	 * @Row({  0,  0,  0,100,  0, 0,  0 })  // Add Event
	 */
	public static final int HOME_PAGE     = 0;
	public static final int LOGIN         = 1;
	public static final int TAG_SEARCH    = 2;
	public static final int EVENT_DETAIL  = 3;
	public static final int PERSON_DETAIL = 4;
	public static final int ADD_PERSON    = 5;
	public static final int ADD_EVENT     = 6;
	
	// URL roots/anchors for each request
	public String baseURL;
	public String personDetailURL;
	public String tagSearchURL;
	public String homepageURL; 
	public String loginURL; 
	public String logoutURL;
	public String addEventURL; 
	public String addPersonURL; 
	public String eventDetailURL;
	public String addEventResultURL; 
	public String addPersonResultURL;
	public String addAttendeeURL;
	public String checkNameURL;
	public String fileServiceURL;
	public String tagCloudURL;
	
	// Statics URLs
	public String[] homepageStatics; 
	public String[] personStatics; 
	public String[] personGets;
	public String[] tagSearchStatics; 
	public String[] eventDetailStatics; 
	public String[] addPersonStatics; 
	public String[] addEventStatics;
	
	// Login header collection 
	public LinkedHashMap<String,String> loginHeaders = new LinkedHashMap<String,String>();
	
	// Local file resources used when creating people and events
	public File eventImg;
	public File eventThumb;
	public File eventPdf; 
	public File personImg; 
	public File personThumb;
	public String type = "html";
	
	/** The number of users created during the benchmark run. */
	public AtomicLong _personsAdded = new AtomicLong(0L);
	
	private boolean _isLoggedOn = false;
	private HashSet<String> _cachedURLs = new HashSet<String>();
	
	private NegativeExponential _thinkTimeGenerator  = null;
	private NegativeExponential _cycleTimeGenerator = null;
	
	/**
	 * Updates the current "logged in" status.
	 */
	public void setIsLoggedOn( boolean val )
	{
		this._isLoggedOn = val;
	}
	
	/**
	 * Returns the current "logged on" status.
	 * 
	 * @return      True if this user is "logged on"; otherwise false.
	 */
	public boolean isLoggedOn() {
		return this._isLoggedOn;
	}
	
	/**
	 * Returns the set of cached URLs.
	 * 
	 * @return      A set of cached URLs.
	 */
	public HashSet<String> getCachedURLs()
	{
		return this._cachedURLs;
	}
	
	private java.util.Random _randomNumberGenerator;
	private radlab.rain.workload.olio.Random _randomUtil;
	private HttpTransport _http;
	private Logger _logger;
	
	/**
	 * Returns an instance of the Olio random utility.
	 * 
	 * @return          A random number generator.
	 */
	public radlab.rain.workload.olio.Random getOlioRandomUtil()
	{
		return getOlioRandomUtil(false);
	}
	
	/**
	 * Returns an instance of the Olio random utility.
	 * 
	 * @param clone     If true, a clone is returned.
	 * @return          A random number generator.
	 */
	public radlab.rain.workload.olio.Random getOlioRandomUtil( boolean clone )
	{
		if( clone )
		{
			// TODO: Implement cloning of random number generator.
			return null;
		}
		else
		{
			return this._randomUtil;
		}
	}
	
	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return getHttpTransport(false);
	}
	
	/**
	 * Returns an HTTP transport.
	 * 
	 * @param clone     If true, a clone is returned.
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport( boolean clone )
	{
		if( clone )
		{
			// TODO: Implement cloning of HTTP transport.
			return null;
		}
		else
		{
			return this._http;
		}
	}
	
	/**
	 * Returns the <code>Logger</code> associated with this generator.
	 * 
	 * @return      A <code>Logger</code> object.
	 */
	public Logger getLogger()
	{
		return this._logger;
	}
	
	/**
	 * Initialize a <code>OlioGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public OlioGenerator( ScenarioTrack track )
	{
		super( track );
		
		this.initializeUrlAnchors();
		this.initializeLocalFileResources();
		this.initializeLoginHeaders();
		this.initializeStaticUrls();
		
		// Initialize random utility generators and HttpTransport instances.
		this._randomNumberGenerator = new java.util.Random();
		this._randomUtil = new radlab.rain.workload.olio.Random();
		this._http = new HttpTransport();
		// Initialize the cycle time and think time generators. If you want non-stop
		// activity, then set mean cycle time, and mean think times to 0 and the
		// number generators should just *always* return 0 for the think/cycle time
		this._cycleTimeGenerator = new NegativeExponential( track.getMeanCycleTime() );
		this._thinkTimeGenerator = new NegativeExponential( track.getMeanThinkTime() );
		ScaleFactors.setActiveUsers( this.getTrack().getMaxUsers() );
		this._cycleTime = (long) track.getMeanCycleTime();
		this._thinkTime = (long) track.getMeanThinkTime();
	}
	
	/**
	 * Initialize this generator.
	 */
	public void initialize()
	{
		this._logger = Logger.getLogger( this._name );
	}
	
	/**
	 * Initialize the fully qualified static URLs for the operations.
	 */
	public void initializeStaticUrls()
	{
		this.homepageStatics    = joinStatics( LAYOUT_STATICS );
		this.personStatics      = joinStatics( LAYOUT_STATICS );
		this.personGets         = joinStatics( LAYOUT_STATICS );
		this.tagSearchStatics   = joinStatics( LAYOUT_STATICS );
		this.eventDetailStatics = joinStatics( LAYOUT_STATICS);
		this.addPersonStatics   = joinStatics( LAYOUT_STATICS);
		this.addEventStatics    = joinStatics( LAYOUT_STATICS);
	}
	
	/**
	 * Joins any number of arrays of relative static URLs and fully qualifies
	 * them by prepending the base URL. Duplicate static URLs are removed. 
	 * 
	 * Copied from original UIDriver.java file.
	 * 
	 * @param staticsLists  Arrays of relative static URLs.
	 * @return              An array of unique, full qualified static URLs.
	 */
	private String[] joinStatics( String[] ... staticsLists ) 
	{
		LinkedHashSet<String> urlSet = new LinkedHashSet<String>();
		
		for ( String[] staticList : staticsLists )
		{
			for ( int i = 0; i < staticList.length; i++ )
			{
				String url = baseURL + staticList[i].trim();
				urlSet.add( url );
			}
		}
		
		return (String[]) urlSet.toArray(new String[0]);
	}
	
	/**
	 * Initialize the login header information.
	 */
	public void initializeLoginHeaders()
	{
		String host = this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();
		
		this.loginHeaders.clear();
		this.loginHeaders.put("Host", host );
		this.loginHeaders.put("User-Agent", "Mozilla/5.0");
		this.loginHeaders.put("Accept", "text/xml.application/xml,application/" +
		        "xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;" +
		        "q=0.5");
		this.loginHeaders.put("Accept-Language", "en-us,en;q=0.5");
		this.loginHeaders.put("Accept-Encoding", "gzip,deflate");
		this.loginHeaders.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		this.loginHeaders.put("Keep-Alive", "300");
		this.loginHeaders.put("Connection", "keep-alive");
		this.loginHeaders.put("Referer", this.homepageURL);
	}
	
	/**
	 * Initialize local resources (e.g. files that we need to submit).
	 */
	public void initializeLocalFileResources()
	{
		String resourcePath = this._loadTrack.getResourcePath();
		if( !resourcePath.endsWith( File.separator ) )
		{
			resourcePath += File.separator;
		}
		
		this.eventImg    = new File(resourcePath + "event.jpg");
		this.eventThumb  = new File(resourcePath + "event_thumb.jpg");
		this.eventPdf    = new File(resourcePath + "event.pdf");
		this.personImg   = new File(resourcePath + "person.jpg");
		this.personThumb = new File(resourcePath + "person_thumb.jpg");
	}
	
	/**
	 * Initialize the roots/anchors of the URLs.
	 */
	public void initializeUrlAnchors()
	{
		this.baseURL = "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();
		this.personDetailURL    = this.baseURL + "/users/";
		this.tagSearchURL       = this.baseURL + "/events/tag_search/";
		this.tagCloudURL        = this.baseURL + "/tagCloud." + type;
		this.addEventURL        = this.baseURL + "/events/new";
		this.addEventResultURL  = this.baseURL + "/events/";
		this.addPersonURL       = this.baseURL + "/users/new";
		this.addPersonResultURL = this.baseURL + "/users";
		this.homepageURL        = this.baseURL + "/";
		this.loginURL           = this.baseURL + "/users/login";
		this.logoutURL          = this.baseURL + "/users/logout";
		this.addAttendeeURL     = this.baseURL + "/events/";
		this.eventDetailURL     = this.baseURL + "/events/";
		this.fileServiceURL     = this.baseURL + "/fileService." + type + '?';
		this.checkNameURL       = this.baseURL + "/users/check_name";
	}
	
	/**
	 * Returns the next <code>Operation</code> given the <code>lastOperation</code>
	 * according to the current mix matrix.
	 * 
	 * @param lastOperation     The last <code>Operation</code> that was executed.
	 */
	public Operation nextRequest( int lastOperation )
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		int nextOperation = -1;
		
		if( lastOperation == -1 )
		{
			nextOperation = 0;
		}
		else
		{
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(currentLoad.getMixName()).getSelectionMix();
			double rand = this._randomNumberGenerator.nextDouble();
			
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
	
	/**
	 * Returns the current think time. The think time is duration between
	 * receiving the response of an operation and the execution of its
	 * succeeding operation during synchronous execution (i.e. closed loop).
	 */
	public long getThinkTime()
	{
		long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextThinkTime, (5*this._thinkTime) );
	}
	
	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	public long getCycleTime()
	{
		long nextCycleTime = (long) this._cycleTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextCycleTime, (5*this._cycleTime) );
	}
	
	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	public void dispose()
	{
		this._http.dispose();
	}
	
	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex   The type of operation to instantiate.
	 * @return          A prepared operation.
	 */
	public Operation getOperation( int opIndex )
	{
		switch( opIndex )
		{
			case HOME_PAGE:     return this.createHomePageOperation();
			case LOGIN:         return this.createLoginOperation();
			case TAG_SEARCH:    return this.createTagSearchOperation();
			case EVENT_DETAIL:  return this.createEventDetailOperation();
			case PERSON_DETAIL: return this.createPersonDetailOperation();
			case ADD_PERSON:    return this.createAddPersonOperation();
			case ADD_EVENT:     return this.createAddEventOperation();	
			default:            return null;
		}
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared HomePageOperation.
	 */
	public HomePageOperation createHomePageOperation()
	{
		HomePageOperation op = new HomePageOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared LoginOperation.
	 */
	public LoginOperation createLoginOperation()
	{
		LoginOperation op = new LoginOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared TagSearchOperation.
	 */
	public TagSearchOperation createTagSearchOperation()
	{
		TagSearchOperation op = new TagSearchOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared EventDetailOperation.
	 */
	public EventDetailOperation createEventDetailOperation()
	{
		EventDetailOperation op = new EventDetailOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared PersonDetailOperation.
	 */
	public PersonDetailOperation createPersonDetailOperation()
	{
		PersonDetailOperation op = new PersonDetailOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared AddPersonOperation.
	 */
	public AddPersonOperation createAddPersonOperation()
	{
		AddPersonOperation op = new AddPersonOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared AddEventOperation.
	 */
	public AddEventOperation createAddEventOperation()
	{
		AddEventOperation op = new AddEventOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
}
