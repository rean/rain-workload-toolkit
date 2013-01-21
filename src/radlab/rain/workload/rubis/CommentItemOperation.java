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
import radlab.rain.workload.rubis.model.RubisComment;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Comment operation.
 *
 * This emulates the operation of commenting on another user for a certain item.
 * Emulates the following requests:
 * 1. Click on the 'Leave a comment on this user' for a certain item and user
 * 2. Send authentication data (login name and password)
 * 3. Fill-in the form anc click on the 'Post this comment now!' button 
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class CommentItemOperation extends RubisOperation 
{
	public CommentItemOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Comment";
		this._operationIndex = RubisGenerator.COMMENT_ITEM_OP;
		//this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Generate a random item and user to which post the comment
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
		RubisUser toUser = null;
		try
		{
			RubisGenerator.lockUsers();
			toUser = this.getGenerator().generateUser();
		}
		finally
		{
			RubisGenerator.unlockUsers();
		}
		if (!this.getGenerator().isValidUser(toUser))
		{
			// Just print a warning, but do not set the operation as failed
			this.getLogger().warning("No valid user has been found. Operation interrupted.");
			this.setFailed(false);
			return;
		}

		// Click on the 'Leave a comment on this user' for a certain item and user
		// This will lead to a user authentification.
		URIBuilder uri = new URIBuilder(this.getGenerator().getPutCommentAuthURL());
		uri.setParameter("itemId", Integer.toString(item.id));
		uri.setParameter("to", Integer.toString(toUser.id));
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
		else
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
			this.getLogger().warning("Need a logged user; got an anonymous one. Operation interrupted.");
			this.setFailed(false);
			return;
		}

		HttpPost reqPost = null;
		List<NameValuePair> form = null;
		UrlEncodedFormEntity entity = null;

		// Send authentication data (login name and password)
		// This is the page the user can access when it has been successfully authenticated.
		reqPost = new HttpPost(this.getGenerator().getPutCommentURL());
		form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("itemId", Integer.toString(item.id)));
		form.add(new BasicNameValuePair("to", Integer.toString(toUser.id)));
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

 		// Fill-in the form anc click on the 'Post this comment now!' button 
		// This will really store the comment on the DB.
		RubisComment comment = this.getGenerator().generateComment(loggedUser.id, toUser.id, item.id);
		reqPost = new HttpPost(this.getGenerator().getStoreCommentURL());
		form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("from", Integer.toString(comment.fromUserId)));
		form.add(new BasicNameValuePair("to", Integer.toString(comment.toUserId)));
		form.add(new BasicNameValuePair("itemId", Integer.toString(comment.itemId)));
		form.add(new BasicNameValuePair("rating", Integer.toString(comment.rating)));
		form.add(new BasicNameValuePair("comment", comment.comment));
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
