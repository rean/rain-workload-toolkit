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
 *
 * Author: Original authors
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013
 */

package radlab.rain.workload.olio;


import java.io.File;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Set;
import org.json.JSONObject;
import org.json.JSONException;
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
 * <br/>
 * This class also provides accessors to many static parameters such as the
 * HTTP client, and it generates other potentially dynamic values such as the
 * think/cycle times. 
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver}
 * class and adapted for RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone"</a>
 */
public class OlioGenerator extends Generator
{
	/** Common static file loaded as part of the layout (for Java incarnation). */
	private static final String[] JAVA_COMMON_STATICS = {
			"/css/scaffold.css",
			"/css/site.css",
			"/images/bg_main.png",
			"/images/RSS-icon-large.gif",
			"/images/php_bg_header.gif",
			"/images/php_main_nav_link_bg.gif",
			"/images/php_corner_top_right.gif",
			"/images/php_corner_top_left.gif",
			"/images/php_corner_bottom_right.gif",
			"/images/php_corner_bottom_left.gif",
			"/resources/jmaki-min.js",
			"/glue.js",
			"/resources/system-glue.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/yahoo-dom-event/yahoo-dom-event.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/element/element-beta-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/container/container_core-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/menu/menu-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/button/button-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/datasource/datasource-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/calendar/calendar-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/menu/assets/skins/sam/menu.css",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/button/assets/skins/sam/button.css",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/calendar/assets/skins/sam/calendar.css",
			"/resources/yahoo/calendar/component.js",
			"/images/php_reflec_tile.gif",
			"/images/php_reflec_left.gif",
			"/images/php_reflec_right.gif",
			"/resources/config.json",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/assets/skins/sam/sprite.png"
		};

	/** Additional static files for Home operation (for Java incarnation). */
	private static final String[] JAVA_HOME_STATICS = {
			"/js/httpobject.js",
			"/js/dragdrop.js",
			"/js/effects.js",
			"/js/prototype.js"
		};

	/** Additional static files for Event Detail operation (for Java incarnation). */
	private static final String[] JAVA_EVENT_DETAIL_COMMON_STATICS = {
			"/js/starrating.js",
			"/js/httpobject.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/yahoo-dom-event/yahoo-dom-event.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/dragdrop/dragdrop-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/animation/animation-min.js",
			"/resources/yahoo/resources/libs/yahoo/v2.6.0/connection/connection-min.js",
			"/resources/yahoo/map/component.js",
			"/images/star_off.png",
			"/images/star_on.png"
		};

	/** Additional static files for Event Detail operation (for Java incarnation). */
	private static final String[] JAVA_EVENT_DETAIL_BASE_STATICS = {
			"/js/attendee.js",
			"/js/comments.js"
		};

	/** Additional static files for Event Detail operation (for Java incarnation). */
	private static final String[] JAVA_EVENT_DETAIL_JMAKI_STATICS = {
			"/resources/blueprints/list/attendeeList/component.css",
			"/resources/blueprints/list/attendeeList/component.js",
			"/resources/blueprints/list/commentList/component.css",
			"/resources/blueprints/list/commentList/component.js"
		};
 
	/** Additional static files for Add Person operation (for Java incarnation). */
	private static final String[] JAVA_ADD_PERSON_STATICS = {
			"/js/validateform.js",
			"/js/httpobject.js"
		};

	/** Additional static files for Add Event operation (for Java incarnation). */
	private static final String[] JAVA_ADD_EVENT_STATICS = {
			"/js/validateform.js",
			"/js/httpobject.js"
		};

	/** Additional static files for Person operation (for Java incarnation). */
	private static final String[] JAVA_PERSON_STATICS = {
			"/js/httpobject.js"
		};

	/** Additional static files for Tag Search operation (for Java incarnation). */
	private static final String[] JAVA_TAG_SEARCH_STATICS = {
			"/js/httpobject.js"
		};

//	private static final String[] JAVA_TINY_MCE_STATICS = {
//			"/js/tiny_mce/tiny_mce.js",
//			"/js/tiny_mce/themes/simple/editor_template.js",
//			"/js/tiny_mce/langs/en.js",
//			"/js/tiny_mce/themes/simple/css/editor_ui.css",
//			"/js/tiny_mce/themes/simple/images/italic.gif",
//			"/js/tiny_mce/themes/simple/images/underline.gif",
//			"/js/tiny_mce/themes/simple/images/strikethrough.gif",
//			"/js/tiny_mce/themes/simple/images/undo.gif",
//			"/js/tiny_mce/themes/simple/images/separator.gif",
//			"/js/tiny_mce/themes/simple/images/redo.gif",
//			"/js/tiny_mce/themes/simple/images/cleanup.gif",
//			"/js/tiny_mce/themes/simple/images/bullist.gif",
//			"/js/tiny_mce/themes/simple/images/numlist.gif",
//			"/js/tiny_mce/themes/simple/css/editor_content.css"
//		};

	private static final String JAVA_CONTEXT_ROOT = "/webapp";

	/** Common static files loaded as part of the layout (for PHP incarnation). */
    private static final String[] PHP_COMMON_STATICS = {
			"/js/prototype.js",
			"/js/effects.js",
			"/js/dragdrop.js",
			"/js/controls.js",
			"/css/scaffold.css",
			"/css/site.css",
			"/images/bg_main.png",
			"/images/RSS-icon-large.gif",
			"/images/php_bg_header.gif",
			"/images/php_main_nav_link_bg.gif",
			"/images/php_corner_top_right.gif",
			"/images/php_corner_top_left.gif",
			"/images/php_corner_bottom_right.gif",
			"/images/php_corner_bottom_left.gif",
			"/images/php_reflec_tile.gif",
			"/images/php_reflec_right.gif",
			"/images/php_reflec_left.gif"
		};

	/** Additional static files for Home operation (for PHP incarnation). */
    private static final String[] PHP_HOME_STATICS = {
			"/images/php_main_nav_hover_bg.gif"
		};

	/** Additional static files for Event Detail operation (for PHP incarnation). */
    private static final String[] PHP_EVENT_DETAIL_COMMON_STATICS = {
			"/js/starrating.js",
			"/images/star_on.png",
			"/images/star_off.png"
		};

	/** Additional static files for Event Detail operation (for Rails incarnation). */
	private static final String[] PHP_EVENT_DETAIL_BASE_STATICS = {};

	/** Additional static files for Event Detail operation (for Rails incarnation). */
	private static final String[] PHP_EVENT_DETAIL_JMAKI_STATICS = {};
 
	/** Additional static files for Add Person operation (for PHP incarnation). */
    private static final String[] PHP_ADD_PERSON_STATICS = {
			"/js/validateform.js"
		};

	/** Additional static files for Add Event operation (for PHP incarnation). */
    private static final String[] PHP_ADD_EVENT_STATICS = {
			"/js/validateform.js"
		};

	/** Additional static files for Person operation (for PHP incarnation). */
    private static final String[] PHP_PERSON_STATICS = {
    	};

	/** Additional static files for Tag Search operation (for PHP incarnation). */
    private static final String[] PHP_TAG_SEARCH_STATICS = {
			"/images/php_main_nav_hover_bg.gif"
		};

	private static final String PHP_CONTEXT_ROOT = "";

	/** Common static files loaded as part of the layout (for Rails incarnation). */
	private static final String[] RAILS_COMMON_STATICS = {
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

	/** Additional static files for Home operation (for Rails incarnation). */
	private static final String[] RAILS_HOME_STATICS = {};

	/** Additional static files for Event Detail operation (for Rails incarnation). */
	private static final String[] RAILS_EVENT_DETAIL_COMMON_STATICS = {
			"/images/reflec_tile.png",
			"/images/reflec_right.png",
			"/images/reflec_left.png",
		};

	/** Additional static files for Event Detail operation (for Rails incarnation). */
	private static final String[] RAILS_EVENT_DETAIL_BASE_STATICS = {};

	/** Additional static files for Event Detail operation (for Rails incarnation). */
	private static final String[] RAILS_EVENT_DETAIL_JMAKI_STATICS = {};
 
	/** Additional static files for Add Person operation (for Rails incarnation). */
	private static final String[] RAILS_ADD_PERSON_STATICS = {
			"/images/reflec_tile.png",
			"/images/reflec_right.png",
			"/images/reflec_left.png",
		};

	/** Additional static files for Add Event operation (for Rails incarnation). */
	private static final String[] RAILS_ADD_EVENT_STATICS = {
			"/images/reflec_tile.png",
			"/images/reflec_right.png",
			"/images/reflec_left.png",
		};

	/** Additional static files for Person operation (for Rails incarnation). */
	private static final String[] RAILS_PERSON_STATICS = {};

	/** Additional static files for Tag Search operation (for Rails incarnation). */
	private static final String[] RAILS_TAG_SEARCH_STATICS = {};

	private static final String RAILS_CONTEXT_ROOT = "";

	/*
	 * @Row({  0, 11, 52, 36,  0, 1,  0 }), // Home Page
	 * @Row({  0,  0, 60, 20,  0, 0, 20 }), // Login
	 * @Row({ 21,  6, 41, 31,  0, 1,  0 }), // Tag Search
	 * @Row({ 72, 21,  0,  0,  6, 1,  0 }), // Event Detail
	 * @Row({ 52,  6,  0, 31, 11, 0,  0 }), // Person Detail
	 * @Row({  0,  0,  0,  0,100, 0,  0 }), // Add Person
	 * @Row({  0,  0,  0,100,  0, 0,  0 })  // Add Event
	 */
	public static final int HOME_PAGE_OP     = 0;
	public static final int LOGIN_OP         = 1;
	public static final int TAG_SEARCH_OP    = 2;
	public static final int EVENT_DETAIL_OP  = 3;
	public static final int PERSON_DETAIL_OP = 4;
	public static final int ADD_PERSON_OP    = 5;
	public static final int ADD_EVENT_OP     = 6;

	public static final String HOME_PAGE_OP_NAME     = "HomePage";
	public static final String LOGIN_OP_NAME         = "Login";
	public static final String TAG_SEARCH_OP_NAME    = "TagSearch";
	public static final String EVENT_DETAIL_OP_NAME  = "EventDetail";
	public static final String PERSON_DETAIL_OP_NAME = "PersonDetails";
	public static final String ADD_PERSON_OP_NAME    = "AddPerson";
	public static final String ADD_EVENT_OP_NAME     = "AddEvent";


	private static Random _rng; ///< The Random Number Generator
	private static OlioConfiguration _conf; ///< The Olio-related configuration found in JSON profile file
	private HttpTransport _http;
	private Logger _logger;
	private OlioUtility _utility;
	private OlioSessionState _sessionState; ///< Holds user session data
	private double _thinkTime = -1; ///< The mean think time; a value <= 0 means that no think time is used.
	private double _cycleTime = -1; ///< The mean cycle time; a value <= 0 means that no cycle time is used.
	private NegativeExponential _thinkTimeRng  = null;
	private NegativeExponential _cycleTimeRng = null;
	// URL roots/anchors for each request
	private String _hostURL;
	private String _baseURL;
	private String _personDetailURL;
	private String _tagSearchURL;
	private String _homepageURL; 
	private String _loginURL; 
	private String _logoutURL;
	private String _addEventURL; 
	private String _addPersonURL; 
	private String _eventDetailURL;
	private String _addEventResultURL; 
	private String _addPersonResultURL;
	private String _addAttendeeURL;
	private String _checkNameURL;
	private String _fileServiceURL;
	private String _tagCloudURL;
	private String _imgStoreURL;
	private String _docStoreURL;
	// Statics URLs
	private String[] _homepageStatics; 
	private String[] _personStatics; 
	private String[] _tagSearchStatics; 
	private String[] _eventDetailStatics; 
	private String[] _addPersonStatics; 
	private String[] _addEventStatics;
	// Local file resources used when creating people and events
	private File _eventImg;
	private File _eventThumb;
	private File _eventPdf; 
	private File _personImg; 
	private File _personThumb;


	/**
	 * Returns the internally used random number generator.
	 * 
	 * @return A Random object.
	 */
	public static Random getRandomGenerator()
	{
		//NOTE: this method is not "synchronized" since java.util.Random is threadsafe.
		return _rng;
	}

	/**
	 * Set the internally used random number generator.
	 * 
	 * @param value A Random object.
	 */
	protected static synchronized void setRandomGenerator(Random value)
	{
		_rng = value;
	}

	/**
	 * Get the internally used RUBiS configuration object.
	 * 
	 * @return A OlioConfiguration object.
	 */
	public static synchronized OlioConfiguration getConfiguration()
	{
		return _conf;
	}

	/**
	 * Set the internally used RUBiS configuration object.
	 * 
	 * @param value A OlioConfiguration object.
	 */
	protected static synchronized void setConfiguration(OlioConfiguration value)
	{
		_conf = value;
	}

	/**
	 * Initialize the shared configuration object.
	 */
	private static synchronized void initializeConfiguration(JSONObject config) throws JSONException
	{
		if (_conf == null)
		{
			_conf = new OlioConfiguration(config);
		}
	}

	/**
	 * Initialize the shared random number generator.
	 */
	private static synchronized void initializeRandomGenerator()
	{
		if (_rng == null)
		{
			if (getConfiguration().getRngSeed() >= 0)
			{
				_rng = new Random(getConfiguration().getRngSeed());
			}
			else
			{
				_rng = new Random();
			}
		}
	}

	/**
	 * Initialize a <code>OlioGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public OlioGenerator(ScenarioTrack track)
	{
		super(track);
	}

	@Override
	public void configure(JSONObject config) throws JSONException
	{
		this.initializeConfiguration(config);
	}

	/**
	 * Initialize this generator.
	 */
	@Override
	public void initialize()
	{
		this.initializeRandomGenerator();
		this._http = new HttpTransport();
		this._logger = Logger.getLogger(this.getName());
		this._utility = new OlioUtility(this._rng, this._conf);
		this._sessionState = new OlioSessionState();

		// Setup think and cycle times
		//this._thinkTime = this.getTrack().getMeanThinkTime()*1000;
		this._thinkTime = this.getTrack().getMeanThinkTime();
		if (this._thinkTime > 0)
		{
			this._thinkTimeRng = new NegativeExponential(this._thinkTime, this._rng);
		}
		//this._cycleTime = this.getTrack().getMeanCycleTime()*1000;
		this._cycleTime = this.getTrack().getMeanCycleTime();
		if (this._cycleTime > 0)
		{
			this._cycleTimeRng = new NegativeExponential(this._cycleTime, this._rng);
		}

//		// Select a random user for current session (if needed)
//		this.getSessionState().setLoggedUserId(this.getUtility().generatePerson().id);

		// Build Olio URLs
		this.initializeUrlAnchors();
		this.initializeLocalFileResources();
		this.initializeStaticUrls();
	}

	/**
	 * Returns the next <code>Operation</code> given the <code>lastOperation</code>
	 * according to the current mix matrix.
	 * 
	 * @param lastOperation     The last <code>Operation</code> that was executed.
	 */
	@Override
	public Operation nextRequest( int lastOperation )
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		int nextOperation = -1;
		
		if( lastOperation == -1 )
		{
			nextOperation = HOME_PAGE_OP;
		}
//		else if (lastOperation == BACK_SPECIAL_OP)
//		{
//			// Back to previous state
//			nextOperation = Math.max(HOME_PAGE_OP, this._sessionState.getLastOperation());
//		}
//		else if (lastOperation == EOS_SPECIAL_OP)
//		{
//			// End-of-session
//
//			// Start from the initial operation
//			nextOperation = HOME_PAGE_OP;
//			// Clear session data
//			this.getSessionState().clear();
//			// Generate a new user for the new session
//			this.getSessionState().setLoggedUserId(this.getUtility().generateUser().id);
//		}
		else
		{
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(currentLoad.getMixName()).getSelectionMix();
			double rand = this._rng.nextDouble();

			int j;
			for (j = 0; j < selectionMix.length; ++j)
			{
				if (rand <= selectionMix[lastOperation][j])
				{
					break;
				}
			}
			nextOperation = j;
		}
		return this.getOperation(nextOperation);
	}

	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	@Override
	public void dispose()
	{
		this._http.dispose();
	}

	/**
	 * Returns the current think time. The think time is duration between
	 * receiving the response of an operation and the execution of its
	 * succeeding operation during synchronous execution (i.e. closed loop).
	 */
	@Override
	public long getThinkTime()
	{
		if (this._thinkTime <= 0)
		{
			return 0;
		}

		double nextThinkTime = this._thinkTimeRng.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.round(Math.min(nextThinkTime, (5.0*this._thinkTime)));
	}

	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	@Override
	public long getCycleTime()
	{
		if (this._cycleTime <= 0)
		{
			return 0;
		}

		double nextCycleTime = this._cycleTimeRng.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.round(Math.min(nextCycleTime, (5.0*this._cycleTime)));
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
	 * Returns the <code>Logger</code> associated with this generator.
	 * 
	 * @return      A <code>Logger</code> object.
	 */
	public Logger getLogger()
	{
		return this._logger;
	}

	public OlioSessionState getSessionState()
	{
		return this._sessionState;
	}

	protected void setSessionState(OlioSessionState value)
	{
		this._sessionState = value;
	}

	public OlioUtility getUtility()
	{
		return this._utility;
	}

	protected void setUtility(OlioUtility value)
	{
		this._utility = value;
	}

	public boolean checkHttpResponse(String response)
	{
		return this.getUtility().checkHttpResponse(this.getHttpTransport(), response);
	}

	public String getHostURL()
	{
		return this._hostURL;
	}

	public String getBaseURL()
	{
		return this._baseURL;
	}

	public String getPersonDetailURL()
	{
		return this._personDetailURL;
	}

	public String getTagSearchURL()
	{
		return this._tagSearchURL;
	}

	public String getHomePageURL()
	{
		return this._homepageURL;
	}

	public String getLoginURL()
	{
		return this._loginURL; 
	}

	public String getLogoutURL()
	{
		return this._logoutURL;
	}

	public String getAddEventURL()
	{
		return this._addEventURL; 
	}

	public String getAddPersonURL()
	{
		return this._addPersonURL; 
	}

	public String getEventDetailURL()
	{
		return this._eventDetailURL;
	}

	public String getAddEventResultURL()
	{
		return this._addEventResultURL; 
	}

	public String getAddPersonResultURL()
	{
		return this._addPersonResultURL;
	}

	public String getAddAttendeeURL()
	{
		return this._addAttendeeURL;
	}

	public String getCheckNameURL()
	{
		return this._checkNameURL;
	}

	public String getFileServiceURL()
	{
		return this._fileServiceURL;
	}

	public String getTagCloudURL()
	{
		return this._tagCloudURL;
	}

	public String getImgStoreURL()
	{
		return this._imgStoreURL;
	}

	public String getDocStoreURL()
	{
		return this._docStoreURL;
	}

	public String[] getHomePageStatics()
	{
		return this._homepageStatics; 
	}

	public String[] getPersonStatics()
	{
		return this._personStatics; 
	}

//	public String[] getPersonGets()
//	{
//		return this._personGets;
//	}

	public String[] getTagSearchStatics()
	{
		return this._tagSearchStatics; 
	}

	public String[] getEventDetailStatics()
	{
		return this._eventDetailStatics; 
	}

	public String[] getAddPersonStatics()
	{
		return this._addPersonStatics; 
	}

	public String[] getAddEventStatics()
	{
		return this._addEventStatics;
	}

	public File getEventImgFile()
	{
		return this._eventImg;
	}

	public File getEventThumbImgFile()
	{
		return this._eventThumb;
	}

	public File getEventPdfFile()
	{
		return this._eventPdf; 
	}

	public File getPersonImgFile()
	{
		return this._personImg; 
	}

	public File getPersonThumbImgFile()
	{
		return this._personThumb;
	}

	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex   The type of operation to instantiate.
	 * @return          A prepared operation.
	 */
	public Operation getOperation(int opIndex)
	{
		switch(opIndex)
		{
			case HOME_PAGE_OP:
				return this.createHomePageOperation();
			case LOGIN_OP:
				return this.createLoginOperation();
			case TAG_SEARCH_OP:
				return this.createTagSearchOperation();
			case EVENT_DETAIL_OP:
				return this.createEventDetailOperation();
			case PERSON_DETAIL_OP:
				return this.createPersonDetailOperation();
			case ADD_PERSON_OP:
				return this.createAddPersonOperation();
			case ADD_EVENT_OP:
				return this.createAddEventOperation();	
			default:
		}

		return null;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared HomePageOperation.
	 */
	public HomePageOperation createHomePageOperation()
	{
		HomePageOperation op = new HomePageOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared LoginOperation.
	 */
	public LoginOperation createLoginOperation()
	{
		LoginOperation op = new LoginOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared TagSearchOperation.
	 */
	public TagSearchOperation createTagSearchOperation()
	{
		TagSearchOperation op = new TagSearchOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared EventDetailOperation.
	 */
	public EventDetailOperation createEventDetailOperation()
	{
		EventDetailOperation op = new EventDetailOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared PersonDetailOperation.
	 */
	public PersonDetailOperation createPersonDetailOperation()
	{
		PersonDetailOperation op = new PersonDetailOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AddPersonOperation.
	 */
	public AddPersonOperation createAddPersonOperation()
	{
		AddPersonOperation op = new AddPersonOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AddEventOperation.
	 */
	public AddEventOperation createAddEventOperation()
	{
		AddEventOperation op = new AddEventOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Initialize the fully qualified static URLs for the operations.
	 */
	private void initializeStaticUrls()
	{
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				this._homepageStatics = this.joinStatics(JAVA_COMMON_STATICS, JAVA_HOME_STATICS);
				this._personStatics = this.joinStatics(JAVA_COMMON_STATICS, JAVA_PERSON_STATICS);
				//this._personGets = this.joinStatics(JAVA_COMMON_STATICS, JAVA_PERSON_GETS);
				this._tagSearchStatics = this.joinStatics(JAVA_COMMON_STATICS, JAVA_TAG_SEARCH_STATICS);
				this._eventDetailStatics = this.joinStatics(JAVA_COMMON_STATICS, JAVA_EVENT_DETAIL_COMMON_STATICS, JAVA_EVENT_DETAIL_BASE_STATICS);
				this._addPersonStatics = this.joinStatics(JAVA_COMMON_STATICS, JAVA_ADD_PERSON_STATICS);
				this._addEventStatics = this.joinStatics(JAVA_COMMON_STATICS, JAVA_ADD_EVENT_STATICS);
				break;
			case OlioConfiguration.PHP_INCARNATION:
				this._homepageStatics = this.joinStatics(PHP_COMMON_STATICS, PHP_HOME_STATICS);
				this._personStatics = this.joinStatics(PHP_COMMON_STATICS, PHP_PERSON_STATICS);
				//this._personGets = this.joinStatics(PHP_COMMON_STATICS, PHP_PERSON_GETS);
				this._tagSearchStatics = this.joinStatics(PHP_COMMON_STATICS, PHP_TAG_SEARCH_STATICS);
				this._eventDetailStatics = this.joinStatics(PHP_COMMON_STATICS, PHP_EVENT_DETAIL_COMMON_STATICS, PHP_EVENT_DETAIL_BASE_STATICS);
				this._addPersonStatics = this.joinStatics(PHP_COMMON_STATICS, PHP_ADD_PERSON_STATICS);
				this._addEventStatics = this.joinStatics(PHP_COMMON_STATICS, PHP_ADD_EVENT_STATICS);
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				this._homepageStatics = this.joinStatics(RAILS_COMMON_STATICS, RAILS_HOME_STATICS);
				this._personStatics = this.joinStatics(RAILS_COMMON_STATICS, RAILS_PERSON_STATICS);
				//this._personGets = this.joinStatics(RAILS_COMMON_STATICS, RAILS_PERSON_GETS);
				this._tagSearchStatics = this.joinStatics(RAILS_COMMON_STATICS, RAILS_TAG_SEARCH_STATICS);
				this._eventDetailStatics = this.joinStatics(RAILS_COMMON_STATICS, RAILS_EVENT_DETAIL_COMMON_STATICS, RAILS_EVENT_DETAIL_BASE_STATICS);
				this._addPersonStatics = this.joinStatics(RAILS_COMMON_STATICS, RAILS_ADD_PERSON_STATICS);
				this._addEventStatics = this.joinStatics(RAILS_COMMON_STATICS, RAILS_ADD_EVENT_STATICS);
				break;
		}
	}

	/**
	 * Initialize local resources (e.g. files that we need to submit).
	 */
	private void initializeLocalFileResources()
	{
		String resourcePath = this._loadTrack.getResourcePath();
		if(!resourcePath.endsWith(File.separator))
		{
			resourcePath += File.separator;
		}

		this._eventImg    = new File(resourcePath + "event.jpg");
		this._eventThumb  = new File(resourcePath + "event_thumb.jpg");
		this._eventPdf    = new File(resourcePath + "event.pdf");
		this._personImg   = new File(resourcePath + "person.jpg");
		this._personThumb = new File(resourcePath + "person_thumb.jpg");
	}

	/**
	 * Initialize the roots/anchors of the URLs.
	 */
	private void initializeUrlAnchors()
	{
		this._hostURL = "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();

		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				this._baseURL = this._hostURL + JAVA_CONTEXT_ROOT;
				this._tagSearchURL = this._baseURL + "/tag/display";
				this._tagCloudURL = this._baseURL + "/tag/display";
				this._addEventURL = this._baseURL + "/event/addEvent";
				this._addEventResultURL = this._baseURL + "/api/event/addEvent";
				this._addPersonURL = this._baseURL + "/site.jsp?page=addPerson.jsp";
				this._addPersonResultURL = this._baseURL + "/api/person/fileuploadPerson";
				this._homepageURL = this._baseURL + "/index.jsp";
				this._loginURL = this._baseURL + "/person/login";
				this._logoutURL = this._baseURL + "/logout";
				this._addAttendeeURL = this._baseURL + "/api/event/addAttendee?socialEventID=";
				//this._eventDetailURL = this._baseURL + "/event/detail";
				this._eventDetailURL = this._baseURL + "/event/detail?socialEventID=";
				this._personDetailURL = this._baseURL + "/person?actionType=display_person&user_name=";
				this._fileServiceURL = this._baseURL + "/access-artifacts";
				this._imgStoreURL = this._baseURL + "/access-artifacts";
				this._docStoreURL = this._baseURL + "/access-artifacts";
				break;
			case OlioConfiguration.PHP_INCARNATION:
				this._baseURL = this._hostURL + PHP_CONTEXT_ROOT;
				this._personDetailURL = this._baseURL + "/users.php?username?";
				this._tagSearchURL = this._baseURL + "/taggedEvents.php";
				this._tagCloudURL = this._baseURL + "/taggedEvents.php";
				this._addEventURL = this._baseURL + "/addEvent.php";
				this._addEventResultURL  = this._baseURL + "/addEventResult.php";
				this._addPersonURL = this._baseURL + "/addPerson.php";
				this._addPersonResultURL = this._baseURL + "/addPersonResult.php";
				this._homepageURL = this._baseURL + "/index.php";
				this._loginURL = this._baseURL + "/login.php";
				this._logoutURL = this._baseURL + "/logout.php";
				this._addAttendeeURL = this._baseURL + "/addAttendee.php?id=";
				this._eventDetailURL = this._baseURL + "/events.php?socialEventID=";
				this._fileServiceURL = this._baseURL + "/fileService.php?";
				this._imgStoreURL = this._baseURL + "/fileService.php";
				this._docStoreURL = this._baseURL + "/fileService.php";
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				this._baseURL = this._hostURL + RAILS_CONTEXT_ROOT;
				this._personDetailURL = this._baseURL + "/users/";
				this._tagSearchURL = this._baseURL + "/events/tag_search/";
				this._tagCloudURL = this._baseURL + "/tagCloud.html";
				this._addEventURL = this._baseURL + "/events/new";
				this._addEventResultURL  = this._baseURL + "/events/";
				this._addPersonURL = this._baseURL + "/users/new";
				this._addPersonResultURL = this._baseURL + "/users";
				this._homepageURL = this._baseURL + "/";
				this._loginURL = this._baseURL + "/users/login";
				this._logoutURL = this._baseURL + "/users/logout";
				this._addAttendeeURL = this._baseURL + "/events/";
				this._eventDetailURL = this._baseURL + "/events/";
				this._fileServiceURL = this._baseURL + "/fileService.html?";
				this._checkNameURL = this._baseURL + "/users/check_name";
				this._imgStoreURL = this._baseURL + "/uploaded_files";
				this._docStoreURL = this._baseURL + "/uploaded_files";
				break;
		}
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
	private String[] joinStatics(String[] ... staticsLists) 
	{
		Set<String> urlSet = new LinkedHashSet<String>();

		for (String[] staticList : staticsLists)
		{
			for (int i = 0; i < staticList.length; ++i)
			{
				String url = this._baseURL + staticList[i].trim();
				urlSet.add(url);
			}
		}

		return (String[]) urlSet.toArray(new String[0]);
	}
}
