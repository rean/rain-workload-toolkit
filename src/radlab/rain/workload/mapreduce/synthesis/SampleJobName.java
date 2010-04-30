package radlab.rain.workload.mapreduce.synthesis;

import java.io.BufferedReader;
import java.io.FileReader;
//import java.util.HashMap;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.text.SimpleDateFormat;

public class SampleJobName {  

    public static void parseFile(String path, ArrayList<ArrayList<String>> data) throws Exception {
	
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

    public static ArrayList<String> sampleDistribution(ArrayList<ArrayList<String>> data, String samplesCount) throws Exception {

	ArrayList<String> toReturn = new ArrayList<String>();
	int count = Integer.parseInt(samplesCount);

	if (data.size() > 0) {

	    java.util.Random rng = new java.util.Random();

	    while (count > 0) {

		float randomNumber = rng.nextFloat();

		String sample = "";
		int i = 0;
		//System.out.println(data.get(i));
		while (Float.parseFloat(data.get(i).get(1)) < randomNumber) {
		    i++;
		}
		sample = data.get(i).get(0);

		toReturn.add(sample + "");
		count--;

	    }
	}

	return toReturn;

    }

    public static void printOutput(ArrayList<ArrayList<String>> data, String duration) throws Exception {

	ArrayList<String> samples = sampleDistribution(data,duration);


	for (int i=0; i<samples.size(); i++) {
	    System.out.println(samples.get(i));
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

	ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		
	parseFile(args[0], data);
	printOutput(data, args[1]);

	}

    }
}

