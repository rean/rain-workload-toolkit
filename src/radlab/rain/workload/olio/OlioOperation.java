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
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.TraceRecord;
import radlab.rain.util.HttpTransport;


/**
 * The OlioOperation class contains common static methods for use by the
 * operations that inherit from this abstract class.<br />
 * <br />
 * When an operation executes, it should:
 * <ol>
 *   <li>Update the number of actions performed (e.g. one operation may issue
 *	     multiple HTTP requests).</li>
 *   <li>Record the requests issued in the order they were performed so a trace
 *	     can be recreated at a later time. The actual recreation of the trace
 *	     is done by the Scoreboard.</li>
 * </ol>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted to RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public abstract class OlioOperation extends Operation 
{
	protected Set<String> _cachedURLs = new HashSet<String>();
	private Map<String, String> _cachedHeaders = new LinkedHashMap<String,String>();


	public OlioOperation( boolean interactive, IScoreboard scoreboard )
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

		// Refresh the cache to simulate real-world browsing.
		this.refreshCache();

		this._cachedHeaders.clear();
		// Create login headers
		String host = this.getGenerator().getTrack().getTargetHostName() + ":" + this.getGenerator().getTrack().getTargetHostPort();
		this._cachedHeaders.put("Host", host);
		this._cachedHeaders.put("User-Agent", "Mozilla/5.0");
		this._cachedHeaders.put("Accept", "text/xml.application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		this._cachedHeaders.put("Accept-Language", "en-us,en;q=0.5");
		this._cachedHeaders.put("Accept-Encoding", "gzip,deflate");
		this._cachedHeaders.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		this._cachedHeaders.put("Keep-Alive", "300");
		this._cachedHeaders.put("Connection", "keep-alive");
		this._cachedHeaders.put("Referer", this.getGenerator().getHomePageURL());
		// Create headers for if-modified-since
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		this._cachedHeaders.put("If-Modified-Since", sdf.format(new Date(System.currentTimeMillis())));
	}

	@Override
	public void postExecute()
	{
		this.getSessionState().setLastOperation(this._operationIndex);

		if (this.isFailed())
		{
			this.getLogger().severe("Operation '" + this.getOperationName() + "' failed to execute. Last request is: '" + this.getLastRequest() + "'. Last response is: " + this.getSessionState().getLastResponse());
			this.getSessionState().setLastResponse(null);
		}
	}

	@Override
	public void cleanup()
	{

	}

	public OlioGenerator getGenerator()
	{
		return (OlioGenerator) this._generator;
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

	public OlioSessionState getSessionState()
	{
		return this.getGenerator().getSessionState();
	}

	public OlioUtility getUtility()
	{
		return this.getGenerator().getUtility();
	}

	public OlioConfiguration getConfiguration()
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
	public Set<String> parseImages(String html) 
	{
		String regex = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				//regex = ".*?<img\\s+.*?src=\"([^\"]+?)\"[^/>]*?(?:/)>.*";
				//regex = ".*?<img\\s+.*?src=\"([^\"]+?)\".*";
				regex = "<img\\s+.*?src=\"([^\"]+?)\"";
				break;
			case OlioConfiguration.PHP_INCARNATION:
				//regex = ".*?<img\\s+.*?src=\"([^\"]+?)\"[^/>]*?(?:/)>.*";
				//regex = ".*?<img\\s+.*?src=\"([^\"]+?)\".*";
				regex = "<img\\s+.*?src=\"([^\"]+?)\"";
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				//regex = ".*?background:\\s+.*?url\\(([^\\)]+?)\\).*";
				regex = "background:\\s+.*?url\\(([^\\)]+?)\\)";
				break;
		}

		this.getLogger().finest("Parsing images from buffer: " + html);
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Set<String> urlSet = new LinkedHashSet<String>();

		Matcher match = pattern.matcher(html);
		while (match.find())
		{
			String url = match.group(1);
//			if (url.startsWith(this.getGenerator().getImgStoreURL()) && url.contains("jpg"))
//			{
				this.getLogger().finest("Adding " + url);
				urlSet.add(url);
//			}
		}

		return urlSet;
	}

	/**
	 * Parses an HTML document for an authenticity token used by the Ruby on
	 * Rails framework to authenticate forms.
	 * 
	 * @param buffer    The HTTP response; expected to be an HTML document.
	 * @return The authenticity token if found; otherwise, null.
	 * 
	 * @throws IOException
	 */
	public String parseAuthToken(String html) throws IOException 
	{
		int idx = html.indexOf("authenticity_token");
		if (idx == -1)
		{
			this.getLogger().info("Trying to add event but authenticity token not found");
			return null;
		}

		int endIdx = html.indexOf("\" />", idx);
		if (endIdx == -1)
		{
			throw new IOException("Invalid authenticity_token element. Buffer = " + html);
		}

		String tmpString = html.substring(idx, endIdx);
		String[] splitStr = tmpString.split("value=\"");
		if (splitStr.length < 2)
		{
			throw new IOException("Invalid authenticity_token element. Buffer = " + html);
		}

		String token = splitStr[1];

		this.getLogger().finer("authenticity_token = " + token);

		return token;
	}

	/**
	 * Load the image files specified by the image URLs if they were not
	 * previously loaded and cached.
	 * 
	 * @param imageURLs The set of image URLs.
	 * @return  The number of images loaded.
	 * 
	 * @throws IOException
	 */
	protected long loadImages(Set<String> imageUrls) throws Throwable 
	{
		long imagesLoaded = 0;

		if (imageUrls != null)
		{
			for (String imageUrl : imageUrls)
			{
				// Do not load if cached (adding returns false if present).
				if (this._cachedURLs.add(imageUrl))
				{
					URI uri = new URI(this.getGenerator().getBaseURL());
					String url = uri.resolve(imageUrl).toString();
					this.getLogger().finer("Loading image: " + url);
					this.getHttpTransport().fetchUrl(url);
					++imagesLoaded;
				} 
				else
				{
					this.getLogger().finer("Image already cached: " + imageUrl);
				}
			}
		}
		return imagesLoaded;
	}

	/**
	 * Load the static files specified by the URLs if the current request is
	 * not cached and the file was not previously loaded and cached.
	 * 
	 * @param urls      The set of static file URLs.
	 * @return          The number of static files loaded.
	 * 
	 * @throws IOException
	 */
	protected long loadStatics(String[] staticUrls) throws Throwable 
	{
		long staticsLoaded = 0;

		for (String staticUrl : staticUrls)
		{
			if (this._cachedURLs.add(staticUrl)) 
			{
				URI uri = new URI(this.getGenerator().getBaseURL());
				String url = uri.resolve(staticUrl).toString();
				this.getLogger().finer("Loading image: " + url);
				this.getHttpTransport().fetchUrl(url, this._cachedHeaders);
				++staticsLoaded;
			}
			else 
			{
				this.getLogger().finer("URL already cached: " + staticUrl);
			}
		}

		return staticsLoaded;
	}

	/**
	 * Refreshes the cache by resetting it 40% of the time.
	 * 
	 * @return True if the cache was refreshed; false otherwise.
	 */
	protected boolean refreshCache()
	{
		boolean resetCache = (this.getRandomGenerator().nextDouble() <= 0.4); 
		if (resetCache)
		{
			this._cachedURLs.clear();
		}

		return resetCache;
	}
}
