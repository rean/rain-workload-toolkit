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

import radlab.rain.IScoreboard;

/**
 * The LoginOperation is an operation that logs in as a random user. If the
 * user executing this operation is already logged in, the user is first
 * logged out. After logging in, the user is redirected to the home page.<br />
 * <br />
 * The process of logging in as a random user entails picking a random user
 * ID, mapping the ID to a username deterministically, and finally logging in
 * by constructing a post message where the user password is the ID.<br />
 * <br />
 * The application is checked (via parsing of the document) for indication
 * that the login succeeded; the logged in state is saved.
 */
public class LoginOperation extends OlioOperation
{
	public LoginOperation( boolean interactive, IScoreboard scoreboard )
	{ 
		super( interactive, scoreboard );
		this._operationName = "Login";
		this._operationIndex = OlioGenerator.LOGIN;
		
		/* Logging in cannot occur asynchronously because the state of the
		 * HTTP client changes, affecting the execution of the following
		 * operation. */
		this._mustBeSync = true;
	}
	
	public void execute() throws Throwable
	{
		if ( this.isLoggedOn() )
		{
			this.logOff();
		}
		
		// Logging in redirects the user to the home page.
		StringBuilder homeResponse = this.logOn();
		
		// Check that the user was successfully logged in.
		if ( homeResponse.indexOf("Successfully logged in!") < 0 ) {
			throw new Exception( "Login did not persist for an unknown reason" );
		}
		
		this.setFailed( false );
	}
	
}
