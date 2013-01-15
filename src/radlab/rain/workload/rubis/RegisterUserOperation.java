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


import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.HttpStatus;
import java.io.IOException;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubis.model.RubisUser;


/**
 * Register User operation.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class RegisterUserOperation extends RubisOperation 
{
	public RegisterUserOperation(boolean interactive, IScoreboard scoreboard)
	{
		super( interactive, scoreboard );
		this._operationName = "Register User";
		this._operationIndex = RubisGenerator.REGISTER_USER_OP;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		response = this.getHttpTransport().fetchUrl( this.getGenerator().getRegisterURL() );
		this.trace( this.getGenerator().getRegisterURL() );
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getRegisterURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Generate a user
		RubisUser user = this.getGenerator().newUser();

		// Construct the POST request
		HttpPost httpPost = new HttpPost(this.getGenerator().getRegisterUserURL());
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("firstname", new StringBody(user.firstname));
		entity.addPart("lastname", new StringBody(user.lastname));
		entity.addPart("nickname", new StringBody(user.nickname));
		entity.addPart("email", new StringBody(user.email));
		entity.addPart("password", new StringBody(user.password));
		entity.addPart("region", new StringBody(this.getGenerator().getRegion(user.region).name));
		//entity.addPart("Submit", new StringBody("Register now!"));
		httpPost.setEntity(entity);

        response = this.getHttpTransport().fetch(httpPost);
		this.trace(this.getGenerator().getRegisterUserURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getRegisterUserURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.setFailed(false);
	}
}
