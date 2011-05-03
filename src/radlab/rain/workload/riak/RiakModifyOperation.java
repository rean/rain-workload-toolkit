package radlab.rain.workload.riak;

import com.basho.riak.client.response.StoreResponse;

import radlab.rain.IScoreboard;

public class RiakModifyOperation extends RiakOperation 
{
	public static final String NAME = "Modify";
	
	public RiakModifyOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationIndex = RiakGenerator.MODIFY;
		this._operationName = NAME;
	}

	@Override
	public void execute() throws Throwable 
	{
		StoreResponse response = null;
		response = this.doUpdate( this._bucket, this._key, this._value );
		if( response != null )
			response.close();
		this.setFailed( false );
	}
}
