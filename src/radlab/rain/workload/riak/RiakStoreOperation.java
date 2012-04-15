package radlab.rain.workload.riak;

import com.basho.riak.client.IRiakObject;
import radlab.rain.IScoreboard;

public class RiakStoreOperation extends RiakOperation 
{
	public static final String NAME = "Store";
	
	public RiakStoreOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = RiakGenerator.STORE;
	}

	@Override
	public void execute() throws Throwable 
	{
		IRiakObject response = null;
		response = this.doStore( this._bucket, this._key, this._value );
		//if( response == null )
		//	throw new Exception( "Empty response for store value of key: " + this._key );
		this.setFailed( false );
	}
}
