package radlab.rain.workload.mapreduce;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.Operation;

public class MapReduceOperation extends Operation {

	private String _request;

	public MapReduceOperation(String request, boolean interactive, IScoreboard scoreboard) {
		super(interactive, scoreboard);
		_request = request;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() throws Throwable {
		// TODO Auto-generated method stub
		System.out.println(_request);
	}

	@Override
	public void prepare(Generator generator) {
		this._generator = generator;
	}

}
