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


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONException;


/**
 * Handle the configuration related to RUBiS.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public final class RubisConfiguration
{
	// Configuration keys
	private static final String CFG_CATEGORIES_FILE_KEY = "rubis.categoriesFile";
	private static final String CFG_INCARNATION_KEY = "rubis.incarnation";
	private static final String CFG_MAX_BIDS_PER_ITEM_KEY = "rubis.maxBidsPerItem";
	private static final String CFG_MAX_COMMENT_LENGTH_KEY = "rubis.maxCommentLen";
	private static final String CFG_MAX_ITEM_BASE_BID_PRICE_KEY = "rubis.maxItemBaseBidPrice";
	private static final String CFG_MAX_ITEM_BASE_BUY_NOW_PRICE_KEY = "rubis.maxItemBaseBuyNowPrice";
	private static final String CFG_MAX_ITEM_BASE_RESERVE_PRICE_KEY = "rubis.maxItemBaseReservePrice";
	private static final String CFG_MAX_ITEM_DESCR_LENGTH_KEY = "rubis.maxItemDescrLen";
	private static final String CFG_MAX_ITEM_DURATION_KEY = "rubis.maxItemDuration";
	private static final String CFG_MAX_ITEM_INIT_PRICE_KEY = "rubis.maxItemInitPrice";
	private static final String CFG_MAX_ITEM_QUANTITY_KEY = "rubis.maxItemQuantity";
	private static final String CFG_MAX_WORD_LENGTH_KEY = "rubis.maxWordLen";
	private static final String CFG_NUM_ITEMS_PER_PAGE_KEY = "rubis.numItemsPerPage";
	private static final String CFG_NUM_OLD_ITEMS_KEY = "rubis.numOldItems";
	private static final String CFG_NUM_PRELOADED_USERS_KEY = "rubis.numPreloadedUsers";
	private static final String CFG_PERCENT_UNIQUE_ITEMS_KEY = "rubis.percentUniqueItems";
	private static final String CFG_PERCENT_ITEMS_RESERVE_KEY = "rubis.percentItemsReserve";
	private static final String CFG_PERCENT_ITEMS_BUY_NOW_KEY = "rubis.percentItemsBuyNow";
	private static final String CFG_REGIONS_FILE_KEY = "rubis.regionsFile";
	private static final String CFG_RNG_SEED_KEY = "rubis.rngSeed";

	// Default values
	private static final long DEFAULT_RNG_SEED = -1;
	private static final String DEFAULT_CATEGORIES_FILE = "resources/rubis-ebay_full_categories.txt";
	private static final String DEFAULT_INCARNATION = "servlet";
	private static final String DEFAULT_REGIONS_FILE = "resources/rubis-ebay_regions.txt";
	private static final int DEFAULT_NUM_OLD_ITEMS = 1000000;
	private static final float DEFAULT_PERCENTAGE_UNIQUE_ITEMS = 80;
	private static final float DEFAULT_PERCENTAGE_ITEMS_RESERVE = 40;
	private static final float DEFAULT_PERCENTAGE_ITEMS_BUY_NOW = 10;
	private static final int DEFAULT_MAX_BIDS_PER_ITEM = 20;
	private static final float DEFAULT_MAX_ITEM_BASE_BID_PRICE = 10;
	private static final float DEFAULT_MAX_ITEM_BASE_BUY_NOW_PRICE = 1000;
	private static final float DEFAULT_MAX_ITEM_BASE_RESERVE_PRICE = 1000;
	private static final int DEFAULT_MAX_ITEM_DESCRIPTION_LENGTH = 8192;
	private static final int DEFAULT_MAX_ITEM_DURATION = 7;
	private static final int DEFAULT_MAX_ITEM_INIT_PRICE = 5000;
	private static final int DEFAULT_MAX_ITEM_QUANTITY = 10;
	private static final int DEFAULT_NUM_ITEMS_PER_PAGE = 20;
	private static final int DEFAULT_MAX_COMMENT_LENGTH = 2048;
	private static final int DEFAULT_NUM_PRELOADED_USERS = 1;
	private static final int DEFAULT_MAX_WORD_LENGTH = 12;


	// Members to hold configuration values
	private List<String> _categories = null; /*Arrays.asList(DEFAULT_CATEGORIES)*/; ///< A collection of categories
	private List<Integer> _numItemsPerCategory = null; ///< Number of items for each category
	private String _categoriesFile = DEFAULT_CATEGORIES_FILE; ///< File name of the RUBiS categories file
	private String _incarnation = DEFAULT_INCARNATION; ///< RUBiS incarnation
	private int _maxCommentLen = DEFAULT_MAX_COMMENT_LENGTH; ///< Maximum comment length
	private float _maxItemBaseBidPrice = DEFAULT_MAX_ITEM_BASE_BID_PRICE; ///< Maximum base bid price for an item
	private float _maxItemBaseBuyNowPrice = DEFAULT_MAX_ITEM_BASE_BUY_NOW_PRICE; ///< Maximum base buy now price for an item
	private float _maxItemBaseReservePrice = DEFAULT_MAX_ITEM_BASE_RESERVE_PRICE; ///< Maximum base reserve price for an item
	private int _maxItemBids = DEFAULT_MAX_BIDS_PER_ITEM; ///< Maximum number of bids per item
	private int _maxItemDescrLen = DEFAULT_MAX_ITEM_DESCRIPTION_LENGTH; ///< Maximum item description length
	private int _maxItemDuration = DEFAULT_MAX_ITEM_DURATION; ///< Maximum duration of an item
	private float _maxItemInitPrice= DEFAULT_MAX_ITEM_INIT_PRICE; ///< Maximum initial price for an item
	private int _maxItemQty = DEFAULT_MAX_ITEM_QUANTITY; ///< Maximum quantity for multiple items
	private int _maxWordLen = DEFAULT_MAX_WORD_LENGTH; ///< Maximum length of a word
	private int _numItemsPerPage = DEFAULT_NUM_ITEMS_PER_PAGE; ///< Number of items per page
	private int _numOldItems = DEFAULT_NUM_OLD_ITEMS; ///< The total number of old items (i.e., whose auction date is over) to be inserted in the database
	private int _numPreloadUsers = DEFAULT_NUM_PRELOADED_USERS; ///< Number of users that have been already preloaded in the RUBiS database
	private float _percItemsBuyNow = DEFAULT_PERCENTAGE_ITEMS_BUY_NOW; ///< Percentage of items that users can 'buy now'
	private float _percItemsReserve = DEFAULT_PERCENTAGE_ITEMS_RESERVE; ///< Percentage of items with a reserve price
	private float _percUniqueItems = DEFAULT_PERCENTAGE_UNIQUE_ITEMS; ///< Percentage of unique items
	private List<String> _regions = null; /*Arrays.asList(DEFAULT_REGIONS)*/; ///< A collection of categories
	private String _regionsFile = DEFAULT_REGIONS_FILE; ///< File name of the RUBiS regions file
    private long _rngSeed = DEFAULT_RNG_SEED; ///< The seed used for the Random Number Generator; a value <= 0 means that no special seed is used.
	private int _totActiveItems = 0/*DEFAULT_TOTAL_ACTIVE_ITEMS*/; ///< The total number of items to generate


	public RubisConfiguration()
	{
	}

	public RubisConfiguration(JSONObject config) throws JSONException
	{
		configure(config);
	}

	public void configure(JSONObject config) throws JSONException
	{
		if (config.has(CFG_CATEGORIES_FILE_KEY))
		{
			this._categoriesFile = config.getString(CFG_CATEGORIES_FILE_KEY);
		}
		if (config.has(CFG_INCARNATION_KEY))
		{
			this._incarnation = config.getString(CFG_INCARNATION_KEY);
		}
		if (config.has(CFG_MAX_BIDS_PER_ITEM_KEY))
		{
			this._maxItemBids = config.getInt(CFG_MAX_BIDS_PER_ITEM_KEY);
		}
		if (config.has(CFG_MAX_COMMENT_LENGTH_KEY))
		{
			this._maxCommentLen = config.getInt(CFG_MAX_COMMENT_LENGTH_KEY);
		}
		if (config.has(CFG_MAX_ITEM_BASE_BID_PRICE_KEY))
		{
			this._maxItemBaseBidPrice = (float) config.getDouble(CFG_MAX_ITEM_BASE_BID_PRICE_KEY);
		}
		if (config.has(CFG_MAX_ITEM_BASE_BUY_NOW_PRICE_KEY))
		{
			this._maxItemBaseBuyNowPrice = (float) config.getDouble(CFG_MAX_ITEM_BASE_BUY_NOW_PRICE_KEY);
		}
		if (config.has(CFG_MAX_ITEM_BASE_RESERVE_PRICE_KEY))
		{
			this._maxItemBaseReservePrice = (float) config.getDouble(CFG_MAX_ITEM_BASE_RESERVE_PRICE_KEY);
		}
		if (config.has(CFG_MAX_ITEM_DESCR_LENGTH_KEY))
		{
			this._maxItemDescrLen = config.getInt(CFG_MAX_ITEM_DESCR_LENGTH_KEY);
		}
		if (config.has(CFG_MAX_ITEM_DURATION_KEY))
		{
			this._maxItemDuration = config.getInt(CFG_MAX_ITEM_DURATION_KEY);
		}
		if (config.has(CFG_MAX_ITEM_INIT_PRICE_KEY))
		{
			this._maxItemInitPrice = (float) config.getDouble(CFG_MAX_ITEM_INIT_PRICE_KEY);
		}
		if (config.has(CFG_MAX_ITEM_QUANTITY_KEY))
		{
			this._maxItemQty = config.getInt(CFG_MAX_ITEM_QUANTITY_KEY);
		}
		if (config.has(CFG_MAX_WORD_LENGTH_KEY))
		{
			this._maxWordLen = config.getInt(CFG_MAX_WORD_LENGTH_KEY);
		}
		if (config.has(CFG_NUM_ITEMS_PER_PAGE_KEY))
		{
			this._numItemsPerPage = config.getInt(CFG_NUM_ITEMS_PER_PAGE_KEY);
		}
		if (config.has(CFG_NUM_OLD_ITEMS_KEY))
		{
			this._numOldItems = config.getInt(CFG_NUM_OLD_ITEMS_KEY);
		}
		if (config.has(CFG_NUM_PRELOADED_USERS_KEY))
		{
			this._numPreloadUsers = config.getInt(CFG_NUM_PRELOADED_USERS_KEY);
		}
		if (config.has(CFG_PERCENT_UNIQUE_ITEMS_KEY))
		{
			this._percUniqueItems = (float) config.getDouble(CFG_PERCENT_UNIQUE_ITEMS_KEY);
		}
		if (config.has(CFG_PERCENT_ITEMS_RESERVE_KEY))
		{
			this._percItemsReserve = (float) config.getDouble(CFG_PERCENT_ITEMS_RESERVE_KEY);
		}
		if (config.has(CFG_PERCENT_ITEMS_BUY_NOW_KEY))
		{
			this._percItemsBuyNow = (float) config.getDouble(CFG_PERCENT_ITEMS_BUY_NOW_KEY);
		}
		if (config.has(CFG_REGIONS_FILE_KEY))
		{
			this._regionsFile = config.getString(CFG_REGIONS_FILE_KEY);
		}
		if (config.has(CFG_RNG_SEED_KEY))
		{
			this._rngSeed = config.getLong(CFG_RNG_SEED_KEY);
		}

		try
		{
			this.parseCategoriesFile();
			this.parseRegionsFile();
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
	 * Get the list of categories.
	 *
	 * @return a list of categories
	 */
	public List<String> getCategories()
	{
		return this._categories;
	}

	/**
	 * Get the categories file name.
	 *
	 * @return the categories file name
	 */
	public String getCategoriesFileName()
	{
		return this._categoriesFile;
	}

	/**
	 * Get the RUBiS incarnation type.
	 *
	 * @return the RUBiS incarnation type.
	 */
	public String getIncarnation()
	{
		return this._incarnation;
	}

	/**
	 * Get the maximum number of bids per item.
	 * 
	 * This is the RAIN counterpart of the max_bids_per_item RUBiS property.
	 *
	 * @return maximum number of bids per item
	 */
	public int getMaxBidsPerItem()
	{
		return this._maxItemBids;
	}

	/**
	 * Get the maximum length of a comment.
	 *
	 * This is the RAIN counterpart of the comment_max_length RUBiS
	 * property.
	 * 
	 * @return maximum item description length
	 */
	public int getMaxCommentLength()
	{
		return this._maxCommentLen;
	}

	/**
	 * Get the maximum base reserve price for an item.
	 *
	 * @return the maximum base reserve price for an item
	 */
	public float getMaxItemBaseReservePrice()
	{
		return this._maxItemBaseReservePrice;
	}

	/**
	 * Get the maximum base bid price for an item
	 * 
	 * @return maximum base bid price for an item
	 */
	public float getMaxItemBaseBidPrice()
	{
		return this._maxItemBaseBidPrice;
	}

	/**
	 * Get the maximum base buy now price for an item.
	 *
	 * @return the maximum base buy now price for an item
	 */
	public float getMaxItemBaseBuyNowPrice()
	{
		return this._maxItemBaseBuyNowPrice;
	}

	/**
	 * Get the maximum item description length.
	 *
	 * This is the RAIN counterpart of the item_description_length RUBiS
	 * property.
	 * 
	 * @return maximum item description length
	 */
	public int getMaxItemDescriptionLength()
	{
		return this._maxItemDescrLen;
	}

	/**
	 * Get the maximum item duration in days
	 *
	 * @return the maximum item duration in days
	 */
	public int getMaxItemDuration()
	{
		return this._maxItemDuration;
	}

	/**
	 * Get the maximum initial price for an item.
	 *
	 * @return the maximum initial price for an item
	 */
	public float getMaxItemInitialPrice()
	{
		return this._maxItemInitPrice;
	}

	/**
	 * Get the maximum quantity for multiple items.
	 * 
	 * This is the RAIN counterpart of the max_quantity_for_multiple_items RUBiS property.
	 *
	 * @return maximum quantity for multiple items
	 */
	public int getMaxItemQuantity()
	{
		return this._maxItemQty;
	}

	/**
	 * Get the maximum length of a word.
	 *
	 * @return the maximum length of a word
	 */
	public int getMaxWordLength()
	{
		return this._maxWordLen;
	}

	/**
	 * Get the number of items for the given category.
	 *
	 * @param categoryId The category identifier for which the number of items has
	 *  to be returned.
	 * @return number of items for the given category
	 */
	public int getNumOfItemsPerCategory(int categoryId)
	{
		return this._numItemsPerCategory.get(categoryId);
	}

	/**
	 * Get the maximum number of items per page.
	 *
	 * This is the RAIN counterpart of the workload_number_of_items_per_page
	 * RUBiS property.
	 * 
	 * @return maximum number of items per page
	 */
	public int getNumOfItemsPerPage()
	{
		return this._numItemsPerPage;
	}

	/**
	 * Get the total number of old items (i.e. whose auction date is over) to be
	 * inserted in the database.
	 * 
	 * @return total number of old items
	 */
	public int getNumOfOldItems()
	{
		return this._numOldItems;
	}

	/**
	 * Get the number of users that have been already preloaded inside the
	 * RUBiS database.
	 *
	 * @return the number of preloaded users.
	 */
	public int getNumOfPreloadedUsers()
	{
		return this._numPreloadUsers;
	}

	/**
	 * Get the percentage of items that users can 'buy now'.
	 * 
	 * This is the RAIN counterpart of the percentage_of_buy_now_items RUBiS property.
	 *
	 * @return percentage of items that users can 'buy now'.
	 */
	public float getPercentageOfItemsBuyNow()
	{
		return this._percItemsBuyNow;
	}

	/**
	 * Get the percentage of items with a reserve price.
	 * 
	 * This is the RAIN counterpart of the percentage_of_items_with_reserve_price RUBiS property.
	 *
	 * @return percentage of items with a reserve price
	 */
	public float getPercentageOfItemsReserve()
	{
		return this._percItemsReserve;
	}

	/**
	 * Get the percentage of unique items.
	 * 
	 * This is the RAIN counterpart of the percentage_of_unique_items RUBiS property.
	 *
	 * @return percentage of unique items
	 */
	public float getPercentageOfUniqueItems()
	{
		return this._percUniqueItems;
	}

	/**
	 * Get the list of regions.
	 *
	 * @return a list of regions
	 */
	public List<String> getRegions()
	{
		return this._regions;
	}

	/**
	 * Get the regions file name.
	 *
	 * @return the regions file name
	 */
	public String getRegionsFileName()
	{
		return this._regionsFile;
	}

	/**
	 * Get the seed for the random number generator used by the RUBiS generator.
	 *
	 * @return the seed for the random number generator
	 */
	public long getRngSeed()
	{
		return this._rngSeed;
	}

	/**
	 * Get the total number of items computed from information found in the
	 * categories file given in the categories_file field
	 * 
	 * @return total number of active items (auction date is not passed)
	 */
	public int getTotalActiveItems()
	{
		return this._totActiveItems;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append( " Categories: " + this.getCategories());
		sb.append(", Categories File Name: " + this.getCategoriesFileName());
		sb.append( " Incarnation: " + this.getIncarnation());
		sb.append(", Max Bids per Item: " + this.getMaxBidsPerItem());
		sb.append(", Max Comment Length: " + this.getMaxCommentLength());
		sb.append(", Max Item Base Reserve Price: " + this.getMaxItemBaseReservePrice());
		sb.append(", Max Item Base Buy Now: " + this.getMaxItemBaseBuyNowPrice());
		sb.append(", Max Item Duration: " + this.getMaxItemDuration());
		sb.append(", Max Item Initial Price: " + this.getMaxItemInitialPrice());
		sb.append(", Max Item Quantity: " + this.getMaxItemQuantity());
		sb.append(", Max Word Length: " + this.getMaxWordLength());
		sb.append(", Number of Items per Page: " + this.getNumOfItemsPerPage());
		sb.append(", Number of Old Items: " + this.getNumOfOldItems());
		sb.append(", Number of Preloaded Users: " + this.getNumOfPreloadedUsers());
		sb.append(", Percentage of Items that Users Can Buy Now: " + this.getPercentageOfItemsBuyNow());
		sb.append(", Percentage of Items with a Reserve Price: " + this.getPercentageOfItemsReserve());
		sb.append(", Percentage of Unique Items: " + this.getPercentageOfUniqueItems());
		sb.append(", Regions: " + this.getRegions());
		sb.append(", Regions File Name: " + this.getRegionsFileName());
		sb.append(", Random Number Generator Seed: " + this.getRngSeed());
		sb.append(", Total Active Items: " + this.getTotalActiveItems());

		return sb.toString();
	}

	/**
	 * Parse the categories file and store the categories.
	 */
	private void parseCategoriesFile() throws Throwable
	{
		this._totActiveItems = 0;
		this._categories = new ArrayList<String>();
		this._numItemsPerCategory = new ArrayList<Integer>();

		BufferedReader rd = null;
		try
		{
			int nc = 0;
			rd = new BufferedReader(new FileReader(this._categoriesFile));
			while (rd.ready())
			{
				String line = rd.readLine();
				int openParIdx = line.lastIndexOf('(');
				int closeParIdx = line.lastIndexOf(')');
				++nc;
				if (openParIdx == -1 || closeParIdx == -1 || openParIdx > closeParIdx)
				{
					throw new Exception("Syntax error in categories file on line " + nc + ": " + line);
				}
				int numItems = Integer.parseInt(line.substring(openParIdx+1, closeParIdx));
				this._totActiveItems += numItems;
				this._categories.add(line.substring(0, openParIdx-1));
				this._numItemsPerCategory.add(numItems);
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

	/**
	 * Parse the regions file and store the regions.
	 */
	private void parseRegionsFile() throws Throwable
	{
		this._regions = new ArrayList<String>();

		BufferedReader rd = null;
		try
		{
			rd = new BufferedReader(new FileReader(this._regionsFile));
			while (rd.ready())
			{
				this._regions.add(rd.readLine());
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
}
