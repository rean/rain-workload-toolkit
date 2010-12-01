package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class LoginOperation extends ScadrOperation {

	public static final String NAME = "Login";
	
	public LoginOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = ScadrGenerator.LOGIN;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		boolean result = this.doLogin();
		if( !result )
		{
			// Try creating the user first and then re-try the log in
			this.doCreateUser();
			result = this.doLogin();
		}
		
		// If we're not able to log in then throw an exception
		if( !result )
			throw new Exception( "Unable to log in." );
		
		this.setFailed( false );
	}
}
