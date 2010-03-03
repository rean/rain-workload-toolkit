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

package radlab.rain.workload.olio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import radlab.rain.IScoreboard;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * The AddEventOperation is an operation that creates a new event. The user
 * must be logged on. The creation of the POST involves populating the request
 * with event details, an image, a document, and address data.<br />
 * <br />
 * The requests made include loading the event form, loading the static URLs
 * (CSS/JS), and sending the POST data to the application.
 */
public class AddEventOperation extends OlioOperation 
{
	public AddEventOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "AddEvent";
		this._operationIndex = OlioGenerator.ADD_EVENT;
	}
	
	@Override
	public void execute() throws Throwable
	{
		if ( this.isLoggedOn() )
		{
			// Fetch the add event form.
			StringBuilder formResponse = this._http.fetchUrl( this.getGenerator().addEventURL );
			this.trace( this.getGenerator().addEventURL );
			if ( formResponse.length() == 0 )
			{
				throw new IOException( "Received empty response" );
			}
			
			// Get the authentication token needed to create the POST request.
			String token = this.parseAuthToken( formResponse );
			if ( token == null )
			{
				throw new Exception( "Authentication token could not be parsed" );
			}
			
			// Load the static files associated with the add event form.
			this.loadStatics( this.getGenerator().addEventStatics );
			this.trace( this.getGenerator().addEventStatics );
			
			// Construct the POST request to create the event.
			HttpPost httpPost = new HttpPost( this.getGenerator().addEventResultURL );
			MultipartEntity entity = new MultipartEntity();
			this.populateEntity( entity );
			entity.addPart( "authenticity_token", new StringBody( token ) );
			httpPost.setEntity( entity );
			
			// Make the POST request.
			StringBuilder postResponse = this._http.fetch( httpPost );
			this.trace( this.getGenerator().addEventResultURL );
			
			// Verify that the request succeeded. 
			int index = postResponse.indexOf( "success" );
			if( index == -1 ) {
				throw new Exception( "Could not find success message in result body!" );
			}
		}
		else
		{
			// TODO: What's the best way to handle this case?
			this._logger.warning( "Login required for " + this._operationName );
		}
		
		this.setFailed( false );
	}
	
	/**
	 * Adds the details and files needed to create a new event in Olio.
	 * 
	 * @param entity        The request entity in which to add event details.
	 * 
	 * @throws UnsupportedEncodingException
	 */
	protected void populateEntity( MultipartEntity entity ) throws UnsupportedEncodingException
	{
		entity.addPart("commit", new StringBody( "Create" ) );
		
		entity.addPart("event[title]", new StringBody( RandomUtil.randomText( this._random, 15, 20 ) ) );
		entity.addPart("event[summary]", new StringBody( RandomUtil.randomText( this._random, 50, 200 ) ) );
		entity.addPart("event[description]", new StringBody( RandomUtil.randomText( this._random, 100, 495 ) ) );
		entity.addPart("event[telephone]", new StringBody( RandomUtil.randomPhone( this._random, new StringBuilder( 256 ) ) ) );
		entity.addPart("event[event_timestamp(1i)]", new StringBody( "2008" ) );
		entity.addPart("event[event_timestamp(2i)]", new StringBody( "10" ) );
		entity.addPart("event[event_timestamp(3i)]", new StringBody( "20" ) );
		entity.addPart("event[event_timestamp(4i)]", new StringBody( "20" ) );
		entity.addPart("event[event_timestamp(5i)]", new StringBody( "10" ) );
		
		// Add uploaded files
		entity.addPart( "event_image", new FileBody(this.getGenerator().eventImg ) );
		entity.addPart( "event_document", new FileBody( this.getGenerator().eventPdf ) );
		
		entity.addPart( "tag_list", new StringBody( "tag1" ) );
		
		this.addAddress( entity );
	}
	
}