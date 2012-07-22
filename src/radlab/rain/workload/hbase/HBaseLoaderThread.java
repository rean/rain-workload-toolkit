package radlab.rain.workload.hbase;

import java.io.IOException;
import java.util.Random;

public class HBaseLoaderThread extends Thread 
{
	private int minKey = -1;
	private int maxKey = -1;
	//private String tableName = "";
	private String columnFamilyName = "";
	private int size = 0;
	private HBaseTransport hbaseClient = null;
	
	public int keysLoaded = 0;
	
	public HBaseLoaderThread( String tableName, String columnFamilyName, int minKey, int maxKey, int size, HBaseTransport client ) 
	{
		//this.tableName = tableName;
		this.columnFamilyName = columnFamilyName;
		this.minKey = minKey;
		this.maxKey = maxKey;
		this.hbaseClient = client;
		this.size = size;
	}

	public void run()
	{
		Random random = new Random();
		int count = (maxKey - minKey) + 1;
		String key = "";
		
		for( int i = 0; i < count; i++ )
		{
			byte[] arrBytes = new byte[size];
			random.nextBytes( arrBytes );
			key = HBaseUtil.KEY_FORMATTER.format( i + minKey );
			
			try
			{
				this.hbaseClient.put( this.columnFamilyName, key, arrBytes );
			}
			catch( IOException ioe )
			{
				System.out.println( "Error loading key: " + key  + " size: " + size );
			}
			
			this.keysLoaded++;
		}
		
		// Dispose flushes any outstanding commits and then closes the table
		this.hbaseClient.dispose();
	}
}
