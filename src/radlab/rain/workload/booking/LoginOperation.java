package radlab.rain.workload.booking;

import java.io.IOException;

import radlab.rain.IScoreboard;

/**
 * The LoginOperation is an operation that logs in as a random user. If the
 * user executing this operation is already logged in, the user is first
 * logged out. After logging in, the user is redirected to the home page.<br />
 *
 */
public class LoginOperation extends BookingOperation 
{
	
	public LoginOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Login";
		this._operationIndex = BookingGenerator.LOGIN;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
        // The workflow requested a Login Operation.  First check if there is a user
		// currently logged in.  If so, then log that user out. 
		// TODO: Move common logout code into a single method.
		if (this.getGenerator().getCurrentUser() != null) {
			this.debugTrace("Workflow requested a Login operation, but " + this.getGenerator().getCurrentUser() + " is logged in.  The user will be logged out.");

			this.trace( this.getGenerator().getCurrentUser() + " GET  " + this.getGenerator().logoutUrl );	
			StringBuilder logoutResponse = this._http.fetchUrl( this.getGenerator().logoutUrl );
			if ( logoutResponse.length() == 0 )
			{
				throw new IOException( "Logout received an empty response" );
			}

			// Check that the user was successfully logged out.
			String successfulLogoutMessage = "You have successfully logged out.";
	    	if ( logoutResponse.indexOf( successfulLogoutMessage ) < 0 ) {
	    		this.debugTrace("ERROR - Logout failed for user " + this.getGenerator().getCurrentUser() + ".");
	    		// TODO: What else to do in this case?
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
		}
   	
        // Now proceed with the new Login operation.		
		StringBuilder response = this._http.fetchUrl( this.getGenerator().loginUrl );
		this.trace( this.getGenerator().getCurrentUser() + " GET  " + this.getGenerator().loginUrl );
		if ( response.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}

		// This is not really needed anymore now that we log out the current user.
		this.traceUser(response);

		// This will pick a random username and do a POST to the loginProcess form.  The
		// lastUrl field will be set to the response page.  If the login fails, this 
		// method will throw an exception.  It return the username, but we don't really
		// need it at this point.  Also in this case we will allow the login process
		// to load the static URLs.
		this.processLoginForm(true);

		this.setFailed( false );
	}
	
}
