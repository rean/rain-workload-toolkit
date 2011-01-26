package radlab.rain.workload.booking;

import java.io.IOException;

import radlab.rain.IScoreboard;


/**
 * The Home Page operation displays the Booking home page.
 */
public class HomePageOperation extends BookingOperation 
{
	public static String NAME = "Home Page"; 
	
	public HomePageOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = NAME; 
		this._operationIndex = BookingGenerator.HOME_PAGE;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = this._http.fetchUrl( this.getGenerator().homePageUrl );
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + this.getGenerator().homePageUrl );
		if ( response.length() == 0 )
		{
			String errorMessage = "HomePage ERROR - Received an empty response";
			this.debugTrace(errorMessage);
			throw new IOException (errorMessage);
		}
		
		// Load the static files (CSS/JS) associated with the Home Page.
		if (!this.getGenerator().staticHomePageUrlsLoaded) {
			loadStatics( this.getGenerator().staticHomePageUrls );
			this.trace( this.getGenerator().staticHomePageUrls );
			this.getGenerator().staticHomePageUrlsLoaded = true;
		}
		
		this.setFailed( false );
	}
	
}