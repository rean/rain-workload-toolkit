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


import java.util.Random;
import org.apache.http.HttpStatus;
import radlab.rain.util.HttpTransport;
import radlab.rain.workload.rubis.model.RubisCategory;
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
	private Random _rand = new Random();


	public RubisUtility()
	{
	}

	public boolean isAnonymousUser(RubisUser user)
	{
		return RubisConstants.ANONYMOUS_USER_ID == user.id;
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
	 * Parses the given HTML text to find an item identifier.
	 *
	 * @param html The HTML string where to look for the item identifier.
	 * @return The found item identifier, or RubisConstants.INVALID_ITEM_ID if
	 *  no item identifier is found. If more than one item is found, returns the
	 *  one picked at random.
	 *
	 * This method is based on the edu.rice.rubis.client.UserSession#extractItemIdFromHTML.
	 */
	public int findItemIdInHtml(String html)
	{
		if (html == null)
		{
			return RubisConstants.INVALID_ITEM_ID;
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
			return RubisConstants.INVALID_ITEM_ID;
		}

		// Choose randomly an item
		count = this._rand.nextInt(count) + 1;

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
		int paramIdx = html.indexOf(paramName);
		if (paramIdx == -1)
		{
			return null;
		}
		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('=', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('\"', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('?', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('&', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('>', paramIdx + paramName.length()));
		return html.substring(paramIdx + paramName.length(), lastIdx);
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
		String key = "name=" + paramName + " value=";
		int paramIdx = html.indexOf(key);
		if (paramIdx == -1)
		{
			return null;
		}
		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('=', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('\"', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('?', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('&', paramIdx + paramName.length()));
		lastIdx = minIndex(lastIdx, html.indexOf('>', paramIdx + paramName.length()));
		return html.substring(paramIdx + paramName.length(), lastIdx);
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

		int firstPageIdx = html.indexOf("&page=");
		if (firstPageIdx == -1)
		{
			return 0;
		}
		int secondPageIdx = html.indexOf("&page=", firstPageIdx + 6); // 6 equals to &page=
		int chosenIdx = 0;
		if (secondPageIdx == -1)
		{
			chosenIdx = firstPageIdx; // First or last page => go to next or previous page
		}
		else
		{
			// Choose randomly a page (previous or next)
			if (this._rand.nextInt(100000) < 50000)
			{
				chosenIdx = firstPageIdx;
			}
			else
			{
				chosenIdx = secondPageIdx;
			}
		}
		int lastIdx = minIndex(Integer.MAX_VALUE, html.indexOf('\"', chosenIdx + 6));
		lastIdx = minIndex(lastIdx, html.indexOf('?', chosenIdx + 6));
		lastIdx = minIndex(lastIdx, html.indexOf('&', chosenIdx + 6));
		lastIdx = minIndex(lastIdx, html.indexOf('>', chosenIdx + 6));

		String str = html.substring(chosenIdx + 6, lastIdx);

		return Integer.parseInt(str);
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
