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

import radlab.rain.IScoreboard;

/**
 * The Operation2 operation is a sample operation.
 */
public class LoginOperation extends RadditOperation 
{
	
	public LoginOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Login";
		this._operationIndex = RadditGenerator.LOGIN;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = this._http.fetchUrl( this.getGenerator().loginUrl );
		this.trace( this.getGenerator().loginUrl );
		if ( response.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}
		
		// Decide on the username and password; parse the authenticity token.
		String username = "test";
		String password = "test123";
		String token = this.parseAuthToken( response );
		
		// Make the POST request to log in.
		StringBuilder postBody = new StringBuilder();
		postBody.append( "login=" ).append( username );
		postBody.append( "&password=" ).append( password );
		postBody.append( "&authenticity_token=" ).append( token );
		postBody.append( "&commit=" ).append( "Log In" );
		StringBuilder postResponse = this._http.fetchUrl( this.getGenerator().sessionUrl, postBody.toString() );
		this.trace( this.getGenerator().sessionUrl );
		
		// Check that the user was successfully logged in.
		if ( postResponse.indexOf( "Logged in successfully!" ) < 0 ) {
			System.out.println( "Did not log in properly." );
			throw new Exception( "Login did not persist for an unknown reason" );
		}
		
		// Load the static files (CSS/JS).
		loadStatics( this.getGenerator().staticUrls );
		this.trace( this.getGenerator().staticUrls );
		
		this.setFailed( false );
	}
	
}
