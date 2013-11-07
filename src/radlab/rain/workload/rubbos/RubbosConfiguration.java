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

package radlab.rain.workload.rubbos;


import java.io.BufferedReader;
import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.json.JSONException;


/**
 * Handle the configuration related to RUBBoS.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class RubbosConfiguration
{
	// RUBBoS incarnations
	public static final int PHP_INCARNATION = 0;
	public static final int SERVLET_INCARNATION = 1;

	// Regular expressions
	private Pattern _dictionaryRegex = Pattern.compile("^\\s*(.+)\\s+(\\d+)\\s*$");

	// RUBBoS incarnation names
	private static final String PHP_INCARNATION_NAME = "php";
	private static final String SERVLET_INCARNATION_NAME = "servlet";

	// RUBBoS operation names
	private static final String STORIES_OF_DAY_OP_NAME = "storiesoftheday";
	private static final String REGISTER_OP_NAME = "register";
	private static final String REGISTER_USER_OP_NAME = "registeruser";
	private static final String BROWSE_OP_NAME = "browse";
	private static final String BROWSE_CATEGORIES_OP_NAME = "browsecategories";
	private static final String BROWSE_STORIES_BY_CATEGORY_OP_NAME = "browsestoriesbycategory";
	private static final String OLDER_STORIES_OP_NAME = "olderstories";
	private static final String VIEW_STORY_OP_NAME = "viewstory";
	private static final String POST_COMMENT_OP_NAME = "postcomment";
	private static final String STORE_COMMENT_OP_NAME = "storecomment";
	private static final String VIEW_COMMENT_OP_NAME = "viewcomment";
	private static final String MODERATE_COMMENT_OP_NAME = "moderatecomment";
	private static final String STORE_MODERATE_LOG_OP_NAME = "storemoderatelog";
	private static final String SUBMIT_STORY_OP_NAME = "submitstory";
	private static final String STORE_STORY_OP_NAME = "storestory";
	private static final String SEARCH_OP_NAME = "search";
	private static final String SEARCH_IN_STORIES_OP_NAME = "searchinstories";
	private static final String SEARCH_IN_COMMENTS_OP_NAME = "searchincomments";
	private static final String SEARCH_IN_USERS_OP_NAME = "searchinusers";
	private static final String AUTHOR_LOGIN_OP_NAME = "authorlogin";
	private static final String AUTHOR_TASKS_OP_NAME = "authortasks";
	private static final String REVIEW_STORIES_OP_NAME = "reviewstories";
	private static final String ACCEPT_STORY_OP_NAME = "acceptstory";
	private static final String REJECT_STORY_OP_NAME = "rejectstory";


	// Configuration keys
	private static final String CFG_DICTIONARY_FILE_KEY = "rubbos.dictionaryFile";
	private static final String CFG_INITIAL_OPERATION_KEY = "rubbos.initOp";
	private static final String CFG_INCARNATION_KEY = "rubbos.incarnation";
//	private static final String CFG_MAX_BIDS_PER_ITEM_KEY = "rubbos.maxBidsPerItem";
	private static final String CFG_MAX_COMMENT_LENGTH_KEY = "rubbos.maxCommentLen";
	private static final String CFG_MAX_STORY_LENGTH_KEY = "rubbos.maxStoryLen";
//	private static final String CFG_MAX_ITEM_BASE_BID_PRICE_KEY = "rubbos.maxItemBaseBidPrice";
//	private static final String CFG_MAX_ITEM_BASE_BUY_NOW_PRICE_KEY = "rubbos.maxItemBaseBuyNowPrice";
//	private static final String CFG_MAX_ITEM_BASE_RESERVE_PRICE_KEY = "rubbos.maxItemBaseReservePrice";
//	private static final String CFG_MAX_ITEM_DESCR_LENGTH_KEY = "rubbos.maxItemDescrLen";
//	private static final String CFG_MAX_ITEM_DURATION_KEY = "rubbos.maxItemDuration";
//	private static final String CFG_MAX_ITEM_INIT_PRICE_KEY = "rubbos.maxItemInitPrice";
//	private static final String CFG_MAX_ITEM_QUANTITY_KEY = "rubbos.maxItemQuantity";
//	private static final String CFG_MAX_WORD_LENGTH_KEY = "rubbos.maxWordLen";
	private static final String CFG_NUM_STORIES_PER_PAGE_KEY = "rubbos.numStoriesPerPage";
//	private static final String CFG_NUM_OLD_ITEMS_KEY = "rubbos.numOldItems";
//	private static final String CFG_NUM_PRELOADED_USERS_KEY = "rubbos.numPreloadedUsers";
	private static final String CFG_OLDEST_STORY_MONTH_KEY = "rubbos.oldestStoryMonth";
	private static final String CFG_OLDEST_STORY_YEAR_KEY = "rubbos.oldestStoryYear";
//	private static final String CFG_PERCENT_UNIQUE_ITEMS_KEY = "rubbos.percentUniqueItems";
//	private static final String CFG_PERCENT_ITEMS_RESERVE_KEY = "rubbos.percentItemsReserve";
//	private static final String CFG_PERCENT_ITEMS_BUY_NOW_KEY = "rubbos.percentItemsBuyNow";
//	private static final String CFG_REGIONS_FILE_KEY = "rubbos.regionsFile";
	private static final String CFG_RNG_SEED_KEY = "rubbos.rngSeed";
	private static final String CFG_SERVER_HTML_PATH_KEY = "rubbos.serverHtmlPath";
	private static final String CFG_SERVER_SCRIPT_PATH_KEY = "rubbos.serverScriptPath";

	// Default values
	private static final String DEFAULT_DICTIONARY_FILE = "resources/rubbos-dictionary.txt";
	private static final int DEFAULT_INCARNATION = PHP_INCARNATION;
	private static final int DEFAULT_INITIAL_OPERATION = RubbosGenerator.HOME_OP;
//	private static final int DEFAULT_MAX_BIDS_PER_ITEM = 20;
	private static final int DEFAULT_MAX_COMMENT_LENGTH = 1024;
	private static final int DEFAULT_MAX_STORY_LENGTH = 1024;
//	private static final float DEFAULT_MAX_ITEM_BASE_BID_PRICE = 10;
//	private static final float DEFAULT_MAX_ITEM_BASE_BUY_NOW_PRICE = 1000;
//	private static final float DEFAULT_MAX_ITEM_BASE_RESERVE_PRICE = 1000;
//	private static final int DEFAULT_MAX_ITEM_DESCRIPTION_LENGTH = 8192;
//	private static final int DEFAULT_MAX_ITEM_DURATION = 7;
//	private static final int DEFAULT_MAX_ITEM_INIT_PRICE = 5000;
//	private static final int DEFAULT_MAX_ITEM_QUANTITY = 10;
//	private static final int DEFAULT_MAX_WORD_LENGTH = 12;
	private static final int DEFAULT_NUM_STORIES_PER_PAGE = 20;
//	private static final int DEFAULT_NUM_OLD_ITEMS = 1000000;
//	private static final int DEFAULT_NUM_PRELOADED_USERS = 1;
	private static final int DEFAULT_OLDEST_STORY_MONTH = 1;
	private static final int DEFAULT_OLDEST_STORY_YEAR = 1998;
//	private static final float DEFAULT_PERCENTAGE_UNIQUE_ITEMS = 80;
//	private static final float DEFAULT_PERCENTAGE_ITEMS_RESERVE = 40;
//	private static final float DEFAULT_PERCENTAGE_ITEMS_BUY_NOW = 10;
//	private static final String DEFAULT_REGIONS_FILE = "resources/rubbos-ebay_regions.txt";
	private static final long DEFAULT_RNG_SEED = -1;
	private static final String DEFAULT_SERVER_HTML_PATH = "/";
	private static final String DEFAULT_SERVER_SCRIPT_PATH = "/";


//	// Members to hold configuration values
	private Map<String,Integer> _dictionary = null; ///< A collection of name-frequency pairs
//	private List<Integer> _numItemsPerCategory = null; ///< Number of items for each category
	private String _dictionaryFile = DEFAULT_DICTIONARY_FILE; ///< File name of the RUBBoS dictionary file
	private int _incarnation = DEFAULT_INCARNATION; ///< RUBBoS incarnation
	private int _initOp = DEFAULT_INITIAL_OPERATION; ///< RUBBoS incarnation
	private int _maxCommentLen = DEFAULT_MAX_COMMENT_LENGTH; ///< Maximum comment length
	private int _maxStoryLen = DEFAULT_MAX_STORY_LENGTH; ///< Maximum story length
//	private float _maxItemBaseBidPrice = DEFAULT_MAX_ITEM_BASE_BID_PRICE; ///< Maximum base bid price for an item
//	private float _maxItemBaseBuyNowPrice = DEFAULT_MAX_ITEM_BASE_BUY_NOW_PRICE; ///< Maximum base buy now price for an item
//	private float _maxItemBaseReservePrice = DEFAULT_MAX_ITEM_BASE_RESERVE_PRICE; ///< Maximum base reserve price for an item
//	private int _maxItemBids = DEFAULT_MAX_BIDS_PER_ITEM; ///< Maximum number of bids per item
//	private int _maxItemDescrLen = DEFAULT_MAX_ITEM_DESCRIPTION_LENGTH; ///< Maximum item description length
//	private int _maxItemDuration = DEFAULT_MAX_ITEM_DURATION; ///< Maximum duration of an item
//	private float _maxItemInitPrice= DEFAULT_MAX_ITEM_INIT_PRICE; ///< Maximum initial price for an item
//	private int _maxItemQty = DEFAULT_MAX_ITEM_QUANTITY; ///< Maximum quantity for multiple items
//	private int _maxWordLen = DEFAULT_MAX_WORD_LENGTH; ///< Maximum length of a word
	private int _numStoriesPerPage = DEFAULT_NUM_STORIES_PER_PAGE; ///< Number of items per page
//	private int _numOldItems = DEFAULT_NUM_OLD_ITEMS; ///< The total number of old items (i.e., whose auction date is over) to be inserted in the database
//	private int _numPreloadUsers = DEFAULT_NUM_PRELOADED_USERS; ///< Number of users that have been already preloaded in the RUBBoS database
	private int _oldestStoryMonth = DEFAULT_OLDEST_STORY_MONTH; ///< The month that the oldest story in the RUBBoS database can have
	private int _oldestStoryYear = DEFAULT_OLDEST_STORY_YEAR; ///< The year that the oldest story in the RUBBoS database can have
//	private float _percItemsBuyNow = DEFAULT_PERCENTAGE_ITEMS_BUY_NOW; ///< Percentage of items that users can 'buy now'
//	private float _percItemsReserve = DEFAULT_PERCENTAGE_ITEMS_RESERVE; ///< Percentage of items with a reserve price
//	private float _percUniqueItems = DEFAULT_PERCENTAGE_UNIQUE_ITEMS; ///< Percentage of unique items
//	private List<String> _regions = null; /*Arrays.asList(DEFAULT_REGIONS)*/; ///< A collection of categories
//	private String _regionsFile = DEFAULT_REGIONS_FILE; ///< File name of the RUBBoS regions file
    private long _rngSeed = DEFAULT_RNG_SEED; ///< The seed used for the Random Number Generator; a value <= 0 means that no special seed is used.
	private String _serverHtmlPath = DEFAULT_SERVER_HTML_PATH; ///< The path to HTML pages on the RUBBoS Web server
	private String _serverScriptPath = DEFAULT_SERVER_SCRIPT_PATH; ///< The path to script pages on the RUBBoS Web server
//	private int _totActiveItems = 0/*DEFAULT_TOTAL_ACTIVE_ITEMS*/; ///< The total number of items to generate


	public RubbosConfiguration()
	{
	}

	public RubbosConfiguration(JSONObject config) throws JSONException
	{
		configure(config);
	}

	public void configure(JSONObject config) throws JSONException
	{
		if (config.has(CFG_DICTIONARY_FILE_KEY))
		{
			this._dictionarysFile = config.getString(CFG_DICTIONARY_FILE_KEY);
		}
		if (config.has(CFG_INCARNATION_KEY))
		{
			String str = config.getString(CFG_INCARNATION_KEY).toLowerCase();

			if (str.equals(PHP_INCARNATION_NAME))
			{
				this._incarnation = PHP_INCARNATION;
			}
			else if (str.equals(SERVLET_INCARNATION_NAME))
			{
				this._incarnation = SERVLET_INCARNATION;
			}
			else
			{
				throw new JSONException("Unknown RUBBoS incarnation: '" + str + "'");
			}
		}
		if (config.has(CFG_INITIAL_OPERATION_KEY))
		{
			String str = config.getString(CFG_INITIAL_OPERATION_KEY).toLowerCase();

			if (str.equals(STORIES_OF_THE_DAY_OP_NAME))
			{
				this._initOp = RubbosGenerator.STORIES_OF_THE_DAY_OP;
			}
			else if (str.equals(REGISTER_OP_NAME))
			{
				this._initOp = RubbosGenerator.REGISTER_OP;
			}
			else if (str.equals(REGISTER_USER_OP_NAME))
			{
				this._initOp = RubbosGenerator.REGISTER_USER_OP;
			}
			else if (str.equals(BROWSE_OP_NAME))
			{
				this._initOp = RubbosGenerator.BROWSE_OP;
			}
			else if (str.equals(BROWSE_CATEGORIES_OP_NAME))
			{
				this._initOp = RubbosGenerator.BROWSE_CATEGORIES_OP;
			}
			else if (str.equals(BROWSE_STORIES_BY_CATEGORY_OP_NAME))
			{
				this._initOp = RubbosGenerator.BROWSE_STORIES_BY_CATEGORY_OP;
			}
			else if (str.equals(OLDER_STORIES_OP_NAME))
			{
				this._initOp = RubbosGenerator.OLDER_STORIES_OP;
			}
			else if (str.equals(VIEW_STORY_OP_NAME))
			{
				this._initOp = RubbosGenerator.VIEW_STORY_OP;
			}
			else if (str.equals(POST_COMMENT_OP_NAME))
			{
				this._initOp = RubbosGenerator.POST_COMMENT_OP;
			}
			else if (str.equals(STORE_COMMENT_OP_NAME))
			{
				this._initOp = RubbosGenerator.STORE_COMMENT_OP;
			}
			else if (str.equals(VIEW_COMMENT_OP_NAME))
			{
				this._initOp = RubbosGenerator.VIEW_COMMENT_OP;
			}
			else if (str.equals(MODERATE_COMMENT_OP_NAME))
			{
				this._initOp = RubbosGenerator.MODERATE_COMMENT_OP;
			}
			else if (str.equals(STORE_MODERATE_LOG_OP_NAME))
			{
				this._initOp = RubbosGenerator.STORE_MODERATE_LOG_OP;
			}
			else if (str.equals(SUBMIT_STORY_OP_NAME))
			{
				this._initOp = RubbosGenerator.SUBMIT_STORY_OP;
			}
			else if (str.equals(STORE_STORY_OP_NAME))
			{
				this._initOp = RubbosGenerator.STORE_STORY_OP;
			}
			else if (str.equals(SEARCH_OP_NAME))
			{
				this._initOp = RubbosGenerator.SEARCH_OP;
			}
			else if (str.equals(SEARCH_IN_STORIES_OP_NAME))
			{
				this._initOp = RubbosGenerator.SEARCH_IN_STORIES_OP;
			}
			else if (str.equals(SEARCH_IN_COMMENTS_OP_NAME))
			{
				this._initOp = RubbosGenerator.SEARCH_IN_COMMENTS_OP;
			}
			else if (str.equals(SEARCH_IN_USERS_OP_NAME))
			{
				this._initOp = RubbosGenerator.SEARCH_IN_USERS_OP;
			}
			else if (str.equals(AUTHOR_LOGIN_OP_NAME))
			{
				this._initOp = RubbosGenerator.AUTHOR_LOGIN_OP;
			}
			else if (str.equals(AUTHOR_TASKS_OP_NAME))
			{
				this._initOp = RubbosGenerator.AUTHOR_TASKS_OP;
			}
			else if (str.equals(REVIEW_STORIES_OP_NAME))
			{
				this._initOp = RubbosGenerator.REVIEW_STORIES_OP;
			}
			else if (str.equals(ACCEPT_STORY_OP_NAME))
			{
				this._initOp = RubbosGenerator.ACCEPT_STORY_OP;
			}
			else if (str.equals(REJECT_STORY_OP_NAME))
			{
				this._initOp = RubbosGenerator.REJECT_STORY_OP;
			}
			else
			{
				throw new JSONException("Unknown RUBBoS operation: '" + str + "'");
			}
		}
//		if (config.has(CFG_MAX_BIDS_PER_ITEM_KEY))
//		{
//			this._maxItemBids = config.getInt(CFG_MAX_BIDS_PER_ITEM_KEY);
//		}
		if (config.has(CFG_MAX_COMMENT_LENGTH_KEY))
		{
			this._maxCommentLen = config.getInt(CFG_MAX_COMMENT_LENGTH_KEY);
		}
		if (config.has(CFG_MAX_STORY_LENGTH_KEY))
		{
			this._maxStoryLen = config.getInt(CFG_MAX_STORY_LENGTH_KEY);
		}
//		if (config.has(CFG_MAX_ITEM_BASE_BID_PRICE_KEY))
//		{
//			this._maxItemBaseBidPrice = (float) config.getDouble(CFG_MAX_ITEM_BASE_BID_PRICE_KEY);
//		}
//		if (config.has(CFG_MAX_ITEM_BASE_BUY_NOW_PRICE_KEY))
//		{
//			this._maxItemBaseBuyNowPrice = (float) config.getDouble(CFG_MAX_ITEM_BASE_BUY_NOW_PRICE_KEY);
//		}
//		if (config.has(CFG_MAX_ITEM_BASE_RESERVE_PRICE_KEY))
//		{
//			this._maxItemBaseReservePrice = (float) config.getDouble(CFG_MAX_ITEM_BASE_RESERVE_PRICE_KEY);
//		}
//		if (config.has(CFG_MAX_ITEM_DESCR_LENGTH_KEY))
//		{
//			this._maxItemDescrLen = config.getInt(CFG_MAX_ITEM_DESCR_LENGTH_KEY);
//		}
//		if (config.has(CFG_MAX_ITEM_DURATION_KEY))
//		{
//			this._maxItemDuration = config.getInt(CFG_MAX_ITEM_DURATION_KEY);
//		}
//		if (config.has(CFG_MAX_ITEM_INIT_PRICE_KEY))
//		{
//			this._maxItemInitPrice = (float) config.getDouble(CFG_MAX_ITEM_INIT_PRICE_KEY);
//		}
//		if (config.has(CFG_MAX_ITEM_QUANTITY_KEY))
//		{
//			this._maxItemQty = config.getInt(CFG_MAX_ITEM_QUANTITY_KEY);
//		}
//		if (config.has(CFG_MAX_WORD_LENGTH_KEY))
//		{
//			this._maxWordLen = config.getInt(CFG_MAX_WORD_LENGTH_KEY);
//		}
		if (config.has(CFG_NUM_STORIES_PER_PAGE_KEY))
		{
			this._numStoriesPerPage = config.getInt(CFG_NUM_STORIES_PER_PAGE_KEY);
		}
//		if (config.has(CFG_NUM_OLD_ITEMS_KEY))
//		{
//			this._numOldItems = config.getInt(CFG_NUM_OLD_ITEMS_KEY);
//		}
//		if (config.has(CFG_NUM_PRELOADED_USERS_KEY))
//		{
//			this._numPreloadUsers = config.getInt(CFG_NUM_PRELOADED_USERS_KEY);
//		}
		if (config.has(CFG_OLDEST_STORY_MONTH_KEY))
		{
			this._oldestStoryMonth = config.getInt(CFG_OLDEST_STORY_MONTH_KEY);
		}
		if (config.has(CFG_OLDEST_STORY_YEAR_KEY))
		{
			this._oldestStoryYear = config.getInt(CFG_OLDEST_STORY_YEAR_KEY);
		}
//		if (config.has(CFG_PERCENT_UNIQUE_ITEMS_KEY))
//		{
//			this._percUniqueItems = (float) config.getDouble(CFG_PERCENT_UNIQUE_ITEMS_KEY);
//		}
//		if (config.has(CFG_PERCENT_ITEMS_RESERVE_KEY))
//		{
//			this._percItemsReserve = (float) config.getDouble(CFG_PERCENT_ITEMS_RESERVE_KEY);
//		}
//		if (config.has(CFG_PERCENT_ITEMS_BUY_NOW_KEY))
//		{
//			this._percItemsBuyNow = (float) config.getDouble(CFG_PERCENT_ITEMS_BUY_NOW_KEY);
//		}
//		if (config.has(CFG_REGIONS_FILE_KEY))
//		{
//			this._regionsFile = config.getString(CFG_REGIONS_FILE_KEY);
//		}
		if (config.has(CFG_RNG_SEED_KEY))
		{
			this._rngSeed = config.getLong(CFG_RNG_SEED_KEY);
		}
		if (config.has(CFG_SERVER_HTML_PATH_KEY))
		{
			this._serverHtmlPath = config.getString(CFG_SERVER_HTML_PATH_KEY);
		}
		if (config.has(CFG_SERVER_SCRIPT_PATH_KEY))
		{
			this._serverScriptPath = config.getString(CFG_SERVER_SCRIPT_PATH_KEY);
		}

		try
		{
			this.parseDictionaryFile();
//			this.parseRegionsFile();
		}
		catch (Throwable t)
		{
			throw new JSONException(t);
		}

		//TODO: check parameters values
		if (this._rngSeed <= 0)
		{
			this._rngSeed = DEFAULT_RNG_SEED;
		}
	}

	/**
	 * Get the dictionary
	 *
	 * @return a collection of name-frequency pairs
	 */
	public Map<String,Integer> getDictionary()
	{
		return this._dictionary;
	}

	/**
	 * Get the dictionary file name.
	 *
	 * @return the dictionary file name
	 */
	public String getDictionaryFileName()
	{
		return this._dictionaryFile;
	}

	/**
	 * Get the RUBBoS incarnation type.
	 *
	 * @return the RUBBoS incarnation type.
	 */
	public int getIncarnation()
	{
		return this._incarnation;
	}

	/**
	 * Get the RUBBoS initial operation type.
	 *
	 * @return the RUBBoS initial operation type.
	 */
	public int getInitialOperation()
	{
		return this._initOp;
	}

//	/**
//	 * Get the maximum number of bids per item.
//	 * 
//	 * This is the RAIN counterpart of the max_bids_per_item RUBBoS property.
//	 *
//	 * @return maximum number of bids per item
//	 */
//	public int getMaxBidsPerItem()
//	{
//		return this._maxItemBids;
//	}

	/**
	 * Get the maximum length of a comment.
	 *
	 * This is the RAIN counterpart of the database_comment_max_length RUBBoS
	 * property.
	 * 
	 * @return maximum comment description length
	 */
	public int getMaxCommentLength()
	{
		return this._maxCommentLen;
	}

	/**
	 * Get the maximum length of a story.
	 *
	 * This is the RAIN counterpart of the database_story_max_length RUBBoS
	 * property.
	 * 
	 * @return maximum story description length
	 */
	public int getMaxStoryLength()
	{
		return this._maxStoryLen;
	}

//	/**
//	 * Get the maximum base reserve price for an item.
//	 *
//	 * @return the maximum base reserve price for an item
//	 */
//	public float getMaxItemBaseReservePrice()
//	{
//		return this._maxItemBaseReservePrice;
//	}

//	/**
//	 * Get the maximum base bid price for an item
//	 * 
//	 * @return maximum base bid price for an item
//	 */
//	public float getMaxItemBaseBidPrice()
//	{
//		return this._maxItemBaseBidPrice;
//	}

//	/**
//	 * Get the maximum base buy now price for an item.
//	 *
//	 * @return the maximum base buy now price for an item
//	 */
//	public float getMaxItemBaseBuyNowPrice()
//	{
//		return this._maxItemBaseBuyNowPrice;
//	}

//	/**
//	 * Get the maximum item description length.
//	 *
//	 * This is the RAIN counterpart of the item_description_length RUBBoS
//	 * property.
//	 * 
//	 * @return maximum item description length
//	 */
//	public int getMaxItemDescriptionLength()
//	{
//		return this._maxItemDescrLen;
//	}

//	/**
//	 * Get the maximum item duration in days
//	 *
//	 * @return the maximum item duration in days
//	 */
//	public int getMaxItemDuration()
//	{
//		return this._maxItemDuration;
//	}

//	/**
//	 * Get the maximum initial price for an item.
//	 *
//	 * @return the maximum initial price for an item
//	 */
//	public float getMaxItemInitialPrice()
//	{
//		return this._maxItemInitPrice;
//	}

//	/**
//	 * Get the maximum quantity for multiple items.
//	 * 
//	 * This is the RAIN counterpart of the max_quantity_for_multiple_items RUBBoS property.
//	 *
//	 * @return maximum quantity for multiple items
//	 */
//	public int getMaxItemQuantity()
//	{
//		return this._maxItemQty;
//	}

//	/**
//	 * Get the maximum length of a word.
//	 *
//	 * @return the maximum length of a word
//	 */
//	public int getMaxWordLength()
//	{
//		return this._maxWordLen;
//	}

//	/**
//	 * Get the number of items for the given category.
//	 *
//	 * @param categoryId The category identifier for which the number of items has
//	 *  to be returned.
//	 * @return number of items for the given category
//	 */
//	public int getNumOfItemsPerCategory(int categoryId)
//	{
//		return this._numItemsPerCategory.get(categoryId);
//	}

	/**
	 * Get the maximum number of stories per page.
	 *
	 * This is the RAIN counterpart of the workload_number_of_stories_per_page
	 * RUBBoS property.
	 * 
	 * @return maximum number of stories per page
	 */
	public int getNumOfStoriesPerPage()
	{
		return this._numStoriesPerPage;
	}

//	/**
//	 * Get the total number of old items (i.e. whose auction date is over) to be
//	 * inserted in the database.
//	 * 
//	 * @return total number of old items
//	 */
//	public int getNumOfOldItems()
//	{
//		return this._numOldItems;
//	}

//	/**
//	 * Get the number of users that have been already preloaded inside the
//	 * RUBBoS database.
//	 *
//	 * @return the number of preloaded users.
//	 */
//	public int getNumOfPreloadedUsers()
//	{
//		return this._numPreloadUsers;
//	}

	/**
	 * Get the month that the oldest RUBBoS story can have
	 *
	 * This is the RAIN counterpart of the database_oldest_story_month
	 * RUBBoS property.
	 * 
	 * @return month of the oldest story
	 */
	public int getOldestStoryMonth()
	{
		return this._oldestStoryMonth;
	}

	/**
	 * Get the year that the oldest RUBBoS story can have
	 *
	 * This is the RAIN counterpart of the database_oldest_story_year
	 * RUBBoS property.
	 * 
	 * @return year of the oldest story
	 */
	public int getOldestStoryYear()
	{
		return this._oldestStoryYear;
	}

//	/**
//	 * Get the percentage of items that users can 'buy now'.
//	 * 
//	 * This is the RAIN counterpart of the percentage_of_buy_now_items RUBBoS property.
//	 *
//	 * @return percentage of items that users can 'buy now'.
//	 */
//	public float getPercentageOfItemsBuyNow()
//	{
//		return this._percItemsBuyNow;
//	}

//	/**
//	 * Get the percentage of items with a reserve price.
//	 * 
//	 * This is the RAIN counterpart of the percentage_of_items_with_reserve_price RUBBoS property.
//	 *
//	 * @return percentage of items with a reserve price
//	 */
//	public float getPercentageOfItemsReserve()
//	{
//		return this._percItemsReserve;
//	}

//	/**
//	 * Get the percentage of unique items.
//	 * 
//	 * This is the RAIN counterpart of the percentage_of_unique_items RUBBoS property.
//	 *
//	 * @return percentage of unique items
//	 */
//	public float getPercentageOfUniqueItems()
//	{
//		return this._percUniqueItems;
//	}

//	/**
//	 * Get the list of regions.
//	 *
//	 * @return a list of regions
//	 */
//	public List<String> getRegions()
//	{
//		return this._regions;
//	}

//	/**
//	 * Get the regions file name.
//	 *
//	 * @return the regions file name
//	 */
//	public String getRegionsFileName()
//	{
//		return this._regionsFile;
//	}

	/**
	 * Get the seed for the random number generator used by the RUBBoS generator.
	 *
	 * @return the seed for the random number generator
	 */
	public long getRngSeed()
	{
		return this._rngSeed;
	}

	/**
	 * Get the path to the HTML pages on the RUBBoS Web server.
	 *
	 * @return the path to the RUBBoS HTML pages.
	 */
	public String getServerHtmlPath()
	{
		return this._serverHtmlPath;
	}

	/**
	 * Get the path to the script pages on the RUBBoS Web server.
	 *
	 * @return the path to the RUBBoS script pages.
	 */
	public String getServerScriptPath()
	{
		return this._serverScriptPath;
	}

//	/**
//	 * Get the total number of items computed from information found in the
//	 * categories file given in the categories_file field
//	 * 
//	 * @return total number of active items (auction date is not passed)
//	 */
//	public int getTotalActiveItems()
//	{
//		return this._totActiveItems;
//	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append( " Dictionary: " + this.getDictionary());
		sb.append(", Dictionary File Name: " + this.getDictionarysFileName());
		sb.append( " Incarnation: " + this.getIncarnation());
//		sb.append(", Max Bids per Item: " + this.getMaxBidsPerItem());
		sb.append(", Max Comment Length: " + this.getMaxCommentLength());
		sb.append(", Max Story Length: " + this.getMaxStoryLength());
//		sb.append(", Max Item Base Reserve Price: " + this.getMaxItemBaseReservePrice());
//		sb.append(", Max Item Base Buy Now: " + this.getMaxItemBaseBuyNowPrice());
//		sb.append(", Max Item Duration: " + this.getMaxItemDuration());
//		sb.append(", Max Item Initial Price: " + this.getMaxItemInitialPrice());
//		sb.append(", Max Item Quantity: " + this.getMaxItemQuantity());
//		sb.append(", Max Word Length: " + this.getMaxWordLength());
		sb.append(", Number of Stories per Page: " + this.getNumOfStoriesPerPage());
//		sb.append(", Number of Old Items: " + this.getNumOfOldItems());
//		sb.append(", Number of Preloaded Users: " + this.getNumOfPreloadedUsers());
		sb.append(", Oldest Story Month: " + this.getOldestStoryMonth());
		sb.append(", Oldest Story Year: " + this.getOldestStoryYear());
//		sb.append(", Percentage of Items that Users Can Buy Now: " + this.getPercentageOfItemsBuyNow());
//		sb.append(", Percentage of Items with a Reserve Price: " + this.getPercentageOfItemsReserve());
//		sb.append(", Percentage of Unique Items: " + this.getPercentageOfUniqueItems());
//		sb.append(", Regions: " + this.getRegions());
//		sb.append(", Regions File Name: " + this.getRegionsFileName());
		sb.append(", Random Number Generator Seed: " + this.getRngSeed());
//		sb.append(", Total Active Items: " + this.getTotalActiveItems());
		sb.append(", Server HTML Path: " + this.getServerHtmlPath());
		sb.append(", Server Script Path: " + this.getServerScriptPath());

		return sb.toString();
	}

	/**
	 * Parse the dictionary file and store the name-frequency pairs.
	 */
	private void parseDictionaryFile() throws Throwable
	{
		this._dictionary = new HashMap<String,Integer>();

		BufferedReader rd = null;
		try
		{
			int nc = 0;
			rd = new BufferedReader(new FileReader(this._dictionaryFile));
			while (rd.ready())
			{
				++nc;

				String line = rd.readLine();
				Matcher m = this._dictionaryRegex.matcher(line);
				if (!m.matches())
				{
					throw new Exception("Syntax error in categories file on line " + nc + ": " + line);
				}
				String name = m.group(1);
				int freq = m.group(2);
				this._dictionary.put(name, freq);
			}
		}
		finally
		{
			if (rd != null)
			{
				rd.close();
			}
		}
	}

//	/**
//	 * Parse the regions file and store the regions.
//	 */
//	private void parseRegionsFile() throws Throwable
//	{
//		this._regions = new ArrayList<String>();
//
//		BufferedReader rd = null;
//		try
//		{
//			rd = new BufferedReader(new FileReader(this._regionsFile));
//			while (rd.ready())
//			{
//				this._regions.add(rd.readLine());
//			}
//		}
//		finally
//		{
//			if (rd != null)
//			{
//				rd.close();
//			}
//		}
//	}
}
