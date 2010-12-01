package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class LogoutOperation extends ScadrOperation 
{
	public static final String NAME = "Logout";
	
	public LogoutOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = ScadrGenerator.LOGOUT;
	}

	@Override
	public void execute() throws Throwable 
	{
		this.doLogout();
		this.setFailed( false );
	}
}
