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


/**
 * The HomePageOperation is an operation that visits the home page. This
 * entails potentially loading static content (CSS/JS) and images.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class HomePageOperation extends OlioOperation 
{
	public HomePageOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.HOME_PAGE_OP_NAME;
		this._operationIndex = OlioGenerator.HOME_PAGE_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		response = this.getHttpTransport().fetchUrl(this.getGenerator().getHomePageURL());
		this.trace(this.getGenerator().getHomePageURL());
		if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getHomePageURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getHomePageURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Load the static files (CSS/JS).
		this.loadStatics(this.getGenerator().getHomePageStatics());
		this.trace(this.getGenerator().getHomePageStatics());

		// Always load the images.
		Set<String> imageURLs = this.parseImages(response.toString());
		this.loadImages(imageURLs);
		this.trace(imageURLs);

		// NOTE: In Apache Olio, the HTTP response is parsed to look for an event ID
		//       Instead, we defer this to the operation that really needs this.
		//       We can do this since the HTTP response is saved in the session state

		// Save session data
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}
}
