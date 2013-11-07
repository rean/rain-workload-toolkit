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
import radlab.rain.IScoreboard;


/**
 * The 'Search-Users' operation.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class SearchUsersOperation extends RubbosOperation 
{
	public SearchUsersOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);

		this._operationName = "Search-Users";
		this._operationIndex = RubbosGenerator.SEARCH_IN_USERS_OP;
	}

	@Override
	public void execute() throws Throwable
	{
        StringBuilder response = null;

		String keyword = null;
		if (this.getSessionState().getLastSearchOperation() != RubbosGenerator.SEARCH_IN_USERS_OP)
		{
			// Randomly generate a new keyword
			keyword = this.getUtility().generateUser().nickname;
		}
		else
		{
			// Get the keyword used in the previous search
			keyword = this.getSessionState().getLastSearchWord();
		}

        URIBuilder uri = new URIBuilder(this.getGenerator().getSearchUsersURL());
        uri.setParameter("type", "2");
        uri.setParameter("search", keyword);
        uri.setParameter("page", Integer.toString(this.getUtility().findPageInHtml(this.getSessionState().getLastResponse())));
        uri.setParameter("nbOfStories", this.getConfiguration().getNumOfStoriesPerPage());
        HttpGet reqGet = new HttpGet(uri.build());
        response = this.getHttpTransport().fetch(reqGet);
		this.trace(this.getGenerator().getSearchUsersURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getSearchUsersURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getSearchUsersURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());
		this.getSessionState().setLastSearchOperation(RubbosGenerator.SEARCH_IN_USERS_OP);
		this.getSessionState().setLastSearchWord(keyword);

		this.setFailed(!this.getUtility().checkRubbosResponse(response.toString()));
	}
}
