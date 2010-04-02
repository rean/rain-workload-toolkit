package radlab.rain.workload.mapreduce;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import radlab.rain.Generator;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;

public class MapReduceGenerator extends Generator {
	
	private BufferedReader traceFile;

	public MapReduceGenerator(ScenarioTrack track) {
		super(track);
		String path = ((MapReduceScenarioTrack)track).getTraceFilePath();
		try {
			traceFile = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Deal with bad path 
		}
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
		String request = null;
		try {
		request = "mapreduce " + traceFile.readLine();
		} catch (IOException e) {
			// TODO Deal with error.
		} finally {
			
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
		return "";
	}

	public String getOutputPath() throws IOException {
		// TODO Auto-generated method stub
		return "";
	}

	public double getSIRatio() throws IOException {
		// TODO Auto-generated method stub
		return 0.0;
	}

	public double getOSRatio() throws IOException {
		// TODO Auto-generated method stub
		return 0.0;
	}

}
