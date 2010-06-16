package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class CreateThoughtOperation extends ScadrOperation {

	public CreateThoughtOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = "CreateThought";
		this._operationIndex = ScadrGenerator.CREATE_THOUGHT;
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
