package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class CreateSubscriptionOperation extends ScadrOperation {

	public CreateSubscriptionOperation(boolean interactive,
			IScoreboard scoreboard) {
		super(interactive, scoreboard);

		this._operationName = "CreateSubscription";
		this._operationIndex = ScadrGenerator.CREATE_SUBSCRIPTION;
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
