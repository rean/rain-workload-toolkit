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
 * Buy-Now-Item operation.
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
		Map<String,String> headers = null;

		// Generate a random item and perform a Buy-Now-Auth operation
		RubisItem item = this.getGenerator().generateItem();
		HttpGet reqGet = new HttpGet(this.getGenerator().getBuyNowAuthURL());
		headers = new HashMap<String,String>();
		headers.put("itemId", Integer.toString(item.id));
		response = this.getHttpTransport().fetch(reqGet, headers);
		this.trace(this.getGenerator().getBuyNowAuthURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getBuyNowAuthURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Perform a Buy-Now operation. Need a logged user
		//headers.put("itemId", Integer.toString(item.id));
		if (this.getGenerator().isUserLoggedIn())
		{
			HttpPost reqPost = null;
			MultipartEntity entity = null;
			RubisUser user = this.getGenerator().getLoggedUser();

			reqPost = new HttpPost(this.getGenerator().getBuyNowURL());
			entity = new MultipartEntity();
			entity.addPart("itemId", new StringBody(Integer.toString(item.id)));
			entity.addPart("nickname", new StringBody(user.nickname));
			entity.addPart("password", new StringBody(user.password));
			reqPost.setEntity(entity);
			response = this.getHttpTransport().fetch(reqPost);
			this.trace(this.getGenerator().getBuyNowURL());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getBuyNowURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}

			// Perform a Store-Buy-Now operation.
			reqPost = new HttpPost(this.getGenerator().getStoreBuyNowURL());
			entity = new MultipartEntity();
			entity.addPart("itemId", new StringBody(Integer.toString(item.id)));
			entity.addPart("userId", new StringBody(Integer.toString(user.id)));
			int maxQty = Math.min(item.quantity, 1);
			entity.addPart("maxQty", new StringBody(Integer.toString(maxQty)));
			entity.addPart("qty", new StringBody(Integer.toString(this.getRandomGenerator().nextInt(maxQty)+1)));
			reqPost.setEntity(entity);
			response = this.getHttpTransport().fetch(reqPost);
			this.trace(this.getGenerator().getStoreBuyNowURL());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getStoreBuyNowURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
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
