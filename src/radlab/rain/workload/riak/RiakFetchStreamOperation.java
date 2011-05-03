package radlab.rain.workload.riak;

import com.basho.riak.client.response.FetchResponse;

import radlab.rain.IScoreboard;

public class RiakFetchStreamOperation extends RiakOperation 
{
	public static final String NAME = "FetchStream";
	
	public RiakFetchStreamOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationIndex = RiakGenerator.FETCH_STREAM;
		this._operationName = NAME;
	}

	@Override
	public void execute() throws Throwable 
	{
		FetchResponse response = this.doFetchStream( this._bucket, this._key );
		// Read from the stream
		
		if( response != null )
			response.close();
		this.setFailed( false );
	}
}
