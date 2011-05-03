package radlab.rain.workload.riak;

import com.basho.riak.client.response.BucketResponse;
import com.basho.riak.client.response.FetchResponse;
import com.basho.riak.client.response.HttpResponse;
import com.basho.riak.client.response.StoreResponse;

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
	
	public FetchResponse doFetch( String bucket, String key ) throws Exception
	{
		FetchResponse response = this._riak.fetch( bucket, key );
		// Check whether the fetch failed because the object doesn't exist
		
		if( response.isSuccess() )
			return response;
		else throw new Exception( "Fetch failed." );
	}
	
	public FetchResponse doFetchStream( String bucket, String key ) throws Exception
	{
		FetchResponse response = this._riak.fetchStream( bucket, key );
		if( response.isSuccess() )
			return response;
		else throw new Exception( "Fetch stream failed" );
	}
	
	public StoreResponse doStore( String bucket, String key, String value ) throws Exception
	{
		StoreResponse response = this._riak.store( bucket, key, value );
		if( response.isSuccess() )
			return response;
		else throw new Exception( "Store string failed." );
	}
	
	public StoreResponse doStore( String bucket, String key, byte[] value ) throws Exception
	{
		StoreResponse response = this._riak.store( bucket, key, value );
		if( response.isSuccess() )
			return response;
		else throw new Exception( "Store bytes failed." );
	}
	
	public StoreResponse doUpdate( String bucket, String key, String value ) throws Exception
	{
		StoreResponse response = this._riak.update( bucket, key, value );
		if( response.isSuccess() )
			return response;
		else throw new Exception( "Update string failed." );
	}
	
	public StoreResponse doUpdate( String bucket, String key, byte[] value ) throws Exception
	{
		StoreResponse response = this._riak.update( bucket, key, value );
		if( response.isSuccess() )
			return response;
		else throw new Exception( "Update bytes failed." );
	}
	
	public HttpResponse doDelete( String bucket, String key ) throws Exception
	{
		HttpResponse response = this._riak.delete( bucket, key );
		if( response.isSuccess() )
		{
			return response;
		}
		else throw new Exception( "Delete key failed." );
	}
	
	public BucketResponse doListBucket( String bucket ) throws Exception
	{
		BucketResponse response = this._riak.listBucket( bucket );
		if( response.isSuccess() )
			return response;
		else throw new Exception( "List bucket failed." );
	}
}
