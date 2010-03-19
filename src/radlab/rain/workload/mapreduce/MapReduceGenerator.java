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
		return sampleInterJobArrivalTime();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public Operation nextRequest(int lastOperation) {
		// TODO Auto-generated method stub
		/*
		 * sample all the various pieces of data.
		 * create new operation that takes in those parameters.
		 */
		return null;
	}
	
	private long sampleInterJobArrivalTime() {
		// TODO Fill me in.
		return 0;
	}
	
	private void sampleJobData() {
		// TODO Figure out return type.
		// TODO Fill me in.
		// Break into multiple methods for each data piece.
	}
	
	
	private void sample(/*generic data structure*/) {
		
	}
	
	private String sampleJobName() {
		// TODO Fill me in.
		return "";
	}

}
