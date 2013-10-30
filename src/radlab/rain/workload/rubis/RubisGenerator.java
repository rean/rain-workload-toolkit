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
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013.
 */

package radlab.rain.workload.rubis;


import java.util.Arrays;
//import java.util.Calendar;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import java.util.Random;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.json.JSONException;
import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;
import radlab.rain.workload.rubis.model.RubisCategory;
import radlab.rain.workload.rubis.model.RubisComment;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisRegion;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Operation generator for the RUBiS workload.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class RubisGenerator extends Generator
{
	// Operation indices used in the mix matrix.
	// Use the same order of the native RUBiS mix matrix
	public static final int HOME_OP = 0;
	public static final int REGISTER_OP = 1;
	public static final int REGISTER_USER_OP = 2;
	public static final int BROWSE_OP = 3;
	public static final int BROWSE_CATEGORIES_OP = 4;
	public static final int SEARCH_ITEMS_BY_CATEGORY_OP = 5;
	public static final int BROWSE_REGIONS_OP = 6;
	public static final int BROWSE_CATEGORIES_BY_REGION_OP = 7;
	public static final int SEARCH_ITEMS_BY_REGION_OP = 8;
	public static final int VIEW_ITEM_OP = 9;
	public static final int VIEW_USER_INFO_OP = 10;
	public static final int VIEW_BID_HISTORY_OP = 11;
	public static final int BUY_NOW_AUTH_OP = 12;
	public static final int BUY_NOW_OP = 13;
	public static final int STORE_BUY_NOW_OP = 14;
	public static final int PUT_BID_AUTH_OP = 15;
	public static final int PUT_BID_OP = 16;
	public static final int STORE_BID_OP = 17;
	public static final int PUT_COMMENT_AUTH_OP = 18;
	public static final int PUT_COMMENT_OP = 19;
	public static final int STORE_COMMENT_OP = 20;
	public static final int SELL_OP = 21;
	public static final int SELECT_CATEGORY_TO_SELL_ITEM_OP = 22;
	public static final int SELL_ITEM_FORM_OP = 23;
	public static final int REGISTER_ITEM_OP = 24;
	public static final int ABOUT_ME_AUTH_OP = 25;
	public static final int ABOUT_ME_OP = 26;
	public static final int BACK_SPECIAL_OP = 27; ///< Emulate a click on the "Back" button of the browser
	public static final int EOS_SPECIAL_OP = 28; ///< Terminate the current user session

	// Static members shared among all instances
	private static Random _rng; ///< The Random Number Generator
	private static RubisConfiguration _conf; ///< The RUBiS-related configuration found in the JSON profile file


	private HttpTransport _http;
	private Logger _logger;
	private RubisSessionState _sessionState; ///< Holds user session data
	private RubisUtility _utility;
	private double _thinkTime = -1; ///< The mean think time; a value <= 0 means that no think time is used.
	private NegativeExponential _thinkTimeRng;
	private double _cycleTime = -1; ///< The mean cycle time; a value <= 0 means that no cycle time is used.
	private NegativeExponential _cycleTimeRng;
	private String _baseURL;
	private String _homeURL; 
	private String _registerURL;
	private String _registerUserURL;
	private String _browseURL;
	private String _browseCategoriesURL; 
	private String _searchItemsByCategoryURL;
	private String _browseRegionsURL; 
	private String _browseCategoriesByRegionURL; 
	private String _searchItemsByRegionURL;
	private String _viewItemURL;
	private String _viewUserInfoURL;
	private String _viewBidHistoryURL;
	private String _buyNowAuthURL;
	private String _buyNowURL;
	private String _storeBuyNowURL;
	private String _putBidAuthURL;
	private String _putBidURL;
	private String _storeBidURL;
	private String _putCommentAuthURL;
	private String _putCommentURL;
	private String _storeCommentURL;
	private String _sellURL;
	private String _selectCategoryToSellItemURL;
	private String _sellItemFormURL;
	private String _registerItemURL;
	private String _aboutMeAuthURL;
	private String _aboutMeURL;


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
	 * @return A RubisConfiguration object.
	 */
	public static synchronized RubisConfiguration getConfiguration()
	{
		return _conf;
	}

	/**
	 * Set the internally used RUBiS configuration object.
	 * 
	 * @param value A RubisConfiguration object.
	 */
	protected static synchronized void setConfiguration(RubisConfiguration value)
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
			_conf = new RubisConfiguration(config);
		}
	}

	/**
	 * Initialize the shared random number generator.
	 */
	private static synchronized void initizializeRandomGenerator()
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
	 * A constructor.
	 */
	public RubisGenerator(ScenarioTrack track)
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
		this.initizializeRandomGenerator();
		this._http = new HttpTransport();
		this._logger = Logger.getLogger(this.getName());
		this._sessionState = new RubisSessionState();
		this._utility = new RubisUtility(this._rng, this._conf);

		// Setup think and cycle times
		this._thinkTime = this.getTrack().getMeanThinkTime();
		if (this._thinkTime > 0)
		{
			this._thinkTimeRng = new NegativeExponential(this._thinkTime, this._rng);
		}
		this._cycleTime = this.getTrack().getMeanCycleTime();
		if (this._cycleTime > 0)
		{
			this._cycleTimeRng = new NegativeExponential(this._cycleTime, this._rng);
		}

		// Select a random user for current session (if needed)
		this.getSessionState().setLoggedUserId(this.getUtility().generateUser().id);

		// Build RUBiS URLs
		this.initializeUrls();
	}

	/**
	 * Returns the next <code>Operation</code> given the <code>lastOperation</code>
	 * according to the current mix matrix.
	 * 
	 * @param lastOperation The last <code>Operation</code> that was executed.
	 */
	@Override
	public Operation nextRequest(int lastOperation)
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		int nextOperation = -1;

		if (lastOperation == -1)
		{
			nextOperation = this.getConfiguration().getInitialOperation();
		}
		else if (lastOperation == BACK_SPECIAL_OP)
		{
			// Back to previous state
			nextOperation = Math.max(this.getConfiguration().getInitialOperation(), this._sessionState.getLastOperation());
		}
		else if (lastOperation == EOS_SPECIAL_OP)
		{
			// End-of-session

			// Start from the initial operation
			nextOperation = this.getConfiguration().getInitialOperation();

			// Clear session data
			this.getSessionState().clear();
			// Generate a new user for the new session
			this.getSessionState().setLoggedUserId(this.getUtility().generateUser().id);
		}
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

		return Math.round(this._thinkTimeRng.nextDouble());
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

		return Math.round(this._cycleTimeRng.nextDouble());
	}

	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}

	/**
	 * Returns the logger.
	 * 
	 * @return A logger.
	 */
	public Logger getLogger()
	{
		return this._logger;
	}

	public RubisSessionState getSessionState()
	{
		return this._sessionState;
	}

	protected void setSessionState(RubisSessionState value)
	{
		this._sessionState = value;
	}

	public RubisUtility getUtility()
	{
		return this._utility;
	}

	protected void setUtility(RubisUtility value)
	{
		this._utility = value;
	}

	public boolean checkHttpResponse(String response)
	{
		return this.getUtility().checkHttpResponse(this.getHttpTransport(), response);
	}

	public String getBaseURL()
	{
		return this._baseURL;
	}

	public String getHomeURL()
	{
		return this._homeURL; 
	}

	public String getRegisterURL()
	{
		return this._registerURL;
	}

	public String getRegisterUserURL()
	{
		return this._registerUserURL;
	}

	public String getBrowseURL()
	{
		return this._browseURL;
	}

	public String getBrowseCategoriesURL()
	{
		return this._browseCategoriesURL; 
	}

	public String getSearchItemsByCategoryURL()
	{
		return this._searchItemsByCategoryURL;
	}

	public String getBrowseRegionsURL()
	{
		return this._browseRegionsURL; 
	}

	public String getBrowseCategoriesByRegionURL()
	{
		return this._browseCategoriesByRegionURL; 
	}

	public String getSearchItemsByRegionURL()
	{
		return this._searchItemsByRegionURL;
	}

	public String getViewItemURL()
	{
		return this._viewItemURL;
	}

	public String getViewUserInfoURL()
	{
		return this._viewUserInfoURL;
	}

	public String getViewBidHistoryURL()
	{
		return this._viewBidHistoryURL;
	}

	public String getBuyNowAuthURL()
	{
		return this._buyNowAuthURL;
	}

	public String getBuyNowURL()
	{
		return this._buyNowURL;
	}

	public String getStoreBuyNowURL()
	{
		return this._storeBuyNowURL;
	}

	public String getPutBidAuthURL()
	{
		return this._putBidAuthURL;
	}

	public String getPutBidURL()
	{
		return this._putBidURL;
	}

	public String getStoreBidURL()
	{
		return this._storeBidURL;
	}

	public String getPutCommentAuthURL()
	{
		return this._putCommentAuthURL;
	}

	public String getPutCommentURL()
	{
		return this._putCommentURL;
	}

	public String getStoreCommentURL()
	{
		return this._storeCommentURL;
	}

	public String getSellURL()
	{
		return this._sellURL;
	}

	public String getSelectCategoryToSellItemURL()
	{
		return this._selectCategoryToSellItemURL;
	}

	public String getSellItemFormURL()
	{
		return this._sellItemFormURL;
	}

	public String getRegisterItemURL()
	{
		return this._registerItemURL;
	}

	public String getAboutMeAuthURL()
	{
		return this._aboutMeAuthURL;
	}

	public String getAboutMeURL()
	{
		return this._aboutMeURL;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared Homepage.
	 */
	public HomeOperation createHomeOperation()
	{
		HomeOperation op = new HomeOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RegisterOperation.
	 */
	public RegisterOperation createRegisterOperation()
	{
		RegisterOperation op = new RegisterOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RegisterUserOperation.
	 */
	public RegisterUserOperation createRegisterUserOperation()
	{
		RegisterUserOperation op = new RegisterUserOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseOperation.
	 */
	public BrowseOperation createBrowseOperation()
	{
		BrowseOperation op = new BrowseOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseCategoriesOperation.
	 */
	public BrowseCategoriesOperation createBrowseCategoriesOperation()
	{
		BrowseCategoriesOperation op = new BrowseCategoriesOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchItemsByCategoryOperation.
	 */
	public SearchItemsByCategoryOperation createSearchItemsByCategoryOperation()
	{
		SearchItemsByCategoryOperation op = new SearchItemsByCategoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseRegionsOperation.
	 */
	public BrowseRegionsOperation createBrowseRegionsOperation()
	{
		BrowseRegionsOperation op = new BrowseRegionsOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseCategoriesByRegionOperation.
	 */
	public BrowseCategoriesByRegionOperation createBrowseCategoriesByRegionOperation()
	{
		BrowseCategoriesByRegionOperation op = new BrowseCategoriesByRegionOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchItemsByRegionOperation.
	 */
	public SearchItemsByRegionOperation createSearchItemsByRegionOperation()
	{
		SearchItemsByRegionOperation op = new SearchItemsByRegionOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewItemOperation.
	 */
	public ViewItemOperation createViewItemOperation()
	{
		ViewItemOperation op = new ViewItemOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewUserInfoOperation.
	 */
	public ViewUserInfoOperation createViewUserInfoOperation()
	{
		ViewUserInfoOperation op = new ViewUserInfoOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewBidHistoryOperation.
	 */
	public ViewBidHistoryOperation createViewBidHistoryOperation()
	{
		ViewBidHistoryOperation op = new ViewBidHistoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BuyNowAuthOperation.
	 */
	public BuyNowAuthOperation createBuyNowAuthOperation()
	{
		BuyNowAuthOperation op = new BuyNowAuthOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BuyNowOperation.
	 */
	public BuyNowOperation createBuyNowOperation()
	{
		BuyNowOperation op = new BuyNowOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoreBuyNowOperation.
	 */
	public StoreBuyNowOperation createStoreBuyNowOperation()
	{
		StoreBuyNowOperation op = new StoreBuyNowOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared PutBidAuthOperation.
	 */
	public PutBidAuthOperation createPutBidAuthOperation()
	{
		PutBidAuthOperation op = new PutBidAuthOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared PutBidOperation.
	 */
	public PutBidOperation createPutBidOperation()
	{
		PutBidOperation op = new PutBidOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoreBidOperation.
	 */
	public StoreBidOperation createStoreBidOperation()
	{
		StoreBidOperation op = new StoreBidOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared PutCommentAuthOperation.
	 */
	public PutCommentAuthOperation createPutCommentAuthOperation()
	{
		PutCommentAuthOperation op = new PutCommentAuthOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared PutCommentOperation.
	 */
	public PutCommentOperation createPutCommentOperation()
	{
		PutCommentOperation op = new PutCommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoreCommentOperation.
	 */
	public StoreCommentOperation createStoreCommentOperation()
	{
		StoreCommentOperation op = new StoreCommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SellOperation.
	 */
	public SellOperation createSellOperation()
	{
		SellOperation op = new SellOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SelectCategoryToSellItemOperation.
	 */
	public SelectCategoryToSellItemOperation createSelectCategoryToSellItemOperation()
	{
		SelectCategoryToSellItemOperation op = new SelectCategoryToSellItemOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SellItemFormOperation.
	 */
	public SellItemFormOperation createSellItemFormOperation()
	{
		SellItemFormOperation op = new SellItemFormOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RegisterItemOperation.
	 */
	public RegisterItemOperation createRegisterItemOperation()
	{
		RegisterItemOperation op = new RegisterItemOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AboutMeAuthOperation.
	 */
	public AboutMeAuthOperation createAboutMeAuthOperation()
	{
		AboutMeAuthOperation op = new AboutMeAuthOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AboutMeOperation.
	 */
	public AboutMeOperation createAboutMeOperation()
	{
		AboutMeOperation op = new AboutMeOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex The type of operation to instantiate.
	 * @return A prepared operation.
	 */
	private Operation getOperation(int opIndex)
	{
		switch (opIndex)
		{
			case HOME_OP:
				return this.createHomeOperation();
			case REGISTER_OP:
				return this.createRegisterOperation();
			case REGISTER_USER_OP:
				return this.createRegisterUserOperation();
			case BROWSE_OP:
				return this.createBrowseOperation();
			case BROWSE_CATEGORIES_OP:
				return this.createBrowseCategoriesOperation();
			case SEARCH_ITEMS_BY_CATEGORY_OP:
				return this.createSearchItemsByCategoryOperation();
			case BROWSE_REGIONS_OP:
				return this.createBrowseRegionsOperation();
			case BROWSE_CATEGORIES_BY_REGION_OP:
				return this.createBrowseCategoriesByRegionOperation();
			case SEARCH_ITEMS_BY_REGION_OP:
				return this.createSearchItemsByRegionOperation();
			case VIEW_ITEM_OP:
				return this.createViewItemOperation();
			case VIEW_USER_INFO_OP:
				return this.createViewUserInfoOperation();
			case VIEW_BID_HISTORY_OP:
				return this.createViewBidHistoryOperation();
			case BUY_NOW_AUTH_OP:
				return this.createBuyNowAuthOperation();
			case BUY_NOW_OP:
				return this.createBuyNowOperation();
			case STORE_BUY_NOW_OP:
				return this.createStoreBuyNowOperation();
			case PUT_BID_AUTH_OP:
				return this.createPutBidAuthOperation();
			case PUT_BID_OP:
				return this.createPutBidOperation();
			case STORE_BID_OP:
				return this.createStoreBidOperation();
			case PUT_COMMENT_AUTH_OP:
				return this.createPutCommentAuthOperation();
			case PUT_COMMENT_OP:
				return this.createPutCommentOperation();
			case STORE_COMMENT_OP:
				return this.createStoreCommentOperation();
			case SELL_OP:
				return this.createSellOperation();
			case SELECT_CATEGORY_TO_SELL_ITEM_OP:
				return this.createSelectCategoryToSellItemOperation();
			case SELL_ITEM_FORM_OP:
				return this.createSellItemFormOperation();
			case REGISTER_ITEM_OP:
				return this.createRegisterItemOperation();
			case ABOUT_ME_AUTH_OP:
				return this.createAboutMeAuthOperation();
			case ABOUT_ME_OP:
				return this.createAboutMeOperation();
			default:
		}

		return null;
	}

	/**
	 * Initialize the roots/anchors of the URLs.
	 */
	private void initializeUrls()
	{
		this.initializeCommonUrls();

		int incarnation = this.getConfiguration().getIncarnation();
		switch (incarnation)
		{
			case RubisConfiguration.PHP_INCARNATION:
				this.initializePhpUrls();
				break;
			case RubisConfiguration.SERVLET_INCARNATION:
				this.initializeServletUrls();
				break;
			default:
				this.getLogger().warning("RUBiS incarnation '" + incarnation + "' has not been implemented yet. Default to 'servlet'");
				this.initializeServletUrls();
				break;
		}
	}

	/**
	 * Initialize the roots/anchors of the URLs for the PHP incarnation.
	 */
	private void initializeCommonUrls()
	{
		String htmlPath = this.getConfiguration().getServerHtmlPath();

		this._baseURL = "http://" + this.getTrack().getTargetHostName() + ":" + this.getTrack().getTargetHostPort();
		this._homeURL = this._baseURL + htmlPath + "/";
		this._registerURL = this._baseURL + htmlPath + "/register.html";
		this._browseURL = this._baseURL + htmlPath + "/browse.html";
		this._sellURL = this._baseURL + htmlPath + "/sell.html";
		this._aboutMeAuthURL = this._baseURL + htmlPath + "/about_me.html";
	}

	/**
	 * Initialize the roots/anchors of the URLs for the PHP incarnation.
	 */
	private void initializePhpUrls()
	{
		String scriptPath = this.getConfiguration().getServerScriptPath();

		this._registerUserURL = this._baseURL + scriptPath + "/RegisterUser";
		this._browseCategoriesURL = this._baseURL + scriptPath + "/BrowseCategories";
		this._searchItemsByCategoryURL = this._baseURL + scriptPath + "/SearchItemsByCategory";
		this._browseRegionsURL = this._baseURL + scriptPath + "/BrowseRegions";
		this._browseCategoriesByRegionURL = this._browseCategoriesURL;
		this._searchItemsByRegionURL = this._baseURL + scriptPath + "/SearchItemsByRegion";
		this._viewItemURL = this._baseURL + scriptPath + "/ViewItem";
		this._viewUserInfoURL = this._baseURL + scriptPath + "/ViewUserInfo";
		this._viewBidHistoryURL = this._baseURL + scriptPath + "/ViewBidHistory";
		this._buyNowAuthURL = this._baseURL + scriptPath + "/BuyNowAuth";
		this._buyNowURL = this._baseURL + scriptPath + "/BuyNow";
		this._storeBuyNowURL = this._baseURL + scriptPath + "/StoreBuyNow";
		this._putBidAuthURL = this._baseURL + scriptPath + "/PutBidAuth";
		this._putBidURL = this._baseURL + scriptPath + "/PutBid";
		this._storeBidURL = this._baseURL + scriptPath + "/StoreBid";
		this._putCommentAuthURL = this._baseURL + scriptPath + "/PutCommentAuth";
		this._putCommentURL = this._baseURL + scriptPath + "/PutComment";	
		this._storeCommentURL = this._baseURL + scriptPath + "/StoreComment";
		this._selectCategoryToSellItemURL = this._baseURL + scriptPath + "/BrowseCategories";
		this._sellItemFormURL = this._baseURL + scriptPath + "/SellItemForm";
		this._registerItemURL = this._baseURL + scriptPath + "/RegisterItem";
		this._aboutMeURL = this._baseURL + scriptPath + "/AboutMe";
	}

	/**
	 * Initialize the roots/anchors of the URLs for the servlet incarnation.
	 */
	private void initializeServletUrls()
	{
		String scriptPath = this.getConfiguration().getServerScriptPath();

		this._registerUserURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.RegisterUser";
		this._browseCategoriesURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.BrowseCategories";
		this._searchItemsByCategoryURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.SearchItemsByCategory";
		this._browseRegionsURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.BrowseRegions";
		this._browseCategoriesByRegionURL = this._browseCategoriesURL;
		this._searchItemsByRegionURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.SearchItemsByRegion";
		this._viewItemURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.ViewItem";
		this._viewUserInfoURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.ViewUserInfo";
		this._viewBidHistoryURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.ViewBidHistory";
		this._buyNowAuthURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.BuyNowAuth";
		this._buyNowURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.BuyNow";
		this._storeBuyNowURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.StoreBuyNow";
		this._putBidAuthURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.PutBidAuth";
		this._putBidURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.PutBid";
		this._storeBidURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.StoreBid";
		this._putCommentAuthURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.PutCommentAuth";
		this._putCommentURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.PutComment";	
		this._storeCommentURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.StoreComment";
		this._selectCategoryToSellItemURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.BrowseCategories";
		this._sellItemFormURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.SellItemForm";
		this._registerItemURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.RegisterItem";
		this._aboutMeURL = this._baseURL + scriptPath + "/edu.rice.rubis.servlets.AboutMe";
	}
}
