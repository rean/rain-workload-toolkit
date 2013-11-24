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


import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import radlab.rain.util.HttpTransport;
import radlab.rain.workload.rubbos.model.RubbosCategory;
import radlab.rain.workload.rubbos.model.RubbosComment;
import radlab.rain.workload.rubbos.model.RubbosStory;
import radlab.rain.workload.rubbos.model.RubbosUser;


/**
 * Collection of RUBBoS utilities.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class RubbosUtility
{
	public static final int ANONYMOUS_USER_ID = 0;
	public static final int INVALID_CATEGORY_ID = -1;
	public static final int INVALID_COMMENT_ID = -1;
	public static final int INVALID_OPERATION_ID = -1;
	public static final int INVALID_STORY_ID = -1;
	public static final int INVALID_USER_ID = -1;
	public static final int MIN_RATING_VALUE = -1;
	public static final int MAX_RATING_VALUE = 1;


	private static final int MIN_USER_ID = 1; ///< Mininum value for user IDs
	private static final int MAX_COMMENT_SUBJECT_LENGTH = 100; ///< Maximum length for a comment's subject
	private static final int MAX_STORY_TITLE_LENGTH = 100; ///< Maximum length for a story's title
	private static AtomicInteger _userId;


	private Random _rng = null;
	private RubbosConfiguration _conf = null;
	private Pattern _pageRegex = Pattern.compile("^.*?[&?]page=(\\d+).*?(?:[&?]page=(\\d+).*?)?$");


	private static int nextUserId()
	{
		return _userId.incrementAndGet();
	}

	private static int lastUserId()
	{
		return _userId.get();
	}

	private static synchronized void initUserId(int numPreloadUsers)
	{
		_userId = new AtomicInteger(MIN_USER_ID+numPreloadUsers-1);
	}


	public RubbosUtility()
	{
	}

	public RubbosUtility(Random rng, RubbosConfiguration conf)
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

	public void setConfiguration(RubbosConfiguration conf)
	{
		this._conf = conf;

		initUserId(this._conf.getNumOfPreloadedUsers());
	}

	public RubbosConfiguration getConfiguration()
	{
		return this._conf;
	}

	public boolean isAnonymousUser(RubbosUser user)
	{
		return this.isValidUser(user) && this.isAnonymousUser(user.id);
	}

	public boolean isAnonymousUser(int userId)
	{
		return ANONYMOUS_USER_ID == userId;
	}

	public boolean isRegisteredUser(RubbosUser user)
	{
		return this.isValidUser(user) && !this.isAnonymousUser(user.id);
	}

	public boolean isRegisteredUser(int userId)
	{
		return !this.isAnonymousUser(userId);
	}

	public boolean isValidUser(RubbosUser user)
	{
		return null != user && INVALID_USER_ID != user.id && (MIN_USER_ID <= user.id || this.isAnonymousUser(user.id));
	}

	public boolean isValidCategory(RubbosCategory category)
	{
		return null != category && INVALID_CATEGORY_ID != category.id;
	}

	public boolean isValidStory(RubbosStory story)
	{
		return null != story && INVALID_STORY_ID != story.id;
	}

	public boolean isValidStory(int storyId)
	{
		return INVALID_STORY_ID != storyId;
	}

	public boolean isValidComment(RubbosComment comment)
	{
		return null != comment && INVALID_COMMENT_ID != comment.id;
	}

	public boolean checkHttpResponse(HttpTransport httpTransport, String response)
	{
		if (response == null
			|| response.length() == 0
			|| HttpStatus.SC_OK != httpTransport.getStatusCode()
			/*|| -1 != response.indexOf("ERROR")*/)
		{
			return false;
		}

		return true;
	}

	public boolean checkRubbosResponse(String response)
	{
		if (response == null
			|| response.length() == 0
			|| -1 != response.indexOf("ERROR"))
		{
			return false;
		}

		return true;
	}

	/**
	 * Creates a new RUBBoS user object.
	 *
	 * @return an instance of RubbosUser.
	 */
	public RubbosUser newUser()
	{
		return this.getUser(nextUserId());
	}

	/**
	 * Generate a random RUBBoS user among the ones already preloaded.
	 *
	 * @return an instance of RubbosUser.
	 */
	public RubbosUser generateUser()
	{
		// Only generate a user among the ones that have been already preloaded in the DB
		int userId = this._rng.nextInt(this._conf.getNumOfPreloadedUsers())+MIN_USER_ID;

		return this.getUser(userId);
	}

	/**
	 * Get the RUBBoS user associated to the given identifier.
	 *
	 * @param id The user identifier.
	 * @return an instance of RubbosUser.
	 */
	public RubbosUser getUser(int id)
	{
		RubbosUser user = new RubbosUser();

		user.id = id;
		user.firstname = "Great" + user.id;
		user.lastname = "User" + user.id;
		user.nickname = "user" + user.id;
		user.email = user.firstname + "." + user.lastname + "@rubbos.com";
		user.password = "password" + user.id;
		user.rating = MIN_RATING_VALUE;
		user.access = 0;
		Calendar cal = Calendar.getInstance();
		user.creationDate = cal.getTime();

		return user;
	}

	public RubbosComment newComment()
	{
		StringBuffer subject = new StringBuffer();
		StringBuffer body = new StringBuffer();

		int size = this._rng.nextInt(MAX_COMMENT_SUBJECT_LENGTH) + 1;
		do
		{
			subject.append(this.generateWord(true));
		}
		while ((subject != null) && (subject.length() < size));
		subject.setLength(size); // Make sure the subject is exactly of 'size' chars

		size = this._rng.nextInt(this._conf.getMaxCommentLength()) + 1;
		do
		{
			body.append(this.generateWord(true));
		}
		while ((body != null) && (body.length() < size));
		body.setLength(size); // Make sure the body is exactly of 'size' chars

		RubbosComment comment = new RubbosComment();

		comment.id = INVALID_COMMENT_ID;
		comment.writer = INVALID_USER_ID;
		comment.storyId = INVALID_STORY_ID;
		comment.parent = INVALID_COMMENT_ID;
		comment.childs = INVALID_COMMENT_ID;
		Calendar cal = Calendar.getInstance();
		comment.rating = 0;
		comment.date = cal.getTime();
		comment.subject = subject.toString();
		comment.body = body.toString();

		return comment;
	}

	public RubbosStory newStory()
	{
		StringBuffer title = new StringBuffer();
		StringBuffer body = new StringBuffer();

		int size = this._rng.nextInt(MAX_STORY_TITLE_LENGTH) + 1;
		do
		{
			title.append(this.generateWord(true));
		}
		while ((title != null) && (title.length() < size));
		title.setLength(size); // Make sure the title is exactly of 'size' chars

		size = this._rng.nextInt(this._conf.getMaxStoryLength()) + 1;
		do
		{
			body.append(this.generateWord(true));
		}
		while ((body != null) && (body.length() < size));
		body.setLength(size); // Make sure the body is exactly of 'size' chars

		RubbosStory story = new RubbosStory();

		story.id = INVALID_STORY_ID;
		story.title = title.toString();
		story.body = body.toString();
		Calendar cal = Calendar.getInstance();
		story.date = cal.getTime();
		story.writer = INVALID_USER_ID;
		story.category = INVALID_CATEGORY_ID;

		return story;
	}

	/**
	 * Parses the given HTML text to find a story identifier.
	 *
	 * @param html The HTML string where to look for the item identifier.
	 * @return The found story identifier, or INVALID_STORY_ID if
	 *  no story identifier is found. If more than one story is found, returns
	 *  the one picked at random.
	 *
	 * This method is based on the edu.rice.rubbos.client.UserSession#extractStoryIdFromHTML.
	 */
	public int findStoryIdInHtml(String html)
	{
		if (html == null)
		{
			return INVALID_STORY_ID;
		}

		int[] pos = this.findRandomLastIndexInHtml(html, "storyId=", false);
		if (pos == null)
		{
			return INVALID_STORY_ID;
		}

		String str = html.substring(pos[0], pos[1]);

		return Integer.parseInt(str);
	}

	/**
	 * Extract the story identifier from the review story page for an accept or reject.
	 * If several entries are found, one of them is picked up randomly.
	 *
	 * @return the selected story identifier
	 */
	public Integer findAcceptRejectStoryIdInHtml(String html, String scriptName)
	{
		if (html == null)
		{
			return INVALID_STORY_ID;
		}

		int[] pos = this.findRandomLastIndexInHtml(html, scriptName, false);
		if (pos == null)
		{
			return INVALID_STORY_ID;
		}

		int storyId = INVALID_STORY_ID;
		int newLast = this.findLastIndexInHtml(html, "storyId=", pos[1]);
		if (newLast != pos[1])
		{
			storyId = Integer.parseInt(html.substring(pos[1]+"storyId=".length()+1, newLast));
		}

		return storyId;
	}

	/**
	 * Parses the given HTML text to find an item identifier.
	 *
	 * @param html The HTML string where to look for the item identifier.
	 * @return The found item identifier, or INVALID_ITEM_ID if
	 *  no item identifier is found. If more than one item is found, returns the
	 *  one picked at random.
	 *
	 * This method is based on the edu.rice.rubbos.client.UserSession#extractCategoryFromHTML.
	 */
	public RubbosCategory findCategoryInHtml(String html)
	{
		if (html == null)
		{
			return null;
		}

		// Randomly choose a category
		int[] pos = this.findRandomLastIndexInHtml(html, "category=", false);
		if (pos == null)
		{
			return null;
		}

		String str = html.substring(pos[0], pos[1]);
		int categoryId = Integer.parseInt(str);

		String  categoryName = null;
		int newLast = this.findLastIndexInHtml(html, "categoryName=", pos[1]);
		if (newLast != pos[1])
		{
			categoryName = html.substring(pos[1]+"categoryName=".length()+1, newLast);
		}

		RubbosCategory category = new RubbosCategory();
		category.id = categoryId;
		category.name = categoryName;

		return category;
	}

	/**
	 * Extract Post-Comment parameters from the last HTML reply.
	 *
	 * If several entries are found, one of them is picked up randomly.
	 *
	 * @return a map containing the found parameters
	 */
	public Map<String,String> findPostCommentParamsInHtml(String html, String scriptName)
	{
		if (html == null)
		{
			return null;
		}

		int[] pos = this.findRandomLastIndexInHtml(html, scriptName, false);
		if (pos == null)
		{
			return null;
		}

		// Now we have chosen a 'scriptName?...' we can extract the parameters
		String  commentTable = null;
		int storyId = INVALID_STORY_ID;
		int parent = INVALID_COMMENT_ID;

		int newLast = this.findLastIndexInHtml(html, "comment_table=", pos[1]);
		if (newLast != pos[1])
		{
			commentTable = html.substring(pos[1]+"comment_table=".length()+1, newLast);
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "storyId=", pos[1]);
		if (newLast != pos[1])
		{
			storyId = Integer.parseInt(html.substring(pos[1]+"storyId=".length()+1, newLast));
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "parent=", pos[1]);
		if (newLast != pos[1])
		{
			parent = Integer.parseInt(html.substring(pos[1]+"parent=".length()+1, newLast));
		}

		Map<String,String> result = new HashMap(3);

		result.put("comment_table", commentTable);
		result.put("storyId", Integer.toString(storyId));
		result.put("parent", Integer.toString(parent));

		return result;
	}

	/**
	 * Extract View-Comment parameters from the last HTML reply.
	 *
	 * If several entries are found, one of them is picked up randomly.
	 *
	 * @return a map containing the found parameters
	 */
	public Map<String,String> findViewCommentParamsInHtml(String html, String scriptName)
	{
		if (html == null)
		{
			return null;
		}

		int[] pos = this.findRandomLastIndexInHtml(html, scriptName, false);
		if (pos == null)
		{
			return null;
		}

		// Now we have chosen a 'scriptName?...' we can extract the parameters
		String  commentTable = null;
		int storyId = INVALID_STORY_ID;
		int commentId = INVALID_COMMENT_ID;
		int filter = 0;
		int display = 0;

		int newLast = this.findLastIndexInHtml(html, "comment_table=", pos[1]);
		if (newLast != pos[1])
		{
			commentTable = html.substring(pos[1]+"comment_table=".length()+1, newLast);
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "storyId=", pos[1]);
		if (newLast != pos[1])
		{
			storyId = Integer.parseInt(html.substring(pos[1]+"storyId=".length()+1, newLast));
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "commentId=", pos[1]);
		if (newLast != pos[1])
		{
			commentId = Integer.parseInt(html.substring(pos[1]+"commentId=".length()+1, newLast));
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "filter=", pos[1]);
		if (newLast != pos[1])
		{
			filter = Integer.parseInt(html.substring(pos[1]+"filter=".length()+1, newLast));
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "display=", pos[1]);
		if (newLast != pos[1])
		{
			filter = Integer.parseInt(html.substring(pos[1]+"display=".length()+1, newLast));
		}

		Map<String,String> result = new HashMap(5);

		result.put("comment_table", commentTable);
		result.put("storyId", Integer.toString(storyId));
		result.put("commentId", Integer.toString(commentId));
		result.put("filter", Integer.toString(filter));
		result.put("display", Integer.toString(display));

		return result;
	}

	/**
	 * Extract Moderate-Comment parameters from the last HTML reply.
	 *
	 * If several entries are found, one of them is picked up randomly.
	 *
	 * @return a map containing the found parameters
	 */
	public Map<String,String> findModerateCommentParamsInHtml(String html, String scriptName)
	{
		if (html == null)
		{
			return null;
		}

		int[] pos = this.findRandomLastIndexInHtml(html, scriptName, false);
		if (pos == null)
		{
			return null;
		}

		// Now we have chosen a 'scriptName?...' we can extract the parameters
		String  commentTable = null;
		int commentId = INVALID_COMMENT_ID;

		int newLast = this.findLastIndexInHtml(html, "comment_table=", pos[1]);
		if (newLast != pos[1])
		{
			commentTable = html.substring(pos[1]+"comment_table=".length()+1, newLast);
		}
		pos[1] = newLast;
		newLast = this.findLastIndexInHtml(html, "commentId=", pos[1]);
		if (newLast != pos[1])
		{
			commentId = Integer.parseInt(html.substring(pos[1]+"commentId=".length()+1, newLast));
		}
		pos[1] = newLast;

		Map<String,String> result = new HashMap(2);

		result.put("comment_table", commentTable);
		result.put("commentId", Integer.toString(commentId));

		return result;
	}

	/**
	 * Extract an int value corresponding to the given key
	 * from the last HTML reply. Example : 
	 * <pre>int userId = extractIdFromHTML("&userId=")</pre>
	 *
	 * @param key the pattern to look for
	 * @return the <code>int</code> value or -1 on error.
	 */
	public int findIntInHtml(String html, String key)
	{
		if (html == null)
		{
			return -1;
		}

		// Look for the key
		int keyIndex = html.indexOf(key);
		if (keyIndex == -1)
		{
			return -1;
		}

		int lastIndex = html.indexOf('\"', keyIndex+key.length());
		lastIndex = minIndex(lastIndex, html.indexOf('?', keyIndex+key.length()));
		lastIndex = minIndex(lastIndex, html.indexOf('&', keyIndex+key.length()));
		lastIndex = minIndex(lastIndex, html.indexOf('>', keyIndex+key.length()));

		return Integer.parseInt(html.substring(keyIndex+key.length(), lastIndex));
	}

	/**
	 * Parses the given HTML text to find the page value.
	 *
	 * @param html The HTML string where to look for the item identifier.
	 * @return The page value.
	 *
	 * This method is based on the edu.rice.rubbos.client.UserSession#extractPageFromHTML
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
//			// Choose randomly a page (previous or next)
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
				// Choose randomly a page (previous or next)
				if (this._rng.nextFloat() < 0.5)
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
	 * Compute the index of the end character of a key value.
	 *
	 * If the key is not found, the lastIndex value is returned unchanged.
	 *
	 * This method is based on the edu.rice.rubbos.client.UserSession#computeLastIndex.
	 *
	 * @param html the string where looking for
	 * @param key the key to look for
	 * @param lastIndex index to start the lookup in html
	 * @return new lastIndex value
	 */
	public int findLastIndexInHtml(String html, String key, int lastIndex)
	{
		if (html == null)
		{
			return -1;
		}

		int keyIndex = html.indexOf(key, lastIndex);
		if (keyIndex != -1)
		{
			keyIndex += key.length();
			lastIndex = html.indexOf('\"', keyIndex);
			lastIndex = minIndex(lastIndex, html.indexOf('?', keyIndex));
			lastIndex = minIndex(lastIndex, html.indexOf('&', keyIndex));
			lastIndex = minIndex(lastIndex, html.indexOf('>', keyIndex));
		}

		return lastIndex;
	}

	/**
	 * Compute the index of the end character of a key value.
	 *
	 * If the key is not found, the lastIndex value is returned unchanged.
	 *
	 * This method is based on the edu.rice.rubbos.client.UserSession#randomComputeLastIndex.
	 *
	 * @param html the string where looking for
	 * @param key the key to look for
	 * @param skipFirst true if the first occurence of key must be ignored
	 * @return new lastIndex value
	 */
	public int[] findRandomLastIndexInHtml(String html, String key, boolean skipFirst)
	{
		if (html == null)
		{
			return null;
		}

		int count = 0;
		int keyIndex = html.indexOf(key);

		// 1. Count the number of times we find key
		while (keyIndex != -1)
		{
			++count;
			keyIndex = html.indexOf(key, keyIndex+key.length());
		}
		if ((count == 0) || (skipFirst && (count <= 1)))
		{
			return null;
		}

		// 2. Pickup randomly a key
		keyIndex = -key.length();
		count = this._rng.nextInt(count)+1;
		if ((skipFirst) && (count == 1))
		{
			++count; // Force to skip the first element
		}
		while (count > 0)
		{
			keyIndex = html.indexOf(key, keyIndex+key.length());
			--count;
		}
		keyIndex += key.length();

		int lastIndex = minIndex(Integer.MAX_VALUE, html.indexOf('\"', keyIndex));
		lastIndex = minIndex(lastIndex, html.indexOf('?', keyIndex));
		lastIndex = minIndex(lastIndex, html.indexOf('&', keyIndex));
		lastIndex = minIndex(lastIndex, html.indexOf('>', keyIndex));
		int[] result = new int[2];
		result[0] = keyIndex;
		result[1] = lastIndex;

		return result;
	}

	/**
	 * Generates a random word.
	 *
	 * @param minLen The minimum length of the word.
	 * @param maxLen The maximum length of the word.
	 * @return The generated word.
	 */
	public String generateWord(boolean withPunctuation)
	{
		final int maxSize = this._conf.getDictionary().size();

		String word = null;

		int pos = this._rng.nextInt(maxSize);
		int n = 0;
		for (String s : this._conf.getDictionary().keySet())
		{
			if (n == pos)
			{
				word = s;
				break;
			}
			++n;
		}
		if (word.indexOf(' ') != -1)
		{
			// Ignore everything after the first space
			word = word.substring(0, word.indexOf(' '));
		}

		if (withPunctuation)
		{
			switch (this._rng.nextInt(10))
			{
				case 0:
					word += ", ";
					break;
				case 1:
					word += ". ";
					break;
				case 2:
					word += " ? ";
					break;
				case 3:
					word += " ! ";
					break;
				case 4:
					word += ": ";
					break;
				case 5:
					word += " ; ";
					break;
				default:
					word += " ";
					break;
			}
		}

		return word;
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
}
