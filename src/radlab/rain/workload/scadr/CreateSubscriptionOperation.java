package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class CreateSubscriptionOperation extends ScadrOperation 
{
	public static String NAME = "CreateSubscription";
	public CreateSubscriptionOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = ScadrGenerator.CREATE_SUBSCRIPTION;
	}
	
	@Override
	public void execute() throws Throwable
	{
		// Do a subscription - create the user if neccessary
		// The user name is based on the thread id so if the
		// user currently doesn't exist, it will at some point
		// during the run i.e., whenever the thread with that id
		// gets a chance to run
		this.doSubscribe( true );
		this.setFailed( false );
	}
}
