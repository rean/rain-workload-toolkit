package radlab.rain.workload.cassandra;

import java.util.Random;

public class CassandraLoaderThread extends Thread 
{
	private int minKey = -1;
	private int maxKey = -1;
	
	private String columnFamilyName = "";
	private int size = 0;
	private CassandraTransport cassandraClient = null;
	
	public int keysLoaded = 0;
	
	public CassandraLoaderThread( String columnFamilyName, int minKey, int maxKey, int size, CassandraTransport client ) 
	{
		this.columnFamilyName = columnFamilyName;
		this.minKey = minKey;
		this.maxKey = maxKey;
		this.cassandraClient = client;
		this.size = size;
	}

	public void run()
	{
		Random random = new Random();
		int count = (maxKey - minKey) + 1;
		
		for( int i = 0; i < count; i++ )
		{
			byte[] arrBytes = new byte[size];
			random.nextBytes( arrBytes );
			
			int myKey = i + minKey;
			
			String key = CassandraUtil.KEY_FORMATTER.format( i + minKey );
			this.cassandraClient.put( this.columnFamilyName, key, arrBytes );
			this.keysLoaded++;
		}
	}
}
