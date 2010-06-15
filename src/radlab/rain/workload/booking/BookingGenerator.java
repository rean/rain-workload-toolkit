package radlab.rain.workload.booking;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;

/**
 * The BookingGenerator class generates operations for a single user thread
 * by producing the next operation to execute given the last operation. The
 * next operation is decided through the use of a load mix matrix. 
 */
public class BookingGenerator extends Generator
{
	
	// Operation indices used in the mix matrix.
	public static final int HOME_PAGE = 0;
	public static final int LOGIN = 1;
	public static final int LOGOUT = 2;
	public static final int SEARCH_HOTEL = 3;
	public static final int SEARCH_HOTEL_RESULTS = 4;
	public static final int VIEW_HOTEL = 5;
	public static final int BOOK_HOTEL = 6;
	public static final int CONFIRM_HOTEL = 7;
	
	/** Static URLs loaded as part of the Home Page. */
	protected static final String[] STATIC_RELATIVE_HOME_URLS = {
		"/resources/dijit/themes/tundra/tundra.css",
		"/styles/blueprint/screen.css",
		"/styles/blueprint/print.css",
		"/styles/booking.css",
		"/resources/dijit/themes/dijit.css",
		"/resources/dijit/themes/tundra/Common.css",
		"/resources/dijit/themes/tundra/layout/ContentPane.css",
		"/resources/dijit/themes/tundra/layout/TabContainer.css",
		"/resources/dijit/themes/tundra/layout/AccordionContainer.css",
		"/resources/dijit/themes/tundra/layout/SplitContainer.css",
		"/resources/dijit/themes/tundra/layout/BorderContainer.css",
		"/resources/dijit/themes/tundra/form/Common.css",
		"/resources/dijit/themes/tundra/form/Button.css",
		"/resources/dijit/themes/tundra/form/Checkbox.css",
		"/resources/dijit/themes/tundra/form/RadioButton.css",
		"/resources/dijit/themes/tundra/form/Slider.css",
		"/resources/dijit/themes/tundra/Tree.css",
		"/resources/dijit/themes/tundra/ProgressBar.css",
		"/resources/dijit/themes/tundra/TitlePane.css",
		"/resources/dijit/themes/tundra/Calendar.css",
		"/resources/dijit/themes/tundra/TimePicker.css",
		"/resources/dijit/themes/tundra/Toolbar.css",
		"/resources/dijit/themes/tundra/Dialog.css",
		"/resources/dijit/themes/tundra/Menu.css",
		"/resources/dijit/themes/tundra/Editor.css",
		"/resources/dijit/themes/tundra/ColorPalette.css",
		"/resources/dijit/themes/dijit_rtl.css",
		"/resources/dijit/themes/tundra/Calendar_rtl.css",
		"/resources/dijit/themes/tundra/Dialog_rtl.css",
		"/resources/dijit/themes/tundra/Editor_rtl.css",
		"/resources/dijit/themes/tundra/Menu_rtl.css",
		"/resources/dijit/themes/tundra/Tree_rtl.css",
		"/resources/dijit/themes/tundra/TitlePane_rtl.css",
		"/resources/dijit/themes/tundra/layout/TabContainer_rtl.css",
		"/resources/dijit/themes/tundra/form/Slider_rtl.css",
	};

	/** Static URLs loaded as part of the Login or LoginProcess Page. */
	protected static final String[] STATIC_RELATIVE_LOGIN_URLS = {
		"/images/btn.bg.gif",
		"/resources/spring/Spring.js"
	};

	/** Static URLs loaded as part of the Search or Search Results Pages. */
	protected static final String[] STATIC_RELATIVE_SEARCH_URLS = {
		"/resources/dojo/dojo.js",
		"/resources/spring/Spring-Dojo.js",
		"/resources/dojo/nls/dojo_en-us.js",
		"/resources/dojo/resources/blank.gif",
		"/resources/dijit/themes/tundra/images/warning.png",
		"/resources/dijit/themes/tundra/images/validationInputBg.png",
		"/images/th.bg.gif"
	};

	// Login 
	// 200	image/gif	/images/btn.bg.gif
	//
	// Login Process
	// 200	text/javascript	resources/spring/Spring.js
	//
	// Search
	// 200	text/javascript	/resources/dojo/dojo.js
	// 200	text/javascript	/resources/spring/Spring-Dojo.js
	// 200	text/javascript	/resources/dojo/nls/dojo_en-us.js
	// 200	image/gif	/resources/dojo/resources/blank.gif
	// 200	image/png	/resources/dijit/themes/tundra/images/warning.png
	// 200	image/png	resources/dijit/themes/tundra/images/validationInputBg.png
	//
	// Search Results
	// 200	image/gif	/images/th.bg.gif
	
	public String[] staticHomePageUrls;
    public String[] staticLoginPageUrls;
    public String[] staticSearchPageUrls;
  
    public boolean staticHomePageUrlsLoaded = false;
    public boolean staticLoginPageUrlsLoaded = false;
    public boolean staticSearchPageUrlsLoaded = false;
    
	public String baseUrl;
	public String homePageUrl;
	public String loginUrl;
	public String loginProcessUrl;
	public String logoutUrl;
	public String searchHotelUrl;
	public String searchHotelResultsUrl;
	public String viewHotelUrl;
	public String bookHotelUrl;
    public String confirmHotelUrl;
	
	private java.util.Random _randomNumberGenerator;
	private HttpTransport _http;

	private String currentUser;
	private String lastUrl;				// Usually a search, or view URL.
	
	/**
	 * Initialize a <code>BookingGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public BookingGenerator( ScenarioTrack track )
	{
		super( track );
		
		this.baseUrl = "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort() + "/swf-booking-faces";
		this.homePageUrl 			= this.baseUrl + "/spring/intro";
		this.loginUrl        		= this.baseUrl + "/spring/login";
		this.loginProcessUrl        = this.baseUrl + "/spring/loginProcess";
		this.logoutUrl       		= this.baseUrl + "/spring/logout";
		this.searchHotelUrl      	= this.baseUrl + "/spring/main";
		this.searchHotelResultsUrl	= this.baseUrl + "/spring/main";
		this.viewHotelUrl      		= this.baseUrl + "/spring/main";
		this.bookHotelUrl      		= this.baseUrl + "/spring/main";
		this.confirmHotelUrl      	= this.baseUrl + "/spring/main";
		
		this.staticHomePageUrls = new String[STATIC_RELATIVE_HOME_URLS.length];
		for ( int i = 0; i < STATIC_RELATIVE_HOME_URLS.length; i++ )
		{
			this.staticHomePageUrls[i] = baseUrl + STATIC_RELATIVE_HOME_URLS[i].trim();
		}

		this.staticLoginPageUrls = new String[STATIC_RELATIVE_LOGIN_URLS.length];
		for ( int i = 0; i < STATIC_RELATIVE_LOGIN_URLS.length; i++ )
		{
			this.staticLoginPageUrls[i] = baseUrl + STATIC_RELATIVE_LOGIN_URLS[i].trim();
		}

		this.staticSearchPageUrls = new String[STATIC_RELATIVE_SEARCH_URLS.length];
		for ( int i = 0; i < STATIC_RELATIVE_SEARCH_URLS.length; i++ )
		{
			this.staticSearchPageUrls[i] = baseUrl + STATIC_RELATIVE_SEARCH_URLS[i].trim();
		}
}
	
	/**
	 * Initialize this generator.
	 */
	public void initialize()
	{
		this._randomNumberGenerator = new java.util.Random();
		this._http = new HttpTransport();
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
		return this._thinkTime;
	}
	
	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	public long getCycleTime()
	{
		return this._cycleTime;
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
	 * Returns the pre-existing Random Number Generator.
	 * 
	 * @return          A java.util.Random.
	 */
	public java.util.Random getRandomNumberGenerator()
	{
		return this._randomNumberGenerator;
	}

	/**
	 * Returns the username of the currently logged in user.
	 * 
	 * @return          A String.
	 */
	public String getCurrentUser()
	{
		return this.currentUser;
	}

	/**
	 * Sets the username of the currently logged in user.
	 * 
	 */
	public void setCurrentUser(String username)
	{
		this.currentUser = username;
	}

	/**
	 * Returns the URL used in the last Booking operation.
	 * 
	 * @return          A String.
	 */
	public String getLastUrl()
	{
		return this.lastUrl;
	}

	/**
	 * Sets the URL of the last Booking operation.  This allows
	 * the generator to do an HTTP GET to the proper web flow. 
	 * 
	 * @return          A String.
	 */
	public void setLastUrl(String url)
	{
		this.lastUrl = url;
	}

	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	public void dispose()
	{
		// TODO: Fill me in.
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
			case HOME_PAGE:				return this.createHomePageOperation();
			case LOGIN:     			return this.createLoginOperation();
			case LOGOUT:    			return this.createLogoutOperation();
			case SEARCH_HOTEL:    		return this.createSearchHotelOperation();
			case SEARCH_HOTEL_RESULTS:	return this.createSearchHotelResultsOperation();
			case VIEW_HOTEL:			return this.createViewHotelOperation();
			case BOOK_HOTEL:			return this.createBookHotelOperation();
			case CONFIRM_HOTEL:			return this.createConfirmHotelOperation();
			default:        			return null;
		}
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoriesIndexOperation.
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
	 * @return  A prepared LogoutOperation.
	 */
	public LogoutOperation createLogoutOperation()
	{
		LogoutOperation op = new LogoutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchHotelOperation.
	 */
	public SearchHotelOperation createSearchHotelOperation()
	{
		SearchHotelOperation op = new SearchHotelOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchHotelResultsOperation.
	 */
	public SearchHotelResultsOperation createSearchHotelResultsOperation()
	{
		SearchHotelResultsOperation op = new SearchHotelResultsOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewHotelOperation.
	 */
	public ViewHotelOperation createViewHotelOperation()
	{
		ViewHotelOperation op = new ViewHotelOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewHotelOperation.
	 */
	public BookHotelOperation createBookHotelOperation()
	{
		BookHotelOperation op = new BookHotelOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ConfirmHotelOperation.
	 */
	public ConfirmHotelOperation createConfirmHotelOperation()
	{
		ConfirmHotelOperation op = new ConfirmHotelOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

}
