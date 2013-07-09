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
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Semaphore;
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

	// Configuration keys
	public static final String CFG_RNG_SEED_KEY = "rngSeed";


	// Static members used to atomically generate users and items
	private static AtomicInteger _userId = new AtomicInteger(RubisConstants.MIN_FREE_USER_ID-1);
	private static AtomicInteger _itemId = new AtomicInteger(RubisConstants.MIN_FREE_ITEM_ID-1);
	private static Semaphore _userLock = new Semaphore(1, true);
	private static Semaphore _itemLock = new Semaphore(1, true);


	private Random _rng; ///< The Random Number Generator
	private long _rngSeed = -1; ///< The seed used for the Random Number Generator; a value <= 0 means that no special seed is used.
	private HttpTransport _http;
	private Logger _logger;
//	private int _loggedUserId;
	private RubisSessionState _sessionState; ///< Holds session data
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
	private String _aboutMePostURL;
	private RubisSessionState _userSession;


	public static int nextUserId()
	{
		return _userId.incrementAndGet();
	}

	public static int lastUserId()
	{
		return _userId.get();
	}

	public static int nextItemId()
	{
		return _itemId.incrementAndGet();
	}

	public static int lastItemId()
	{
		return _itemId.get();
	}

	public static void lockUsers() throws InterruptedException
	{
		_userLock.acquire();
	}

	public static void unlockUsers()
	{
		_userLock.release();
	}

	public static void lockItems() throws InterruptedException
	{
		_itemLock.acquire();
	}

	public static void unlockItems()
	{
		_itemLock.release();
	}


	public RubisGenerator(ScenarioTrack track)
	{
		super(track);
	}

	/**
	 * Initialize this generator.
	 */
	@Override
	public void initialize()
	{
		this._http = new HttpTransport();
		if (this._rngSeed >= 0)
		{
			this._rng = new Random(this._rngSeed);
		}
		else
		{
			this._rng = new Random();
		}
		this._logger = Logger.getLogger(this.getName());
		this._sessionState = new RubisSessionState();
//		this._loggedUserId = ANONYMOUS_USER_ID;
		this._utility = new RubisUtility();
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

		this.initializeUrls();
	}

	@Override
	public void configure(JSONObject config) throws JSONException
	{
		if (config.has(CFG_RNG_SEED_KEY))
		{
			this._rngSeed = config.getLong(CFG_RNG_SEED_KEY);
		}
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
			nextOperation = HOME_OP;
		}
		else if (lastOperation == BACK_SPECIAL_OP)
		{
			nextOperation = Math.max(HOME_OP, this._sessionState.getLastOperation());
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
		// TODO: Fill me in.
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
	 * Returns the internally used random number generator.
	 * 
	 * @return A Random object.
	 */
	public Random getRandomGenerator()
	{
		return this._rng;
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

	public boolean isUserLoggedIn()
	{
		return RubisConstants.ANONYMOUS_USER_ID != this._sessionState.getLoggedUserId() && RubisConstants.MIN_USER_ID <= this._sessionState.getLoggedUserId();
	}

//	public int getLoggedUserId()
//	{
//		return this._loggedUserId;
//	}
//
//	public void setLoggedUserId(int val)
//	{
//		this._loggedUserId = val;
//	}

	public RubisUser getLoggedUser()
	{
		if (!this.isUserLoggedIn())
		{
			return null;
		}

		return this.getUser(this._sessionState.getLoggedUserId());
	}

	/**
 	 * Tells if at least one user exists in the DB.
 	 *
 	 * A user exists in the DB if either (s)he has been generated by the
 	 * current run of the benchmark or (s)he was already present in the DB.
 	 *
 	 * @return <code>true</code> if at least one user exists in the DB;
 	 *  <code>false</code> otherwise.
 	 */
	public boolean isUserAvailable()
	{
		return RubisConstants.MIN_USER_ID <= RubisGenerator.lastUserId();
	}

	/**
 	 * Tells if at least one item exists in the DB.
 	 *
 	 * An item exists in the DB if either it has been generated by the
 	 * current run of the benchmark or it was already present in the DB.
 	 *
 	 * @return <code>true</code> if at least one item exists in the DB;
 	 *  <code>false</code> otherwise.
 	 */
	public boolean isItemAvailable()
	{
		return RubisConstants.MIN_ITEM_ID <= RubisGenerator.lastItemId();
	}

	public boolean isValidUser(RubisUser user)
	{
		return null != user && RubisConstants.MIN_USER_ID <= user.id;
	}

	public boolean isValidItem(RubisItem item)
	{
		return null != item && RubisConstants.MIN_ITEM_ID <= item.id;
	}

	public boolean isValidCategory(RubisCategory category)
	{
		return null != category && RubisConstants.MIN_CATEGORY_ID <= category.id;
	}

	public boolean isValidRegion(RubisRegion region)
	{
		return null != region && RubisConstants.MIN_REGION_ID <= region.id;
	}

	public boolean checkHttpResponse(String response)
	{
		if (response.length() == 0
			|| HttpStatus.SC_OK != this.getHttpTransport().getStatusCode()
			|| -1 != response.indexOf("ERROR"))
		{
			return false;
		}

		return true;
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

	public String getAboutMePostURL()
	{
		return this._aboutMePostURL;
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
	 * Factory method.
	 * 
	 * @return  A prepared DummyOperation.
	 */
	public DummyOperation createDummyOperation()
	{
		DummyOperation op = new DummyOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	public RubisUser newUser()
	{
		return this.getUser(RubisGenerator.nextUserId());
	}

	public RubisUser generateUser()
	{
		int userId = RubisConstants.MIN_USER_ID-1;;
		int lastUserId = RubisGenerator.lastUserId();
		if (lastUserId >= RubisConstants.MIN_USER_ID)
		{
			userId = this._rng.nextInt(lastUserId+1-RubisConstants.MIN_USER_ID)+RubisConstants.MIN_USER_ID;
		}
		return this.getUser(userId);
	}

	public RubisUser getUser(int id)
	{
		RubisUser user = new RubisUser();

		user.id = id;
		user.firstname = "Great" + user.id;
		user.lastname = "User" + user.id;
		user.nickname = "user" + user.id;
		user.email = user.firstname + "." + user.lastname + "@rubis.com";
		user.password = "password" + user.id;
		user.region = this.generateRegion().id;

		return user;
	}

	public RubisItem newItem()
	{
		return this.getItem(RubisGenerator.nextItemId());
	}

	public RubisItem generateItem()
	{
		int itemId = RubisConstants.MIN_ITEM_ID-1;
		int lastItemId = RubisGenerator.lastItemId();
		if (lastItemId >= RubisConstants.MIN_ITEM_ID)
		{
			itemId = this._rng.nextInt(lastItemId+1-RubisConstants.MIN_ITEM_ID)+RubisConstants.MIN_ITEM_ID;
		}
		return this.getItem(itemId);
	}

	public RubisItem getItem(int id)
	{
		RubisItem item = new RubisItem();

		item.id = id;
		item.name = "RUBiS automatically generated item #" + item.id;
		item.description = this.generateText(1, RubisConstants.MAX_ITEM_DESCR_LEN);
		item.initialPrice = this._rng.nextInt(RubisConstants.MAX_ITEM_INIT_PRICE)+1;
		if (this._rng.nextInt(RubisConstants.TOTAL_ACTIVE_ITEMS) < (RubisConstants.PERCENT_UNIQUE_ITEMS*RubisConstants.TOTAL_ACTIVE_ITEMS/100))
		{
			item.quantity = 1;
		}
		else
		{
			item.quantity = this._rng.nextInt(RubisConstants.MAX_ITEM_QUANTITY)+1;
		}
		if (this._rng.nextInt(RubisConstants.TOTAL_ACTIVE_ITEMS) < (RubisConstants.PERCENT_ITEMS_RESERVE_PRICE*RubisConstants.TOTAL_ACTIVE_ITEMS/100))
		{
			item.reservePrice = this._rng.nextInt(RubisConstants.MIN_ITEM_RESERVE_PRICE)+item.initialPrice;
		}
		else
		{
			item.reservePrice = 0;
		}
		if (this._rng.nextInt(RubisConstants.TOTAL_ACTIVE_ITEMS) < (RubisConstants.PERCENT_ITEMS_BUY_NOW_PRICE*RubisConstants.TOTAL_ACTIVE_ITEMS/100))
		{
			item.buyNow = this._rng.nextInt(RubisConstants.MIN_ITEM_BUY_NOW_PRICE)+item.initialPrice+item.reservePrice;
		}
		else
		{
			item.buyNow = 0;
		}
		item.nbOfBids = 0;
		item.maxBid = 0;
		Calendar cal = Calendar.getInstance();
		item.startDate = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, this._rng.nextInt(RubisConstants.MAX_ITEM_DURATION)+1);
		item.endDate = cal.getTime();
		item.seller = this._sessionState.getLoggedUserId();
		item.category = this.generateCategory().id;

		return item;
	}

	public RubisCategory generateCategory()
	{
		return this.getCategory(this._rng.nextInt(RubisConstants.CATEGORIES.length-RubisConstants.MIN_CATEGORY_ID)+RubisConstants.MIN_CATEGORY_ID);
	}

	public RubisRegion generateRegion()
	{
		return this.getRegion(this._rng.nextInt(RubisConstants.REGIONS.length-RubisConstants.MIN_REGION_ID)+RubisConstants.MIN_REGION_ID);
	}

	public RubisCategory getCategory(int id)
	{
		RubisCategory category = new RubisCategory();

		category.id = id;
		category.name = RubisConstants.CATEGORIES[category.id];

		return category;
	}

	public RubisRegion getRegion(int id)
	{
		RubisRegion region = new RubisRegion();

		region.id = id;
		region.name = RubisConstants.REGIONS[region.id];

		return region;
	}

	public int getNumItemsPerPage()
	{
		return RubisConstants.NUM_ITEMS_PER_PAGE;
	}

	public int getMaxAddBid()
	{
		return RubisConstants.MAX_ADD_BID;
	}

	public int getMaxCommentLength()
	{
		return RubisConstants.MAX_COMMENT_LEN;
	}

	public RubisComment generateComment(int fromUserId, int toUserId, int itemId)
	{
		return getComment(fromUserId,
						  toUserId,
						  itemId,
						  RubisConstants.COMMENT_RATINGS[this._rng.nextInt(RubisConstants.COMMENT_RATINGS.length)]);
	}

	public RubisComment getComment(int fromUserId, int toUserId, int itemId, int rating)
	{
		RubisComment comment = new RubisComment();

		comment.fromUserId = fromUserId;
		comment.toUserId = toUserId;
		comment.itemId = itemId;
		int rateIdx = Arrays.binarySearch(RubisConstants.COMMENT_RATINGS, rating);
		comment.rating = RubisConstants.COMMENT_RATINGS[rateIdx];
		comment.comment = this.generateText(1, RubisConstants.MAX_COMMENT_LEN-RubisConstants.COMMENTS[rateIdx].length()-System.lineSeparator().length()) + System.lineSeparator() + RubisConstants.COMMENTS[rateIdx];
		Calendar cal = Calendar.getInstance();
		comment.date = cal.getTime();

		return comment;
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
			case BACK_SPECIAL_OP:
				return this.createDummyOperation();
			case EOS_SPECIAL_OP:
				return this.createDummyOperation();
			default:
		}

		return null;
	}

	/**
	 * Generates a random text.
	 *
	 * @param minLen The minimum length of the text.
	 * @param maxLen The maximum length of the text.
	 * @return The generated text.
	 */
	private String generateText(int minLen, int maxLen)
	{
		int len = minLen+this._rng.nextInt(maxLen-minLen+1);
		StringBuilder buf = new StringBuilder(len);
		int left = len;
		while (left > 0)
		{
			if (buf.length() > 0)
			{
				buf.append(' ');
				--left;
			}

			String word = this.generateWord(1, left < RubisConstants.MAX_WORD_LEN ? left : RubisConstants.MAX_WORD_LEN);
			buf.append(word);
			left -= word.length();
		}

		return buf.toString();
	}

	/**
	 * Generates a random word.
	 *
	 * @param minLen The minimum length of the word.
	 * @param maxLen The maximum length of the word.
	 * @return The generated word.
	 */
	private String generateWord(int minLen, int maxLen)
	{
		if (minLen > maxLen)
		{
			return "";
		}

		int len = minLen+this._rng.nextInt(maxLen-minLen+1);

		char[] buf = new char[len];

		for (int i = 0; i < len; ++i)
		{
			int j = this._rng.nextInt(RubisConstants.ALNUM_CHARS.length);
			buf[i] = RubisConstants.ALNUM_CHARS[j];
		}

		return new String(buf);
	}

	/**
	 * Initialize the roots/anchors of the URLs.
	 */
	private void initializeUrls()
	{
		this._baseURL = "http://" + this.getTrack().getTargetHostName() + ":" + this.getTrack().getTargetHostPort();
		this._homeURL = this._baseURL + "/rubis_servlets/";
		this._registerURL = this._baseURL + "/rubis_servlets/register.html";
		this._registerUserURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.RegisterUser";
		this._browseURL = this._baseURL + "/rubis_servlets/browse.html";
		this._browseCategoriesURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BrowseCategories";
		this._searchItemsByCategoryURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByCategory";
		this._browseRegionsURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BrowseRegions";
		this._browseCategoriesByRegionURL = this._browseCategoriesURL;
		this._searchItemsByRegionURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByRegion";
		this._viewItemURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.ViewItem";
		this._viewUserInfoURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.ViewUserInfo";
		this._viewBidHistoryURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.ViewBidHistory";
		this._buyNowAuthURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BuyNowAuth";
		this._buyNowURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BuyNow";
		this._storeBuyNowURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreBuyNow";
		this._putBidAuthURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutBidAuth";
		this._putBidURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutBid";
		this._storeBidURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreBid";
		this._putCommentAuthURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutCommentAuth";
		this._putCommentURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutComment";	
		this._storeCommentURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreComment";
		this._sellURL = this._baseURL + "/rubis_servlets/sell.html";
		this._selectCategoryToSellItemURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BrowseCategories";
		this._sellItemFormURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SellItemForm";
		this._registerItemURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.RegisterItem";
		this._aboutMeAuthURL = this._baseURL + "/rubis_servlets/about_me.html";
		this._aboutMeURL = this._baseURL + "/rubis_servlets/about_me.html";
		this._aboutMePostURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.AboutMe";
	}
}
