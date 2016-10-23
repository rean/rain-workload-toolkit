package radlab.rain.workload.ukweb;

import radlab.rain.Operation;
import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.util.HttpTransport;

public abstract class WebOperation extends Operation
{
	// These references will be set by the Generator.
	protected HttpTransport _http;
	
	/**
	 * Returns the WebOperationGenerator that created this operation.
	 * 
	 * @return      The WebGenerator that created this operation.
	 */
	public WebGenerator getGenerator()
	{
		return (WebGenerator) this._generator;
	}
	
	public WebOperation( boolean interactive, IScoreboard scoreboard )
	{
		super( interactive, scoreboard );
	}
	
	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
		
		WebGenerator webGenerator = (WebGenerator) generator;
		this._http = webGenerator.getHttpTransport();
	
	}
	
	@Override
	public void cleanup()
	{
		
	}
}
