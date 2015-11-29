package radlab.rain.workload.cassandra;

import java.util.List;

import radlab.rain.IScoreboard;

public class CassandraScanOperation extends CassandraOperation 
{
	public static String NAME = "Scan";
	
	public CassandraScanOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = CassandraGenerator.SCAN;

	}

	@Override
	public void execute() throws Throwable
	{
		List<byte[]> results = this.doScan( this._key, this._maxScanRows );
		if( results.size() == 0 )
		{
			//throw new Exception( "Empty scan results for stsart key: " + this._key + " rows: " + this._maxScanRows );
			this.getLogger().warning(NAME + "(" + this._key + ", " + this._maxScanRows + ") returned an empty result");
		}
		
		this.setFailed( false );
	}	
}
