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
 * Author Original authors
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013
 */

package radlab.rain.workload.olio;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.HttpStatus;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioPerson;
import radlab.rain.workload.olio.model.OlioSocialEvent;
import radlab.rain.workload.olio.model.OlioTag;


/**
 * The AddEventOperation is an operation that creates a new event. The user
 * must be logged on. The creation of the POST involves populating the request
 * with event details, an image, a document, and address data.<br />
 * <br />
 * The requests made include loading the event form, loading the static URLs
 * (CSS/JS), and sending the POST data to the application.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class AddEventOperation extends OlioOperation 
{
	public AddEventOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.ADD_EVENT_OP_NAME;
		this._operationIndex = OlioGenerator.ADD_EVENT_OP;
	}
	
	@Override
	public void execute() throws Throwable
	{
		// Need a logged person
		OlioPerson loggedPerson = this.getUtility().getPerson(this.getSessionState().getLoggedPersonId());
		if (!this.getUtility().isRegisteredPerson(loggedPerson))
		{
			this.getLogger().severe("Login required for adding an event");
			//throw new Exception("Login required for adding an event");
			this.setFailed(true);
			return;
		}

		StringBuilder response = null;

		// Fetch the add event form.
		response = this.getHttpTransport().fetchUrl(this.getGenerator().getAddEventURL());
		this.trace(this.getGenerator().getAddEventURL());
		// Verify that the request succeeded. 
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getAddEventURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getAddEventURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}
		// Load the static files associated with the add event form.
		this.loadStatics(this.getGenerator().getAddEventStatics());
		this.trace(this.getGenerator().getAddEventStatics());

		// Get the authentication token needed to create the POST request.
		String token = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				// No token to parse
				break;
			case OlioConfiguration.PHP_INCARNATION:
				// No token to parse
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				token = this.parseAuthToken(response.toString());
				if ( token == null )
				{
					throw new Exception( "Authentication token could not be parsed" );
				}
				break;
		}
		
		// Generate a new Olio social event
		OlioSocialEvent event = this.getUtility().newSocialEvent();
		event.submitterUserName = loggedPerson.userName;

		// Submit the add event form to create the event.
		HttpPost reqPost = new HttpPost(this.getGenerator().getAddEventResultURL());
		MultipartEntity entity = new MultipartEntity();
		this.populateEntity(entity, event);
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				// No token to set
				break;
			case OlioConfiguration.PHP_INCARNATION:
				// No token to set
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				entity.addPart("authenticity_token", new StringBody(token));
				break;
		}
		reqPost.setEntity(entity);
		response = this.getHttpTransport().fetch(reqPost);
		this.trace(this.getGenerator().getAddEventResultURL());
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
		// Verify that the request succeeded. 
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}
		// Verify that the operation succeeded. 
		int index = response.toString().toLowerCase().indexOf("success");
		if (index == -1)
		{
			this.getLogger().severe("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "): Could not find success message in result body. Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + reqPost.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "): Could not find success message in result body");
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}

	/**
	 * Adds the details and files needed to create a new event in Olio.
	 * 
	 * @param entity The request entity in which to add event details.
	 */
	protected void populateEntity(MultipartEntity entity, OlioSocialEvent evt) throws Throwable
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(evt.eventTimestamp);

		StringBuilder tags = new StringBuilder();
		for (OlioTag tag : evt.tags)
		{
			tags.append(tag.name).append(' ');
		}
		tags.setLength(tags.length()-1); // Trim trailing space

		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				entity.addPart("title", new StringBody(evt.title));
				entity.addPart("summary", new StringBody(evt.summary));
				entity.addPart("description", new StringBody(evt.description));
            	entity.addPart("submitter_user_name", new StringBody(evt.submitterUserName));
				entity.addPart("telephone", new StringBody(evt.telephone));
				entity.addPart("timezone", new StringBody(evt.timezone));
				entity.addPart("year", new StringBody(Integer.toString(cal.get(Calendar.YEAR))));
				entity.addPart("month", new StringBody(Integer.toString(cal.get(Calendar.MONTH))));
				entity.addPart("day", new StringBody(Integer.toString(cal.get(Calendar.DAY_OF_MONTH))));
				entity.addPart("hour", new StringBody(Integer.toString(cal.get(Calendar.HOUR_OF_DAY))));
				entity.addPart("minute", new StringBody(Integer.toString(cal.get(Calendar.MINUTE))));
				entity.addPart("tags", new StringBody(tags.toString()));
				entity.addPart("street1", new StringBody(evt.address[0]));
				entity.addPart("street2", new StringBody(evt.address[1]));
				entity.addPart("city", new StringBody(evt.address[2]));
				entity.addPart("state", new StringBody(evt.address[3]));
				entity.addPart("zip", new StringBody(evt.address[4]));
				entity.addPart("country", new StringBody(evt.address[5]));
				entity.addPart("upload_event_image", new FileBody(this.getGenerator().getEventImgFile()));
				entity.addPart("upload_event_literature", new FileBody(this.getGenerator().getEventPdfFile()));
				entity.addPart("submit", new StringBody("Create"));
				break;
			case OlioConfiguration.PHP_INCARNATION:
				entity.addPart("title", new StringBody(evt.title));
				//entity.addPart("summary", new StringBody(evt.summary));
				entity.addPart("description", new StringBody(evt.description));
            	entity.addPart("submitter_user_name", new StringBody(evt.submitterUserName));
				entity.addPart("telephone", new StringBody(evt.telephone));
				entity.addPart("timezone", new StringBody(evt.timezone));
				entity.addPart("year", new StringBody(Integer.toString(cal.get(Calendar.YEAR))));
				entity.addPart("month", new StringBody(Integer.toString(cal.get(Calendar.MONTH))));
				entity.addPart("day", new StringBody(Integer.toString(cal.get(Calendar.DAY_OF_MONTH))));
				entity.addPart("hour", new StringBody(Integer.toString(cal.get(Calendar.HOUR_OF_DAY))));
				entity.addPart("minute", new StringBody(Integer.toString(cal.get(Calendar.MINUTE))));
				entity.addPart("tags", new StringBody(tags.toString()));
				entity.addPart("street1", new StringBody(evt.address[0]));
				entity.addPart("street2", new StringBody(evt.address[1]));
				entity.addPart("city", new StringBody(evt.address[2]));
				entity.addPart("state", new StringBody(evt.address[3]));
				entity.addPart("zip", new StringBody(evt.address[4]));
				entity.addPart("country", new StringBody(evt.address[5]));
				entity.addPart("upload_event_image", new FileBody(this.getGenerator().getEventImgFile()));
				entity.addPart("upload_event_literature", new FileBody(this.getGenerator().getEventPdfFile()));
				entity.addPart("addeventsubmit", new StringBody("Create"));
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				entity.addPart("event[title]", new StringBody(evt.title));
				entity.addPart("event[summary]", new StringBody(evt.summary));
				entity.addPart("event[description]", new StringBody(evt.description));
				//FIXME: Submitter user name?
				entity.addPart("event[telephone]", new StringBody(evt.telephone));
				//FIXME: Timezone?
				entity.addPart("event[event_timestamp(1i)]", new StringBody(Integer.toString(cal.get(Calendar.YEAR))));
				entity.addPart("event[event_timestamp(2i)]", new StringBody(Integer.toString(cal.get(Calendar.MONTH))));
				entity.addPart("event[event_timestamp(3i)]", new StringBody(Integer.toString(cal.get(Calendar.DAY_OF_MONTH))));
				entity.addPart("event[event_timestamp(4i)]", new StringBody(Integer.toString(cal.get(Calendar.HOUR_OF_DAY))));
				entity.addPart("event[event_timestamp(5i)]", new StringBody(Integer.toString(cal.get(Calendar.MINUTE))));
				entity.addPart("tag_list", new StringBody(tags.toString()));
				entity.addPart("event_image", new FileBody(this.getGenerator().getEventImgFile()));
				entity.addPart("event_document", new FileBody(this.getGenerator().getEventPdfFile()));
				entity.addPart("address[street1]", new StringBody(evt.address[0]));
				entity.addPart("address[street2]", new StringBody(evt.address[1]));
				entity.addPart("address[city]", new StringBody(evt.address[2]));
				entity.addPart("address[state]", new StringBody(evt.address[3]));
				entity.addPart("address[zip]", new StringBody(evt.address[4]));
				entity.addPart("address[country]", new StringBody(evt.address[5]));
				entity.addPart("commit", new StringBody("Create"));
				break;
		}
	}
}
