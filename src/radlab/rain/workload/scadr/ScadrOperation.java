package radlab.rain.workload.scadr;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.IScoreboard;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public class ScadrOperation extends Operation {

	// These references will be set by the Generator.
	protected HttpTransport _http;
		
	public ScadrOperation(boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() throws Throwable {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void prepare(Generator generator) {
		this._generator = generator;
		ScadrGenerator scadrGenerator = (ScadrGenerator) generator;
		
		this._http = scadrGenerator.getHttpTransport();
		LoadProfile currentLoadProfile = scadrGenerator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this._generatedDuringProfile = currentLoadProfile;
			//this._generatedDuring = currentLoadProfile._name;
	}

}
