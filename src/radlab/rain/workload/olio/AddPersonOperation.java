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
import java.io.UnsupportedEncodingException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.HttpStatus;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioPerson;


/**
 * The AddPersonOperation is an operation that creates a new user. If the user
 * is logged in, the session is first logged out. The creation of the user
 * involves obtaining a new user ID (via a synchronized counter), generating
 * a unique username (uniqueness is checked via a name checking request), and
 * creating and executing the POST request with all the necessary user details.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class AddPersonOperation extends OlioOperation 
{
	public AddPersonOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.ADD_PERSON_OP_NAME;
		this._operationIndex = OlioGenerator.ADD_PERSON_OP;

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

		// Fetch the new user form.
		response = this.getHttpTransport().fetchUrl(this.getGenerator().getAddPersonURL());
		this.trace(this.getGenerator().getAddPersonURL());
		if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getAddPersonURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getAddPersonURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Load the static files associated with the new user form.
		this.loadStatics(this.getGenerator().getAddPersonStatics());
		this.trace( this.getGenerator().getAddPersonStatics());

		// Generate a new Olio person
		OlioPerson person = this.getUtility().newPerson();

		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				// Not available
				break;
			case OlioConfiguration.PHP_INCARNATION:
				// Not available
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				// Check that the username is unique.
				response = this.getHttpTransport().fetchUrl(this.getGenerator().getCheckNameURL(), "name=" + person.userName);
				this.trace(this.getGenerator().getCheckNameURL());
				if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
				{
					this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getCheckNameURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
					throw new IOException("Problems in performing request to URL: " + this.getGenerator().getCheckNameURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
				}
				if (response.toString().toLowerCase().equals("name taken"))
				{
					this.getLogger().severe("Generated username was not unique");
					throw new Exception("Generated username was not unique");
				}
				break;
		}

		// Construct the POST request to create the user.
		String addPersonResultURL = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				addPersonResultURL = this.getGenerator().getAddPersonResultURL() + "?user_name=" + person.userName;
				break;
			case OlioConfiguration.PHP_INCARNATION:
				addPersonResultURL = this.getGenerator().getAddPersonResultURL();
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				addPersonResultURL = this.getGenerator().getAddPersonResultURL();
				break;
		}
		HttpPost reqPost = new HttpPost(addPersonResultURL);
		MultipartEntity entity = new MultipartEntity();
		this.populateEntity(entity, person);
		reqPost.setEntity(entity);

		// Make the POST request and verify that it succeeds.
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(addPersonResultURL);
		if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		//FIXME: In Apache Olio there is also a check for redirection. Do we need it?
		//       Probably no, since HttpTransport#fecth already take care of it
		//String[] locationHeader = this.getHttpTransport().getHeadersMap().get("location");
		//if (redirectionLocation != null)
		//{
		//	String redirectUrl = null;
		//	switch (this.getConfiguration().getIncarnation())
		//	{
		//		case OlioConfiguration.JAVA_INCARNATION:
		//			redirectUrl = this.getGenerator().getBaseURL() + '/' + locationHeader[0];
		//			break;
		//		case OlioConfiguration.PHP_INCARNATION:
		//			redirectUrl = this.getGenerator().getBaseURL() + '/' + locationHeader[0];
		//			break;
		//		case OlioConfiguration.RAILS_INCARNATION:
		//			redirectUrl = locationHeader[0];
		//			break;
		//	}
		//	response = this.getHttpTransport().fetchURL(redirectUrl);
		//}

//		// Login with the newly created person
//		response = this.getHttpTransport().fetchUrl(this.getGenerator().getLoginURL());
//		this.trace(this.getGenerator().getLoginURL());
//		if (this.getUtility().checkHttpResponse(this.getHttpTransport(), response.toString()))
//		{
//			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getLoginURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
//			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getLoginURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
//		}

		// Save session data
		this.getSessionState().setLoggedPersonId(person.id);
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}
	
	/**
	 * Adds the details and images needed to create a new user.
	 * 
	 * @param entity The request entity in which to add the details.
	 * 
	 * @throws UnsupportedEncodingException
	 */
	protected void populateEntity(MultipartEntity entity, OlioPerson person) throws UnsupportedEncodingException
	{
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				entity.addPart("user_name", new StringBody(person.userName));
				entity.addPart("password", new StringBody(person.password));
				entity.addPart("passwordx", new StringBody(person.password));
				entity.addPart("first_name", new StringBody(person.firstName));
				entity.addPart("last_name", new StringBody(person.lastName));
				entity.addPart("email", new StringBody(person.email));
				entity.addPart("telephone", new StringBody(person.telephone));
				entity.addPart("summary", new StringBody(person.summary));
				entity.addPart("timezone", new StringBody(person.timezone));
				entity.addPart("street1", new StringBody(person.address[0]));
				entity.addPart("street2", new StringBody(person.address[1]));
				entity.addPart("city", new StringBody(person.address[2]));
				entity.addPart("state", new StringBody(person.address[3]));
				entity.addPart("zip", new StringBody(person.address[4]));
				entity.addPart("country", new StringBody(person.address[5]));
				entity.addPart("upload_person_image", new FileBody(this.getGenerator().getPersonImgFile()));
				entity.addPart("Submit", new StringBody("Create"));
				break;
			case OlioConfiguration.PHP_INCARNATION:
				entity.addPart("add_user_name", new StringBody(person.userName));
				entity.addPart("psword", new StringBody(person.password));
				entity.addPart("passwordx", new StringBody(person.password));
				entity.addPart("first_name", new StringBody(person.firstName));
				entity.addPart("last_name", new StringBody(person.lastName));
				entity.addPart("email", new StringBody(person.email));
				entity.addPart("telephone", new StringBody(person.telephone));
				entity.addPart("summary", new StringBody(person.summary));
				entity.addPart("timezone", new StringBody(person.timezone));
				entity.addPart("street1", new StringBody(person.address[0]));
				entity.addPart("street2", new StringBody(person.address[1]));
				entity.addPart("city", new StringBody(person.address[2]));
				entity.addPart("state", new StringBody(person.address[3]));
				entity.addPart("zip", new StringBody(person.address[4]));
				entity.addPart("country", new StringBody(person.address[5]));
				entity.addPart("user_image", new FileBody(this.getGenerator().getPersonImgFile()));
				entity.addPart("addpersonsubmit", new StringBody("Create"));
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				entity.addPart("user[username]", new StringBody(person.userName));
				entity.addPart("user[password]", new StringBody(person.password));
				entity.addPart("user[password_confirmation]", new StringBody(person.password));
				entity.addPart("user[firstname]", new StringBody(person.firstName));
				entity.addPart("user[lastname]", new StringBody(person.lastName));
				entity.addPart("user[telephone]", new StringBody(person.telephone));
				entity.addPart("user[summary]", new StringBody(person.summary));
				entity.addPart("user[timezone]", new StringBody(person.timezone));
				entity.addPart("user_image", new FileBody(this.getGenerator().getPersonImgFile()));
				entity.addPart("user[email]", new StringBody(person.email));
				entity.addPart("address[street1]", new StringBody(person.address[0]));
				entity.addPart("address[street2]", new StringBody(person.address[1]));
				entity.addPart("address[city]", new StringBody(person.address[2]));
				entity.addPart("address[state]", new StringBody(person.address[3]));
				entity.addPart("address[zip]", new StringBody(person.address[4]));
				entity.addPart("address[country]", new StringBody(person.address[5]));
				entity.addPart("user_image", new FileBody(this.getGenerator().getPersonImgFile()));
				break;
		}
	}
}
