package radlab.rain.workload.booking;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
 * 
 * TODO: This driver does not yet support rebooking, when the user clicks 
 * the Revise button in the Confirm Booking Details page.  That redisplays 
 * the Book Hotel page and allows the user to modify the booking.
 * 
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
		if (this.getGenerator().getFoundHotels() == false) {
			this.debugTrace("No search result available, thus no hotel(s) to book.  Skipping.");
			this.setFailed( false );
			return;
		}

		// Step 1 - Re-fetch the last URL which should be the last view hotel operation.
		// TODO: Decide if this fetch is necessary.
		String lastViewHotelUrl = this.getGenerator().getLastUrl();

		// Do a GET for the View Hotel page page.
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + lastViewHotelUrl );
		HttpGet viewHotelGet = new HttpGet( lastViewHotelUrl );
		this._http.fetch( viewHotelGet );

		if ( this._http.getStatusCode() != 200 )
		{
			String errorMessage = "BookHotel ERROR - GET view hotel result status: " + this._http.getStatusCode();
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}

		// Step 2
		// The process Book Hotel button in the View Hotel page.  If an error 
		// occurs it will throw an exception, the return status for now is always
		// true.  Check the response for a login URL or a Book Hotel page.
		boolean status = processBookHotelButton("1st attempt");
		if (status == false) {
			String errorMessage = "BookHotel ERROR - Book Hotel button processing failed."; 
        	this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}
    	
		// If the user is not currently logged in we may have to take a slight
		// detour and process the login page.  This application uses form-based login.
		// Once the login page is processed, it appears that the application oddly 
		// redisplays the View Hotel Page.  That means we have to process the Book
		// Hotel form again.  Strange.
		
    	// Get the finalUrl after the POST, because this always redirects.
		String bookingFormUrl = this._http.getFinalUrl();
		
		if (bookingFormUrl.indexOf("/login") > 0) {
			// Step 2A - Get a user logged in.
			this.debugTrace( "We must log the user in to book the hotel!" );   		
			this.processLoginForm(false);

			// The View Hotel page should be redisplayed now.  Process the Book Hotel button again.
			status = processBookHotelButton("2nd attempt");
			if (status == false) {
				String errorMessage = "BookHotel ERROR - Book Hotel button processing (second try after login) failed."; 
	        	this.debugTrace(errorMessage);
				throw new Exception(errorMessage);
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
		// Determine check-in, check-out and card-expiry dates dynamically rather
		// than hard coding dates
		Calendar cal = Calendar.getInstance();
		DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		// Set the current time to today
		cal.setTimeInMillis( System.currentTimeMillis() );
		// Add 4 weeks to compute the check-in date
		cal.add( Calendar.DAY_OF_MONTH, 4*7 );
				
		String checkinDate = formatter.format( cal.getTime() );
		// Add 2 days to calendar for checkout
		cal.add( Calendar.DAY_OF_MONTH, 2 );
		String checkoutDate = formatter.format( cal.getTime() );
		String creditCardNumber = "1111222233334444";
		String creditCardName = "Visa";
		String creditCardExpiryMonth = "1";
		// Change the expiry date to a year from now
		cal.add( Calendar.YEAR, 1 );
		String creditCardExpiryYear = String.valueOf( cal.get( Calendar.YEAR ) );
				
		List<NameValuePair> bookingFormParams = new ArrayList<NameValuePair>();
		bookingFormParams.add( new BasicNameValuePair( "bookingForm", "bookingForm" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:checkinDate", checkinDate ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:checkoutDate", checkoutDate ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:beds", "1" ) );
		bookingFormParams.add( new BasicNameValuePair( "bookingForm:smoking", "false" ) );
		// The amenities feature was commented out of the RC1 version of booking-faces.
		//bookingFormParams.add( new BasicNameValuePair( "bookingForm:amenities", "OCEAN_VIEW" ) );
		//bookingFormParams.add( new BasicNameValuePair( "bookingForm:amenities", "LATE_CHECKOUT" ) );
		//bookingFormParams.add( new BasicNameValuePair( "bookingForm:amenities", "MINIBAR" ) );
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
			String errorMessage = "BookHotel ERROR - POST bookingForm status: " + this._http.getStatusCode();
        	this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}
	
		// If the bookingForm POST worked there should be a Spring-Redirect-URL response 
		// header that points to the Confirm Booking Details page which we have to GET.
		Hashtable<String,String> headerMap = this._http.getHeaderMap();
		String springRedirectUrl = headerMap.get( "Spring-Redirect-URL" );
		if ( springRedirectUrl == null )
		{
			String errorMessage = "BookHotel ERROR - POST bookingForm did not return a Spring-Redirect-URL in the response headers";
        	this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
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
			String errorMessage = "BookHotel ERROR - GET Confirm Booking Details result status: " + this._http.getStatusCode();
        	this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}

		// See if we can find some proof that a Confirm Booking Details page was returned.
		// This is only informational.  If called, the Confirm Hotel operation will double
		// check that the correct page as displayed and return an error if it's not.
		// TODO: We should check for the hotel name.
    	if ( confirmBookingDetailsResponse.indexOf( "<legend>Confirm Booking Details</legend>" ) > 0 ) {
    		this.debugTrace( "Success - Response contains a Confirm Booking Details page!" );   		
    	}		

    	// Save the last Confirm Booking Details URL.  We will need it later in the Confirm
		// operation which may or may not be invoked.
		this.getGenerator().setLastUrl(confirmBookingDetailsUrl);
		
		this.setFailed( false );
	}
	
	// Quick test for dynamic check-in, check-out and card expiry date
	public static void main( String[] args )
	{
		Calendar cal = Calendar.getInstance();
		DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		// Set the current time to today
		cal.setTimeInMillis( System.currentTimeMillis() );
		// Add 4 weeks to compute the check-in date
		cal.add( Calendar.DAY_OF_MONTH, 4*7 );
				
		String checkinDate = formatter.format( cal.getTime() );
		// Add 2 days to calendar for checkout
		cal.add( Calendar.DAY_OF_MONTH, 2 );
		String checkoutDate = formatter.format( cal.getTime() );
		String creditCardExpiryMonth = "1";
		// Change the expiry date to a year from now
		cal.add( Calendar.YEAR, 1 );
		String creditCardExpiryYear = String.valueOf( cal.get( Calendar.YEAR ) );

		System.out.println( "Check-in    : " + checkinDate );
		System.out.println( "Check-out   : " + checkoutDate );
		System.out.println( "Card expiry : " + creditCardExpiryMonth + "/" + creditCardExpiryYear );
	}
}