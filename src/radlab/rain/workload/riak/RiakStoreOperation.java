package radlab.rain.workload.riak;

import com.basho.riak.client.response.StoreResponse;

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
		StoreResponse response = null;
		response = this.doStore( this._bucket, this._key, this._value );
		if( response != null )
			response.close();
		this.setFailed( false );
	}
}
