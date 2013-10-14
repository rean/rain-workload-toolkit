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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioPerson;
import radlab.rain.workload.olio.model.OlioSocialEvent;


/**
 * The EventDetailOperation is an operation that shows the details of an event.
 * The event is chosen at random by parsing the home page for an event ID. The
 * event details page is then loaded. If the user is logged in and can attend
 * the event, the user is added as an attendee 10% of the time.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class EventDetailOperation extends OlioOperation 
{
	public EventDetailOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.EVENT_DETAIL_OP_NAME;
		this._operationIndex = OlioGenerator.EVENT_DETAIL_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Select an event by parsing the home page.
		response = this.getHttpTransport().fetchUrl(this.getGenerator().getHomePageURL());
		this.trace(this.getGenerator().getHomePageURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + this.getGenerator().getHomePageURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getHomePageURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		int eventId = this.getUtility().findEventIdInHtml(response.toString());
		OlioSocialEvent event = this.getUtility().getSocialEvent(eventId);
		if (!this.getUtility().isValidSocialEvent(event)) 
		{
			this.getLogger().severe("Could not find an event on the home page (" + this.getGenerator().getHomePageURL() + "). Server response: " + response);
			throw new IOException("Could not find an event on the home page (" + this.getGenerator().getHomePageURL() + "). Server response: " + response);
		}

		// Make the GET request to load the event details.
		String eventUrl = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				eventUrl = this.getGenerator().getEventDetailURL() + event.id;
				break;
			case OlioConfiguration.PHP_INCARNATION:
				eventUrl = this.getGenerator().getEventDetailURL() + event.id;
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				eventUrl = this.getGenerator().getEventDetailURL() + event.id;
				break;
		}
		response = this.getHttpTransport().fetchUrl(eventUrl);
		this.trace(eventUrl);
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + eventUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + eventUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Load the static files (CSS/JS).
		this.loadStatics(this.getGenerator().getEventDetailStatics());
		this.trace(this.getGenerator().getEventDetailStatics());

		// Load images associated with the event.
		Set<String> imageUrls = this.parseImages(response.toString());
		this.loadImages(imageUrls);
		this.trace(imageUrls);
		
		// Check if user can be added as an attendee.
		OlioPerson loggedPerson = this.getUtility().getPerson(this.getSessionState().getLoggedPersonId());
		if (this.getUtility().isRegisteredPerson(loggedPerson))
		{
			boolean canAttend = (response.indexOf( "\"Attend\"" ) != -1);
			boolean isAttending = (response.indexOf( "\"Unattend\"" ) != -1);

			if (canAttend || isAttending)
			{
				// 10% of the time we can add ourselves, we will.
				if (this.getRandomGenerator().nextDouble() <= 0.1)
				{
					Map<String,String> header = null;
					String attendUrl = null;
					switch (this.getConfiguration().getIncarnation())
					{
						case OlioConfiguration.JAVA_INCARNATION:
							attendUrl = this.getGenerator().getAddAttendeeURL() + event.id + "/attend" + "&userName=" + loggedPerson.userName;
							break;
						case OlioConfiguration.PHP_INCARNATION:
							attendUrl = this.getGenerator().getAddAttendeeURL() + event.id;
							break;
						case OlioConfiguration.RAILS_INCARNATION:
							attendUrl = this.getGenerator().getAddAttendeeURL() + event.id + "/attend";
							// Need to add header that will request js instead of
							// html. This will prevent the redirect.
							header = new HashMap<String,String>(1);
							header.put("Accept", "text/javascript");
							break;
					}
					if (header != null)
					{
						response = this.getHttpTransport().fetchUrl(attendUrl, "", header);
					}
					else
					{
						response = this.getHttpTransport().fetchUrl(attendUrl);
					}
					this.trace(attendUrl);

					if (!this.getGenerator().checkHttpResponse(response.toString()))
					{
						this.getLogger().severe("Problems in performing request to URL: " + attendUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
						throw new IOException("Problems in performing request to URL: " + attendUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
					}
//					if (response.indexOf("You are attending") == -1)
//					{
//						this.getLogger().warning("Add attendee failed, possible race condition");
//					}
				}
			}
//			else
//			{
//				boolean notFound = response.indexOf("This file lives in public/404.html") != -1; 
//
//				if (notFound)
//				{
//					this.getLogger.severe("404 Error! Page not found: " + eventUrl);
//					throw new IOException("404 Error! Page not found: " + eventUrl);
//				}
//				else
//				{
//					boolean statusConflict = response.indexOf("Not logged in.") != -1;
//					if (statusConflict)
//					{
//						this.getLogger().warning("Status conflict: server reported user not logged in.");
//					}
//					else
//					{
//						this.getLogger().warning("Logged on but unable to attend an event: " + canAttend + ", " + isAttending + ". HTTP Response: " + response);
//					}
//				}
//			}
		}

		// Save session data
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}

//	// Taken for apache.olio.workload.driver.UIDriver
//	protected boolean validateEvent(String event)
//	{
//		return event != null && event.length() != 0 && !event.equals("-1");
//	}
}
