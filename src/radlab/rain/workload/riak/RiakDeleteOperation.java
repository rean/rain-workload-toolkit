package radlab.rain.workload.riak;

import com.basho.riak.client.response.HttpResponse;

import radlab.rain.IScoreboard;

public class RiakDeleteOperation extends RiakOperation 
{
	public static final String NAME = "Delete";
	
	public RiakDeleteOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationIndex = RiakGenerator.DELETE;
		this._operationName = NAME;
	}

	@Override
	public void execute() throws Throwable 
	{
		HttpResponse response = this.doDelete( this._bucket, this._key );
		if( response != null )
			response.close();
		this.setFailed( false );
	}
}
