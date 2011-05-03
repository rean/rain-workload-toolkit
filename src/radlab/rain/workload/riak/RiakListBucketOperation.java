package radlab.rain.workload.riak;

import com.basho.riak.client.response.BucketResponse;

import radlab.rain.IScoreboard;

public class RiakListBucketOperation extends RiakOperation 
{
	public static final String NAME = "Delete";
	
	public RiakListBucketOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationIndex = RiakGenerator.LIST_BUCKET;
		this._operationName = NAME;
	}

	@Override
	public void execute() throws Throwable 
	{
		BucketResponse response = this.doListBucket( this._bucket );
		if( response != null )
			response.close();
		this.setFailed( false );
	}
}
