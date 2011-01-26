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
 * The Cancel Hotel Results operation gets the Search Hotels page
 * and checks that there is a Current Hotel Bookings label and
 * associated table.  If bookings are present there will be a Cancel
 * link next to each booking.  This method will cycle through the
 * hotel bookings and cancel all of them.
 */
public class CancelHotelOperation extends BookingOperation 
{
	public static String NAME = "Cancel Hotel";
	private int sleepTimeBetweenCancels = 1000;		// In milliseconds.

	public CancelHotelOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = NAME;
		this._operationIndex = BookingGenerator.CANCEL_HOTEL;
		this._mustBeSync = true;
	}
		
	@Override
	public void execute() throws Throwable
	{
		// After the a hotel booking is confirmed it displays the Search Hotel page which now 
		// contains an additional table, labeled Current Hotel Bookings, which shows the 
		// user's currently booked hotels.  There is a Cancel link for each booked hotel
		// in the table.
		//
		// The results URL from a previously completed Confirm Hotel operation should
		// have been saved.
		String searchGetUrl = this.getGenerator().getLastUrl();
		if (searchGetUrl == null || (searchGetUrl != null && searchGetUrl.length() == 0)) {
			String errorMessage = "CancelHotel INTERNAL ERROR - No search hotel URL saved.";
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}

		// Get the search hotel page to get things started.
		this.trace(this.getGenerator().getCurrentUser() + " GET  " + searchGetUrl);
		HttpGet hotelSearchGet = new HttpGet( searchGetUrl );
		StringBuilder response = this._http.fetch( hotelSearchGet );
		//System.out.println( "Hotel results: " + response.toString() );

		if ( this._http.getStatusCode() != 200 )
		{
			// We should probably bail here
			String errorMessage = "CancelHotel ERROR - GET search page status: " + this._http.getStatusCode();
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}		
		
		// See if we can find some proof that a Search Hotel page with current bookings
		// table was returned in the response.
		// TODO: We should check that the recently booked hotel name is in the table?
    	if ( response.indexOf( "Search Hotels" ) > 0  &&
    		 response.indexOf( "Current Hotel Bookings" ) > 0) {
    		this.debugTrace( "INFO - Response page contains the expected Current Hotel Bookings table!" );   		
    	} else {
    		// ISSUE: Should this really warrant being treated as an error.  We could just exit.
    		String errorMessage = "CancelHotel - GET search page does not contain Current Hotel Bookings.  Skipping";
			this.debugTrace(errorMessage);
			//throw new Exception(errorMessage);
			this.setFailed( false );		
			return;
    	}		

		this.traceUser(response);
    	
    	// TODO: We should decide if we cancel only one, some or all current hotel bookings.  For 
    	// now we'll cancel all that we find.
		int cancelCount = 0;
		while (true) {

			// If there is a booked hotel, there the first is always the value below with a
			// sequence number 0.  Even after we cancel a hotel, when the response page
			// is redisplayed, the first booking will have a sequence number of 0.  So we
			// can always search for the same link.
			String cancelHotelValue = "bookingsForm:bookings:0:cancel";

			if ( response.indexOf( cancelHotelValue ) == -1) {
				//this.debugTrace( "Did not find " + cancelHotelValue );   		
				break;
			}
			
			// Get the ViewState from the search form so we can specify it later in the POST data.
            // Note: A new viewState is returned each time the search results is retrieved.
			String viewState = this.getViewStateFromResponse( response );
			
    		// Some think time between Cancel Hotel requests.
    		if (cancelCount > 0) {
    			this.debugTrace("Sleep for " + sleepTimeBetweenCancels + " milliseconds");
    			Thread.sleep(sleepTimeBetweenCancels);
    		}

			this.debugTrace( "Canceling " + cancelHotelValue );   		

			String postUrl = this._http.getFinalUrl();
			this.trace(this.getGenerator().getCurrentUser() + " POST " + postUrl);
		
			// Create a POST request
			HttpPost cancelHotelPost = new HttpPost( postUrl );
		
	        // Create the POST data.
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add( new BasicNameValuePair( "bookingsForm", "bookingsForm" ) );
			formParams.add( new BasicNameValuePair( "javax.faces.ViewState", viewState ) );
			formParams.add( new BasicNameValuePair( "processIds", cancelHotelValue + ", bookingsFragment" ) );
			formParams.add( new BasicNameValuePair( cancelHotelValue, cancelHotelValue ) );
			formParams.add( new BasicNameValuePair( "ajaxSource", cancelHotelValue ) );
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity( formParams, "UTF-8" );
		
			// Do the POST to search for hotels - NO content will be returned here.  
			// In the response header there will be a "Spring-Redirect-URL" that 
			// will you can HTTP GET for the results page.
			cancelHotelPost.setEntity( entity );

			// Submit the POST request to cancel the booking.  The response if successful 
			// is is a new Search Hotel page.  It may or may not contain additional booked
			// hotels for us to cancel.
			response = this._http.fetch( cancelHotelPost );
			if ( this._http.getStatusCode() != 200 )
			{
	        	String errorMessage = "CancelHotel ERROR - POST cancel hotel status: " + this._http.getStatusCode();
	        	this.debugTrace(errorMessage);
				throw new Exception(errorMessage);
			}
 
			// Check the page contents.  The response seems to contain only a portion of the
			// original search hotel page, just a <div> section containing the remaining booking
			// information or a message indicating "No Bookings Found".
			if ( response.indexOf( "<div id=\"bookingsSection\">" ) == -1) {
	       		String errorMessage = "CancelHotel ERROR - POST response page does not contain Current Hotel Bookings section.";
	   			this.debugTrace(errorMessage);
	   			throw new Exception(errorMessage);
			}

			// Update the counter and go on to determine if there's another Cancel link.
			cancelCount++;
			}
		
		this.debugTrace( "Canceled " + cancelCount + " hotel(s)." );
		
		this.setFailed( false );		
	}
}
