package radlab.rain.workload.mapreduce;

import java.io.BufferedReader;
import java.io.IOException;

import radlab.rain.Generator;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;

public class MapReduceGenerator extends Generator {
	
	private BufferedReader inputPaths, outputPaths, siRatios, osRatios;

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
		String request;
		try {
		request = "mapreduce " + getNumReducers() + " " + getInputPath() +
				" " + getOutputPath() + " " + getSIRatio() + " " + getOSRatio();
		} finally {
			inputPaths.close();
			outputPaths.close();
			siRatios.close();
			osRatios.close();
		}
		return new MapReduceOperation(request, true, getScoreboard());
	}

	public int getNumReducers() {
		// TODO Auto-generated method stub
		// This value is calculated
		return 0;
	}

	public String getInputPath() throws IOException {
		// TODO Auto-generated method stub
		return inputPaths.readLine();
	}

	public String getOutputPath() throws IOException {
		// TODO Auto-generated method stub
		return outputPaths.readLine();
	}

	public double getSIRatio() throws IOException {
		// TODO Auto-generated method stub
		return Double.parseDouble(siRatios.readLine());
	}

	public double getOSRatio() throws IOException {
		// TODO Auto-generated method stub
		return Double.parseDouble(osRatios.readLine());
	}

}
