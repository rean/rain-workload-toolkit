package radlab.rain.workload.mapreduce;

import radlab.rain.Generator;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;

public class MapReduceGenerator extends Generator {

	public MapReduceGenerator(ScenarioTrack track) {
		super(track);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public long getCycleTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getThinkTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public Operation nextRequest(int lastOperation) {
		// TODO Auto-generated method stub
		/*
		 * lastOperation could be line number of file we are on?
		 * This is not necessary, our line reader will keep track of that.
		 */
		
		String request = "mapreduce " + getNumReducers() + " " + getInputPath() +
				" " + getOutputPath() + " " + getSIRatio() + " " + getOSRatio();
		
		return new MapReduceOperation(request, true, getScoreboard());
	}

	public int getNumReducers() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getInputPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutputPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getSIRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getOSRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

}
