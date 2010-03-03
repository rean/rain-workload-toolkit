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

package radlab.rain.workload.raddit;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;
/**
 * The RadditOperation class contains common static methods for use by the
 * operations that inherit from this abstract class.
 */
public abstract class RadditOperation extends Operation 
{
	// These references will be set by the Generator.
	protected HttpTransport _http;
	
	/**
	 * Returns the RadditGenerator that created this operation.
	 * 
	 * @return      The RadditGenerator that created this operation.
	 */
	public RadditGenerator getGenerator()
	{
		return (RadditGenerator) this._generator;
	}
	
	public RadditOperation( boolean interactive, IScoreboard scoreboard )
	{
		super( interactive, scoreboard );
	}
	
	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		RadditGenerator radditGenerator = (RadditGenerator) generator;
		
		this._http = radditGenerator.getHttpTransport();
	}
	
	/**
	 * Load the image files specified by the image URLs if they were not
	 * previously loaded and cached.
	 * 
	 * @param imageURLs     The set of image URLs.
	 * @return              The number of images loaded.
	 * 
	 * @throws IOException
	 */
	protected long loadImages( Set<String> imageUrls ) throws IOException 
	{
		long imagesLoaded = 0;
		
		if ( imageUrls != null )
		{
			for (String imageUrl : imageUrls )
			{
				// Do not load if cached (adding returns false if present).
				this._http.fetchUrl( imageUrl );
				imagesLoaded++;
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
	protected long loadStatics( String[] urls ) throws IOException 
	{
		long staticsLoaded = 0;
		
		for ( String url : urls )
		{
			this._http.fetchUrl( url );
			staticsLoaded++;
		}
		
		return staticsLoaded;
	}
	
	/**
	 * Parses an HTML document for an authenticity token used by the Ruby on
	 * Rails framework to authenticate forms.
	 * 
	 * @param buffer    The HTTP response; expected to be an HTML document.
	 * @return          The authenticity token if found; otherwise, null.
	 * 
	 * @throws IOException
	 */
	public String parseAuthToken( StringBuilder buffer ) throws IOException 
	{
		String token = "";
		
		try {
			// TODO: Share this factory.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			Document document = factory.newDocumentBuilder().parse( new InputSource( new StringReader( buffer.toString() ) ) );
			
			NodeList inputList = document.getElementsByTagName("input");
			for ( int i = 0; i < inputList.getLength(); i++ )
			{
				Element input = (Element) inputList.item(i);
				String name = input.getAttribute("name");
				if ( name.equals("authenticity_token") )
				{
					token = input.getAttribute("value");
					break;
				}
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		return token;
	}
	
	@Override
	public void cleanup()
	{
		
	}
	
}
