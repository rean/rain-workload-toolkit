package radlab.rain.workload.bookingHotspots;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import radlab.rain.IScoreboard;

/**
 * The Book Hotel operation allows the user to book a reservation at the
 * hotel.  They must supply the several pieces of information, i.e. checkin
 * date, checkout date, credit card number, credit card name, expiration 
 * date, etc.  The user can cancel the operation.<br />
 */
public class BookHotelOperation extends BookingOperation 
{
	public BookHotelOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Book Hotel";
		this._operationIndex = BookingGenerator.BOOK_HOTEL;
		this._mustBeSync = true;
	}
		
	@Override
	public void execute() throws Throwable
	{
		// Step 1 - Re-fetch the last URL which should be the last view hotel operation.
		// TODO: Decide if this fetch is necessary.
		String lastViewHotelUrl = this.getGenerator().getLastUrl();

		// Do a GET for the View Hotel page page.
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + lastViewHotelUrl );
		HttpGet viewHotelGet = new HttpGet( lastViewHotelUrl );
		//StringBuilder viewGetResponse = this._http.fetch( viewHotelGet );
		this._http.fetch( viewHotelGet );

		if ( this._http.getStatusCode() != 200 )
		{
			// We should probably bail here
			this.setFailed( true );
        	this.trace("DEBUG: GET view hotel result status: " + this._http.getStatusCode());
			return; // or throw an exception?
		}

/*
		// See if we can find some proof that a View Hotel page was returned.
		// TODO: We should check for the hotel name.
    	if ( viewGetResponse.indexOf( "value=\"Book Hotel\"" ) > 0 ) {
    		this.trace( "DEBUG: Response contains a View Hotel page!" );   		
    	} else {
			// We should probably bail here
			this.setFailed( true );
        	this.trace("DEBUG: GET did not display a view hotel result page.  Aborting this operation.");
			return; // or throw an exception?    		
    	}    		
*/
		// Step 2
		boolean status = processBookHotelButton();
		if (status == false) {
			// We should probably bail here
			this.setFailed( true );
        	this.debugTrace("Book Hotel button processing failed.");
			return; // or throw an exception?
		}
    	
		// If the user is not currently logged in we may have to take a slight
		// detour and process the login page.  This application uses form-based login.
		// Once the login page is processed, it appears that the application oddly 
		// redisplays the View Hotel Page.  That means we have to process the Book
		// Hotel form again.  Strange.
		
		// See if we can find some proof that a Book Hotel page was returned.
		// TODO: We should check for the hotel name.
  //  	if ( bookHotelResponse.indexOf( "<legend>Book Hotel</legend>" ) > 0 ) {
  //  		this.trace( "DEBUG: Success - Response contains a Book Hotel page!" );   		
  //  	}		

    	// Get the finalUrl after the POST, because this always redirects.
		String bookingFormUrl = this._http.getFinalUrl();
		
		if (bookingFormUrl.indexOf("/login") > 0) {
			// Step 2A - Get a user logged in.
			this.debugTrace( "We must log the user in to book the hotel!" );   		
			this.processLoginForm(false);

			// The View Hotel page should be redisplayed now.  Process the Book Hotel button again.
			status = processBookHotelButton();
			if (status == false) {
				// We should probably bail here
				this.setFailed( true );
	        	this.debugTrace("Book Hotel button processing (second try after login) failed.");
				return; // or throw an exception?
			}
			bookingFormUrl = this._http.getFinalUrl();
		}
		
		StringBuilder bookHotelResponse = this._http.getResponseBuffer();
		
		// Get the ViewState from the form so we can specify it later in the next POST data.
		String viewState = this.getViewStateFromResponse( bookHotelResponse );	

		// Step 3 - Fill out and POST the Booking form, as if the user clicked on
		// the Proceed button.

		HttpPost bookingFormPost = new HttpPost( bookingFormUrl );
		
		// Create the POST data.
		String checkinDate = "08-01-2010";
		String checkoutDate = "08-02-2010";
		String creditCardNumber = "1111222233334444";
		String creditCardName = "Visa";
		String creditCardExpiryMonth = "1";
		String creditCardExpiryYear = "2010";

		List<NameValuePair> bookingFormParams = new ArrayList<NameValuePair>();
		bookingFormParams.add( new BasicNameValuePair( "bookingForm", "bookingForm" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:checkinDate", checkinDate ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:checkoutDate", checkoutDate ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:beds", "1" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:smoking", "false" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:amenities", "OCEAN_VIEW" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:amenities", "LATE_CHECKOUT" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:amenities", "MINIBAR" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:creditCard", creditCardNumber ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:creditCardName", creditCardName ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:creditCardExpiryMonth", creditCardExpiryMonth ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:creditCardExpiryYear", creditCardExpiryYear ) );
		bookingFormParams.add( new BasicNameValuePair( "javax.faces.ViewState", viewState ) );
		bookingFormParams.add( new BasicNameValuePair( "processIds", "bookingForm:proceed, *" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:proceed", "bookingForm:proceed" ) );
		bookingFormParams.add( new BasicNameValuePair( "ajaxSource", "bookingForm:proceed" ) );
		
		UrlEncodedFormEntity bookingFormEntity = new UrlEncodedFormEntity( bookingFormParams, "UTF-8" );
		bookingFormPost.setEntity( bookingFormEntity );

        // Do the POST of the bookingForm.  Don't look at the at the response contents,
		// because it will be empty.  
		this.trace( this.getGenerator().getCurrentUser() + " POST " + bookingFormUrl );
		StringBuilder bookingFormResponse = this._http.fetch( bookingFormPost );
        if (bookingFormResponse.length() > 0) {
        	this.debugTrace(bookingFormResponse.toString());
        }
        String msg = "Status " + this._http.getStatusCode();
        this.debugTrace(msg);
		if ( this._http.getStatusCode() != 200 )
		{
			// We should probably bail here
			this.setFailed( true );
        	this.debugTrace("POST of bookingForm status: " + this._http.getStatusCode());
			return; // or throw an exception?
		}
	
		// If the bookingForm POST worked there should be a Spring-Redirect-URL response 
		// header that points to the Confirm Booking Details page which we have to GET.
		Hashtable<String,String> headerMap = this._http.getHeaderMap();
		String springRedirectUrl = headerMap.get( "Spring-Redirect-URL" );
		if ( springRedirectUrl == null )
		{
			// We should probably bail here
			this.setFailed( true );
        	this.debugTrace("ERROR - POST bookingForm did not return a Spring-Redirect-URL in the response headers");
			return; // or throw an exception?
		}
		this.debugTrace( "Spring-Redirect-URL: " + springRedirectUrl );
		
		// The Spring redirect URL does NOT have the host and port info so we need to 
		// prepend it with http://<host>:<port>		
		String confirmBookingDetailsUrl = "http://" + this.getGenerator().getTrack().getTargetHostName() + ":" + this.getGenerator().getTrack().getTargetHostPort() + springRedirectUrl;
						
        // Step 4 - Get the Confirm Booking Details page.
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + confirmBookingDetailsUrl );
		HttpGet confirmBookingDetailsGet = new HttpGet( confirmBookingDetailsUrl );
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
    		this.debugTrace( "Success - Response contains a Confirm Booking Details page!" );   		
    	}		

    	// Save the last Confirm Booking Details URL.  We will need it in the Confirm
		// operation.
		this.getGenerator().setLastUrl(confirmBookingDetailsUrl);
		
		this.setFailed( false );
	}
		
}