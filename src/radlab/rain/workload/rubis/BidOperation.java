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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Bid operation.
 *
 * Emulates the following requests:
 * 1. Click on the 'Bid Now' image for a certain item
 * 2. Provide authentication data (login name and password)
 * 3. Fill-in the form anc click on the 'Bid now!' button
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class BidOperation extends RubisOperation 
{
	public BidOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Bid";
		this._operationIndex = RubisGenerator.BID_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		this.getLogger().finest("Begin Bid execution");

		StringBuilder response = null;

		// Since an item id must be provided, generate a random one
		RubisItem item = this.getGenerator().generateItem();
		if (!this.getGenerator().isValidItem(item))
		{
			// Just print a warning, but do not set the operation as failed
			this.getLogger().warning("No valid item has been found. Operation interrupted.");
			this.setFailed(false);
			return;
		}

		// Click on the 'Bid Now' image for a certain item
		// This will lead to a user authentification.
		URIBuilder uri = new URIBuilder(this.getGenerator().getPutBidAuthURL());
		uri.setParameter("itemId", Integer.toString(item.id));
		HttpGet reqGet = new HttpGet(uri.build());
		this.getLogger().finest("Send GET " + reqGet.getURI().toString());
		response = this.getHttpTransport().fetch(reqGet);
		this.trace(reqGet.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Need a logged user
		RubisUser loggedUser = null;
		if (this.getGenerator().isUserLoggedIn())
		{
			loggedUser = this.getGenerator().getLoggedUser();
		}
		else
		{
			// Randomly generate a user
			loggedUser = this.getGenerator().generateUser();
			this.getGenerator().setLoggedUserId(loggedUser.id);
		}
		// If no user has been already registered, we still get an anonymous user.
		if (!this.getGenerator().isValidUser(loggedUser))
		{
			// Just print a warning, but do not set the operation as failed
			this.getLogger().warning("Need a logged user; got an anonymous one. Operation interrupted.");
			this.setFailed(false);
			return;
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
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Fill-in the form anc click on the 'Bid now!' button
		// This will really store the bid on the DB.
		String str = null;
		str = RubisUtility.extractFormParamFromHtml(response.toString(), "maxQty");
		int maxQty = 1;
		if (str != null)
		{
			maxQty = Integer.parseInt(str);
		}
		int qty = this.getRandomGenerator().nextInt(maxQty)+1;
		str = RubisUtility.extractFormParamFromHtml(response.toString(), "minBid");
		float minBid = 0;
		if (str != null)
		{
			minBid = Float.parseFloat(str);
		}
		int addBid = this.getRandomGenerator().nextInt(this.getGenerator().getMaxAddBid())+1;
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
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.setFailed(false);

		this.getLogger().finest("End Bid execution");
	}
}
