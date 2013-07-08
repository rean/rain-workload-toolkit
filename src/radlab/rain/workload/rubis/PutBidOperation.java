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
 * Put-Bid operation.
 *
 * Emulates the following requests:
 * 1. Provide authentication data (login name and password)
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class PutBidOperation extends RubisOperation 
{
	public PutBidOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Put-Bid";
		this._operationIndex = RubisGenerator.PUT_BID_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		//this.getLogger().finest("Begin Bid execution");

		StringBuilder response = null;

		// Get an item (from last response or from session)
		int itemId = this.getUtility().findItemIdInHtml(this.getSessionState().getLastResponse());
		RubisItem item = this.getGenerator().getItem(itemId);
		if (!this.getGenerator().isValidItem(item))
		{
			// Try to see if there an item in session
			item = this.getGenerator().getItem(this.getSessionState().getItemId());
			if (!this.getGenerator().isValidItem(item))
			{
				//TODO: in the original RUBiS client, a transition to the previous page is performed.
				//      We can emulate the same behavior by keeping the last visited page inside session state
				this.getLogger().warning("No valid item has been found. Operation interrupted.");
				this.setFailed(true);
				return;
			}
		}

		// Need a logged user
		RubisUser loggedUser = this.getGenerator().getUser(this.getSessionState().getLoggedUserId());
		if (!this.getGenerator().isValidUser(loggedUser) || this.getUtility().isAnonymousUser(loggedUser))
		{
			loggedUser = this.getGenerator().generateUser();
			if (!this.getGenerator().isValidUser(loggedUser) || this.getUtility().isAnonymousUser(loggedUser))
			{
				this.getLogger().warning("No valid user has been found to log-in. Operation interrupted.");
				this.setFailed(true);
				return;
			}
		}

		HttpPost reqPost = null;
		List<NameValuePair> form = null;
		UrlEncodedFormEntity entity = null;

		// Provide authentication data (login name and password)
		// This is the page the user can access when it has been successfully authenticated.
		reqPost = new HttpPost(this.getGenerator().getPutBidURL());
		form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("itemId", Integer.toString(item.id)));
		form.add(new BasicNameValuePair("nickname", loggedUser.nickname));
		form.add(new BasicNameValuePair("password", loggedUser.password));
		entity = new UrlEncodedFormEntity(form, "UTF-8");
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.getLogger().finest("Send POST " + reqPost.getURI().toString());
		this.trace(reqPost.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());
		this.getSessionState().setItemId(item.id);
		this.getSessionState().setLoggedUserId(loggedUser.id);

		this.setFailed(false);

		//this.getLogger().finest("End Bid execution");
	}
}
