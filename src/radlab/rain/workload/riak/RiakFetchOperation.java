package radlab.rain.workload.riak;

import com.basho.riak.client.response.FetchResponse;

import radlab.rain.IScoreboard;

public class RiakFetchOperation extends RiakOperation 
{
	public static final String NAME = "Fetch";
		
	public RiakFetchOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = RiakGenerator.FETCH;
	}

	@Override
	public void execute() throws Throwable 
	{
		FetchResponse response = this.doFetch( this._bucket, this._key );
		if( response != null )
			response.close();
		this.setFailed( false );
	}
}
