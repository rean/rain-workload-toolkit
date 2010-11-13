package radlab.rain.workload.httptest;

import java.io.IOException;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public class FixedUrlOperation extends Operation 
{
	// These references will be set by the Generator.
	protected HttpTransport _http;
		
	public static String NAME = "FixedUrl";
	
	public FixedUrlOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = FixedUrlGenerator.PING_URL;
	}

	/**
	 * Returns the Generator that created this operation.
	 * 
	 * @return      The Generator that created this operation.
	 */
	public FixedUrlGenerator getGenerator()
	{
		return (FixedUrlGenerator) this._generator;
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
		if( response.length() == 0 )
		{
			String errorMessage = "Url GET ERROR - Received an empty response";
			throw new IOException (errorMessage);
		}
		
		// Once we get here mark the operation as successful
		this.setFailed( false );
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		FixedUrlGenerator specificUrlGenerator = (FixedUrlGenerator) generator;
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this._generatedDuringProfile = currentLoadProfile;
		
		this._http = specificUrlGenerator.getHttpTransport();
	}
}
