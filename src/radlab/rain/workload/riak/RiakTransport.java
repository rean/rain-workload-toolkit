package radlab.rain.workload.riak;

import java.util.Set;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.cap.UnresolvedConflictException;
import com.basho.riak.client.convert.ConversionException;
import com.basho.riak.client.raw.pbc.PBClientConfig.Builder;

public class RiakTransport 
{
	// Use the protobuf port 8087 rather than the http port 8098
	public static final int DEFAULT_HTTP_PORT = 8098;
	public static final int DEFAULT_PROTOBUF_PORT = 8087;
	
	private IRiakClient _riak = null;
		
	private int _connectTimeout 			= 10000;
	private int _socketIdleTimeout			= 10000;
	//private int _maxConcurrentConnections 	= 100;
	private boolean _debug					= false;
	//private String _riakUrl					= "";
	
	public RiakTransport( String host, int port ) throws RiakException
	{
		Builder builder = new Builder();
		builder.withHost( host );
		builder.withPort( port );
		
		// HTTP builder
		//builder.withTimeout( _connectTimeout );
		//builder.withRiakPath( "/riak" );
		//builder.withMaxConnctions( 100 );
		
		// Protobuf builder
		builder.withConnectionTimeoutMillis( _connectTimeout );
		builder.withIdleConnectionTTLMillis( _socketIdleTimeout );
		
		this._riak = RiakFactory.newClient( builder.build() );
	}
		
	// Can be called repeatedly before a request is issued (ideally)
	public void configureRiakClient()
	{}
	
	public IRiakClient getRiakClient()
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
	
	public IRiakObject fetch( String bucket, String key ) throws UnresolvedConflictException, RiakRetryFailedException, ConversionException
	{
		this.configureRiakClient();
		Bucket bkt = this._riak.fetchBucket( bucket ).execute();
		return bkt.fetch( key ).execute();
	}
		
	public Iterable<String> listBucket( String bucket ) throws RiakException
	{
		this.configureRiakClient();
		Bucket bkt = this._riak.fetchBucket( bucket ).execute();
		return bkt.keys();
	}
	
	public Set<String> listBuckets() throws RiakException
	{
		this.configureRiakClient();
		return this._riak.listBuckets();
	}
	
	public IRiakObject store( String bucket, String key, byte[] value ) throws RiakRetryFailedException
	{
		this.configureRiakClient();
		Bucket bkt = this._riak.fetchBucket( bucket ).execute();
		// Nothing/null is returned by bkt.store().execute() we should just change this return type to void
		return bkt.store( key, value ).execute();
	}
			
	public void delete( String bucket, String key ) throws RiakException
	{
		Bucket bkt = this._riak.fetchBucket( bucket ).execute();
		bkt.delete( key ).execute();
	}
	
	public void dispose()
	{
		this._riak.shutdown();
	}
}
