package radlab.rain.workload.cassandra;

import radlab.rain.IScoreboard;

public class CassandraPutOperation extends CassandraOperation 
{
	public static String NAME = "Put";
	
	public CassandraPutOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = CassandraGenerator.WRITE;	
	}

	@Override
	public void execute() throws Throwable 
	{
		this.doPut( this._key, this._value );
		this.setFailed( false );
	}
}
