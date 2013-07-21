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
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Register-User operation.
 *
 * Emulates the following operations:
 * 1. Go the the user registration page
 * 2. Fill-in the form and click on the 'Register now!' button
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class RegisterUserOperation extends RubisOperation 
{
	public RegisterUserOperation(boolean interactive, IScoreboard scoreboard)
	{
		super( interactive, scoreboard );
		this._operationName = "Register-User";
		this._operationIndex = RubisGenerator.REGISTER_USER_OP;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Generate a new user
		RubisUser user = this.getUtility().newUser();
		if (!this.getUtility().isValidUser(user) || this.getUtility().isAnonymousUser(user))
		{
			this.getLogger().warning("No valid user has been found. Operation interrupted.");
			this.setFailed(true);
			return;
		}

		// Fill-in the form and click on the 'Register now!' button
		HttpPost reqPost = new HttpPost(this.getGenerator().getRegisterUserURL());
		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("firstname", user.firstname));
		form.add(new BasicNameValuePair("lastname", user.lastname));
		form.add(new BasicNameValuePair("nickname", user.nickname));
		form.add(new BasicNameValuePair("email", user.email));
		form.add(new BasicNameValuePair("password", user.password));
		form.add(new BasicNameValuePair("region", this.getUtility().getRegion(user.region).name));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, "UTF-8");
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(reqPost.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}
}
