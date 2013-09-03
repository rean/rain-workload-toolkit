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
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioPerson;


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
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmailcom">Marco Guazzone</a>
 */
public class LoginOperation extends OlioOperation
{
	public LoginOperation(boolean interactive, IScoreboard scoreboard)
	{ 
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.LOGIN_OP_NAME;
		this._operationIndex = OlioGenerator.LOGIN_OP;

		/* Logging in cannot occur asynchronously because the state of the
		 * HTTP client changes, affecting the execution of the following
		 * operation. */
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Check if current session has a logged user and, in this case, log him/her out
		if (this.getUtility().isRegisteredPerson(this.getSessionState().getLoggedPersonId()))
		{
			response = this.getHttpTransport().fetchUrl(this.getGenerator().getLogoutURL());
			this.trace(this.getGenerator().getLogoutURL());
			if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
			{
				this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getLogoutURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
				throw new IOException("Problems in performing request to URL: " + this.getGenerator().getLogoutURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
			}
		}

		OlioPerson person = this.getUtility().generatePerson();
		if (!this.getUtility().isValidPerson(person))
		{
			//TODO
		}

		HttpPost reqPost = null;
		List<NameValuePair> form = null;
		UrlEncodedFormEntity entity = null;

		reqPost = new HttpPost(this.getGenerator().getLoginURL());
		form = new ArrayList<NameValuePair>();
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				form.add(new BasicNameValuePair("user_name", person.userName));
				form.add(new BasicNameValuePair("password", person.password));
				form.add(new BasicNameValuePair("submit", "Login"));
				break;
			case OlioConfiguration.PHP_INCARNATION:
				form.add(new BasicNameValuePair("user_name", person.userName));
				form.add(new BasicNameValuePair("password", person.password));
				form.add(new BasicNameValuePair("submit", "Login"));
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				form.add(new BasicNameValuePair("users[username]=", person.userName));
				form.add(new BasicNameValuePair("users[password]=", person.password));
				form.add(new BasicNameValuePair("submit", "Login"));
				break;
		}
		entity = new UrlEncodedFormEntity(form, "UTF-8");
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(reqPost.getURI().toString());
		// Check that the request succeeded
		if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getAddEventURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getAddEventURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Check that the login succeeded
		boolean failed = false;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				response = this.getHttpTransport().fetchUrl(this.getGenerator().getHomePageURL());
				if (response.indexOf("Username") != -1)
				{
					failed = true;
				}
				break;
			case OlioConfiguration.PHP_INCARNATION:
				response = this.getHttpTransport().fetchUrl(this.getGenerator().getHomePageURL());
				if (response.indexOf("Username") != -1)
				{
					failed = true;
				}
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				if (response.indexOf("Login:") != -1)
				{
					failed = true;
				}
				break;
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());
 
		if (failed)
		{
			this.getLogger().warning("Login (" + person.userName + "," + person.password + ") failed");
		}
		this.setFailed(failed);
	}
}
