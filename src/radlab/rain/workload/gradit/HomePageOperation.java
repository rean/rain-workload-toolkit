package radlab.rain.workload.gradit;

import radlab.rain.IScoreboard;

public class HomePageOperation extends GraditOperation 
{
	public static String NAME = "HomePage";
	
	public HomePageOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = GraditGenerator.HOME_PAGE;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable 
	{
		// Gradit doesn't use authenticity tokens.
		//String authToken = 
		this.doHomePage();
		/*
		// Save the auth token in the generator if one is returned
		if( authToken == null || authToken.trim().length() == 0 )
			throw new Exception( "Authenticity token not found." );
		else this.getGenerator()._homePageAuthToken = authToken; 
		*/		
		this.setFailed( false );
	}
}
