package radlab.rain.workload.cassandra;

import java.util.ArrayList;

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
		ArrayList<byte[]> results = this.doScan( this._key, this._maxScanRows );
		if( results.size() == 0 )
			throw new Exception( "Empty scan results for stsart key: " + this._key + " rows: " + this._maxScanRows );
		
		this.setFailed( false );
	}	
}
