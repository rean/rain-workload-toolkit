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

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;

import radlab.rain.IScoreboard;

/**
 * The AddPersonOperation is an operation that creates a new user. If the user
 * is logged in, the session is first logged out. The creation of the user
 * involves obtaining a new user ID (via a synchronized counter), generating
 * a unique username (uniqueness is checked via a name checking request), and
 * creating and executing the POST request with all the necessary user details.
 */
public class AddPersonOperation extends OlioOperation 
{
	public AddPersonOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "AddPerson";
		this._operationIndex = OlioGenerator.ADD_PERSON;
		
		/* Logging in cannot occur asynchronously because the state of the
		 * HTTP client changes, affecting the execution of the following
		 * operation. */
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable 
	{
		if ( this.isLoggedOn() )
		{
			this.logOff();
		}
		
		// Fetch the new user form.
		this._http.fetchUrl( this.getGenerator().addPersonURL );
		this.trace( this.getGenerator().addPersonURL );
		
		// Load the static files associated with the new user form.
		this.loadStatics( this.getGenerator().addPersonStatics );
		this.trace( this.getGenerator().addPersonStatics );
		
		// Decide on a user ID and username.
		long id = this.generateUserId();
		String username = UserName.getUserName(id);
		if (username == null || username.length() == 0)
		{
			this._logger.warning( "Username is null!" );
		}
		String password = String.valueOf( id );
		
		// Check that the username is unique.
		StringBuilder checkResponse = this._http.fetchUrl( this.getGenerator().checkNameURL, "name=" + username );
		this.trace( this.getGenerator().checkNameURL );
		if ( checkResponse.equals( "Name taken" ) )
		{
			throw new Exception( "Generated username was not unique" );
		}
		
		// Construct the POST request to create the user.
		HttpPost httpPost = new HttpPost( this.getGenerator().addPersonResultURL );
		MultipartEntity entity = new MultipartEntity();
		this.populateEntity( entity );
		entity.addPart( "user[username]", new StringBody( username ) );
		entity.addPart( "user[password]", new StringBody( password ) );
		entity.addPart( "user[password_confirmation]", new StringBody( password ) );
		entity.addPart( "user[email]", new StringBody( username + "@" + this._random.makeCString( 3, 10 ) + ".com") );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		this._http.fetch( httpPost );
		this.trace( this.getGenerator().addPersonResultURL );
		
		this.logOn();
		
		this.setFailed( false );
	}
	
	/**
	 * Adds the details and images needed to create a new user.
	 * 
	 * @param entity        The request entity in which to add the details.
	 * 
	 * @throws UnsupportedEncodingException
	 */
	protected void populateEntity( MultipartEntity entity ) throws UnsupportedEncodingException
	{
		StringBuilder buffer = new StringBuilder( 256 );
		
		StringBody firstName = new StringBody( RandomUtil.randomName( this._random, buffer, 2, 12 ).toString() );
		buffer.setLength( 0 );
		
		StringBody lastName  = new StringBody( RandomUtil.randomName( this._random, buffer, 5, 12 ).toString() );
		buffer.setLength( 0 );
		
		StringBody telephone = new StringBody( RandomUtil.randomPhone( this._random, buffer ).toString() );
		
		entity.addPart( "user[firstname]", firstName );
		entity.addPart( "user[lastname]", lastName );
		entity.addPart( "user[telephone]", telephone );
		entity.addPart( "user[summary]", new StringBody( RandomUtil.randomText( this._random, 50, 200 ) ) );
		entity.addPart( "user[timezone]", new StringBody( RandomUtil.randomTimeZone( this._random ) ) );
		
		// Add image for person
		entity.addPart( "user_image", new FileBody( this.getGenerator().personImg ) );
		
		this.addAddress( entity );
	}
	
}
