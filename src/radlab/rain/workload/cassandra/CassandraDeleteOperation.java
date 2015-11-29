package radlab.rain.workload.cassandra;

import radlab.rain.IScoreboard;

public class CassandraDeleteOperation extends CassandraOperation 
{
	public static String NAME = "Delete";
		
	public CassandraDeleteOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = CassandraGenerator.DELETE;
	}

	@Override
	public void execute() throws Throwable
	{
		this.doDelete( this._key );

		this.setFailed( false );
	}
}
