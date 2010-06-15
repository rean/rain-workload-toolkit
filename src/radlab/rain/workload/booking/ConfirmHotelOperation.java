package radlab.rain.workload.booking;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import radlab.rain.IScoreboard;

/**
 * The Confirm Hotel operation allows the user to confirm a reservation
 * make using the Book Hotel page.  The user can accept the reservation, which
 * then displays the Search Hotel page listing this revervation under the
 * Current Reservations list.  The user can Revise the booking, in which 
 * case the Book Hotel page is redisplayed.  The user can also Cancel the
 * reservation.<br />
 */
public class ConfirmHotelOperation extends BookingOperation 
{
	public ConfirmHotelOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Confirm Hotel";
		this._operationIndex = BookingGenerator.CONFIRM_HOTEL;
		this._mustBeSync = true;
	}
		
	@Override
	public void execute() throws Throwable
	{
		// Step 1 - Refetch the last URL which should be a Confirm Booking Details page
		// from a previous Book Hotel operation.
		// TODO: Decide if this fetch is necessary.
		String lastConfirmBookingDetailsUrl = this.getGenerator().getLastUrl();

		// Do a GET for the View Hotel page page.
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + lastConfirmBookingDetailsUrl );
		HttpGet confirmBookingDetailsGet = new HttpGet( lastConfirmBookingDetailsUrl );
		StringBuilder confirmBookingDetailsResponse = this._http.fetch( confirmBookingDetailsGet );

		if ( this._http.getStatusCode() != 200 )
		{
			// We should probably bail here
			this.setFailed( true );
        	this.debugTrace("GET confirm booking details result status: " + this._http.getStatusCode());
			return; // or throw an exception?
		}

		// See if we can find some proof that a Confirm Booking Details page was returned.
		// TODO: We should check for the hotel name.
    	if ( confirmBookingDetailsResponse.indexOf( "<legend>Confirm Booking Details</legend>" ) > 0 ) {
    		this.debugTrace( "Response contains a Confirm Booking Details page!" );   		
    	} else {
			// We should probably bail here
			this.setFailed( true );
        	this.debugTrace("GET did not display a Confirm Bookin Details result page.  Aborting this operation.");
			return; // or throw an exception?    		
    	}    		

		// Get the finalUrl in case there was a redirect (unlikely).
		String confirmBookingDetailsFinalUrl = this._http.getFinalUrl();
		
		// Get the ViewState from the form so we can specify it later in the POST data.
		String viewState = this.getViewStateFromResponse( confirmBookingDetailsResponse );		

        // Step 2 - From the Confirm Booking Details page, emulate the user clicking the 
		// Proceed button button.

		// Create a POST request
		HttpPost confirmBookingPost = new HttpPost( confirmBookingDetailsFinalUrl );

		this.trace( this.getGenerator().getCurrentUser() + " POST " + confirmBookingDetailsFinalUrl );

		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add( new BasicNameValuePair( "confirm", "confirm" ) );
		formParams.add( new BasicNameValuePair( "confirm:confirm", "Confirm" ) );
		formParams.add( new BasicNameValuePair( "javax.faces.ViewState", viewState ) );

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity( formParams, "UTF-8" );
		confirmBookingPost.setEntity( entity );

        StringBuilder response = this._http.fetch( confirmBookingPost );
		if ( this._http.getStatusCode() != 200 )
		{
			// We should probably bail here
			this.setFailed( true );
        	this.debugTrace("POST confirm booking details status: " + this._http.getStatusCode());
			return; // or throw an exception?
		}

		// Here is the really peculiar design issues in the travel application.  After 
		// the hotel booking is confirmed it displays the search hotel page which now 
		// contains an additional table showing the user's currently booked hotels.  It 
		// also has a way to cancel existing bookings.
		//
		// See if we can find some proof that a Search Hotel page with current bookings
		// table was returned in the response.
		// TODO: We should check that the recently booked hotel name is in the table?
    	if ( response.indexOf( "Search Hotels" ) > 0  &&
    		 response.indexOf( "Current Hotel Bookings" ) > 0) {
    		this.debugTrace( "Success - Response contains the expected Search Hotel page!" );   		
    	} else {
			this.setFailed( true );
        	this.debugTrace("ERROR - POST to confirm booking details did not respond with the expected Search page.");
			return; // or throw an exception?    		   		
    	}

    	// Get the finalUrl after the POST, because this always redirects.
		String finalUrl = this._http.getFinalUrl();
		
    	// Save the last Confirm Booking Details URL.  We will need it in the Confirm
		// operation.
		this.getGenerator().setLastUrl(finalUrl);				
		
		this.setFailed( false );
	}
		
}