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
 * Author: Original authors
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013
 */

package radlab.rain.workload.olio;


import java.io.IOException;
import java.util.Set;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioTag;


/**
 * The TagSearchOperation is an operation that does a tag search. A random tag
 * is generated and the search result page is loaded.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class TagSearchOperation extends OlioOperation 
{
	public TagSearchOperation(boolean interactive, IScoreboard scoreboard)
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.TAG_SEARCH_OP_NAME;
		this._operationIndex = OlioGenerator.TAG_SEARCH_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		OlioTag tag = this.getUtility().generateTag();

		String searchUrl = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				searchUrl = this.getGenerator().getTagSearchURL() + "?tag=" + tag.name + "&tagsearchsubmit=Seach+Tags";
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				searchUrl = this.getGenerator().getTagSearchURL() + "?tag=" + tag.name + "&submit=Search+Tags";
				break;
		}
		StringBuilder response = this.getHttpTransport().fetchUrl(searchUrl);
		this.trace(searchUrl);
		if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + searchUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + searchUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.loadStatics(this.getGenerator().getTagSearchStatics());
		this.trace( this.getGenerator().getTagSearchStatics());

		Set<String> imageUrls = this.parseImages(response.toString());
		this.loadImages(imageUrls);
		this.trace(imageUrls);

		// Save session data
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}
}
