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
 */

package radlab.rain.util;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpMessage; 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/**
 * The HttpTransport class is used to issue various HTTP requests.
 */
public class HttpTransport 
{
	/** Default HTTP headers required for a POST request. */
	private static Map<String, String> postHeaders;
	static 
	{
		postHeaders = new HashMap<String, String>();
		postHeaders.put( "Content-Type", "application/x-www-form-urlencoded" );
	}
	
	/** The HTTP client used to make requests. */
	private HttpClient _httpClient;
	
	/** Contents of the response of the last HTTP request executed. */
	private StringBuilder _responseBuffer = new StringBuilder();
	
	/** HTTP status code of the last HTTP request executed. */
	private int _statusCode = HttpStatus.SC_OK;
	
	/** HTTP headers of the last HTTP request executed. */
	private Header[] _headers;
	
	/** HTTP client configuration: whether to handle redirects or not. */
	private boolean _followRedirects = true;
	
	/** HTTP client configuration: maximum number of redirects to follow. */
	private int _redirectLimit = 3;
	
	/** HTTP client configuration: Time to wait for connection to be established. */
	private int _connectTimeout = 10000;
	
	/** HTTP client configuration: Time to wait between data packets. */
	private int _socketIdleTimeout = 10000;

	/* URL where we end it if we're redirected at any point. */
	private String _finalUrl = "";
		
	/**
	 * Returns the HTTP client used to execute requests.
	 * 
	 * @return      The HTTP client used to execute requests.
	 */
	public HttpClient getHttpClient()
	{
		return this._httpClient;
	}
	
	/**
	 * Returns the contents of the response of the last HTTP request executed.
	 * 
	 * @return  Contents of the response of the last HTTP request executed.
	 */
	public StringBuilder getResponseBuffer()
	{
		return this._responseBuffer;
	}
	
	/**
	 * Returns the final URL where we end up after issuing a request.
	 * @return Our final location after a redirect
	 */
	public String getFinalUrl()
	{
		return this._finalUrl;
	}
	
	/**
	 * Returns the length of the response of the last HTTP request executed.
	 * 
	 * @return  The length of the response of the last HTTP request executed.
	 */
	public int getResponseLength()
	{
		return this._responseBuffer.length();
	}
	
	/**
	 * Returns the HTTP status code of the last HTTP request executed.
	 * 
	 * @return  HTTP status code of the last HTTP request executed.
	 */
	public int getStatusCode()
	{
		return this._statusCode;
	}
	
	/**
	 * Returns the HTTP headers of the last HTTP request executed.
	 * 
	 * @return  HTTP headers of the last HTTP request executed.
	 */
	public Header[] getHeaders()
	{
		return this._headers;
	}
	
	/**
	 * Returns whether future HTTP requests should handle redirects or not.
	 * 
	 * @return  True if future HTTP requests should follow redirects or not.
	 */
	public boolean getFollowRedirects()
	{
		return this._followRedirects;
	}
	
	/**
	 * Sets whether future HTTP requests should handle redirects or not. 
	 * 
	 * @param val   The new configuration.
	 */
	public void setFollowRedirects( boolean val )
	{
		this._followRedirects = val;
	}
	
	/**
	 * Returns the maximum number of redirects to follow.
	 * 
	 * @return  The maximum number of redirects to follow.
	 */
	public int getRedirectLimit()
	{
		return this._redirectLimit;
	}
	
	/**
	 * Sets the maximum number of redirects to follow. 
	 * 
	 * @param val   The new configuration.
	 */
	public void setRedirectLimit( int val )
	{
		this._redirectLimit = val;
	}
	
	/**
	 * Returns the time to wait for future connections to be established.
	 * 
	 * @return  Time to wait for future connections to be established.
	 */
	public int getConnectTimeout()
	{
		return this._connectTimeout;
	}
	
	/**
	 * Sets the time to wait for future connections to be established. 
	 * 
	 * @param val   The new configuration.
	 */
	public void setConnectTimeout( int val )
	{
		this._connectTimeout = val;
	}
	
	/**
	 * Returns the time to wait between data packets.
	 * 
	 * @return  The time to wait between data packets.
	 */
	public int getSocketIdleTimeout()
	{
		return this._socketIdleTimeout;
	}
	
	/**
	 * Sets the time to wait between data packets. 
	 * 
	 * @param val   The new configuration.
	 */
	public void setSocketIdleTimeout( int val )
	{
		this._socketIdleTimeout = val;
	}
	
	/**
	 * Stores the body of the given response into the given buffer. The buffer
	 * is guaranteed to contain only the response after this method.
	 * 
	 * @param entity  The HTTP response entity with the content to buffer.
	 * @param buffer  The buffer in which to store the HTTP response content.
	 * 
	 * @throws IOException
	 */
	public static void readResponseIntoBuffer( HttpEntity entity, StringBuilder buffer ) throws IOException
	{
		// Empty the buffer.
		buffer.setLength(0);
		
		if( entity != null )
		{
			BufferedReader reader = new BufferedReader( new InputStreamReader( entity.getContent() ) );
			// Copy the response into the buffer one line at a time.
			try 
			{
				String data = reader.readLine();
				while( data != null )
				{
					buffer.append( data );
					data = reader.readLine();
				}
			} 
			catch( IOException e ) 
			{
				throw e;
			}
			catch( RuntimeException e )
			{
				throw e;
			}
			finally
			{
				reader.close();
			}
		}
	}
	
	/**
	 * Sets the headers of an HTTP request necessary to execute. 
	 * 
	 * @param httpMessage   The HTTP request to add the basic headers.
	 * @param headers       A map of key-value pairs representing the headers.
	 */
	public static void setHeaders( HttpMessage httpMessage, Map<String,String> headers )
	{
		if( headers == null )
		{
			httpMessage.setHeader( "Accept-Language", "en-us,en;q=0.5" );
			return;
		} 
		else if( !headers.containsKey( "Accept-Language" ) ) 
		{
			headers.put( "Accept-Language", "en-us,en;q=0.5" );
		}
		
		for( Map.Entry<String, String> entry : headers.entrySet() )
		{
			httpMessage.setHeader( entry.getKey(), entry.getValue() );
		}
	}
	
	/**
	 * Ensures that the map of headers contains a "Content-Type" header.
	 * 
	 * @param headers   The map of key-value pairs representing headers.
	 * 
	 * @throws IOException
	 */
	public static void checkContentType( Map<String, String> headers ) throws IOException
	{
		String type = headers.get( "Content-type" );
		if( type == null )
		{
			// Note: The case is different! (Content-type vs. Content-Type)
			type = headers.get( "Content-Type" );
		}
		if( type == null )
		{
			headers.put( "Content-Type", "application/x-www-form-urlencoded" );
		}
	}
	
	/**
	 * Creates an HttpTransport. This entails creating and initializing the
	 * HTTP client used to execute requests.
	 */
	public HttpTransport()
	{
		this._httpClient = new DefaultHttpClient();
	}
	
	/**
	 * Reconfigures the HTTP client used for execution based on the variables
	 * set in the HttpTransport.<br />
	 * <br />
	 * The HttpTransport variables include: <code>_connectionTimeout</code>
	 * and <code>_socketIdleTimeout</code>.
	 */
	private void configureHttpClient()
	{
		HttpParams params = this._httpClient.getParams();
		
		HttpConnectionParams.setConnectionTimeout( params, this._connectTimeout );
		HttpConnectionParams.setSoTimeout( params, this._socketIdleTimeout );
	}
	
	/**
	 * Executes the given URL as an HTTP GET request.
	 * 
	 * @param url   The URL of the request.
	 * @return      The content of the response.
	 */
	public StringBuilder fetchUrl( String url ) throws IOException
	{
		return this.fetchUrl( url, (Map<String, String>) null );
	}
	
	/**
	 * Executes the given URL as an HTTP GET request. Adds the provided headers
	 * to the request before executing it.
	 * 
	 * @param url       The URL of the request.
	 * @param headers   The headers to add to the request.
	 * @return          The content of the response.
	 */
	public StringBuilder fetchUrl( String url, Map<String, String> headers ) throws IOException
	{
		// Create the GET request.
		HttpGet httpGet = new HttpGet( url );
		
		// Return the response body.
		return this.fetch(httpGet, headers);
	}
	
	/**
	 * Executes the given URL and postBody as an HTTP POST request.
	 * 
	 * @param url       The URL of the request.
	 * @param postBody  The contents of the POST body.
	 * @return          The content of the response.
	 */
	public StringBuilder fetchUrl( String url, String postBody ) throws IOException
	{
		return this.fetchUrl( url, postBody, (Map<String, String>) null );
	}
	
	/**
	 * Executes the given URL and postBody as an HTTP POST request. Adds the
	 * provided headers to the request before executing it.
	 * 
	 * @param url       The URL of the request.
	 * @param postBody  The contents of the POST body.
	 * @param headers   The headers to add to the request.
	 * @return          The content of the response.
	 */
	public StringBuilder fetchUrl( String url, String postBody, Map<String, String> headers ) throws IOException, RuntimeException 
	{
		// Create the POST request.
		HttpPost httpPost = new HttpPost( url );
		
		// Set the POST parameters.
		try
		{
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String[] pairs = postBody.split( "&" );
			for ( String pair : pairs )
			{
				String[] keyValue = pair.split( "=", 2 );
				params.add( new BasicNameValuePair( keyValue[0], keyValue[1] ) );
			}
			httpPost.setEntity( new UrlEncodedFormEntity( params, HTTP.UTF_8 ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( "Error parsing postBody: " + postBody, e );
		}
		
		// Return the response body.
		return this.fetch(httpPost, headers);
	}
	
	/**
	 * Executes an HTTP GET request.
	 * 
	 * @param httpPost      The HTTP GET request to execute.
	 * @return              The content of the response.
	 * 
	 * @throws IOException
	 */
	public StringBuilder fetch( HttpGet httpGet ) throws IOException 
	{
		return this.fetch( httpGet, (Map<String, String>) null );
	}
	
	/**
	 * Executes an HTTP GET request.
	 * 
	 * @param httpGet       The HTTP GET request to execute.
	 * @param headers       The headers to add to the request.
	 * @return              The content of the response.
	 * 
	 * @throws IOException
	 */
	public StringBuilder fetch( HttpGet httpGet, Map<String, String> headers ) throws IOException 
	{
		// Add the necessary headers.
		if (headers != null) {
			HttpTransport.setHeaders( httpGet, headers );
		}
		
		return this.fetch( (HttpUriRequest) httpGet );
	}
	
	/**
	 * Executes an HTTP POST request.
	 * 
	 * @param httpPost      The HTTP POST request to execute.
	 * @return              The content of the response.
	 * 
	 * @throws IOException
	 */
	public StringBuilder fetch( HttpPost httpPost ) throws IOException 
	{
		return this.fetch( httpPost, (Map<String, String>) null );
	}
	
	/**
	 * Executes an HTTP POST request.
	 * 
	 * @param httpPost      The HTTP POST request to execute.
	 * @param headers       The headers to add to the request.
	 * @return              The content of the response.
	 * 
	 * @throws IOException
	 */
	public StringBuilder fetch( HttpPost httpPost, Map<String, String> headers ) throws IOException 
	{
		// Set the headers as necessary.
		if( headers != null )
		{
			HttpTransport.checkContentType( headers );
		}
		else
		{
			headers = HttpTransport.postHeaders;
		}

		return this.fetch( (HttpUriRequest) httpPost );
	}
	
	/**
	 * Executes a generic HTTP request. Assumes that all headers and entities
	 * are already set on the HTTP request.<br />
	 * <br />
	 * This method is overloaded with the parameter typed to be HttpGet or
	 * HttpPost. You will probably never need to directly use this method.
	 * 
	 * @param httpRequest   The HTTP request to execute.
	 * @return              The content of the response.
	 * 
	 * @throws IOException
	 */
	public StringBuilder fetch( HttpUriRequest httpRequest ) throws IOException
	{
		// Update the HTTP client configuration.
		this.configureHttpClient();
		
		// By default we'll end up at the URI being requested (unless a redirect occurs)
		this._finalUrl = httpRequest.getURI().toString();
		
		// Execute the HTTP request and get the response entity.
		HttpResponse response = this._httpClient.execute( httpRequest );
		HttpEntity entity = response.getEntity();
		
		// Save the status code and headers.
		this._statusCode = response.getStatusLine().getStatusCode();
		this._headers = response.getAllHeaders();
		
		// Follow redirects.
		if ( this.getFollowRedirects() )
		{
			/*
			 * 301 Moved Permanently.  HttpStatus.SC_MOVED_PERMANENTLY
			 * 302 Moved Temporarily.  HttpStatus.SC_MOVED_TEMPORARILY
			 * 303 See Other.          HttpStatus.SC_SEE_OTHER
			 * 307 Temporary Redirect. HttpStatus.SC_TEMPORARY_REDIRECT
			 */
			int redirectsFollowed = this.getRedirectLimit();
			while ( this._statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
					this._statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
					this._statusCode == HttpStatus.SC_SEE_OTHER         ||
					this._statusCode == HttpStatus.SC_TEMPORARY_REDIRECT )
			{
				// At this point, we will either execute the redirect or throw
				// an exception. Regardless, we need to consume this entity.
				entity.consumeContent();
				
				if ( redirectsFollowed > 0 )
				{
					Header[] locationHeaders = response.getHeaders( "Location" );
					if ( locationHeaders.length > 0 )
					{
						// TODO: Is it safe to assume all redirects are GET requests?
						httpRequest = new HttpGet( locationHeaders[0].getValue() );
						// Save the redirect URL
						this._finalUrl = locationHeaders[0].getValue();
						
						response = this._httpClient.execute( httpRequest );
						entity = response.getEntity();
						
						this._statusCode = response.getStatusLine().getStatusCode();
						this._headers = response.getAllHeaders();
						
						redirectsFollowed--;
					}
					else
					{
						throw new IOException("Unspecified location header for a redirect response.");
					}
				}
				else
				{
					throw new IOException( "Too many redirects! Limit: " + this.getRedirectLimit() );
				}
			}
		}
		
		
		// Read the final response of the request.
		try
		{
			HttpTransport.readResponseIntoBuffer( entity, this._responseBuffer );
		}
		catch( RuntimeException e )
		{
			httpRequest.abort();
			throw e;
		}
		finally
		{
			entity.consumeContent();
		}
		
		return this._responseBuffer;
	}
	
	/**
	 * Cleans up the HTTP client allocated for use by this HttpTransport.
	 */
	public void dispose()
	{
		this._httpClient.getConnectionManager().shutdown();
	}
}
