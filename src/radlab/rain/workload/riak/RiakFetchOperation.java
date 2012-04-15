package radlab.rain.workload.riak;

import com.basho.riak.client.IRiakObject;
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
		IRiakObject response = this.doFetch( this._bucket, this._key );
		if( response == null )
			throw new Exception( "Empty response for key: " + this._key );
			
		//@SuppressWarnings("unused")
		//byte[] value = response.getValue();
		//System.out.println( value );
	
		this.setFailed( false );
	}
}
