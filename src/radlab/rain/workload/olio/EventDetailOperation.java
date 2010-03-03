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
import java.util.Set;

/**
 * The EventDetailOperation is an operation that shows the details of an event.
 * The event is chosen at random by parsing the home page for an event ID. The
 * event details page is then loaded. If the user is logged in and can attend
 * the event, the user is added as an attendee 10% of the time.
 */
public class EventDetailOperation extends OlioOperation 
{
	public EventDetailOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "EventDetail";
		this._operationIndex = OlioGenerator.EVENT_DETAIL;
	}
	
	@Override
	public void execute() throws Throwable
	{
		// Select an event by parsing the home page.
		StringBuilder homeResponse = this._http.fetchUrl( this.getGenerator().homepageURL );
		this.trace( this.getGenerator().homepageURL );
		String selectedEvent = RandomUtil.randomEvent( this._random, homeResponse );
		if ( selectedEvent == null ) 
		{
			throw new IOException( "Could not find an event on the home page" );
		}
		
		// Make the GET request to load the event details.
		String eventUrl = this.getGenerator().eventDetailURL + selectedEvent;
		StringBuilder eventResponse = this._http.fetchUrl( eventUrl );
		this.trace( eventUrl );
		if ( eventResponse.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}
		
		// Load the static files (CSS/JS).
		this.loadStatics( this.getGenerator().eventDetailStatics );
		this.trace( this.getGenerator().eventDetailStatics );
		
		// Load images associated with the event.
		Set<String> imageUrls = parseImages( eventResponse );
		this.loadImages( imageUrls );
		this.trace( imageUrls );
		
		// Check if user can be added as an attendee.
		if ( this.isLoggedOn() )
		{
			boolean canAttend = ( eventResponse.indexOf( "\"Attend\"" ) != -1 );
			boolean isAttending = ( eventResponse.indexOf( "\"Unattend\"" ) != -1 );
			
			if ( canAttend || isAttending )
			{
				// 10% of the time we can add ourselves, we will.
				if ( this._random.random( 0, 9 ) == 0 )
				{
					String attendUrl = this.getGenerator().addAttendeeURL + selectedEvent + "/attend";
					this._http.fetchUrl( attendUrl  );
					this.trace( attendUrl );
				}
			}
			else
			{
				boolean notFound = eventResponse.indexOf( "This file lives in public/404.html" ) != -1; 
				
				if ( notFound )
				{
					throw new IOException( "404 Error! Page not found: " + eventUrl );
				}
				else
				{
					this._logger.warning( "Logged on but unable to attend an event: " + canAttend + ", " + isAttending + "\n" + eventResponse );
				}
			}
		}
		
		this.setFailed( false );
	}
	
}
