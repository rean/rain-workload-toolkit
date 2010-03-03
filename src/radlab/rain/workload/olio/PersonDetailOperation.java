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

import java.io.IOException;
	
/**
 * The PersonDetailOperation is an operation that shows the details of a
 * randomly selected user. The user must be logged in to see the details.
 */
public class PersonDetailOperation extends OlioOperation 
{
	public PersonDetailOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "PersonDetails";
		this._operationIndex = OlioGenerator.PERSON_DETAIL;
	}
	
	@Override
	public void execute() throws Throwable
	{
		if ( this.isLoggedOn() )
		{
			int userId = this._random.random( 1, ScaleFactors.loadedUsers );
			
			String personUrl = this.getGenerator().personDetailURL + userId;
			StringBuilder personResponse = this._http.fetchUrl( personUrl );
			this.trace( personUrl );
			if (personResponse.length() == 0)
			{
				throw new IOException("Received empty response");
			}
		}
		else
		{
			if ( this.checkIsLoggedIn() )
			{
				this._logger.warning( "isLoggedOn() returned false but checkIsLoggedIn() returned true" );
			}
			// TODO: What's the best way to handle this case?
			this._logger.warning( "Login required for " + this._operationName );
		}
		
		this.setFailed( false );
	}
	
}
