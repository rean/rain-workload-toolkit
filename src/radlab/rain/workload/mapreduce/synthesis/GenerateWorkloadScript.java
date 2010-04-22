package radlab.rain.workload.mapreduce.synthesis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

public class GenerateWorkloadScript {  

    public static void parseFileArrayList(String path, ArrayList<ArrayList<String>> data) throws Exception {
	
	BufferedReader input = new BufferedReader(new FileReader(path));
	String s;
	String[] array;
	int rowIndex = 0;
	int columnIndex = 0;
	while (true) {
	    if (!input.ready()) break;
	    s = input.readLine();
	    //System.out.println(s);
	    array = s.split("\t");
	    try {
		columnIndex = 0;
		while (columnIndex < array.length) {
		    if (columnIndex == 0) {
			data.add(rowIndex,new ArrayList<String>());
		    }
		    String value = array[columnIndex];
		    data.get(rowIndex).add(value);
		    columnIndex++;
		}
		//if (rowIndex==0) System.out.println(data.get(rowIndex).size());
		rowIndex++;
	    } catch (Exception e) {
		
	    }
	}
	
    }


    public static void parseFileHashMap(String path, HashMap<String, ArrayList<String>> data) throws Exception {

        BufferedReader input = new BufferedReader(new FileReader(path));
        String s;
        String[] array;
        int rowIndex = 0;
        int columnIndex = 0;
        while (true) {
            if (!input.ready()) break;
            s = input.readLine();
            //System.out.println(s);
            array = s.split("\t");
            try {
                columnIndex = 0;
                while (columnIndex < array.length) {
                    if (columnIndex == 0) {
                        data.put(array[0],new ArrayList<String>());
                    }
                    String value = array[columnIndex];
                    data.get(array[0]).add(value);
                    columnIndex++;
                }
                //if (rowIndex==0) System.out.println(data.get(rowIndex).size());
                rowIndex++;
            } catch (Exception e) {

            }
        }

    }



    public static void generateScript(ArrayList<ArrayList<String>> interArrival,
				      ArrayList<ArrayList<String>> jobNames    ,
				      ArrayList<ArrayList<String>> inputSizes  ,
				      ArrayList<ArrayList<String>> shuffleSizes,
				      ArrayList<ArrayList<String>> outputSizes ,
				      int clusterSizeRaw, // Our cluster size
				      int clusterSizeWorkload, // Cluster used to generate data
				      int inputPartitionSize, // Size of chunks
				      int inputPartitionCount, // Number of chunks
				      String scriptDirPath, // Output script file directory. Not needed in Rain.
				      String hdfsInputDir, // Where the data is?
				      long totalDataPerReduce // Highest amount of data output?
				      ) throws Exception {


	// ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

	if (interArrival.size() > 0) {

	    long maxInput = 0;
	    String toWrite = "";

	    FileWriter runAllJobs = new FileWriter(scriptDirPath + "/run-jobs-all.sh");

	    /*
	    toReturn.add(new ArrayList<String>());
            toReturn.get(toReturn.size()-1).add("rm -r output # pipe to run-all-jobs.sh");
            toReturn.add(new ArrayList<String>());
            toReturn.get(toReturn.size()-1).add("mkdir output # pipe to run-all-jobs.sh");
	    */

	    toWrite = "#!/bin/bash\n";
	    runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    toWrite = "rm -r output\n"; 
	    runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    toWrite = "mkdir output\n";
            runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    /*
	    int threadsCount = 20;

	    
	    FileWriter[] runFiles = new FileWriter[threadsCount];
	    for (int i=0; i<runFiles.length; i++) {
		runFiles[i] = new FileWriter(scriptDirPath + "/run-job-" + i + ".sh", true);
                toWrite = "./run-job-" + i + ".sh &\n";
                runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    }
	    */

	    FileWriter runFile;

	    
	    for (int i=0; i<interArrival.size(); i++) {

		String name = jobNames.get(i).get(0);
		String nameCheck1 = inputSizes.get(i).get(0);
		String nameCheck2 = shuffleSizes.get(i).get(0);
		String nameCheck3 = outputSizes.get(i).get(0);

		long sleep   = Long.parseLong(interArrival.get(i).get(0));
		long input   = Long.parseLong(inputSizes  .get(i).get(1));
		long shuffle = Long.parseLong(shuffleSizes.get(i).get(1));
		long output  = Long.parseLong(outputSizes .get(i).get(1));

		if (!name.equals(nameCheck1) || !name.equals(nameCheck2) || !name.equals(nameCheck3)) throw new Exception();

		// don't scale sleep time for now
		//sleep   = sleep   * clusterSizeRaw / clusterSizeWorkload; // larger cluster = more intense inter job arrival rate
		
		input   = input   * clusterSizeWorkload / clusterSizeRaw;
		shuffle = shuffle * clusterSizeWorkload / clusterSizeRaw;
		output  = output  * clusterSizeWorkload / clusterSizeRaw; 

		if (input > maxInput) maxInput = input;

		if (input   < 67108864) input   = 67108864;
		if (shuffle < 1024    ) shuffle = 1024    ;
		if (output  < 1024    ) output  = 1024    ;

		/*
		 * Aaron: I don't know what this does. I haven't copied it to the
		 * Rain implementation.
		 */
		ArrayList<Integer> inputPartitionSamples = new ArrayList<Integer>();
		long inputCopy = input; 
		java.util.Random rng = new java.util.Random();
		int tryPartitionSample = -1;
		while (inputCopy > 0) {
		    boolean alreadySampled = true;
		    while (alreadySampled) {
			if (inputPartitionSamples.size()>=inputPartitionCount) {
                            System.err.println(input);
                            System.err.println(inputPartitionSize);
                            System.err.println(inputPartitionSamples.size());
                            throw new Exception(); // if thrown, input set not large enough - generate bigger input set
                        }
			tryPartitionSample = rng.nextInt(inputPartitionCount);
			boolean testSample = false;
			for (int j=0; j<inputPartitionSamples.size(); j++) {
			    testSample = (testSample || (inputPartitionSamples.get(j) == tryPartitionSample));
			}
			if (!testSample) alreadySampled = false;
		    }
		    inputPartitionSamples.add(new Integer(tryPartitionSample));
		    inputCopy -= inputPartitionSize;
		}

		String inputPath = "";
		for (int j=0; j<inputPartitionSamples.size(); j++) {
		    inputPath += (hdfsInputDir + "/part-" + String.format("%05d", inputPartitionSamples.get(j)));
		    if (j != (inputPartitionSamples.size()-1)) inputPath += ",";
		}

		String outputPath = "workGenOut-job" + i;

		float SIRatio = ((float) shuffle) / ((float) input  );
		float OSRatio = ((float) output ) / ((float) shuffle);

		long numReduces = -1;

		runFile = new FileWriter(scriptDirPath + "/run-job-" + i + ".sh", true);
		
		if (totalDataPerReduce > 0) {
		    numReduces = Math.round((shuffle + output) / ((double) totalDataPerReduce));
		    if (numReduces < 1) numReduces = 1;
		    toWrite =
                        "bin/hadoop jar WorkGen.jar org.apache.hadoop.examples.WorkGen -conf conf/workGenKeyValue_conf.xsl " +
                        "-r " + numReduces + " " + inputPath + " " + outputPath + " " + SIRatio + " " + OSRatio +
			" >> output/job-" + i + ".txt 2>> output/job-" + i + ".txt\n";
		} else {
		    toWrite = 
			"bin/hadoop jar WorkGen.jar org.apache.hadoop.examples.WorkGen -conf conf/workGenKeyValue_conf.xsl " +
			inputPath + " " + outputPath + " " + SIRatio + " " + OSRatio +
			" >> output/job-" + i + ".txt 2>> output/job-" + i + ".txt\n";
		}


		
                runFile.write(toWrite.toCharArray(), 0, toWrite.length());
                toWrite = "bin/hadoop dfs -rmr " + outputPath + "\n";
                runFile.write(toWrite.toCharArray(), 0, toWrite.length());
                toWrite = "# inputSize " + input + "\n";
                runFile.write(toWrite.toCharArray(), 0, toWrite.length());

		runFile.close();
		
		/*
		runFiles[i % threadsCount].write(toWrite.toCharArray(), 0, toWrite.length());
		toWrite = "bin/hadoop dfs -rmr " + outputPath + "\n";
		runFiles[i % threadsCount].write(toWrite.toCharArray(), 0, toWrite.length());
		toWrite = "# inputSize " + input + "\n";
		runFiles[i % threadsCount].write(toWrite.toCharArray(), 0, toWrite.length());
		*/
		
		toWrite = "sleep " + sleep + "\n";
		runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
		toWrite = "./run-job-" + i + ".sh &\n";
		runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
		
	    }

	    toWrite = "# max input " + maxInput + "\n";
	    runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    toWrite = "# partitionSize" + inputPartitionSize + "\n";
	    runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    toWrite = "# partitionCount" + inputPartitionCount + "\n";
            runAllJobs.write(toWrite.toCharArray(), 0, toWrite.length());
	    /*
            for (int i=0; i<runFiles.length; i++) {
                runFiles[i].close();
	    }
	    */


	    runAllJobs.close();
	}

    }

    public static void printOutput(ArrayList<ArrayList<String>> interArrival, 
				   ArrayList<ArrayList<String>> jobNames    ,
				   ArrayList<ArrayList<String>> inputSizes  ,
				   ArrayList<ArrayList<String>> shuffleSizes,
				   ArrayList<ArrayList<String>> outputSizes ,
				   int clusterSizeRaw, 
				   int clusterSizeWorkload,
				   int inputPartitionSize,
				   int inputPartitionCount,
				   String scriptDirPath, 
				   String hdfsInputDir,
				   long totalDataPerReduce) throws Exception {

	generateScript(interArrival, jobNames, inputSizes, shuffleSizes, outputSizes, 
		       clusterSizeRaw, clusterSizeWorkload,
		       inputPartitionSize, inputPartitionCount, scriptDirPath, hdfsInputDir, totalDataPerReduce);
	/*
	System.out.println("#!/bin/bash");

	for (int i=0; i<commands.size(); i++) {
	    System.out.println(commands.get(i).get(0));
	}
	*/
	
    }

    public static double mean(long[] data) {

        double sum = 0;
        for (int i=0; i<data.length; i++) {
	    long value = data[i];
	    //if (value==null) value = 0;
            sum += value;
        }
        return sum / data.length;

    }

    public static double stddev(long[] data) {

	double avg = mean(data);
	double sum = 0;
	for (int i=0; i<data.length; i++) {
	    long value = data[i];
	    //if (value==null) value = 0;
	    double difference = value - avg;
	    sum += difference * difference;
	}
	double stdDev = Math.sqrt(sum / data.length);
	return stdDev;

    }

    public static long min(long[] data) {

        long min = Long.MAX_VALUE;
        for (int i=0; i<data.length; i++) {
            long value = data[i];
            //if (value==null) value = 0;
            if (value < min) min = value;
        }
        return min;

    }

    public static long max(long[] data) {

        long max = Long.MIN_VALUE;
        for (int i=0; i<data.length; i++) {
            long value = data[i];
            //if (value==null) value = 0;
            if (value > max) max = value;
        }
        return max;

    }

    public static void main(String args[]) throws Exception {
	
	/*
	FileWriter blah = new FileWriter("temp.txt");
	blah.write("#test\nblah".toCharArray(), 0, "#test\nblah".length());
	blah.close();
	*/

	if (args.length == 0) {
	    //System.out.println("Computes and prints column to column correlation matrix for input cvs file.");
	    System.out.println("Usage: ");
	} else {

	    
	ArrayList<ArrayList<String>> interArrival = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> jobNames     = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> inputSizes   = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> shuffleSizes = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> outputSizes  = new ArrayList<ArrayList<String>>();

	String fileInterArrival = args[0];
	String fileJobNames     = args[1];
	String fileInputSizes   = args[2];
	String fileShuffleSizes = args[3];
	String fileOutputSizes  = args[4];
	int clusterSizeRaw      = Integer.parseInt(args[5]); 
	int clusterSizeWorkload = Integer.parseInt(args[6]); 
	int inputPartitionSize  = Integer.parseInt(args[7]); 
	int inputPartitionCount = Integer.parseInt(args[8]);
	String scriptDirPath    = args[9];
	String hdfsInputDir     = args[10];
	long totalDataPerReduce = Long.parseLong(args[11]);
	
	parseFileArrayList(fileInterArrival, interArrival);
	parseFileArrayList(fileJobNames    , jobNames    );
	parseFileArrayList(fileInputSizes  , inputSizes  );
	parseFileArrayList(fileShuffleSizes, shuffleSizes);
	parseFileArrayList(fileOutputSizes , outputSizes );

	printOutput(interArrival, jobNames, inputSizes, shuffleSizes, outputSizes, 
		    clusterSizeRaw, clusterSizeWorkload, 
		    inputPartitionSize, inputPartitionCount, scriptDirPath, hdfsInputDir, totalDataPerReduce);

	}

	
    }
}

