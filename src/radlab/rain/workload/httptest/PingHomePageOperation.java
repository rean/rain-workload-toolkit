package radlab.rain.workload.httptest;

import java.io.IOException;

import radlab.rain.IScoreboard;

public class PingHomePageOperation extends HttpTestOperation 
{
	public static String NAME = "PingHome";
	
	public PingHomePageOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = HttpTestGenerator.PING_HOMEPAGE;
	}

	@Override
	public void execute() throws Throwable 
	{
		// Fetch the base url
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._baseUrl );
		
		this.trace( this.getGenerator()._baseUrl );
		if( response.length() == 0 )
		{
			String errorMessage = "Home page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);
		}
		
		// Once we get here mark the operation as successful
		this.setFailed( false );
	}
}
