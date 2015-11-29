package radlab.rain.workload.cassandra;

import radlab.rain.IScoreboard;

public class CassandraGetOperation extends CassandraOperation 
{
	public static String NAME = "Get";
		
	public CassandraGetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = CassandraGenerator.READ;
	}

	@Override
	public void execute() throws Throwable
	{
		byte[] result = this.doGet( this._key );
		if( result == null || result.length == 0 )
		{
			//throw new Exception( "Empty value for key: " + this._key );
			this.getLogger().warning(NAME + "(" + this._key + ") returned an empty value");
		}
		
		this.setFailed( false );
	}
}
