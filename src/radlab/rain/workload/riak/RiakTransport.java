package radlab.rain.workload.riak;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.basho.riak.client.RiakClient;
import com.basho.riak.client.RiakConfig;
import com.basho.riak.client.RiakObject;
import com.basho.riak.client.request.RequestMeta;
import com.basho.riak.client.response.BucketResponse;
import com.basho.riak.client.response.FetchResponse;
import com.basho.riak.client.response.HttpResponse;
import com.basho.riak.client.response.StoreResponse;

public class RiakTransport 
{
	public static final int DEFAULT_RIAK_PORT = 8098;
		
	private RiakClient _riak = null;
	
	private int _connectTimeout 			= 10000;
	private int _socketIdleTimeout			= 10000;
	private int _maxConcurrentConnections 	= 100;
	private boolean _debug					= false;
	private String _riakUrl					= "";
	
	public RiakTransport( String riakUrl )
	{
		this._riakUrl = riakUrl;
		RiakConfig config = new RiakConfig();
		config.setMaxConnections( this._maxConcurrentConnections );
		config.setUrl( this._riakUrl );
		config.setTimeout( this._connectTimeout );
		this._riak = new RiakClient( config );
	}
		
	// Can be called repeatedly before a request is issued (ideally)
	public void configureRiakClient()
	{
		// Reconfigure the connection and socket idle timeout
		HttpClient httpClient = this._riak.getHttpClient();
		
		HttpClientParams params = httpClient.getParams();
		// Via setParameter the connection timeout must be passed as a Long and the
		// socket idle timeout must be passed as an integer otherwise weird failures (ClassCastExceptions) 
		// occur in the depths of the http client
		params.setParameter( HttpClientParams.CONNECTION_MANAGER_TIMEOUT, new Long(this._connectTimeout) );
		params.setParameter( HttpClientParams.SO_TIMEOUT, new Integer(this._socketIdleTimeout) );
		httpClient.setParams( params );
		
	}
	
	public RiakClient getRiakClient()
	{
		return this._riak;
	}
	
	/**
	 * Returns the time to wait for future connections to be established.
	 * 
	 * @return  Time to wait for future connections to be established.
	 */
	public int getConnectTimeout()
	{
		return this._connectTimeout;
	}
	
	/**
	 * Sets the time to wait for future connections to be established. 
	 * 
	 * @param val   The new configuration.
	 */
	public void setConnectTimeout( int val )
	{
		this._connectTimeout = val;
	}
	
	/**
	 * Returns the time to wait between data packets.
	 * 
	 * @return  The time to wait between data packets.
	 */
	public int getSocketIdleTimeout()
	{
		return this._socketIdleTimeout;
	}
	
	/**
	 * Sets the time to wait between data packets. 
	 * 
	 * @param val   The new configuration.
	 */
	public void setSocketIdleTimeout( int val )
	{
		this._socketIdleTimeout = val;
	}
	
	public boolean getDebug() { return this._debug; }
	public void setDebug( boolean val ) { this._debug = val; }
	
	public FetchResponse fetch( String bucket, String key )
	{
		this.configureRiakClient();
		return this._riak.fetch( bucket, key );
	}
	
	public FetchResponse fetch( String bucket, String key, RequestMeta meta )
	{
		this.configureRiakClient();
		return this._riak.fetch( bucket, key, meta );
	}
	
	public FetchResponse fetchMetadata( String bucket, String key )
	{
		this.configureRiakClient();
		return this._riak.fetchMeta( bucket, key );
	}
	
	public FetchResponse fetchStream( String bucket, String key )
	{
		this.configureRiakClient();
		return this._riak.stream( bucket, key );
	}
	
	public FetchResponse fetchStream( String bucket, String key, RequestMeta meta )
	{
		this.configureRiakClient();
		return this._riak.stream( bucket, key, meta );
	}
	
	public FetchResponse fetchMetadata( String bucket, String key, RequestMeta meta )
	{	
		this.configureRiakClient();
		return this._riak.fetchMeta( bucket, key, meta );
	}

	public BucketResponse listBucket( String bucket )
	{
		this.configureRiakClient();
		return this._riak.listBucket( bucket );
	}
	
	public BucketResponse listBucket( String bucket, RequestMeta meta )
	{
		this.configureRiakClient();
		return this._riak.listBucket( bucket, meta );
	}
	
	public StoreResponse store( String bucket, String key, String value )
	{
		this.configureRiakClient();
		RiakObject o = new RiakObject( bucket, key );
		o.setValue( value );
		return this._riak.store( o );
	}
	
	public StoreResponse store( String bucket, String key, String value, RequestMeta meta )
	{
		this.configureRiakClient();
		RiakObject o = new RiakObject( bucket, key );
		o.setValue( value );
		return this._riak.store( o, meta );
	}
	
	public StoreResponse store( String bucket, String key, byte[] value )
	{
		this.configureRiakClient();
		RiakObject o = new RiakObject( bucket, key );
		o.setValue( value );
		return this._riak.store( o );
	}
	
	public StoreResponse store( String bucket, String key, byte[] value, RequestMeta meta )
	{
		this.configureRiakClient();
		RiakObject o = new RiakObject( bucket, key );
		o.setValue( value );
		return this._riak.store( o, meta );
	}
	
	public StoreResponse update( String bucket, String key, String value ) throws Exception
	{
		this.configureRiakClient();
		FetchResponse response = this._riak.fetch( bucket, key );
		if( response.isSuccess() )
		{
			RiakObject o = response.getObject();
			o.setValue( value );
			return this._riak.store( o );
		}
		else throw new Exception( "Update failed on fetch" );
	}
	
	public StoreResponse update( String bucket, String key, byte[] value ) throws Exception
	{
		this.configureRiakClient();
		FetchResponse response = this._riak.fetch( bucket, key );
		if( response.isSuccess() )
		{
			RiakObject o = response.getObject();
			o.setValue( value );
			return this._riak.store( o );
		}
		else 
		{
			// If the key doesn't exist, just write it rather than throwing an exception
			RiakObject o = new RiakObject( bucket, key );
			o.setValue( value );
			return this._riak.store( o );
		}	
	}
		
	public HttpResponse delete( String bucket, String key )
	{
		this.configureRiakClient();
		return this._riak.delete( bucket, key );
	}
	
	public HttpResponse delete( String bucket, String key, RequestMeta meta )
	{
		this.configureRiakClient();
		return this._riak.delete( bucket, key, meta );
	}
	
	public void dispose()
	{}
}
