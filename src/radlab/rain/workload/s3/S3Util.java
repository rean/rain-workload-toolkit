package radlab.rain.workload.s3;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.json.JSONException;

public class S3Util 
{

	public S3Util() 
	{
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		// We're loading data into an n-level hierarchy, how should we parallelize things
		// Take our default scenario of 10 million objects spread across a 3-level hierarchy of
		// 10 x 1000 x 1000
		// If we have 100 threads then that's 100,000 items per thread
		// We could have a single thread create the first two levels and parallize the loading of
		// level 3 only
		
		Random rnd = new Random();
		S3Transport s3Client = null;
		// Serial loader
		int[] object_keys = new int[]{2, 5, 5};//new int[]{10, 1000, 1000};
		NumberFormat formatter = new DecimalFormat( "00000" );		
		HashMap<Integer,String> objectKeyPrefixes = new HashMap<Integer,String>();
		objectKeyPrefixes.put( 0, S3Generator.DEFAULT_LEVEL1_PREFIX );
		objectKeyPrefixes.put( 1, S3Generator.DEFAULT_LEVEL2_PREFIX );
		objectKeyPrefixes.put( 2, S3Generator.DEFAULT_LEVEL3_PREFIX );
		
		// Create bucket hierarchy - levels 1 and 2
		for( int i = 0; i < object_keys[0]; i++ )
		{
			StringBuffer bucketName = new StringBuffer();
						
			// Get the level 1 prefix
			bucketName.append( objectKeyPrefixes.get( 0 ) );
			// Add the suffix - the formatted random number we generated
			bucketName.append( formatter.format( i ) );
			
			Properties aws = new Properties();
			try
			{
				InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream( S3Generator.AWS_PROPERTIES_FILE ); 
				aws.load( stream );			
				s3Client = new S3Transport( aws.getProperty( S3Generator.AWS_ACCESS_KEY_PROPERTY ), aws.getProperty( S3Generator.AWS_SECRET_KEY_PROPERTY ) );
			}
			catch( Exception e )
			{
				throw new Exception( "Error initializing S3Transport. Make sure the properties file: " + S3Generator.AWS_PROPERTIES_FILE + " is on the classpath!" );
			}
			
			// Create bucket
			System.out.println( "Creating level1 bucket: " + bucketName.toString() );
			S3Bucket bucket = s3Client.createBucket( bucketName.toString() );
			if( bucket == null )
				throw new Exception( "Unable to create bucket: " + bucketName.toString() );
						
			for( int j = 0; j < object_keys[1]; j++ )
			{
				StringBuffer level2 = new StringBuffer();
				// Get the level2 prefix
				level2.append( objectKeyPrefixes.get( 1 ) );
				// Add the suffix - the formatted random number we generated
				level2.append( formatter.format( j ) );
				
				for( int k = 0; k < object_keys[2]; k++ )
				{
					StringBuffer level3 = new StringBuffer();
					level3.append( level2.toString() ).append( "/" );
					// Get the level3 prefix
					level3.append( objectKeyPrefixes.get( 2 ) );
					// Add the suffix - the formatted random number we generated
					level3.append( formatter.format( k ) );
					
					String key = level3.toString();
					
					System.out.println( "Bucket: " + bucketName.toString() + " key: " + key );
					byte[] value = new byte[1024];
					rnd.nextBytes( value );
					S3Object obj = s3Client.putObject( bucketName.toString(), key, value );
				}
			}
		}
		
		
		
		// Load the data
		
		
		// Todo: work on a parallel loader
	}
}
