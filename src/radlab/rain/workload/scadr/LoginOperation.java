package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;
import java.util.Random;

public class LoginOperation extends ScadrOperation {

	//private Random _random = new Random();
	
	public LoginOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = "Login";
		this._operationIndex = ScadrGenerator.LOGIN;
		this._mustBeSync = true;
	}

	@Override
	public void execute() throws Throwable
	{
		/*this.trace( this._operationName );
		double rndVal = this._random.nextDouble();
		if( rndVal <= 0.2 )
			throw new Exception( "Just testing..." );
		else Thread.sleep( 25 );
		this.setFailed( false );*/
	}
}
