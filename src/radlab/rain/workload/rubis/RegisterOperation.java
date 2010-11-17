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

package radlab.rain.workload.rubis;

import java.io.IOException;

import radlab.rain.IScoreboard;

/**
 * Register operation.
 */
public class RegisterOperation extends RubisOperation 
{

	public RegisterOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Register";
		this._operationIndex = RubisGenerator.REGISTER;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = this._http.fetchUrl( this.getGenerator().registerURL );
		this.trace( this.getGenerator().registerURL );
		if ( response.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}
		
		// Decide on the username and password; parse the authenticity token.
//		String firstname = "cercs_" + counter;
//		String lastname = "cercs_" + counter;
//		String nickname = "cercs_" + counter;
//		String password = "cercs";
//		String email = "cercs@gatech.edu";
//		String region = "GA--Atlanta";
		
		// Make the POST request to log in.
//		StringBuilder postBody = new StringBuilder();
//		postBody.append( "firstname=" ).append( firstname );
//		postBody.append( "&lastname=" ).append( lastname );
//		postBody.append( "&nickname=" ).append( nickname );
//		postBody.append( "&password=" ).append( password );
//		postBody.append( "&email=" ).append( email );
//		postBody.append( "&region=" ).append( region );
//
//		System.out.println( "Counter - " + counter );
//
//		StringBuilder postResponse = this._http.fetchUrl( this.getGenerator().postRegisterURL, postBody.toString());
//		this.trace(this.getGenerator().postRegisterURL);
//		
//		counter += 1;

		
		// Check that the user was successfully register in.
//		if ( postResponse.indexOf( "Your registration has been processed successfully" ) < 0 ) {
//			System.out.println( "Did not register properly." );
//			throw new Exception( "Registration did not happen for unknown reason" );
//		}
		
		this.setFailed( false );
	}
	
}
