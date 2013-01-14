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
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013.
 */

package radlab.rain.workload.rubis;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * View-User-Information operation.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class ViewUserInfoOperation extends RubisOperation 
{
	public ViewUserInfoOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "View User Information";
		this._operationIndex = RubisGenerator.VIEW_USER_INFO_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		if (this.getGenerator().isUserLoggedIn())
		{
			RubisUser user = this.getGenerator().getLoggedUser();

			Map<String,String> headers = new HashMap<String,String>();
			headers.put("userId", Integer.toString(user.id));
			StringBuilder response = this.getHttpTransport().fetchUrl(this.getGenerator().getViewUserInfoURL());
			this.trace(this.getGenerator().getViewUserInfoURL());
			if (!this.getGenerator().checkHttpResponse(response.toString()))
			{
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getViewUserInfoURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}
		}
		else
		{
			//FIXME: What's the best way to handle this case?
			//NOTE: We do not throw any exception since this isn't a RAIN error
			System.err.println("Login required for " + this._operationName);
		}

		this.setFailed(false);
	}
}
