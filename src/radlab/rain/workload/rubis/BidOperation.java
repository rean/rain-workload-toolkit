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
import java.util.HashMap;
import java.util.Map;
import radlab.rain.IScoreboard;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Bid operation.
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
		//this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;
		Map<String,String> headers = null;

		// Perform a Put-Bid-Auth operation
		// This will lead to a user authentification.
		// Since an item id must be provided, generate a random item.
		RubisItem item = this.getGenerator().generateItem();
		HttpGet reqGet = new HttpGet(this.getGenerator().getPutBidAuthURL());
		headers = new HashMap<String,String>();
		headers.put("itemId", Integer.toString(item.id));
		response = this.getHttpTransport().fetch(reqGet, headers);
		this.trace(this.getGenerator().getPutBidAuthURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getPutBidAuthURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Perform a Buy-Now operation. Need a logged user
		// This is the page the user can access when it has been successfully authenticated.
		// You must provide the item id, user name and password.
		//headers.put("itemId", Integer.toString(item.id));
		if (this.getGenerator().isUserLoggedIn())
		{
			HttpPost reqPost = null;
			MultipartEntity entity = null;
			RubisUser user = this.getGenerator().getLoggedUser();

			reqPost = new HttpPost(this.getGenerator().getPutBidURL());
			entity = new MultipartEntity();
			entity.addPart("itemId", new StringBody(Integer.toString(item.id)));
			entity.addPart("nickname", new StringBody(user.nickname));
			entity.addPart("password", new StringBody(user.password));
			reqPost.setEntity(entity);
			response = this.getHttpTransport().fetch(reqPost);
			this.trace(this.getGenerator().getPutBidURL());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getPutBidURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}

			// Perform a Store-Bid operation to really store the bid on the DB.
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
			entity = new MultipartEntity();
			entity.addPart("itemId", new StringBody(Integer.toString(item.id)));
			entity.addPart("userId", new StringBody(Integer.toString(user.id)));
			entity.addPart("minBid", new StringBody(Float.toString(minBid)));
			entity.addPart("bid", new StringBody(Float.toString(bid)));
			entity.addPart("maxBid", new StringBody(Float.toString(maxBid)));
			entity.addPart("maxQty", new StringBody(Integer.toString(maxQty)));
			entity.addPart("qty", new StringBody(Integer.toString(qty)));
			reqPost.setEntity(entity);
			response = this.getHttpTransport().fetch(reqPost);
			this.trace(this.getGenerator().getStoreBidURL());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getStoreBidURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}
		}
		else
		{
			//FIXME: What's the best way to handle this case?
			//NOTE: We do not throw any exception since this isn't a RAIN error
			System.err.println("Login required for " + this._operationName);
		}

		this.setFailed(false);
	}
}
