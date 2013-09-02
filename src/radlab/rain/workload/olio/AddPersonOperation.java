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


import java.io.UnsupportedEncodingException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.HttpStatus;
import radlab.rain.IScoreboard;


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
		if (this.isLoggedOn())
		{
			this.logOff();
		}

		// Fetch the new user form.
		this.getHttpTransport().fetchUrl(this.getGenerator().getAddPersonURL());
		this.trace(this.getGenerator().getAddPersonURL());
		
		// Load the static files associated with the new user form.
		this.loadStatics(this.getGenerator().getAddPersonStatics());
		this.trace( this.getGenerator().getAddPersonStatics());
		
		// Decide on a user ID and username.
		long id = this.generateUserId();
		String username = UserName.getUserName(id);
		if (username == null || username.length() == 0)
		{
			this.getLogger().warning("Username is null!");
		}
		String password = String.valueOf(id);

		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConstants.JAVA_INCARNATION:
				// Not available
				break;
			case OlioConstants.PHP_INCARNATION:
				// Not available
				break;
			case OlioConstants.RAILS_INCARNATION:
				// Check that the username is unique.
				StringBuilder checkResponse = this.getHttpTransport().fetchUrl(this.getGenerator().getCheckNameURL(), "name=" + username);
				this.trace(this.getGenerator().getCheckNameURL());
				if (checkResponse.equals( "Name taken" ))
				{
					throw new Exception( "Generated username was not unique" );
				}
				break;
		}

		// Construct the POST request to create the user.
		String addPersonResultURL = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConstants.JAVA_INCARNATION:
				addPersonResultURL = this.getGenerator().getAddPersonResultURL() + "?user_name=" + username;
				break;
			case OlioConstants.PHP_INCARNATION:
				addPersonResultURL = this.getGenerator().getAddPersonResultURL();
				break;
			case OlioConstants.RAILS_INCARNATION:
				addPersonResultURL = this.getGenerator().getAddPersonResultURL();
				break;
		}
		HttpPost httpPost = new HttpPost(addPersonResultURL);
		MultipartEntity entity = new MultipartEntity();
		this.populateEntity(entity, username, password);
		httpPost.setEntity(entity);

		// Make the POST request and verify that it succeeds.
		this.getHttpTransport().fetch(httpPost);
		this.trace(addPersonResultURL);

		//TODO: in Apache Olio there is a check on header for redirection

		int status = this.getHttpTransport().getStatusCode();
		if (HttpStatus.SC_OK != status)
		{
			throw new IOException("Multipart POST did not work for URL: " + addPersonResultURL + ". Returned status code: " + status + "!");
		}

		this.logOn();

		this.setFailed(false);
	}
	
	/**
	 * Adds the details and images needed to create a new user.
	 * 
	 * @param entity The request entity in which to add the details.
	 * 
	 * @throws UnsupportedEncodingException
	 */
	protected void populateEntity(MultipartEntity entity, String username, String password) throws UnsupportedEncodingException
	{
		String firstName = this.getUtility().generateName(2, 12);
		String lastName  = this.getUtility().generateName(5, 12);
		String telephone = this.getUtility().generatePhone(buffer);
		String summary = this.getUtility.generateText(50, 200);
		String timezone = this.getUtility.generateTimeZone();
		String domain = this.getUtility().generateAlphaString(3, 10);
		String address = this.getUtility().generateAddressParts();

		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConstants.JAVA_INCARNATION:
				entity.addPart("user_name", new StringBody(username));
				entity.addPart("password", new StringBody(password));
				entity.addPart("passwordx", new StringBody(password));
				entity.addPart("first_name", new StringBody(firstName));
				entity.addPart("last_name", new StringBody(lastName));
				entity.addPart("email", new StringBody(username + "@" + domain + ".com"));
				entity.addPart("telephone", new StringBody(telephone));
				entity.addPart("summary", new StringBody(summary));
				entity.addPart("timezone", new StringBody(timezone));
				entity.addPart("street1", new StringBody(address[0]));
				entity.addPart("street2", new StringBody(address[1]));
				entity.addPart("city", new StringBody(address[2]));
				entity.addPart("state", new StringBody(address[3]));
				entity.addPart("zip", new StringBody(address[4]));
				entity.addPart("country", new StringBody(address[5]));
				entity.addPart("upload_person_image", new FileBody(this.getGenerator().getPersonImg()));
				entity.addPart("Submit", new StringBody("Create"));
				break;
			case OlioConstants.PHP_INCARNATION:
				entity.addPart("add_user_name", new StringBody(username));
				entity.addPart("psword", new StringBody(password));
				entity.addPart("passwordx", new StringBody(password));
				entity.addPart("first_name", new StringBody(firstName));
				entity.addPart("last_name", new StringBody(lastName));
				entity.addPart("email", new StringBody(username + "@" + domain + ".com"));
				entity.addPart("telephone", new StringBody(telephone));
				entity.addPart("summary", new StringBody(summary));
				entity.addPart("timezone", new StringBody(timezone));
				entity.addPart("street1", new StringBody(address[0]));
				entity.addPart("street2", new StringBody(address[1]));
				entity.addPart("city", new StringBody(address[2]));
				entity.addPart("state", new StringBody(address[3]));
				entity.addPart("zip", new StringBody(address[4]));
				entity.addPart("country", new StringBody(address[5]));
				entity.addPart("user_image", new FileBody(this.getGenerator().getPersonImg()));
				entity.addPart("addpersonsubmit", new StringBody("Create"));
				break;
			case OlioConstants.RAILS_INCARNATION:
				entity.addPart("user[username]", new StringBody(username));
				entity.addPart("user[password]", new StringBody(password));
				entity.addPart("user[password_confirmation]", new StringBody(password));
				entity.addPart("user[firstname]", firstName);
				entity.addPart("user[lastname]", lastName);
				entity.addPart("user[telephone]", telephone);
				entity.addPart("user[summary]", new StringBody(summary));
				entity.addPart("user[timezone]", new StringBody(timezone));
				entity.addPart("user_image", new FileBody(this.getGenerator().getPersonImg()));
				entity.addPart("user[email]", new StringBody(username + "@" + domain + ".com"));
				entity.addPart("address[street1]", new StringBody(address[0]));
				entity.addPart("address[street2]", new StringBody(address[1]));
				entity.addPart("address[city]", new StringBody(address[2]));
				entity.addPart("address[state]", new StringBody(address[3]));
				entity.addPart("address[zip]", new StringBody(address[4]));
				entity.addPart("address[country]", new StringBody(address[5]));
				entity.addPart("user_image", new FileBody(this.getGenerator().getPersonImg()));
				break;
		}
	}
}
