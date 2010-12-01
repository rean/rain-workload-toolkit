package radlab.rain.workload.scadr;

import radlab.rain.IScoreboard;

public class PostThoughtOperation extends ScadrOperation {

	public static final String NAME = "PostThought";
	
	public PostThoughtOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = ScadrGenerator.POST_THOUGHT;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		this.doPostThought();
		this.setFailed( false );
	}
}
