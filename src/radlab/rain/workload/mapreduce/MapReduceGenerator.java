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
	private long nextThinkTime;
	private long maxInputSize;
	
	/*
	 * Other inputs from code.
	 * We can gather this stuff from track configuration json.
	 * int clusterSizeRaw,
	 * int clusterSizeWorkload,
	 * int inputPartitionSize,
	 * int inputPartitionCount, 
	 * String scriptDirPath,
	 * String hdfsInputDir,
	 * long totalDataPerReduce
	 */

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
		return nextThinkTime;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		// Read whole input file to determine max input size.
		// Close and reopen file to get back to the beginning
		// Is this the best way to get back to the beginning?
		
		String line;
		long inputSize;
		
		try {
			while((line = traceFile.readLine()) != null) {
				inputSize = Long.parseLong(line.split("\t")[4]);
				if (inputSize > maxInputSize)
					maxInputSize = inputSize;
			}
			
			traceFile.close();
			
			String path = ((MapReduceScenarioTrack)getTrack()).getTraceFilePath();
			traceFile = new BufferedReader(new FileReader(path));
		} catch (IOException E) {
			// TODO Deal with exception
		}
		
		// TODO Write data to HDFS
	}

	@Override
	public Operation nextRequest(int lastOperation) {
		// TODO Auto-generated method stub
		/*
		 * lastOperation could be line number of file we are on?
		 * This is not necessary, our line reader will keep track of that.
		 */
		String line = null;
		String request[] = null;
		String jobName;
		long inputSize;
		long shuffleSize;
		long outputSize;
		String command = "";
		try {
			line = traceFile.readLine();
			if (line == null) {
				// TODO Reached end of input
			} else {
				request = line.split("\t");
			}
		} catch (IOException e) {
			// TODO Deal with error.
		}
		
		jobName = request[0];
		inputSize = Long.parseLong(request[1]);
		shuffleSize = Long.parseLong(request[2]);
		outputSize = Long.parseLong(request[3]);
		nextThinkTime = Long.parseLong(request[4]);
		
		/* 
		 * Scaling from other code:
		 * 		input   = input   * clusterSizeWorkload / clusterSizeRaw;
		 *      shuffle = shuffle * clusterSizeWorkload / clusterSizeRaw;
		 *      output  = output  * clusterSizeWorkload / clusterSizeRaw; 
		 */
		
		// Copied the following. No idea what constants mean.
		if (inputSize   < 67108864) inputSize   = 67108864;
		if (shuffleSize < 1024    ) shuffleSize = 1024    ;
		if (outputSize  < 1024    ) outputSize  = 1024    ;
		
		return new MapReduceOperation(command, true, getScoreboard());
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
