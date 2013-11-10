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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubis.model.RubisItem;


/**
 * View-Item operation.
 *
 * Emulates the following requests:
 * 1. Click on the item name link
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class ViewItemOperation extends RubisOperation 
{
	public ViewItemOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "View-Item";
		this._operationIndex = RubisGenerator.VIEW_ITEM_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		// Get an item (from last response or from session)
		int itemId = this.getUtility().findItemIdInHtml(this.getSessionState().getLastResponse());
		RubisItem item = this.getUtility().getItem(itemId, this.getSessionState().getLoggedUserId());
		if (!this.getUtility().isValidItem(item))
		{
			// Try to see if there an item in session
			item = this.getUtility().getItem(this.getSessionState().getItemId(), this.getSessionState().getLoggedUserId());
			if (!this.getUtility().isValidItem(item))
			{
				//NOTE: The official RUBiS client goes back to the previous operation.
				//      Currently, it seems there is no way to instruct RAIN to do a
				//      similar thing.
				this.getLogger().warning("No valid item has been found neither in last HTML response nor in session. Last response is: " + this.getSessionState().getLastResponse() + ". Operation interrupted.");
				this.setFailed(true);
				return;
			}
		}

		// Click on the item name link
		URIBuilder uri = new URIBuilder(this.getGenerator().getViewItemURL());
		uri.setParameter("itemId", Integer.toString(item.id));
		HttpGet reqGet = new HttpGet(uri.build());
		StringBuilder response = this.getHttpTransport().fetch(reqGet);
		this.trace(reqGet.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());
		this.getSessionState().setItemId(item.id);

		this.setFailed(!this.getUtility().checkRubisResponse(response.toString()));
	}
}
