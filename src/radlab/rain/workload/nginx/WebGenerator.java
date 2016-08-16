package radlab.rain.workload.nginx;

import radlab.rain.Generator;
import radlab.rain.Operation;
import radlab.rain.LoadProfile;
import radlab.rain.ObjectPool;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;

public class WebGenerator extends Generator
{
	// Operation indices used in the mix matrix.
	public static final int GET = 0;
		
	private HttpTransport _http;
	
	/**
	 * Initialize a <code>WebGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public WebGenerator( ScenarioTrack track )
	{
		super( track );
	}
	
	@Override
	public long getThinkTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCycleTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Operation nextRequest(int lastOperation)
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		this._latestLoadProfile = currentLoad;
		
		// Always return a GET operation
		return getOperation(WebGenerator.GET);
	}

	@Override
	public void initialize()
	{
		this._http = new HttpTransport();

	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}
	
	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex   The type of operation to instantiate.
	 * @return          A prepared operation.
	 */
	public Operation getOperation( int opIndex )
	{
		switch( opIndex )
		{
			case GET: return createGetOperation();
			default: return null;
		}
	}
	
	public WebOperation createGetOperation()
	{
		GetOperation op = null;
		ObjectPool pool = this.getTrack().getObjectPool();
		op = (GetOperation) pool.rentObject( GetOperation.NAME );
		// Nothing available in pool so get an instance the tried and true way.
		if( op == null )
			op = new GetOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
}
