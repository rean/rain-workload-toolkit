package radlab.rain.workload.nginx;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;

public class GetOperation extends WebOperation
{

	public static String NAME = "Get"; 
	
	public GetOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super(interactive, scoreboard);
		this._operationName = GetOperation.NAME;
		this._operationIndex = WebGenerator.GET;
	}

	@Override
	public void execute() throws Throwable
	{
		this.trace( this._operationName );
		//Thread.sleep( 53 );
		
		Generator generator = this.getGenerator();
		StringBuilder url = new StringBuilder();
		url.append("http://");
		url.append(generator.getTrack().getTargetHostName());
		url.append(":");
		url.append(generator.getTrack().getTargetHostPort());
		url.append("/index.html");
		this.getGenerator().getHttpTransport().fetchUrl(url.toString());
		this.setFailed( false );
	}
	
	@Override
	public void prepare(Generator g)
	{
		super.prepare( g );
	}
}
