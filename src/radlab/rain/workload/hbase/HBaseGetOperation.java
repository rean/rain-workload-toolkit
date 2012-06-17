package radlab.rain.workload.hbase;

import radlab.rain.IScoreboard;

public class HBaseGetOperation extends HBaseOperation 
{
	public static String NAME = "Get";
	
	public HBaseGetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = HBaseGenerator.READ;
	}
	
	@Override
	public void execute() throws Throwable
	{
		byte[] result = this.doGet( this._key );
		if( result == null || result.length == 0 )
			throw new Exception( "Empty value for key: " + this._key );
		
		this.setFailed( false );
	}
}
