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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import radlab.rain.IScoreboard;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Store-Bid operation.
 *
 * Emulates the following requests:
 * 1. Click on the 'Bid Now' image for a certain item
 * 2. Provide authentication data (login name and password)
 * 3. Fill-in the form anc click on the 'Bid now!' button
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class StoreBidOperation extends RubisOperation 
{
	public StoreBidOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Store-Bid";
		this._operationIndex = RubisGenerator.STORE_BID_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		//this.getLogger().finest("Begin Bid execution");

		StringBuilder response = null;

		// Get an item (from last response or from session)
		int itemId = this.getUtility().findItemIdInHtml(this.getSessionState().getLastResponse());
		RubisItem item = this.getUtility().getItem(itemId, this.getSessionState().getLoggedUserId());
		if (!this.getUtility().isValidItem(item))
		{
			// Try to see if there an item in session
			item = this.getUtility().getItem(this.getSessionState().getItemId(), this.getSessionState().getLoggedUserId());
			if (!this.getUtility().isValidItem(item))
			{
				this.getLogger().warning("No valid item has been found. Operation interrupted.");
				this.setFailed(true);
				return;
			}
		}

		// Need a logged user
		RubisUser loggedUser = this.getUtility().getUser(this.getSessionState().getLoggedUserId());
		if (!this.getUtility().isRegisteredUser(loggedUser))
		{
			this.getLogger().warning("No valid user has been found to log-in. Operation interrupted.");
			this.setFailed(true);
			return;
		}

		HttpPost reqPost = null;
		List<NameValuePair> form = null;
		UrlEncodedFormEntity entity = null;

		// Fill-in the form anc click on the 'Bid now!' button
		// This will really store the bid on the DB.
		String str = null;
		int maxQty = 0;
		str = this.getUtility().findFormParamInHtml(this.getSessionState().getLastResponse(), "maxQty");
		if (str != null && !str.isEmpty())
		{
			maxQty = Math.max(Integer.parseInt(str), maxQty);
		}
		int qty = (maxQty > 0) ? (this.getRandomGenerator().nextInt(maxQty)+1) : 0;
		float minBid = 0;
		str = this.getUtility().findFormParamInHtml(this.getSessionState().getLastResponse(), "minBid");
		if (str != null && !str.isEmpty())
		{
			minBid = Float.parseFloat(str);
		}
		int addBid = this.getRandomGenerator().nextInt(Math.round(this.getConfiguration().getMaxItemBaseBidPrice()))+1;
		float bid = minBid+addBid;
		float maxBid = minBid+addBid*2;
		reqPost = new HttpPost(this.getGenerator().getStoreBidURL());
		form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("itemId", Integer.toString(item.id)));
		form.add(new BasicNameValuePair("userId", Integer.toString(loggedUser.id)));
		form.add(new BasicNameValuePair("minBid", Float.toString(minBid)));
		form.add(new BasicNameValuePair("bid", Float.toString(bid)));
		form.add(new BasicNameValuePair("maxBid", Float.toString(maxBid)));
		form.add(new BasicNameValuePair("maxQty", Integer.toString(maxQty)));
		form.add(new BasicNameValuePair("qty", Integer.toString(qty)));
		entity = new UrlEncodedFormEntity(form, "UTF-8");
		reqPost.setEntity(entity);
		this.getLogger().finest("Send POST " + reqPost.getURI().toString());
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(reqPost.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());
		this.getSessionState().setItemId(item.id);

		this.setFailed(!this.getUtility().checkRubisResponse(response.toString()));

		//this.getLogger().finest("End Bid execution");
	}
}
