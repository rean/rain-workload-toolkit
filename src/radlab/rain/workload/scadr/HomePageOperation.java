package radlab.rain.workload.scadr;

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
		/*String authToken =*/ this.doHomePage();
		// Save the auth token in the generator if one is returned
		/*if( authToken == null || authToken.trim().length() == 0 )
			throw new Exception( "Authenticity token not found." );
		else this.getGenerator()._loginAuthToken = authToken;*/ 
				
		this.setFailed( false );
	}
}
