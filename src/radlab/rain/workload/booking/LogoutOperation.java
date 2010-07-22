package radlab.rain.workload.booking;

import java.io.IOException;

import radlab.rain.IScoreboard;

/**
 * The LogoutOperation logs out the current user.  After successfully logging
 * out the logoutSuccess page is displayed.<br />
 *
 */
public class LogoutOperation extends BookingOperation 
{
	
	public LogoutOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Logout";
		this._operationIndex = BookingGenerator.LOGOUT;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
        // GET a main page to see if there is no logout hyperlink present.  If true we 
		// can skip this Logout operation.
		String searchGetUrl = this.getGenerator().searchHotelUrl;
		StringBuilder response = this._http.fetchUrl( searchGetUrl );

		this.traceUser(response);

		if ( response.indexOf("spring/logout") == -1) {
            this.debugTrace("Skipped logout, no user logged in.");
			this.setFailed(false);
			return;
		}

		this.debugTrace(this.getGenerator().getCurrentUser() + " is logged in.  Will be logged out.");

		response = this._http.fetchUrl( this.getGenerator().logoutUrl );
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + this.getGenerator().logoutUrl );
	
		if ( response.length() == 0 ) {
			String errorMessage = "Logout ERROR - Received empty response";
			this.debugTrace(errorMessage);
			throw new IOException(errorMessage);
		}
			
		// Check that the user was successfully logged out.
		String successfulLogoutMessage = "You have successfully logged out.";
    	if ( response.indexOf( successfulLogoutMessage ) < 0 ) {
    		String errorMessage = "Logout ERROR - Logout failed for user " + this.getGenerator().getCurrentUser() + ".";
    		this.debugTrace(errorMessage);
			throw new Exception(errorMessage);
    	}
    	else {
        	this.getGenerator().setCurrentUser(null);
    	}

    	// Force reload of the static pages after a user logout to approximate
    	// Web browser caching, the theory being that each new user in the real
    	// world comes from a different address.
    	this.getGenerator().staticHomePageUrlsLoaded = false;   	
    	this.getGenerator().staticLoginPageUrlsLoaded = false;   	
    	this.getGenerator().staticSearchPageUrlsLoaded = false;   	

    	this.setFailed( false );
	}
	
}
