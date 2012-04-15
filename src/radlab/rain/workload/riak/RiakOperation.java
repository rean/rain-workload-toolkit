package radlab.rain.workload.riak;

import com.basho.riak.client.IRiakObject;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

public abstract class RiakOperation extends Operation 
{
	protected String _bucket = "";
	protected String _key = "";
	protected byte[] _value = null;
	protected RiakTransport _riak = null;
	
	
	public RiakOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
	}

	@Override
	public void cleanup() 
	{
		this._bucket = "";
		this._key = "";
		this._value = null;
		this._bucket = "";
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		RiakGenerator riakGenerator = (RiakGenerator) generator;
		
		this._riak = riakGenerator.getRiakTransport();
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
		
		// Bucket to use
		this._bucket = riakGenerator._bucket;
	}
	
	public IRiakObject doFetch( String bucket, String key ) throws Exception
	{
		IRiakObject response = this._riak.fetch( bucket, key );
		// Check whether the fetch failed because the object doesn't exist
		if( response != null )
			return response;
		else throw new Exception( "Fetch failed." );
	}
		
	public IRiakObject doStore( String bucket, String key, byte[] value ) throws Exception
	{
		IRiakObject response = this._riak.store( bucket, key, value );
		//if( response != null )
		//	return response;
		//else throw new Exception( "Store bytes failed." );
		return response;
	}
	
	public void doDelete( String bucket, String key ) throws Exception
	{
		this._riak.delete( bucket, key );
	}
	
	public Iterable<String> doListBucket( String bucket ) throws Exception
	{
		Iterable<String> response = this._riak.listBucket( bucket );
		if( response != null )
			return response;
		else throw new Exception( "List bucket failed." );
	}
}
