package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class LoginOperation extends ScadrOperation {

	public LoginOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = "Login";
		this._operationIndex = ScadrGenerator.LOGIN;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		this.trace( this._operationName );
		Thread.sleep( 25 );
		this.setFailed( false );
	}
}
