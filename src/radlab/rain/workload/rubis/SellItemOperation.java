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
import java.util.Calendar;
import java.util.Date;
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
 * Sell operation.
 *
 * This is the operation of selling a certain item.
 *
 * Emulates the following requests:
 * 1. Go to the sell page
 * 2. Send authentication data (login name and password) and click on the 'Log In!' link
 * 3. Select the category of the item to sell
 * 4. Fill-in the form and click on the 'Register item!' button
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class SellItemOperation extends RubisOperation 
{
	public SellItemOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Sell";
		this._operationIndex = RubisGenerator.SELL_ITEM_OP;
		//this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Go to the sell home page
		response = this.getHttpTransport().fetchUrl(this.getGenerator().getSellURL());
		this.trace(this.getGenerator().getSellURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getSellURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
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

		HttpPost reqPost = null;
		MultipartEntity entity = null;

		// Send authentication data (login name and password) and click on the 'Log In!' link
		reqPost = new HttpPost(this.getGenerator().getBrowseCategoriesURL());
		entity = new MultipartEntity();
		entity.addPart("nickname", new StringBody(loggedUser.nickname));
		entity.addPart("password", new StringBody(loggedUser.password));
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(this.getGenerator().getBrowseCategoriesURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getBrowseCategoriesURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Generate a new item
		RubisItem item = this.getGenerator().newItem();

		// Select the category of the item to sell
		Map<String,String> headers = null;
		HttpGet reqGet = null;
		reqGet = new HttpGet(this.getGenerator().getSellItemFormURL());
		headers = new HashMap<String,String>();
		headers.put("user", Integer.toString(loggedUser.id));
		headers.put("category", Integer.toString(item.category));
		response = this.getHttpTransport().fetch(reqGet, headers);
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getSellItemFormURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Fill-in the form and click on the 'Register item!' button
		reqPost = new HttpPost(this.getGenerator().getRegisterItemURL());
		entity = new MultipartEntity();
		entity.addPart("name", new StringBody(item.name));
		entity.addPart("description", new StringBody(item.description));
		entity.addPart("initialPrice", new StringBody(Float.toString(item.initialPrice)));
		entity.addPart("reservePrice", new StringBody(Float.toString(item.reservePrice)));
		entity.addPart("buyNow", new StringBody(Float.toString(item.buyNow)));
		entity.addPart("duration", new StringBody(Integer.toString(this.getDuration(item.startDate, item.endDate))));
		entity.addPart("quantity", new StringBody(Integer.toString(item.quantity)));
		entity.addPart("userId", new StringBody(Integer.toString(loggedUser.id)));
		entity.addPart("categoryId", new StringBody(Integer.toString(this.getGenerator().getCategory(item.category).id)));
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(this.getGenerator().getRegisterItemURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getRegisterItemURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.setFailed(false);
	}

	/// Returns the number of days between the two dates.
	private static int getDuration(Date from, Date to)
	{
		final int DAY_MILLISECS = 1000*60*60*24;

		Calendar cal = Calendar.getInstance();

		cal.setTime(from);
		long fromTs = cal.getTimeInMillis();
		cal.setTime(to);
		long toTs = cal.getTimeInMillis();

		long diffTs = Math.abs(toTs-fromTs);

		return (int)(diffTs/DAY_MILLISECS);
	}
}
