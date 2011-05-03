package radlab.rain.workload.mongodb;

import radlab.rain.IScoreboard;

public class MongoPutOperation extends MongoOperation 
{
	public static String NAME = "Put";
	
	public MongoPutOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = MongoGenerator.WRITE;
	}
	
	@Override
	public void execute() throws Throwable 
	{
		this.doPut( this._key, this._value );
		this.setFailed( false );
	}
}
