package radlab.rain.workload.scadr;

import java.io.IOException;

import radlab.rain.IScoreboard;

public class HomePageOperation extends ScadrOperation 
{
	public static String NAME = "HomePage";
	
	public HomePageOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = ScadrGenerator.HOME_PAGE;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable 
	{
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._homeUrl );
		this.trace( this.getGenerator()._homeUrl );
		if( response.length() == 0 )
		{
			String errorMessage = "Home page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);
		}
		
		// Get the authenticity token for login and pass it to the generator
		String authToken = this.parseAuthTokenRegex( response ); 
		if( authToken.length() > 0 )
			this.getGenerator()._loginAuthToken = authToken;
		else throw new Exception( "Authenticity token not found." );
		
		this.loadStatics( this.getGenerator().homepageStatics );
		this.trace( this.getGenerator().homepageStatics );
				
		this.setFailed( false );
	}
}
