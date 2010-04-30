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
	private int counter;
	
	/*
	 * Other inputs from code.
	 * We can gather this stuff from track configuration json.
	 */
	//private int clusterSizeRaw;
	//private int clusterSizeWorkload;
	//private int inputPartitionSize;
	//private int inputPartitionCount; 
	//private String hdfsInputDir;
	private long totalDataPerReduce = 100; // Dummy value.
	 

	public MapReduceGenerator(ScenarioTrack track) {
		super(track);
		counter = 0;
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
	
	private void openConfigFile() {
		String path = ((MapReduceScenarioTrack)getTrack()).getTraceFilePath();
		try {
			traceFile = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Deal with bad path 
		}
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		// Read whole input file to determine max input size.
		// Close and reopen file to get back to the beginning
		
		String line;
		long inputSize;
		
		openConfigFile();
		
		try {
			while((line = traceFile.readLine()) != null) {
				inputSize = Long.parseLong(line.split("\t")[4]);
				if (inputSize > maxInputSize)
					maxInputSize = inputSize;
			}
			
			traceFile.close();
			
			openConfigFile();
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
		 * This is not necessary, our file reader will keep track of that.
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
		
		// This is where some stuff from Yanpei's code goes.
		// I haven't copied it yet because I don't understand it.
		// It deals with an ArrayList inputPartitionSample and sets up
		// the inputPath.
		
		String inputPath = "";
		
		String outputPath = "workGenOut-job" + counter;
				
		double SIRatio = ((double) shuffleSize) / ((double) inputSize);
		double OSRatio = ((double) outputSize) / ((double) shuffleSize);
		
		long numReduces = -1;

		if (totalDataPerReduce > 0) {
			numReduces = Math.round((shuffleSize + outputSize) / ((double) totalDataPerReduce));
			if (numReduces < 1) numReduces = 1;
			
			command = "bin/hadoop jar WorkGen.jar org.apache.hadoop.examples.WorkGen -conf conf/workGenKeyValue_conf.xsl " +
			          "-r " + numReduces + " " + inputPath + " " + outputPath + " " + SIRatio + " " + OSRatio +
			          " >> output/job-" + counter + ".txt 2>> output/job-" + counter + ".txt\n";
		} else {
			command = "bin/hadoop jar WorkGen.jar org.apache.hadoop.examples.WorkGen -conf conf/workGenKeyValue_conf.xsl " +
			          inputPath + " " + outputPath + " " + SIRatio + " " + OSRatio +
			          " >> output/job-" + counter + ".txt 2>> output/job-" + counter + ".txt\n";
		}
		counter ++;
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
