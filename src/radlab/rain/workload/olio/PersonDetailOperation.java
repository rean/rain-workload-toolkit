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
import radlab.rain.IScoreboard;


/**
 * The PersonDetailOperation is an operation that shows the details of a
 * randomly selected user. The user must be logged in to see the details.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class PersonDetailOperation extends OlioOperation 
{
	public PersonDetailOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.PERSON_DETAIL_OP_NAME;
		this._operationIndex = OlioGenerator.PERSON_DETAIL_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		if (this.isLoggedOn())
		{
			int userId = this.getUtility().generateInt(1, ScaleFactors.loadedUsers);

			String personUrl = null;
			switch (this.getConfiguration().getIncarnation())
			{
				case JAVA_INCARNATION:
					personUrl = this.getGenerator().getPersonDetailURL() + "&user_name=" + UserName.getUserName( userId );
					break;
				case RAILS_INCARNATION:
					personUrl = this.getGenerator().getPersonDetailURL() + userId;
					break;
			}
			StringBuilder personResponse = this.getHttpTransport().fetchUrl(personUrl);
			this.trace(personUrl);
			if (personResponse.length() == 0)
			{
				throw new IOException("Received empty response");
			}

			this.loadStatics(this.getGenerator().getPersonStatics());
			this.trace(this.getGenerator().getPersonStatics());

			Set<String> imageUrls = this.parseImages(personResponse);
			this.loadImages(imageUrls);
			this.trace(imageUrls);
		}
		else
		{
			if (this.checkIsLoggedIn())
			{
				this.getLogger().warning("isLoggedOn() returned false but checkIsLoggedIn() returned true");
			}
			// TODO: What's the best way to handle this case?
			this.getLogger().warning("Login required for " + this._operationName);
		}
		
		this.setFailed(false);
	}
}
