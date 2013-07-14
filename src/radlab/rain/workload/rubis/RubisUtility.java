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
//import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import radlab.rain.util.HttpTransport;
import radlab.rain.workload.rubis.model.RubisCategory;
import radlab.rain.workload.rubis.model.RubisComment;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisRegion;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Collection of RUBiS utilities.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public final class RubisUtility
{
	public static final int ANONYMOUS_USER_ID = -1;
	public static final int INVALID_CATEGORY_ID = -1;
	public static final int INVALID_REGION_ID = -1;
	public static final int INVALID_ITEM_ID = -1;
	public static final int INVALID_OPERATION_ID = -1;

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
												'y', 'z'}; ///< The set of alphanumeric characters
	private static final String[] COMMENTS = {"This is a very bad comment. Stay away from this seller!!",
											  "This is a comment below average. I don't recommend this user!!",
											  "This is a neutral comment. It is neither a good or a bad seller!!",
											  "This is a comment above average. You can trust this seller even if it is not the best deal!!",
											  "This is an excellent comment. You can make really great deals with this seller!!"}; ///< Descriptions associated to comment ratings
	private static final int[] COMMENT_RATINGS = {-5, // Bad
												  -3, // Below average
												   0, // Neutral
												   3, // Average
												   5  /* Excellent */ }; ///< Possible comment ratings
	private static final int MIN_USER_ID = 1; ///< Mininum value for user IDs
	private static final int MIN_ITEM_ID = 1; ///< Mininum value for item IDs
	private static final int MIN_REGION_ID = 1; ///< Mininum value for region IDs
	private static final int MIN_CATEGORY_ID = 1; ///< Mininum value for category IDs
	private static AtomicInteger _userId = new AtomicInteger();
//	private static Semaphore _userLock = new Semaphore(1, true);


	private Random _rng = null;
	private RubisConfiguration _conf = null;
	private Pattern _pageRegex = Pattern.compile("^.*?[&?]page=(\\d+).*?(?:[&?]page=(\\d+).*?)?$");


	private static int nextUserId()
	{
		return _userId.incrementAndGet();
	}

	private static int lastUserId()
	{
		return _userId.get();
	}

//	private static void lockUsers() throws InterruptedException
//	{
//		_userLock.acquire();
//	}

//	private static void unlockUsers()
//	{
//		_userLock.release();
//	}

	private static synchronized void initUserId(int numPreloadUsers)
	{
		_userId = new AtomicInteger(MIN_USER_ID+numPreloadUsers);
	}


	public RubisUtility()
	{
	}

	public RubisUtility(Random rng, RubisConfiguration conf)
	{
		this._rng = rng;
		this._conf = conf;

		initUserId(this._conf.getNumOfPreloadedUsers());
	}

	public void setRandomGenerator(Random rng)
	{
		this._rng = rng;
	}

	public Random getRandomGenerator()
	{
		return this._rng;
	}

	public void setConfiguration(RubisConfiguration conf)
	{
		this._conf = conf;

		initUserId(this._conf.getNumOfPreloadedUsers());
	}

	public RubisConfiguration getConfiguration()
	{
		return this._conf;
	}

	public boolean isAnonymousUser(RubisUser user)
	{
		return this.isAnonymousUser(user.id);
	}

	public boolean isAnonymousUser(int userId)
	{
		return ANONYMOUS_USER_ID == userId;
	}

	public boolean isLoggedUser(RubisUser user)
	{
		return !this.isLoggedUser(user.id);
	}

	public boolean isLoggedUser(int userId)
	{
		return !this.isAnonymousUser(userId);
	}

	public boolean isValidUser(RubisUser user)
	{
		return null != user && MIN_USER_ID <= user.id;
	}

	public boolean isValidItem(RubisItem item)
	{
		return null != item && MIN_ITEM_ID <= item.id;
	}

	public boolean isValidCategory(RubisCategory category)
	{
		return null != category && MIN_CATEGORY_ID <= category.id;
	}

	public boolean isValidRegion(RubisRegion region)
	{
		return null != region && MIN_REGION_ID <= region.id;
	}

	public boolean checkHttpResponse(HttpTransport httpTransport, String response)
	{
		if (response.length() == 0
			|| HttpStatus.SC_OK != httpTransport.getStatusCode()
			|| -1 != response.indexOf("ERROR"))
		{
			return false;
		}

		return true;
	}

	/**
	 * Creates a new RUBiS user object.
	 *
	 * @return an instance of RubisUser.
	 */
	public RubisUser newUser()
	{
		return this.getUser(nextUserId());
	}

	/**
	 * Generate a rngom RUBiS user among the ones already preloaded.
	 *
	 * @return an instance of RubisUser.
	 */
	public RubisUser generateUser()
	{
		// Only generate a user among the ones that have been already preloaded in the DB
		int userId = this._rng.nextInt(this._conf.getNumOfPreloadedUsers()-MIN_USER_ID)+MIN_USER_ID;

		return this.getUser(userId);
	}

	/**
	 * Get the RUBiS user associated to the given identifier.
	 *
	 * @param id The user identifier.
	 * @return an instance of RubisUser.
	 */
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

	/**
	 * Creates a new RUBiS item.
	 *
	 * @return an instance of RubisItem.
	 */
	public RubisItem newItem(int loggedUserId)
	{
		return this.getItem(INVALID_ITEM_ID, loggedUserId);
	}

//	/**
//	 * Generate a rngom RUBiS item among the ones already preloaded.
//	 *
//	 * @return an instance of RubisItem.
//	 */
//	public RubisItem generateItem()
//	{
//		int itemId = MIN_ITEM_ID-1;
//		int lastItemId = RubisGenerator.getLastItemId();
//		if (lastItemId >= MIN_ITEM_ID)
//		{
//			itemId = this.getRandomGenerator().nextInt(lastItemId+1-MIN_ITEM_ID)+MIN_ITEM_ID;
//		}
//		return this.getItem(itemId);
//	}

	/**
	 * Get the RUBiS item associated to the given identifier.
	 *
	 * @param id The item identifier.
	 * @return an instance of RubisItem.
	 */
	public RubisItem getItem(int id, int loggedUserId)
	{
		RubisItem item = new RubisItem();

		item.id = id;
		item.name = "RUBiS automatically generated item #" + item.id;
		item.description = this.generateText(1, this._conf.getMaxItemDescriptionLength());
		item.initialPrice = this._rng.nextInt((int) Math.round(this._conf.getMaxItemInitialPrice()))+1;
		if (this._rng.nextInt(this._conf.getTotalActiveItems()) < (this._conf.getPercentageOfUniqueItems()*this._conf.getTotalActiveItems()/100.0))
		{
			item.quantity = 1;
		}
		else
		{
			item.quantity = this._rng.nextInt(this._conf.getMaxItemQuantity())+1;
		}
		if (this._rng.nextInt(this._conf.getTotalActiveItems()) < (this._conf.getPercentageOfItemsReserve()*this._conf.getTotalActiveItems()/100.0))
		{
			item.reservePrice = this._rng.nextInt((int) Math.round(this._conf.getMaxItemBaseReservePrice()))+item.initialPrice;
		}
		else
		{
			item.reservePrice = 0;
		}
		if (this._rng.nextInt(this._conf.getTotalActiveItems()) < (this._conf.getPercentageOfItemsBuyNow()*this._conf.getTotalActiveItems()/100.0))
		{
			item.buyNow = this._rng.nextInt((int) Math.round(this._conf.getMaxItemBaseBuyNowPrice()))+item.initialPrice+item.reservePrice;
		}
		else
		{
			item.buyNow = 0;
		}
		item.nbOfBids = 0;
		item.maxBid = 0;
		Calendar cal = Calendar.getInstance();
		item.startDate = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, this._rng.nextInt(this._conf.getMaxItemDuration())+1);
		item.endDate = cal.getTime();
		item.seller = loggedUserId;
		item.category = this.generateCategory().id;

		return item;
	}

	public RubisCategory generateCategory()
	{
		return this.getCategory(this._rng.nextInt(this._conf.getCategories().size()-MIN_CATEGORY_ID)+MIN_CATEGORY_ID);
	}

	public RubisRegion generateRegion()
	{
		return this.getRegion(this._rng.nextInt(this._conf.getRegions().size()-MIN_REGION_ID)+MIN_REGION_ID);
	}

	public RubisCategory getCategory(int id)
	{
		RubisCategory category = new RubisCategory();

		category.id = id;
		category.name = this._conf.getCategories().get(category.id);

		return category;
	}

	public RubisRegion getRegion(int id)
	{
		RubisRegion region = new RubisRegion();

		region.id = id;
		region.name = this._conf.getRegions().get(region.id);

		return region;
	}

	public RubisComment generateComment(int fromUserId, int toUserId, int itemId)
	{
		return getComment(fromUserId,
						  toUserId,
						  itemId,
						  COMMENT_RATINGS[this._rng.nextInt(COMMENT_RATINGS.length)]);
	}

	public RubisComment getComment(int fromUserId, int toUserId, int itemId, int rating)
	{
		RubisComment comment = new RubisComment();

		comment.fromUserId = fromUserId;
		comment.toUserId = toUserId;
		comment.itemId = itemId;
		int rateIdx = Arrays.binarySearch(COMMENT_RATINGS, rating);
		comment.rating = COMMENT_RATINGS[rateIdx];
		comment.comment = this.generateText(1, this._conf.getMaxCommentLength()-COMMENTS[rateIdx].length()-System.lineSeparator().length()) + System.lineSeparator() + COMMENTS[rateIdx];
		Calendar cal = Calendar.getInstance();
		comment.date = cal.getTime();

		return comment;
	}

	/**
	 * Parses the given HTML text to find an item identifier.
	 *
	 * @param html The HTML string where to look for the item identifier.
	 * @return The found item identifier, or INVALID_ITEM_ID if
	 *  no item identifier is found. If more than one item is found, returns the
	 *  one picked at rngom.
	 *
	 * This method is based on the edu.rice.rubis.client.UserSession#extractItemIdFromHTML.
	 */
	public int findItemIdInHtml(String html)
	{
		if (html == null)
		{
			return INVALID_ITEM_ID;
		}

		// Count number of itemId
		int count = 0;
		int keyIdx = html.indexOf("itemId=");
		while (keyIdx != -1)
		{
			++count;
			keyIdx = html.indexOf("itemId=", keyIdx + 7); // 7 equals to "itemId="
		}
		if (count == 0)
		{
			return INVALID_ITEM_ID;
		}

		// Choose rngomly an item
		count = this._rng.nextInt(count) + 1;

		keyIdx = -7;
		while (count > 0)
		{
			keyIdx = html.indexOf("itemId=", keyIdx + 7); // 7 equals to itemId=
			--count;
		}

		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('\"', keyIdx + 7));
		lastIdx = minIndex(lastIdx, html.indexOf('?', keyIdx + 7));
		lastIdx = minIndex(lastIdx, html.indexOf('&', keyIdx + 7));
		lastIdx = minIndex(lastIdx, html.indexOf('>', keyIdx + 7));

		String str = html.substring(keyIdx + 7, lastIdx);

		return Integer.parseInt(str);
	}

	/**
	 * Parses the given HTML text to find the value of the given parameter.
	 *
	 * @param html The HTML string where to look for the parameter.
	 * @param paramName The name of the parameter to look for.
	 * @return The value of the parameter as a string, or null if
	 *  no parameter is found.
	 *
	 * This method is based on the edu.rice.rubis.client.UserSession#extractIntFromHTML
	 * and edu.rice.rubis.client.UserSession#extractFloatFromHTML.
	 */
	public String findParamInHtml(String html, String paramName)
	{
		if (html == null)
		{
			return null;
		}

		// Look for the parameter
//		int paramIdx = html.indexOf(paramName);
//		if (paramIdx == -1)
//		{
//			return null;
//		}
//		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('=', paramIdx + paramName.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('\"', paramIdx + paramName.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('?', paramIdx + paramName.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('&', paramIdx + paramName.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('>', paramIdx + paramName.length()));

//		return html.substring(paramIdx + paramName.length(), lastIdx);

		Pattern p = Pattern.compile("^.*?[&?]" + paramName + "=([^\"?&]*).+$");
		Matcher m = p.matcher(html);
		if (m.matches())
		{
			return m.group(1);
		}

		return null;
	}

	/**
	 * Parses the given HTML text to find the value of the given form parameter.
	 *
	 * @param html The HTML string where to look for the form parameter.
	 * @param paramName The name of the form parameter to look for.
	 * @return The value of the form parameter as a string, or null if
	 *  no parameter is found.
	 *
	 * This method is based on the edu.rice.rubis.client.UserSession#extractIntFromHTML
	 * and edu.rice.rubis.client.UserSession#extractFloatFromHTML.
	 */
	public String findFormParamInHtml(String html, String paramName)
	{
		if (html == null)
		{
			return null;
		}

		// Look for the parameter
//		String key = "name=" + paramName + " value=";
//		int keyIdx = html.indexOf(key);
//		if (keyIdx == -1)
//		{
//			return null;
//		}
//		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('=', keyIdx + key.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('\"', keyIdx + key.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('?', keyIdx + key.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('&', keyIdx + key.length()));
//		lastIdx = minIndex(lastIdx, html.indexOf('>', keyIdx + key.length()));
//
//		return html.substring(keyIdx + key.length(), lastIdx);

		Pattern p = Pattern.compile("^.*?<(?i:input)\\s+(?:.+\\s)?(?i:name)=" + paramName + "\\s+(?:.+\\s)?(?i:value)=([^\"?&>]+).+$");
		Matcher m = p.matcher(html);
		if (m.matches())
		{
			return m.group(1);
		}

		return null;
	}


	/**
	 * Parses the given HTML text to find the page value.
	 *
	 * @param html The HTML string where to look for the item identifier.
	 * @return The page value.
	 *
	 * This method is based on the edu.rice.rubis.client.UserSession#extractPageFromHTML
	 */
	public int findPageInHtml(String html)
	{
		if (html == null)
		{
			return 0;
		}

//		int firstPageIdx = html.indexOf("&page=");
//		if (firstPageIdx == -1)
//		{
//			return 0;
//		}
//		int secondPageIdx = html.indexOf("&page=", firstPageIdx + 6); // 6 equals to &page=
//		int chosenIdx = 0;
//		if (secondPageIdx == -1)
//		{
//			chosenIdx = firstPageIdx; // First or last page => go to next or previous page
//		}
//		else
//		{
//			// Choose rngomly a page (previous or next)
//			if (this._rng.nextInt(100000) < 50000)
//			{
//				chosenIdx = firstPageIdx;
//			}
//			else
//			{
//				chosenIdx = secondPageIdx;
//			}
//		}
//		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('\"', chosenIdx + 6));
//		lastIdx = minIndex(lastIdx, html.indexOf('?', chosenIdx + 6));
//		lastIdx = minIndex(lastIdx, html.indexOf('&', chosenIdx + 6));
//		lastIdx = minIndex(lastIdx, html.indexOf('>', chosenIdx + 6));
//
//		String str = html.substring(chosenIdx + 6, lastIdx);
//
//		return Integer.parseInt(str);

		////Pattern p = Pattern.compile("^.*?[&?]page=([^\"?&]*).*(?:[&?]page=([^\"?&]*).*)?$");
		//Pattern p = Pattern.compile("^.*?[&?]page=(\\d+).*?(?:[&?]page=(\\d+).*?)?$");
		Matcher m = _pageRegex.matcher(html);
		if (m.matches())
		{
			if (m.groupCount() == 2 && m.group(2) != null)
			{
				// Choose rngomly a page (previous or next)
				if (this._rng.nextInt(100000) < 50000)
				{
					return Integer.parseInt(m.group(1));
				}
				return Integer.parseInt(m.group(2));
			}

			// First or last page => go to next or previous page
			return (m.group(1) != null) ? Integer.parseInt(m.group(1)) : 0;
		}

		return 0;
	}

	/**
	 * Internal method that returns the min between ix1 and ix2 if ix2 is not
	 * equal to -1.
	 * 
	 * @param ix1 The first index
	 * @param ix2 The second index to compare with ix1
	 * @return ix2 if (ix2 < ix1 and ix2!=-1) else ix1
	 */
	private static int minIndex(int ix1, int ix2)
	{
		if (ix2 == -1)
		{
			return ix1;
		}
		if (ix1 <= ix2)
		{
			return ix1;
		}
		return ix2;
	}

	/**
	 * Generates a rngom text.
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

			String word = this.generateWord(1, left < this._conf.getMaxWordLength() ? left : this._conf.getMaxWordLength());
			buf.append(word);
			left -= word.length();
		}

		return buf.toString();
	}

	/**
	 * Generates a rngom word.
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
			int j = this._rng.nextInt(ALNUM_CHARS.length);
			buf[i] = ALNUM_CHARS[j];
		}

		return new String(buf);
	}
}
