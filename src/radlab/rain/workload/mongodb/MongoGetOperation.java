package radlab.rain.workload.mongodb;

import radlab.rain.IScoreboard;

public class MongoGetOperation extends MongoOperation 
{
	public static String NAME = "Get";
		
	public MongoGetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = MongoGenerator.READ;
	}
	
	@Override
	public void execute() throws Throwable
	{
		this.doGet( this._key );
		this.setFailed( false );
	}
}
