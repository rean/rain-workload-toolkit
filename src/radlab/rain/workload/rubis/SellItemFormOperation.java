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
import radlab.rain.IScoreboard;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import radlab.rain.workload.rubis.model.RubisCategory;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Sell-Item-Form operation.
 *
 * This is the operation of selling a certain item.
 *
 * Emulates the following requests:
 * 1. Fill-in the form and click on the 'Register item!' button
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class SellItemFormOperation extends RubisOperation 
{
	public SellItemFormOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Sell-Item-Form";
		this._operationIndex = RubisGenerator.SELL_ITEM_FORM_OP;
		//this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		RubisUser loggedUser = this.getUtility().getUser(this.getSessionState().getLoggedUserId());
		if (!this.getUtility().isValidUser(loggedUser))
		{
			loggedUser = this.getUtility().generateUser();
			if (!this.getUtility().isValidUser(loggedUser) || this.getUtility().isAnonymousUser(loggedUser))
			{
				this.getLogger().warning("No valid user has been found. Operation interrupted.");
				this.setFailed(true);
				return;
			}
		}

		// Generate a random category
		RubisCategory category = this.getUtility().generateCategory();
		if (!this.getUtility().isValidCategory(category))
		{
			this.getLogger().warning("No valid category has been found. Operation interrupted.");
			this.setFailed(true);
			return;
		}

		// Select the category of the item to sell
		URIBuilder uri = new URIBuilder(this.getGenerator().getSellItemFormURL());
		uri.setParameter("user", Integer.toString(loggedUser.id));
		uri.setParameter("category", Integer.toString(category.id));
		HttpGet reqGet = new HttpGet(uri.build());
		response = this.getHttpTransport().fetch(reqGet);
		this.trace(reqGet.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());
		this.getSessionState().setLoggedUserId(loggedUser.id);

		this.setFailed(false);
	}
}
