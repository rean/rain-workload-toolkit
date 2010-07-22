package radlab.rain.workload.booking;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

//import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

//import org.apache.http.Header;
//import org.apache.http.client.HttpClient;
//import org.apache.http.params.HttpParams;

import radlab.rain.IScoreboard;

/**
 * The Search Hotel operation allows the user to enter a search query
 * string containing all or part of a hotel name or city.  They can also
 * control the number of results to be shown in the Search Hotel Results
 * page.
 */
public class SearchHotelOperation extends BookingOperation
{
	public SearchHotelOperation( boolean interactive, IScoreboard scoreboard )
	{
		super( interactive, scoreboard );
		this._operationName = "Search Hotel";
		this._operationIndex = BookingGenerator.SEARCH_HOTEL;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		// Set this since we have not done a search yet.
		this.getGenerator().setFoundHotels(false);

		// Get the home page (Is this necessary?)
		String homePageUrl = this.getGenerator().homePageUrl;
		StringBuilder response = this._http.fetchUrl( homePageUrl );

		// Then get the search hotel page
		String searchGetUrl = this.getGenerator().searchHotelUrl;
		this.trace(this.getGenerator().getCurrentUser() + " GET  " + searchGetUrl);
		response = this._http.fetchUrl( searchGetUrl );
		//System.out.println( "Search page: " + response.toString() );

		if ( this._http.getStatusCode() != 200 ) {
			String errorMessage = "SearchHotel ERROR - GET search hotel criteria page status: " + this._http.getStatusCode();
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}

		this.traceUser(response);

		// Check that we really received a Search Hotels page in the response.
		String findHotelsButtonText = "value=\"Find Hotels\"";
    	if ( response.indexOf( findHotelsButtonText ) == -1 ) {
			String errorMessage = "SearchHotel ERROR - GET did not received a search hotel criteria page";
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
    	}

		// Get the finalUrl, which should be the location of the redirect (if everything worked)
		String searchPostUrl = this._http.getFinalUrl();
		this.trace( this.getGenerator().getCurrentUser() + " POST " + searchPostUrl );

		// Get the ViewState from the form so we can specify it later in the POST data.
		String viewState = this.getViewStateFromResponse( response );

		// Select a random page size, favoring the default of 5.
		int pageSizeArray[] = {5, 5, 5, 10, 20, 5, 10, 20, 5, 5};
		int randomPageSizeIndex = this._randomNumberGenerator.nextInt(pageSizeArray.length);
		String pageSize = new Integer(pageSizeArray[randomPageSizeIndex]).toString();

		// Select a random hotel search string.
        String hotelSearchArray[]       = {"", "W Hotel", "Marriott", "Hilton", "Doubletree",
                                           "Ritz", "Super 8", "No Tell Motel", "Conrad", "InterContinental",
                                           "Westin", "Mar", "foobar", "hyatt", "Embassy",
                                           "New York", "Suite", "Boston", "Manchester", "Portland",
                                           "Days", "London", "resort", "Sydney", "Phil" };

        boolean expectHotelFoundArray[] = {true, true, true, true, true,
                                           true, true, false, true, true,
                                           true, true, false, true, true,
                                           true, true, true, true, true,
                                           false, true, true, true, true};

		int randomHotelIndex = this._randomNumberGenerator.nextInt(hotelSearchArray.length);
		String hotelSearchString = hotelSearchArray[randomHotelIndex];

		// Create a POST request
		HttpPost searchHotelPost = new HttpPost( searchPostUrl );

        // Create the POST data.
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add( new BasicNameValuePair( "mainForm", "mainForm" ) );
		formParams.add( new BasicNameValuePair( "mainForm:searchString", hotelSearchString ) );
		formParams.add( new BasicNameValuePair( "mainForm:pageSize", pageSize ) );
		formParams.add( new BasicNameValuePair( "javax.faces.ViewState", viewState ) );
		formParams.add( new BasicNameValuePair( "processIds", "mainForm:findHotels, *" ) );
		formParams.add( new BasicNameValuePair( "mainForm:findHotels", "mainForm:findHotels" ) );
		formParams.add( new BasicNameValuePair( "ajaxSource", "mainForm:findHotels" ) );

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity( formParams, "UTF-8" );

		// Do the POST to search for hotels - NO content will be returned here.
		// In the response header there will be a "Spring-Redirect-URL" that
		// will you can HTTP GET for the results page.
		searchHotelPost.setEntity( entity );

        response = this._http.fetch( searchHotelPost );
		if ( this._http.getStatusCode() != 200 )
		{
			String errorMessage = "SearchHotel ERROR - POST search criteria status: " + this._http.getStatusCode();
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}

		// If the search worked there should be a Spring-Redirect-URL response header that
		// points at the results page
		Hashtable<String,String> headerMap = this._http.getHeaderMap();
		String searchResultsUrl = headerMap.get( "Spring-Redirect-URL" );
		if ( searchResultsUrl == null )
		{
			String errorMessage = "SearchHotel ERROR - POST search criteria did not return a Spring-Redirect-URL in the response headers";
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}
		this.debugTrace( "Spring-Redirect-URL: " + searchResultsUrl );

		// The Spring redirect URL does NOT have the host and port info so we need to prepend it with
		// http://<host>:<port>
		String fullQSearchResultsUrl = "http://" + this.getGenerator().getTrack().getTargetHostName() + ":" + this.getGenerator().getTrack().getTargetHostPort() + searchResultsUrl;
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + fullQSearchResultsUrl );

		// Do a GET for the hotel search results page.
		HttpGet hotelSearchResultsGet = new HttpGet( fullQSearchResultsUrl );
		response = this._http.fetch( hotelSearchResultsGet );
		//System.out.println( "Hotel results: " + response.toString() );

		if ( this._http.getStatusCode() != 200 )
		{
			String errorMessage = "SearchHotel ERROR - GET search result status: " + this._http.getStatusCode();
			this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
		}

		// Save the Search Results URL so that if the next operation chosen by the
		// generator is a Search Hotel Results operation it can do another HTTP GET
		// to retrieve the page contents.
		this.getGenerator().setLastUrl(fullQSearchResultsUrl);

		// Now we should have a search results page.  It should could be a page
		// containing a message that the search found no results.  Or it will be
		// a page containing a table of hotels that satisfied the search criteria.

    	// Check if the search did not find any hotels.
		String noHotelsFoundMessage = "No Hotels Found";
    	if ( response.indexOf( noHotelsFoundMessage ) > 0 ) {
    		// Remember that the last search hotel operation did not find any hotels.
    		this.getGenerator().setFoundHotels(false);

    		if (expectHotelFoundArray[randomHotelIndex] == false) {
        		this.debugTrace( "SearchHotel - Search for \"" + hotelSearchString + "\" found no hotels as expected." );
    			this.setFailed( false );
          	    return;
            }

    		// This isn't necessarily an error.  It's common for a search operation
    		// to return nothing depending on the criteria specified.  But since we use
    		// very limited search criteria, we know what should return a result and
    		// what should not.
    		this.debugTrace( "ERROR - Search for \"" + hotelSearchString + "\" found no hotels." );
			this.setFailed( false );
			return;
		}

		String noBookingsFoundMessage = "No Bookings Found";
    	if ( response.indexOf( noBookingsFoundMessage ) > 0 ) {
			this.debugTrace( "ERROR - The Search for \"" + hotelSearchString + "\" did not happen." );
            // Issue: is it worth throwing an exception for this?
			throw new Exception( "SearchHotel ERROR - Hotel search did not happen." );
		}

    	// Alternatively we could search for "Hotel Results".
    	//
    	// If there is one of these IDs present in the response, then the search
    	// worked and the results contain at least one hotel.
    	String hotelFoundIndicator = "hotels:hotels:0:viewHotelLink";
    	if ( response.indexOf( hotelFoundIndicator ) > 0 ) {
    		this.debugTrace( "Success - Search for \"" + hotelSearchString + "\" found hotels!" );
    		//this.trace(postResponse.toString());

    		// Remember that the last search hotel operation found one or more hotels.
    		this.getGenerator().setFoundHotels(true);
    	}

		// Load the static files (CSS/JS) associated with the Search page.
		// Note: This normally happens after the initial GET, but is being done
		// when this method completed
		if (!this.getGenerator().staticSearchPageUrlsLoaded) {
	    	loadStatics( this.getGenerator().staticSearchPageUrls );
			this.trace( this.getGenerator().staticSearchPageUrls );
			this.getGenerator().staticSearchPageUrlsLoaded = true;
		}

		this.setFailed( false );
	}

}