package radlab.rain.workload.mapreduce.synthesis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

public class SampleJobData {  

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



    public static ArrayList<ArrayList<String>> sampleDistribution(ArrayList<ArrayList<String>> workloadJobNames, 
								  HashMap<String, ArrayList<String>> jobNameDataSizes,
								  int rawDataClusterSize,
								  int workloadClusterSize) throws Exception {

	ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();

	if (workloadJobNames.size() > 0) {

	    java.util.Random rng = new java.util.Random();

	    for (int i=0; i<workloadJobNames.size(); i++) {

		float randomNumber = rng.nextFloat();

		long sample = 0;
		String jobName = workloadJobNames.get(i).get(0);
		ArrayList<String> distribution = jobNameDataSizes.get(jobName);

		long ptile1  = Long.parseLong(distribution.get(1));
		long ptile25 = Long.parseLong(distribution.get(2));
		long ptile50 = Long.parseLong(distribution.get(3));
		long ptile75 = Long.parseLong(distribution.get(4));
		long ptile99 = Long.parseLong(distribution.get(5));

                if      (randomNumber < 0.01f) sample = ptile1;
                else if (randomNumber < 0.25f) sample = (int) (ptile1  + (ptile25 - ptile1 )/(0.25-0.01)*(randomNumber - 0.01f));
                else if (randomNumber < 0.50f) sample = (int) (ptile25 + (ptile50 - ptile25)/(0.50-0.25)*(randomNumber - 0.25f));
                else if (randomNumber < 0.75f) sample = (int) (ptile50 + (ptile75 - ptile50)/(0.75-0.50)*(randomNumber - 0.50f));
                else if (randomNumber < 0.99f) sample = (int) (ptile75 + (ptile99 - ptile75)/(0.99-0.75)*(randomNumber - 0.75f));
                else                           sample = ptile99;

		sample = sample * workloadClusterSize / rawDataClusterSize;

		toReturn.add(new ArrayList<String>());
		toReturn.get(i).add(jobName);
		toReturn.get(i).add(sample + "");

	    }
	}

	return toReturn;

    }

    public static void printOutput(ArrayList<ArrayList<String>> workloadJobNames, 
				   HashMap<String, ArrayList<String>> jobNameDataSizes, 
				   int rawDataClusterSize, 
				   int workloadClusterSize) throws Exception {

	ArrayList<ArrayList<String>> samples = sampleDistribution(workloadJobNames, jobNameDataSizes, rawDataClusterSize, workloadClusterSize);

	for (int i=0; i<samples.size(); i++) {
	    System.out.println(samples.get(i).get(0) + "\t" + samples.get(i).get(1));
	}
	
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
	
	if (args.length == 0) {
	    //System.out.println("Computes and prints column to column correlation matrix for input cvs file.");
	    System.out.println("Usage: ");
	} else {

	ArrayList<ArrayList<String>> workloadJobNames = new ArrayList<ArrayList<String>>();
	HashMap<String, ArrayList<String>> jobNameDataSizes = new HashMap<String, ArrayList<String>>();

	String workloadJobNamesFile = args[0];
	String jobNameDataSizesFile = args[1];
	int rawDataClusterSize = 1; // scaling data by cluster size left for elsewhere	
	int workloadClusterSize = 1; // scaling data by cluster size left for elsewhere	

	parseFileArrayList(workloadJobNamesFile, workloadJobNames);
	parseFileHashMap(jobNameDataSizesFile, jobNameDataSizes);
	printOutput(workloadJobNames, jobNameDataSizes, rawDataClusterSize, workloadClusterSize);

	}

    }
}

