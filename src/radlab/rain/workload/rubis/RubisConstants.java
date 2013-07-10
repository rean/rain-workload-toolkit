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


/**
 * Collection of RUBiS constants.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public final class RubisConstants
{
	public static final int ANONYMOUS_USER_ID = -1;
	public static final int INVALID_CATEGORY_ID = -1;
	public static final int INVALID_REGION_ID = -1;
	public static final int INVALID_ITEM_ID = -1;
	public static final int INVALID_OPERATION_ID = -1;

	/// The set of alphanumeric characters
	public static final char[] ALNUM_CHARS = {  '0', '1', '2', '3', '4', '5',
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
	public static final String[] REGIONS = {  "AZ--Phoenix",
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
	public static final String[] CATEGORIES = { "Antiques & Art",
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

	public static final String[] COMMENTS = { "This is a very bad comment. Stay away from this seller!!",
											  "This is a comment below average. I don't recommend this user!!",
											  "This is a neutral comment. It is neither a good or a bad seller!!",
											  "This is a comment above average. You can trust this seller even if it is not the best deal!!",
											  "This is an excellent comment. You can make really great deals with this seller!!"};

	public static final int[] COMMENT_RATINGS = { -5, // Bad
												  -3, // Below average
												   0, // Neutral
												   3, // Average
												   5}; // Excellent

	public static final int DEFAULT_TOTAL_ACTIVE_ITEMS = 1374+2691+259+2874+538+7521+664+586+1077+976+2325+1051+1420+170+1069+3029+305+242+3671+825;
	public static final int DEFAULT_NUM_OLD_ITEMS = 100000;
	public static final int DEFAULT_PERCENT_UNIQUE_ITEMS = 80;
	public static final int DEFAULT_PERCENT_ITEMS_RESERVE_PRICE = 40;
	public static final int DEFAULT_PERCENT_ITEMS_BUY_NOW_PRICE = 10;
	public static final int DEFAULT_MAX_ITEM_QUANTITY = 10;
	public static final int DEFAULT_MAX_ADD_BID = 10;
	public static final int DEFAULT_MAX_ITEM_DESCR_LEN = 8192;
	public static final int MAX_WORD_LEN = 12;
	public static final int MAX_ITEM_INIT_PRICE = 5000;
	public static final int MIN_ITEM_RESERVE_PRICE = 1000;
	public static final int MIN_ITEM_BUY_NOW_PRICE = 1000;
	public static final int MAX_ITEM_DURATION = 7;
	public static final int DEFAULT_NUM_ITEMS_PER_PAGE = 20;
	public static final int DEFAULT_MAX_COMMENT_LEN = 2048;
	public static final int MIN_USER_ID = 1;
	public static final int MIN_ITEM_ID = 1;
	public static final int MIN_REGION_ID = 1;
	public static final int MIN_CATEGORY_ID = 1;
	public static final int DEFAULT_NEXT_AVAIL_USER_ID = MIN_USER_ID;
	public static final int DEFAULT_NEXT_AVAIL_ITEM_ID = MIN_ITEM_ID;
//	public static final int DEFAULT_NEXT_AVAIL_REGION_ID = MIN_REGION_ID;
//	public static final int DEFAULT_NEXT_AVAIL_CATEGORY_ID = MIN_CATEGORY_ID;
}
