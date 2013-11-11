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


import java.net.URI;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.TraceRecord;
import radlab.rain.util.HttpTransport;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Base class for RUBiS operations.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public abstract class RubisOperation extends Operation 
{
	public RubisOperation(boolean interactive, IScoreboard scoreboard)
	{
		super(interactive, scoreboard);
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;

		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if (currentLoadProfile != null)
		{
			this.setGeneratedDuringProfile(currentLoadProfile);
		}

		final String lastResponse = this.getSessionState().getLastResponse();

		if (lastResponse != null && lastResponse.indexOf("Sorry") != -1)
		{
			// FIXME: Nothing matched the request, we have to go back to the previous operation
//			this.getLogger().warning("Operation completed with warnings. Last response is: " + lastResponse);
//			//this.setFailed(true);
		}
	}

	@Override
	public void postExecute() 
	{
		this.getSessionState().setLastOperation(this._operationIndex);

		final String lastResponse = this.getSessionState().getLastResponse();

		if (this.isFailed())
		{
			this.getLogger().severe("Operation '" + this.getOperationName() + "' failed to execute. Last request is: '" + this.getLastRequest() + "'. Last response is: " + lastResponse);
			this.getSessionState().setLastResponse(null);
		}
		else if (lastResponse != null)
		{
			// Look for any image to download
			try
			{
				this.loadImages(this.parseImagesInHtml(lastResponse));
			}
			catch (Throwable t)
			{
				this.getLogger().warning("Unable to load images");
				this.setFailed(true);
			}

			// Check for possible errors
			if (lastResponse.indexOf("ERROR") != -1)
			{
				// A logic error happened on the server-side
				this.getLogger().severe("Operation '" + this.getOperationName() + "' completed with server-side. Last request is: '" + this.getLastRequest() + "'. Last response is: " + lastResponse);
				this.getSessionState().setLastResponse(null);
				this.setFailed(true);
			}
		}
	}

	@Override
	public void cleanup()
	{
		// Empty
	}

	public RubisGenerator getGenerator()
	{
		return (RubisGenerator) this._generator;
	}

	public HttpTransport getHttpTransport()
	{
		return this.getGenerator().getHttpTransport();
	}

	public Random getRandomGenerator()
	{
		return this.getGenerator().getRandomGenerator();
	}

	public Logger getLogger()
	{
		return this.getGenerator().getLogger();
	}

	public RubisSessionState getSessionState()
	{
		return this.getGenerator().getSessionState();
	}

	public RubisUtility getUtility()
	{
		return this.getGenerator().getUtility();
	}

	public RubisConfiguration getConfiguration()
	{
		return this.getGenerator().getConfiguration();
	}

	/**
	 * Get the last request issued by this operation.
	 *
	 * @return The last request issued by this operation.
	 */
	protected String getLastRequest()
	{
		final TraceRecord trace = this.getTrace();
		if (trace._lstRequests == null || trace._lstRequests.isEmpty())
		{
			return null;
		}
		return trace._lstRequests.get(trace._lstRequests.size()-1);
	}

	/**
	 * Parses an HTML document for image URLs specified by IMG tags.
	 *
	 * @param buffer The HTTP response; expected to be an HTML document.
	 * @return An unordered set of image URLs.
	 */
	protected Set<String> parseImagesInHtml(String html)
	{
		String regex = null;
		regex = "<img\\s+.*?src=\"([^\"]+?)\"";

		this.getLogger().finest("Parsing images from buffer: " + html);
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Set<String> urlSet = new LinkedHashSet<String>();

		Matcher match = pattern.matcher(html);
		while (match.find())
		{
			String url = match.group(1);
			this.getLogger().finest("Adding " + url);
			urlSet.add(url);
		}

		return urlSet;
	}

	/**
	 * Load the image files specified by the image URLs.
	 *
	 * @param imageURLs The set of image URLs.
	 * @return The number of images loaded.
	 *
	 * @throws Throwable
	 */
	protected long loadImages(Set<String> imageUrls) throws Throwable
	{
		long imagesLoaded = 0;

		if (imageUrls != null)
		{
			for (String imageUrl : imageUrls)
			{
				URI uri = new URI(this.getGenerator().getBaseURL());
				String url = uri.resolve(imageUrl).toString();
				this.getLogger().finer("Loading image: " + url);
				this.getHttpTransport().fetchUrl(url);
				++imagesLoaded;
			}
		}

		return imagesLoaded;
	}
}
