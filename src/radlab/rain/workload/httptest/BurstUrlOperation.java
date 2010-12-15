package radlab.rain.workload.httptest;

import java.io.IOException;

import org.apache.http.HttpStatus;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public class BurstUrlOperation extends Operation 
{
	// These references will be set by the Generator.
	protected HttpTransport _http;
	private int _burstSize = 1;
	
	public static String NAME = "BurstUrl";
		
	public BurstUrlOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = BurstUrlGenerator.PING_URL;
	}

	/**
	 * Returns the Generator that created this operation.
	 * 
	 * @return      The Generator that created this operation.
	 */
	public BurstUrlGenerator getGenerator()
	{
		return (BurstUrlGenerator) this._generator;
	}
	
	public void setName( String val )
	{
		this._operationName = val;
	}
	
	@Override
	public void cleanup() 
	{
		
	}

	@Override
	public void execute() throws Throwable 
	{
		// Fetch the base url
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._baseUrl );
		
		this.trace( this.getGenerator()._baseUrl );
		if( response.length() == 0 || this._http.getStatusCode() != HttpStatus.SC_OK )
		{
			String errorMessage = "Url GET ERROR - Received an empty/failed response";
			throw new IOException (errorMessage);
		}
		
		// Do burst for base url/1 to /burst size
		for( int i = 0; i < this._burstSize; i++ )
		{
			String url = this.getGenerator()._baseUrl + "/" + (i+1);
			response = this._http.fetchUrl( url );
			
			this.trace( url );
			if( response.length() == 0 || this._http.getStatusCode() != HttpStatus.SC_OK )
			{
				String errorMessage = "Url GET ERROR - Received an empty response";
				throw new IOException (errorMessage);
			}	
		}
		
		// Once we get here mark the operation as successful
		this.setFailed( false );
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		BurstUrlGenerator specificUrlGenerator = (BurstUrlGenerator) generator;
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
		
		this._http = specificUrlGenerator.getHttpTransport();
		// Set the burst count
		this._burstSize = specificUrlGenerator.getBurstSize();
	}

}
