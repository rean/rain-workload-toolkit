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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioTag;


/**
 * The TagSearchOperation is an operation that does a tag search. A random tag
 * is generated and the search result page is loaded.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
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

		HttpPost reqPost = null;
		List<NameValuePair> form = null;
		UrlEncodedFormEntity entity = null;
		StringBuilder response = null;

		reqPost = new HttpPost(this.getGenerator().getTagSearchURL());
		form = new ArrayList<NameValuePair>();
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
                form.add(new BasicNameValuePair("tag", tag.name));
                form.add(new BasicNameValuePair("tagsearchsubmit", "Search+Tags"));
				break;
			case OlioConfiguration.PHP_INCARNATION:
                form.add(new BasicNameValuePair("tag", tag.name));
                form.add(new BasicNameValuePair("tagsearchsubmit", "Search+Tags"));
				break;
			case OlioConfiguration.RAILS_INCARNATION:
                form.add(new BasicNameValuePair("tag", tag.name));
                form.add(new BasicNameValuePair("submit", "Search+Tags"));
				break;
		}
        entity = new UrlEncodedFormEntity(form, "UTF-8");
        reqPost.setEntity(entity);
        response = this.getHttpTransport().fetch(reqPost);
		this.trace(reqPost.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}


		// 
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				{
					URIBuilder uri = new URIBuilder(this.getGenerator().getTagCloudURL());
					uri.setParameter("tag", tag.name);
					HttpGet reqGet = new HttpGet(uri.build());
					response = this.getHttpTransport().fetch(reqGet);
					this.trace(reqGet.getURI().toString());
					if (!this.getGenerator().checkHttpResponse(response.toString()))
					{
						this.getLogger().severe("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
						throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
					}
				}
			case OlioConfiguration.PHP_INCARNATION:
				{
					URIBuilder uri = new URIBuilder(this.getGenerator().getTagCloudURL());
					uri.setParameter("tag", tag.name);
					uri.setParameter("count", Integer.toString(tag.refCount));
					HttpGet reqGet = new HttpGet(uri.build());
					response = this.getHttpTransport().fetch(reqGet);
					this.trace(reqGet.getURI().toString());
					if (!this.getGenerator().checkHttpResponse(response.toString()))
					{
						this.getLogger().severe("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
						throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
					}
				}
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				// In original Apache Olio, there is no tag cloud request for RAILS incarnation
				break;
		}
		this.loadStatics(this.getGenerator().getTagSearchStatics());
		this.trace( this.getGenerator().getTagSearchStatics());

		Set<String> imageUrls = this.parseImages(response.toString());
		this.loadImages(imageUrls);
		this.trace(imageUrls);

		//NOTE: In the original Apache Olio, at this point a new event is
		//      selected from last HTTP response and stored in a member variable
		//      in order to be used later.
		//      Here, we prefer to defer this action when it is really needed.
		//      This is possible since we store the last response in the session
		//      state.

		// Save session data
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}
}
