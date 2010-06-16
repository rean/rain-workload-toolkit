package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class ReadThoughtstreamOperation extends ScadrOperation {

	public ReadThoughtstreamOperation(boolean interactive,
			IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = "ReadThoughtstream";
		this._operationIndex = ScadrGenerator.READ_THOUGHTSTREAM;
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
