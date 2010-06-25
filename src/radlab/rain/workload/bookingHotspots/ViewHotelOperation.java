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
 * The View Hotel operation allows the user to see details about a hotel
 * (e.g. it's address, and price per night).  The user can then Book the hotel or start a different
 * search.
 * 
 * This code currently needs to rely on the Search Results page, because that's the
 * page that contains individual links to view each hotel in the search results.  Also
 * the javax.faces.viewState value changes each time this Search Results page is fetched.
 * The view hotel sequence is a POST to get a Spring-Redirect-URL, followed by a GET to 
 * fetch the actual View Hotel page for a specific hotel.
 * 
 * The execute method in this class will follow every hotel link visible on the 
 * search results page.  So it does the GET-POST-GET sequence described above.
 */
public class ViewHotelOperation extends BookingOperation 
{
	public ViewHotelOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "View Hotel";
		this._operationIndex = BookingGenerator.VIEW_HOTEL;
		this._mustBeSync = true;
	}
		
	@Override
	public void execute() throws Throwable
	{
		// The results URL from a previously completed Search Hotel operation should
		// have been saved.  This get is like displaying the search results page for the
		// first time or clicking the Web browser's Back Button after viewing the
		// View Hotel page.
		String searchResultsUrl = this.getGenerator().getLastUrl();
		if (searchResultsUrl == null || (searchResultsUrl != null && searchResultsUrl.length() == 0)) {
			this.debugTrace("ERROR - No search results URL saved.");
			this.setFailed( true );
        	this.setFailureReason(new Exception("No search results URL saved.") );
        	return;
		}
		
		// View all the hotels found by the search.
		int linkSequenceNumber = 0;
		int viewCount = 0;
		while (true) {

			this.trace(this.getGenerator().getCurrentUser() + " GET  " + searchResultsUrl);

			HttpGet hotelSearchResultsGet = new HttpGet( searchResultsUrl );
			StringBuilder response = this._http.fetch( hotelSearchResultsGet );
			//System.out.println( "Hotel results: " + response.toString() );

			if ( this._http.getStatusCode() != 200 )
			{
				// We should probably bail here
				String errorMessage = "GET search result error status: " + this._http.getStatusCode();
	        	this.debugTrace(errorMessage);
				this.setFailed( true );
	        	this.setFailureReason(new Exception(errorMessage) );
				return; // or throw an exception?
			}		

			this.traceUser(response);

			// Get the ViewState from the search form so we can specify it later in the POST data.
            // Note: A new viewState is returned each time the search results is retrieved.
			String viewState = this.getViewStateFromResponse( response );
		
			String viewHotelValue = "hotels:hotels:" + linkSequenceNumber + ":viewHotelLink";
    	
			if ( response.indexOf( viewHotelValue ) == -1) {
				this.debugTrace( "Did not find " + viewHotelValue );   		
				break;
			}
			
			// View the hotel
			this.debugTrace( "Viewing " + viewHotelValue );   		

			// Create a POST request
			HttpPost viewHotelPost = new HttpPost( searchResultsUrl );
		
	        // Create the POST data.
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add( new BasicNameValuePair( "hotels", "hotels" ) );
			formParams.add( new BasicNameValuePair( "javax.faces.ViewState", viewState ) );
			formParams.add( new BasicNameValuePair( "processIds", viewHotelValue ) );
			formParams.add( new BasicNameValuePair( viewHotelValue, viewHotelValue ) );
			formParams.add( new BasicNameValuePair( "ajaxSource", viewHotelValue ) );
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity( formParams, "UTF-8" );
		
			// Do the POST to search for hotels - NO content will be returned here.  
			// In the response header there will be a "Spring-Redirect-URL" that 
			// will you can HTTP GET for the results page.
			viewHotelPost.setEntity( entity );

			this.trace(this.getGenerator().getCurrentUser() + " POST " + searchResultsUrl);

			//StringBuilder viewPostResponse = this._http.fetch( viewHotelPost );
			this._http.fetch( viewHotelPost );
			if ( this._http.getStatusCode() != 200 )
			{
				// We should probably bail here
				this.setFailed( true );
	        	this.debugTrace("POST view hotel status: " + this._http.getStatusCode());
				return; // or throw an exception?
			}
					
			// If the POST worked there should be a Spring-Redirect-URL response header that
			// points at the View Hotel page.
			Hashtable<String,String> headerMap = this._http.getHeaderMap();
			String viewSpringRedirectUrl = headerMap.get( "Spring-Redirect-URL" );
			if ( viewSpringRedirectUrl == null )
			{
				// We should probably bail here
				this.setFailed( true );
	        	this.debugTrace("ERROR - POST view hotel did not return a Spring-Redirect-URL in the response headers");
				return; // or throw an exception?
			}
			this.debugTrace( "Spring-Redirect-URL: " + viewSpringRedirectUrl );
			
			// The Spring redirect URL does NOT have the host and port info so we need to prepend it with 
			// http://<host>:<port>		
			String fullViewGetUrl = "http://" + this.getGenerator().getTrack().getTargetHostName() + ":" + this.getGenerator().getTrack().getTargetHostPort() + viewSpringRedirectUrl;
			this.trace( this.getGenerator().getCurrentUser() + " GET  " + fullViewGetUrl );
							
			// Do a GET for the View Hotel page page.
			HttpGet viewHotelGet = new HttpGet( fullViewGetUrl );
			StringBuilder viewGetResponse = this._http.fetch( viewHotelGet );

			if ( this._http.getStatusCode() != 200 )
			{
				// We should probably bail here
				this.setFailed( true );
	        	this.debugTrace("GET view hotel result status: " + this._http.getStatusCode());
				return; // or throw an exception?
			}

			// Save the last View Hotel URL.  We will need it in the Booking operation.
			// TODO: Issue, is this true?
			this.getGenerator().setLastUrl(fullViewGetUrl);

			// See if we can find some proof that a View Hotel page was returned.
			// TODO: We should check for the hotel name.
	    	if ( viewGetResponse.indexOf( "value=\"Book Hotel\"" ) > 0 ) {
	    		this.debugTrace( "Success - Response contains a View Hotel page!" );   		
	    	}

    		this.debugTrace("View Hotel GET response is: " + viewGetResponse.toString());

			// Update the counters and go on to the next view link.
			viewCount++;
    		linkSequenceNumber++;
			}
		
		this.debugTrace( "Viewed " + viewCount + " hotel(s)." );
		
		// TODO: Should we save the last View Hotel URL here to stage it for a
		// possible Book Hotel operation?
	
		this.setFailed( false );
	}
		
}
