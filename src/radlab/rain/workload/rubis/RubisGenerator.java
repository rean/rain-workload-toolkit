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


import java.util.Calendar;
import java.util.Random;
import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.workload.rubis.model.RubisCategory;
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
	//public static final int DO_NOTHING_OP = 0;
	public static final int HOME_PAGE_OP = 0;
	public static final int BROWSE_CATEGORIES_OP = 1;
	public static final int REGISTER_OP = 2;
	public static final int SELL_OP = 3;
	public static final int BID_OP = 4;
	public static final int COMMENT_OP = 5;
	public static final int BROWSE_OP = 9999;
	public static final int SEARCH_ITEMS_BY_CATEGORY_OP = 9999;
	public static final int BROWSE_REGIONS_OP = 9999;
//	public static final int BROWSE_CATEGORIES_IN_REGIONS_OP = 9999;
//	public static final int BROWSE_ITEMS_IN_REGIONS_OP = 9999;
//	public static final int VIEW_ITEM_OP = 9999;
//	public static final int VIEW_USER_OP = 9999;
//	public static final int VIEW_BID_HISTORY_OP = 9999;
//	public static final int BUY_NOW_AUTH_OP = 9999;
//	public static final int BUY_NOW_OP = 9999;

	/// The set of alphanumeric characters
	private static final char[] ALNUM_CHARS = { '0', '1', '2', '3', '4', '5',
												'6', '7', '8', '9', 'A', 'B',
												'C', 'D', 'E', 'F', 'G', 'H',
												'I', 'J', 'K', 'L', 'M', 'N',
												'O', 'P', 'Q', 'R', 'S', 'T',
												'U', 'V', 'W', 'X', 'Y', 'Z',
												'a', 'b', 'c', 'd', 'e', 'f',
												'g', 'h', 'i', 'j', 'k', 'l',
												'm', 'n', 'o', 'p', 'q', 'r',
												's', 't', 'u', 'v', 'w', 'x',
												'y', 'z'};

	/// A collection of e-Bay regions (see RUBiS 'ebay_regions.txt' file)
	private static final String[] REGIONS = { "AZ--Phoenix",
											  "CA--Los Angeles",
											  "CA--Oakland",
											  "CA--Sacramento",
											  "CA--San Diego",
											  "CA--San Francisco",
											  "CA--San Jose",
											  "CO--Denver",
											  "CT--Hartford",
											  "DC--Washington",
											  "FL--Jacksonville",
											  "FL--Miami",
											  "FL--Orlando",
											  "FL--Tampa-St. Pete",
											  "FL--West Palm Beach",
											  "GA--Atlanta",
											  "HI--Honolulu",
											  "ID--Billings-Boise",
											  "IL--Chicago",
											  "IN--Indianapolis",
											  "KS--Kansas City",
											  "KY--Louisville",
											  "LA--New Orleans",
											  "MA--Boston",
											  "MD--Baltimore",
											  "MI--Detroit",
											  "MI--Grand Rapids",
											  "MN--Minn-St. Paul",
											  "MO--Kansas City",
											  "MO--St. Louis",
											  "MT--Billings-Boise",
											  "NC--Charlotte",
											  "NC--Greensboro",
											  "NC--Raleigh-Durham",
											  "ND--Bismarck-Pierre",
											  "NM--Albuquerque",
											  "NV--Las Vegas",
											  "NY--Albany",
											  "NY--Buffalo",
											  "NY--New York",
											  "NY--Rochester",
											  "OH--Cincinnati",
											  "OH--Cleveland",
											  "OH--Columbus",
											  "OH--Dayton",
											  "OK--Oklahoma City",
											  "OR--Portland",
											  "PA--Philadelphia",
											  "PA--Pittsburgh",
											  "RI--Providence",
											  "SD--Bismarck-Pierre",
											  "TN--Memphis",
											  "TN--Nashville",
											  "TX--Austin",
											  "TX--Dallas-Fort Worth",
											  "TX--Houston",
											  "TX--San Antonio",
											  "UT--Salt Lake City",
											  "VA--Norfolk-VA Beach",
											  "VA--Richmond",
											  "WA--Seattle-Tacoma",
											  "WI--Milwaukee"};

	/// A collection of e-Bay simple categories (see RUBiS 'ebay_simple_categories.txt' file)
	private static final String[] CATEGORIES = {"Antiques & Art",
												"Books",
												"Business, Office & Industrial",
												"Clothing & Accessories",
												"Coins",
												"Collectibles",
												"Computers",
												"Consumer Electronics",
												"Dolls & Bears",
												"Home & Garden",
												"Jewelry, Gems & Watches",
												"Movies & Television",
												"Music",
												"Photo",
												"Pottery & Glass",
												"Sports",
												"Stamps",
												"Tickets & Travel",
												"Toys & Hobbies",
												"Everything Else"};

//	private static final String ITEM_DESCR =  "This incredible item is exactly what you need !<br>It has a lot of very nice features including "
//											+ "a coffee option.<br>It comes with a free license for the free RUBiS software, that's really cool. But RUBiS even if it "
//											+ "is free, is <B>(C) Rice University/INRIA 2001</B>. It is really hard to write an interesting generic description for "
//											+ "automatically generated items, but who will really read this <br>You can also check some cool software available on "
//											+ "http://sci-serv.inrialpes.fr. There is a very cool DSM system called SciFS for SCI clusters, but you will need some "
//											+ "SCI adapters to be able to run it ! Else you can still try CART, the amazing 'Cluster Administration and Reservation "
//											+ "Tool'. All those software are open source, so don't hesitate ! If you have a SCI Cluster you can also try the Whoops! "
//											+ "clustered web server. Actually Whoops! stands for something ! Yes, it is a Web cache with tcp Handoff, On the fly "
//											+ "cOmpression, parallel Pull-based lru for Sci clusters !! Ok, that was a lot of fun but now it is starting to be quite late "
//											+ "and I'll have to go to bed very soon, so I think if you need more information, just go on <h1>http://sci-serv.inrialpes.fr</h1> "
//											+ "or you can even try http://www.cs.rice.edu and try to find where Emmanuel Cecchet or Julie Marguerite are and you will "
//											+ "maybe get fresh news about all that !!<br>";

	private static final int TOTAL_ACTIVE_ITEMS = 1374+2691+259+2874+538+7521+664+586+1077+976+2325+1051+1420+170+1069+3029+305+242+3671+825;
	private static final int NUM_OLD_ITEMS = 100000;
	private static final int PERCENT_UNIQUE_ITEMS = 80;
	private static final int PERCENT_ITEMS_RESERVE_PRICE = 40;
	private static final int PERCENT_ITEMS_BUY_NOW_PRICE = 10;
	private static final int MAX_ITEM_QUANTITY = 10;
	private static final int MAX_ITEM_DESCR_LEN = 8192;
	private static final int MAX_WORD_LEN = 12;
	private static final int MAX_ITEM_INIT_PRICE = 5000;
	private static final int MIN_ITEM_RESERVE_PRICE = 1000;
	private static final int MIN_ITEM_BUY_NOW_PRICE = 1000;
	private static final int MAX_ITEM_DURATION = 7;
	private static final int NUM_ITEMS_PER_PAGE = 20;
	private static final int ANONYMOUS_USER_ID = -1;


	private static int _userId = 0;
	private static int _itemId = 0;


	private Random _rng;
	private HttpTransport _http;
	private boolean _userLoggedIn;
	private int _loggedUserId;
	private String _baseURL;
	private String _homepageURL; 
	private String _browseURL;
	private String _browseCategoriesURL; 
	private String _browseRegionsURL; 
	private String _registerURL;
	private String _postRegisterURL;
	private String _sellURL;
	private String _sellItemFormURL;
	private String _postRegisterItemURL;
	private String _searchItemsByCategoryURL;
	private String _putBidAuthURL;
	private String _postPutBidURL;
	private String _postStoreBidURL;
	private String _postAboutMeURL;
	private String _viewItemURL;
	private String _putCommentAuthURL;
	private String _postPutCommentURL;
	private String _postStoreCommentURL;


	public static synchronized int nextUserId()
	{
		return _userId++;
	}

	public static synchronized int nextItemId()
	{
		return _itemId++;
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
		//TODO: add a config param to set RNG seed

		this._rng = new Random();
		this._http = new HttpTransport();
		this._userLoggedIn = false;
		this._loggedUserId = ANONYMOUS_USER_ID;

		this.initializeUrlAnchors();
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

		if(lastOperation == -1)
		{
			nextOperation = 0;
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
		//FIXME
		return 0;
	}

	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	@Override
	public long getCycleTime()
	{
		//FIXME
		return 0;
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

	public boolean isUserLoggedIn()
	{
		return _userLoggedIn;
	}

	public void setIsUserLoggedIn(boolean val)
	{
		this._userLoggedIn = val;
	}

	public int getLoggedUserId()
	{
		return this._loggedUserId;
	}

	public void setLoggedUserId(int val)
	{
		this._loggedUserId = val;
	}

	public RubisUser getLoggedUser()
	{
		if (!this.isUserLoggedIn())
		{
			return null;
		}

		return this.getUser(this.getLoggedUserId());
	}

	public String getBaseURL()
	{
		return this._baseURL;
	}

	public String getHomepageURL()
	{
		return this._homepageURL; 
	}

	public String getBrowseURL()
	{
		return this._browseURL;
	}

	public String getBrowseCategoriesURL()
	{
		return this._browseCategoriesURL; 
	}

	public String getBrowseRegionsURL()
	{
		return this._browseRegionsURL; 
	}

	public String getRegisterURL()
	{
		return this._registerURL;
	}

	public String getPostRegisterURL()
	{
		return this._postRegisterURL;
	}

	public String getSellURL()
	{
		return this._sellURL;
	}

	public String getSellItemFormURL()
	{
		return this._sellItemFormURL;
	}

	public String getPostRegisterItemURL()
	{
		return this._postRegisterItemURL;
	}

	public String getSearchItemsByCategoryURL()
	{
		return this._searchItemsByCategoryURL;
	}

	public String getPutBidAuthURL()
	{
		return this._putBidAuthURL;
	}

	public String getPostPutBidURL()
	{
		return this._postPutBidURL;
	}

	public String getPostStoreBidURL()
	{
		return this._postStoreBidURL;
	}

	public String getPostAboutMeURL()
	{
		return this._postAboutMeURL;
	}

	public String getViewItemURL()
	{
		return this._viewItemURL;
	}

	public String getPutCommentAuthURL()
	{
		return this._putCommentAuthURL;
	}

	public String getPostPutCommentURL()
	{
		return this._postPutCommentURL;
	}

	public String getPostStoreCommentURL()
	{
		return this._postStoreCommentURL;
	}

	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex The type of operation to instantiate.
	 * @return A prepared operation.
	 */
	public Operation getOperation(int opIndex)
	{
		switch (opIndex)
		{
			//case DO_NOTHING_OP: return this.createDoNothingOperation();
			case HOME_PAGE_OP: return this.createHomePageOperation();
			case BROWSE_CATEGORIES_OP: return this.createBrowseCategoriesOperation();
			case REGISTER_OP: return this.createRegisterOperation();
//			case SELL_OP: return this.createSellOperation();
//			case BID_OP: return this.createBidOperation();
//			case COMMENT_OP: return this.createCommentOperation();
			default: return null;
		}
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
	 * @return  A prepared Homepage.
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
	 * @return  A prepared SellOperation.
	 */
//	public SellOperation createSellOperation()
//	{
//		SellOperation op = new SellOperation(this.getTrack().getInteractive(), this.getScoreboard());
//		op.prepare(this);
//		return op;
//	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BidOperation.
	 */
//	public BidOperation createBidOperation()
//	{
//		BidOperation op = new BidOperation(this.getTrack().getInteractive(), this.getScoreboard());
//		op.prepare(this);
//		return op;
//	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared CommentOperation.
	 */
//	public CommentOperation createCommentOperation()
//	{
//		CommentOperation op = new CommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
//		op.prepare(this);
//		return op;
//	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchItemsByCategoryOperation.
	 */
//	public SearchItemsByCategoryOperation createSearchItemsByCategoryOperation()
//	{
//		SearchItemsByCategoryOperation op = new SearchItemsByCategoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
//		op.prepare(this);
//		return op;
//	}

	public RubisUser newUser()
	{
		return this.getUser(RubisGenerator.nextUserId());
	}

	public RubisItem newItem()
	{
		return this.getItem(RubisGenerator.nextItemId());
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
		user.region = this.generateRegion();

		return user;
	}

	public RubisItem getItem(int id)
	{
		RubisItem item = new RubisItem();

		item.id = id;
		item.name = "RUBiS automatically generated item #" + item.id;
		item.description = this.generateText(1, MAX_ITEM_DESCR_LEN);
		item.initialPrice = this._rng.nextInt(MAX_ITEM_INIT_PRICE)+1;
		if (this._rng.nextInt(TOTAL_ACTIVE_ITEMS) < (PERCENT_UNIQUE_ITEMS*TOTAL_ACTIVE_ITEMS/100))
		{
			item.quantity = 1;
		}
		else
		{
			item.quantity = this._rng.nextInt(MAX_ITEM_QUANTITY)+1;
		}
		if (this._rng.nextInt(TOTAL_ACTIVE_ITEMS) < (PERCENT_ITEMS_RESERVE_PRICE*TOTAL_ACTIVE_ITEMS/100))
		{
			item.reservePrice = this._rng.nextInt(MIN_ITEM_RESERVE_PRICE)+item.initialPrice;
		}
		else
		{
			item.reservePrice = 0;
		}
		if (this._rng.nextInt(TOTAL_ACTIVE_ITEMS) < (PERCENT_ITEMS_BUY_NOW_PRICE*TOTAL_ACTIVE_ITEMS/100))
		{
			item.buyNow = this._rng.nextInt(MIN_ITEM_BUY_NOW_PRICE)+item.initialPrice+item.reservePrice;
		}
		else
		{
			item.buyNow = 0;
		}
		//item.nbOfBids = ;
		//item.maxBid = ;
		Calendar cal = Calendar.getInstance();
		item.startDate = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, this._rng.nextInt(MAX_ITEM_DURATION)+1);
		item.endDate = cal.getTime();
		//item.seller = ;
		item.category = this.generateCategory();

		return item;
	}

	public RubisCategory generateCategory()
	{
		return this.getCategory(this._rng.nextInt(CATEGORIES.length));
	}

	public RubisRegion generateRegion()
	{
		return this.getRegion(this._rng.nextInt(REGIONS.length));
	}

	public RubisCategory getCategory(int id)
	{
		RubisCategory category = new RubisCategory();

		category.id = id;
		category.name = CATEGORIES[category.id];

		return category;
	}

	public RubisRegion getRegion(int id)
	{
		RubisRegion region = new RubisRegion();

		region.id = id;
		region.name = REGIONS[region.id];

		return region;
	}

	public int getNumItemsPerPage()
	{
		return NUM_ITEMS_PER_PAGE;
	}

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

			String word = this.generateWord(1, left < MAX_WORD_LEN ? left : MAX_WORD_LEN);
			buf.append(word);
			left -= word.length();
		}

		return buf.toString();
	}

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
			int j = this._rng.nextInt(ALNUM_CHARS.length);
			buf[i] = ALNUM_CHARS[j];
		}

		return new String(buf);
	}

/*
	public int extractPageFrommHTML(String html)
	{
		if (html == null || html.isEmpty())
		{
			return 0;
		}

		int firstPageIdx = html.indexOf("&page=");
		if (firstPageIdx == -1)
		{
			return 0;
		}
		int lastPageIdx = html.indexOf("&page=", firstPageIdx+6); // 6 == length("&page=")
		int pageIdx = 0;
		if (lastPageIdx == -1)
		{
			// First or last page => go to next or previous page
			pageIdx = firstPageIdx;
		}
		else
		{
			// Choose randomly a page (previous or next)
			if (this._rng.nextInt(100000) < 50000)
			{
				pageIdx = firstPageIdx;
			}
			else
			{
				pageIdx = lastPageIdx;
			}
		}
		int pageValIdx = pageIdx+6;
		int idx = 0;
		idx = minIndex(Integer.MAX_VALUE, html.indexOf('\"', pageValIdx+6));
		idx = minIndex(idx, html.indexOf('?', pageValIdx+6));
		idx = minIndex(idx, html.indexOf('&', pageValIdx+6));
		idx = minIndex(idx, html.indexOf('>', pageValIdx+6));
		int pageNum = 0;
		try
		{
			pageNum = Integer.parseInt(html.substring(pageValIdx, idx));
		}
		catch (Exception e)
		{
			pageNum = 0;
		}

		return pageNum;
	}
*/

	private static int minIndex(int idx1, int idx2)
	{
		if (idx1 < 0)
		{
			return idx2;
		}
		if (idx2 < 0)
		{
			return idx1;
		}

		return (idx1 < idx2) ? idx1 : idx2;
	}
	/**
	 * Initialize the roots/anchors of the URLs.
	 */
	private void initializeUrlAnchors()
	{
		this._baseURL = "http://" + this.getTrack().getTargetHostName() + ":" + this.getTrack().getTargetHostPort();
		this._homepageURL = this._baseURL + "/rubis_servlest/";
		this._browseURL = this._baseURL + "/rubis_servlets/browse.html";
		this._browseCategoriesURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BrowseCategories";
		this._browseRegionsURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BrowseRegions";
		this._registerURL = this._baseURL + "/rubis_servlets/register.html";
		this._postRegisterURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.RegisterUser";
		this._sellURL = this._baseURL + "/rubis_servlets/sell.html";
		this._sellItemFormURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SellItemForm";
		this._postRegisterItemURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.RegisterItem";
		this._searchItemsByCategoryURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByCategory";
		this._putBidAuthURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutBidAuth";
		this._postPutBidURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutBid";
		this._postStoreBidURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreBid";
		this._postAboutMeURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.AboutMe";
		this._viewItemURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.ViewItem";
		this._putCommentAuthURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutCommentAuth";
		this._postPutCommentURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutComment";	
		this._postStoreCommentURL = this._baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreComment";
	}
}
