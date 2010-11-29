package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class PostThoughtOperation extends ScadrOperation {

	public PostThoughtOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = "CreateThought";
		this._operationIndex = ScadrGenerator.POST_THOUGHT;
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
