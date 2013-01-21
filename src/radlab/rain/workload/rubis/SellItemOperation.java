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
import java.util.Calendar;
import java.util.Date;
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
		try
		{
			RubisGenerator.lockItems();

			StringBuilder response = null;

			// Go to the sell home page
			response = this.getHttpTransport().fetchUrl(this.getGenerator().getSellURL());
			this.trace(this.getGenerator().getSellURL());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getSellURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getSellURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}

			// Need a logged user
			RubisUser loggedUser = null;
			if (this.getGenerator().isUserLoggedIn())
			{
				loggedUser = this.getGenerator().getLoggedUser();
			}
			else if (this.getGenerator().isUserAvailable())
			{
				// Randomly generate a user
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
				this.getLogger().warning("No valid user has been found. Operation interrupted.");
				this.setFailed(false);
				return;
			}

			HttpPost reqPost = null;
			List<NameValuePair> form = null;
			UrlEncodedFormEntity entity = null;

			// Send authentication data (login name and password) and click on the 'Log In!' link
			reqPost = new HttpPost(this.getGenerator().getBrowseCategoriesURL());
			form = new ArrayList<NameValuePair>();
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

			// Generate a new item
			RubisItem item = this.getGenerator().newItem();
			if (!this.getGenerator().isValidItem(item))
			{
				// Just print a warning, but do not set the operation as failed
				this.getLogger().warning("No valid item has been found. Operation interrupted.");
				this.setFailed(false);
				return;
			}

			// Select the category of the item to sell
			URIBuilder uri = new URIBuilder(this.getGenerator().getSellItemFormURL());
			uri.setParameter("user", Integer.toString(loggedUser.id));
			uri.setParameter("category", Integer.toString(item.category));
			HttpGet reqGet = new HttpGet(uri.build());
			response = this.getHttpTransport().fetch(reqGet);
			this.trace(reqGet.getURI().toString());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				this.getLogger().severe("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
				throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}

			// Fill-in the form and click on the 'Register item!' button
			reqPost = new HttpPost(this.getGenerator().getRegisterItemURL());
			form = new ArrayList<NameValuePair>();
			form.add(new BasicNameValuePair("name", item.name));
			form.add(new BasicNameValuePair("description", item.description));
			form.add(new BasicNameValuePair("initialPrice", Float.toString(item.initialPrice)));
			form.add(new BasicNameValuePair("reservePrice", Float.toString(item.reservePrice)));
			form.add(new BasicNameValuePair("buyNow", Float.toString(item.buyNow)));
			form.add(new BasicNameValuePair("duration", Integer.toString(this.getDuration(item.startDate, item.endDate))));
			form.add(new BasicNameValuePair("quantity", Integer.toString(item.quantity)));
			form.add(new BasicNameValuePair("userId", Integer.toString(loggedUser.id)));
			form.add(new BasicNameValuePair("categoryId", Integer.toString(this.getGenerator().getCategory(item.category).id)));
			entity = new UrlEncodedFormEntity(form, "UTF-8");
			reqPost.setEntity(entity);
			response = this.getHttpTransport().fetch(reqPost);
			this.trace(reqPost.getURI().toString());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
				throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}
		}
		finally
		{
			RubisGenerator.unlockItems();
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
