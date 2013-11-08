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

package radlab.rain.workload.rubbos;


import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubbos.model.RubbosComment;
import radlab.rain.workload.rubbos.model.RubbosUser;


/**
 * Store-Comment operation.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class StoreCommentOperation extends RubbosOperation 
{
	private static final int MAX_COMMENT_SUBJECT_LENGTH = 100;


	public StoreCommentOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);

		this._operationName = "Store-Comment";
		this._operationIndex = RubbosGenerator.STORE_COMMENT_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		// Need a logged user
		RubbosUser loggedUser = this.getUtility().getUser(this.getSessionState().getLoggedUserId());
		if (!this.getUtility().isRegisteredUser(loggedUser))
		{
			this.getLogger().warning("No valid user has been found to log-in. Operation interrupted.");
			this.setFailed(true);
			return;
		}

		// Retrieve the comment to post in the last HTML response

		final String lastResponse = this.getSessionState().getLastResponse();

		RubbosComment comment = this.getUtility().newComment();

		int[] pos = this.getUtility().findRandomLastIndexInHtml(lastResponse, "name=comment_table value=", false);
		if (pos == null)
		{
			this.getLogger().warning("No 'comment_table' parameter has been found. Operation interrupted.");
			this.setFailed(true);
			return;
		}
		String commentTable = lastResponse.substring(pos[0], pos[1]);

		comment.storyId = this.getUtility().findIntInHtml(lastResponse, "name=storyId value=");
		comment.parent = this.getUtility().findIntInHtml(lastResponse, "name=parent value=");

		// Post the comment

		StringBuilder response = null;

		URIBuilder uri = new URIBuilder(this.getGenerator().getStoreCommentURL());
		uri.setParameter("comment_table", commentTable);
		uri.setParameter("nickname", loggedUser.nickname);
		uri.setParameter("password", loggedUser.password);
		uri.setParameter("storyId", Integer.toString(comment.storyId));
		uri.setParameter("parent", Integer.toString(comment.parent));
		uri.setParameter("subject", comment.subject);
		uri.setParameter("body", comment.body);
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

		this.setFailed(!this.getUtility().checkRubbosResponse(response.toString()));
	}
}
