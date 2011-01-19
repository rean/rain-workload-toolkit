package radlab.rain.workload.mapreduce;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;

import radlab.rain.IScoreboard;

@SuppressWarnings("deprecation")
public class WorkGenMapReduceOperation extends MapReduceOperation 
{
	public static String NAME = "workGen";
	
	public WorkGenMapReduceOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = 0;
	}

	/**
     * User counters
     */
    static enum Counters { MAP_RECORDS_WRITTEN, MAP_BYTES_WRITTEN, RED_RECORDS_WRITTEN, RED_BYTES_WRITTEN }

    /** 
     *  Comments
     */
    @SuppressWarnings("unchecked")
    static class RatioMapper extends MapReduceBase implements Mapper<WritableComparable, Writable, BytesWritable, BytesWritable> 
    {
		private float shuffleInputRatio = 1.0f;
	
		private int minKeySize;
		private int keySizeRange;
		private int minValueSize;
		private int valueSizeRange;
		private Random random = new Random();
		private BytesWritable randomKey; 
		private BytesWritable randomValue;
	
		private void randomizeBytes(byte[] data, int offset, int length) 
		{
		    for(int i=offset + length - 1; i >= offset; --i) 
		    {
		    	data[i] = (byte) random.nextInt(256);
		    }
		}
	
		/** Input key/val pair is swallowed up, no action is taken */
		public void map(WritableComparable key, Writable val, OutputCollector<BytesWritable, BytesWritable> output, Reporter reporter) throws IOException 
		{
		    float shuffleInputRatioTemp = shuffleInputRatio;
		    
		    // output floor(shuffleInputRatio) number of intermediate pairs
		    while (shuffleInputRatioTemp >= 0.0f) 
		    {
				int keyLength = minKeySize + (keySizeRange != 0 ? random.nextInt(keySizeRange) : 0);
				randomKey = new BytesWritable();
				randomKey.setSize(keyLength);
				randomizeBytes(randomKey.get(), 0, randomKey.getSize());
				int valueLength = minValueSize + (valueSizeRange != 0 ? random.nextInt(valueSizeRange) : 0);
				randomValue = new BytesWritable();
				randomValue.setSize(valueLength);
				randomizeBytes(randomValue.get(), 0, randomValue.getSize());
				if (shuffleInputRatioTemp >= 1.0f || (random.nextFloat() < shuffleInputRatioTemp)) 
				{
				    output.collect(randomKey, randomValue);
				    reporter.incrCounter(Counters.MAP_BYTES_WRITTEN, keyLength + valueLength);
				    reporter.incrCounter(Counters.MAP_RECORDS_WRITTEN, 1);
				}
				shuffleInputRatioTemp -= 1.0f;
		    } // end while
	

		} // end map()
	
		@Override
		public void configure(JobConf job) 
		{
		    shuffleInputRatio = Float.parseFloat(job.getRaw("workGen.ratios.shuffleInputRatio"));
		    minKeySize        = job.getInt("workGen.randomwrite.min_key", 10);
		    keySizeRange      = job.getInt("workGen.randomwrite.max_key", 1000) - minKeySize;
		    minValueSize      = job.getInt("workGen.randomwrite.min_value", 0);
		    valueSizeRange    = job.getInt("workGen.randomwrite.max_value", 20000) - minValueSize;
		}

    } // end static class RatioMapper

    /**
     *  Comments
     */
    @SuppressWarnings("unchecked")
    static class RatioReducer extends MapReduceBase implements Reducer<WritableComparable, Writable, BytesWritable, BytesWritable> 
    {
        private float outputShuffleRatio = 1.0f;

        private int minKeySize;
        private int keySizeRange;
        private int minValueSize;
        private int valueSizeRange;
        private Random random = new Random();
        private BytesWritable randomKey;
        private BytesWritable randomValue;

        private void randomizeBytes(byte[] data, int offset, int length) 
        {
            for(int i=offset + length - 1; i >= offset; --i) 
            {
                data[i] = (byte) random.nextInt(256);
            }
        }

		public void reduce(WritableComparable key, Iterator<Writable> values, OutputCollector<BytesWritable, BytesWritable> output, Reporter reporter) throws IOException 
		{
		    while ( values.hasNext() ) 
		    {
		    	@SuppressWarnings("unused")
				Writable value = values.next();
	
		    	float outputShuffleRatioTemp = outputShuffleRatio;
		    				
		    	// output floor(outputShuffleRatio) number of intermediate pairs 
				while(outputShuffleRatioTemp >= 0.0f) 
				{
				    int keyLength = minKeySize + (keySizeRange != 0 ? random.nextInt(keySizeRange) : 0);
				    randomKey = new BytesWritable();
				    randomKey.setSize(keyLength);
				    randomizeBytes(randomKey.get(), 0, randomKey.getSize());
				    int valueLength = minValueSize + (valueSizeRange != 0 ? random.nextInt(valueSizeRange) : 0);
				    randomValue = new BytesWritable();
				    randomValue.setSize(valueLength);
				    randomizeBytes(randomValue.get(), 0, randomValue.getSize());
				    if (outputShuffleRatioTemp >= 1.0f || (random.nextFloat() < outputShuffleRatioTemp)) 
				    {
				    	output.collect(randomKey, randomValue);
						reporter.incrCounter(Counters.RED_BYTES_WRITTEN, keyLength + valueLength);
						reporter.incrCounter(Counters.RED_RECORDS_WRITTEN, 1);
				    }
				    outputShuffleRatioTemp -= 1.0f;
				    
				} // end while

		    }
		}

        @Override
	    public void configure(JobConf job) 
        {
            outputShuffleRatio = Float.parseFloat(job.getRaw("workGen.ratios.outputShuffleRatio"));
            minKeySize        = job.getInt("workGen.randomwrite.min_key", 10);
            keySizeRange      = job.getInt("workGen.randomwrite.max_key", 10) - minKeySize;
            minValueSize      = job.getInt("workGen.randomwrite.min_value", 90);
            valueSizeRange    = job.getInt("workGen.randomwrite.max_value", 90) - minValueSize;
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public void execute() throws Throwable 
	{
    	if( this._jobName.length() == 0 )
    		this._jobName = "WorkGenMR";
    	
		JobConf jobConf = new JobConf(WorkGenMapReduceOperation.class);//new JobConf(getConf(), WorkGen.class);
	    jobConf.setJobName( "WorkGenMR" + "-" + this._jobName );

	    jobConf.setMapperClass(RatioMapper.class);        
	    jobConf.setReducerClass(RatioReducer.class);

	    jobConf.set( "mapred.job.tracker", this._jobTracker ); // Set this so we can see progress on the web UI
	    JobClient client = new JobClient(jobConf);
	    
	    ClusterStatus cluster = client.getClusterStatus();
	    
	    System.out.println( "Cluster max reduce tasks: " + cluster.getMaxReduceTasks() );
	    // Run at least 1 reduce task
	    int num_reduces = (int) Math.max(1, (cluster.getMaxReduceTasks() * 0.45) );
	    System.out.println( "Cluster max map tasks: " + cluster.getMaxMapTasks() );
	    // Run at least one map task
	    int num_maps = (int) Math.max(1, (cluster.getMaxMapTasks() * 0.9) );
	    
	    String sort_reduces = jobConf.get("workGen.sort.reduces_per_host");
	    if (sort_reduces != null) {
	       num_reduces = cluster.getTaskTrackers() * Integer.parseInt(sort_reduces);
	    }
	    Class<? extends InputFormat> inputFormatClass = 
	      SequenceFileInputFormat.class;
	    Class<? extends OutputFormat> outputFormatClass = 
	      SequenceFileOutputFormat.class;
	    Class<? extends WritableComparable> outputKeyClass = BytesWritable.class;
	    Class<? extends Writable> outputValueClass = BytesWritable.class;
	    
	    /*
	     * Set by ReplayMapReduceGenerator
	     * Default Hadoop -m -r options no longer have an effect 
	     * 
	    List<String> otherArgs = new ArrayList<String>();
	    for(int i=0; i < args.length; ++i) {
	      try {
	        if ("-m".equals(args[i])) {
		    num_maps = Integer.parseInt(args[++i]);
		    //jobConf.setNumMapTasks(Integer.parseInt(args[++i]));
	        } else if ("-r".equals(args[i])) {
		    num_reduces = Integer.parseInt(args[++i]);
	        } else if ("-inFormat".equals(args[i])) {
	          inputFormatClass = 
	            Class.forName(args[++i]).asSubclass(InputFormat.class);
	        } else if ("-outFormat".equals(args[i])) {
	          outputFormatClass = 
	            Class.forName(args[++i]).asSubclass(OutputFormat.class);
	        } else if ("-outKey".equals(args[i])) {
	          outputKeyClass = 
	            Class.forName(args[++i]).asSubclass(WritableComparable.class);
	        } else if ("-outValue".equals(args[i])) {
	          outputValueClass = 
	            Class.forName(args[++i]).asSubclass(Writable.class);
	        } else {
	          otherArgs.add(args[i]);
	        }
	      } catch (NumberFormatException except) {
	        System.out.println("ERROR: Integer expected instead of " + args[i]);
	        return;// printUsage();
	      } catch (ArrayIndexOutOfBoundsException except) {
	        System.out.println("ERROR: Required parameter missing from " +
	            args[i-1]);
	        return;// printUsage(); // exits
	      }
	    }
		*/
	    
	    // Set user-supplied (possibly default) job configs
	    jobConf.setNumReduceTasks(num_reduces);
	    //jobConf.setNumMapTasks(num_maps);

	    jobConf.setInputFormat(inputFormatClass);
	    jobConf.setOutputFormat(outputFormatClass);

	    jobConf.setOutputKeyClass(outputKeyClass);
	    jobConf.setOutputValueClass(outputValueClass);
	    //jobConf.set( "mapred.job.tracker", this._jobTracker ); // Set this so we can see progress on the web UI
	    
	    // Make sure there are exactly 4 parameters left.
	    /*
	     * Set by ReplayMapReduceGenerator
	     * 
	     * if (otherArgs.size() != 4) {
	      System.out.println("ERROR: Wrong number of parameters: " +
	          otherArgs.size() + " instead of 4.");
	      return;// printUsage();
	    }
	    FileInputFormat.setInputPaths(jobConf, otherArgs.get(0));
	    FileOutputFormat.setOutputPath(jobConf, new Path(otherArgs.get(1)));
	    jobConf.set("workGen.ratios.shuffleInputRatio", otherArgs.get(2));
	    jobConf.set("workGen.ratios.outputShuffleRatio", otherArgs.get(3));

	    System.out.println("Number of map tasks " + cluster.getMaxMapTasks());
	    System.out.println("Number of red tasks " + cluster.getMaxReduceTasks());
	    System.out.println("shuffleInputRatio  = " + jobConf.getFloat("workGen.ratios.shuffleInputRatio", 1.0f));
	    System.out.println("outputShuffleRatio = " + jobConf.getFloat("workGen.ratios.outputShuffleRatio", 1.0f));
		*/
	    FileInputFormat.setInputPaths( jobConf, this._inputPath );
	    FileOutputFormat.setOutputPath( jobConf, new Path( this._outputPath ) );
	    jobConf.setFloat( "workGen.ratios.shuffleInputRatio", this._shuffleInputRatio );
	    jobConf.setFloat( "workGen.ratios.outputShuffleRatio", this._outputShuffleRatio );

	    System.out.println("Running on " +
	        cluster.getTaskTrackers() + " nodes with " + 
	        num_maps + " maps and " +
	        num_reduces + " reduces.");
	    Date startTime = new Date();
	    Random random = new Random();
	    System.out.println(random.nextFloat());
	    System.out.println(random.nextFloat());
	    System.out.println("Job started: " + startTime);
	    JobClient.runJob(jobConf);
	    
	    //client.submitJob( jobConf );
	    Date end_time = new Date();
	    System.out.println("Job ended: " + end_time);
	    System.out.println("The job took " + 
	       (end_time.getTime() - startTime.getTime()) /1000 + " seconds.");
	    
	    // Mark the operation as successful
	    this.setFailed( false );
	}

}
