package radlab.rain.workload.riak;

import radlab.rain.IScoreboard;

public class RiakDeleteOperation extends RiakOperation 
{
	public static final String NAME = "Delete";
	
	public RiakDeleteOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationIndex = RiakGenerator.DELETE;
		this._operationName = NAME;
	}

	@Override
	public void execute() throws Throwable 
	{
		this.doDelete( this._bucket, this._key );
		this.setFailed( false );
	}
}
