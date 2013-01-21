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
 * Buy-Now-Item operation.
 *
 * Emulates the following requests:
 * 1. Click on the 'Buy-Now' link located in the item detail page
 * 2. Send user authentication (login name and password)
 * 3. Fill-in the form and click on the 'Buy now!' button, to buy the item
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class BuyNowItemOperation extends RubisOperation 
{
	public BuyNowItemOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Buy Now Item";
		this._operationIndex = RubisGenerator.BUY_NOW_ITEM_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Generate a random item and perform a Buy-Now-Auth operation
		RubisItem item = null;
		try
		{
			RubisGenerator.lockItems();
			item = this.getGenerator().generateItem();
		}
		finally
		{
			RubisGenerator.unlockItems();
		}
		if (!this.getGenerator().isValidItem(item))
		{
			// Just print a warning, but do not set the operation as failed
			this.getLogger().warning("No valid item has been found. Operation interrupted.");
			this.setFailed(false);
			return;
		}

		// Click on the 'Buy-Now' link located in the item detail page
		URIBuilder uri = new URIBuilder(this.getGenerator().getBuyNowAuthURL());
		uri.setParameter("itemId", Integer.toString(item.id));
		HttpGet reqGet = new HttpGet(uri.build());
		response = this.getHttpTransport().fetch(reqGet);
		this.trace(reqGet.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Need a logged user
		RubisUser loggedUser = null;
		if (this.getGenerator().isUserLoggedIn())
		{
			loggedUser = this.getGenerator().getLoggedUser();
		}
		else if (this.getGenerator().isUserAvailable())
		{
			try
			{
				RubisGenerator.lockUsers();
				loggedUser = this.getGenerator().generateUser();
			}
			finally
			{
				RubisGenerator.unlockUsers();
			}
			this.getGenerator().setLoggedUserId(loggedUser.id);
		}
		if (!this.getGenerator().isValidUser(loggedUser))
		{
			// Just print a warning, but do not set the operation as failed
			this.getLogger().warning("No valid user has been found to log-in. Operation interrupted.");
			this.setFailed(false);
			return;
		}

		HttpPost reqPost = null;
		List<NameValuePair> form = null;
		UrlEncodedFormEntity entity = null;

		// Send user authentication (login name and password)
		reqPost = new HttpPost(this.getGenerator().getBuyNowURL());
		form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("itemId", Integer.toString(item.id)));
		form.add(new BasicNameValuePair("nickname", loggedUser.nickname));
		form.add(new BasicNameValuePair("password", loggedUser.password));
		entity = new UrlEncodedFormEntity(form, "UTF-8");
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(reqPost.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Fill-in the form and click on the 'Buy now!' button, to buy the item
		reqPost = new HttpPost(this.getGenerator().getStoreBuyNowURL());
		form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("itemId", Integer.toString(item.id)));
		form.add(new BasicNameValuePair("userId", Integer.toString(loggedUser.id)));
		String str = null;
		int maxQty = 1;
		str = RubisUtility.extractFormParamFromHtml(response.toString(), "maxQty");
		if (str != null && !str.isEmpty())
		{
			maxQty = Math.max(Integer.parseInt(str), maxQty);
		}
		form.add(new BasicNameValuePair("maxQty", Integer.toString(maxQty)));
		int qty = this.getRandomGenerator().nextInt(maxQty)+1;
		form.add(new BasicNameValuePair("qty", Integer.toString(qty)));
		entity = new UrlEncodedFormEntity(form, "UTF-8");
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(reqPost.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.setFailed(false);
	}
}
