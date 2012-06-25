package radlab.rain.workload.hbase;

import java.util.ArrayList;
import java.util.Random;

import java.io.IOException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.HTableDescriptor;


public class HBaseTransport 
{
	public static int DEFAULT_HBASE_PORT = 60000;
	
	private String _server = "";
	private int _port = DEFAULT_HBASE_PORT;
	private boolean _initialized = false;
	private Configuration _config = null;
	private HTable _table = null;
	
	public HBaseTransport( String server, int port )
	{
		this._server = server;
		this._port = port;
		this._config = HBaseConfiguration.create();
		StringBuffer buf = new StringBuffer();
		buf.append( this._server ).append( ":" ).append( this._port );
		// Configuring the transport to contact a specific hbase node/region server
		this._config.set( "hbase.master", buf.toString() );
		// Make these configurable
		// Assume zookeeper is running on the same host as the master
		//this._config.set( "hbase.zookeeper.quorum", server );
		// Set the hbase root dir (is this really necessary?)
		//this._config.set( "hbase.rootdir", "file:////hbase" );
		//this._config.setBoolean( "hbase.cluster.distributed", false );
		//this._config.setInt( "hbase.zookeeper.property.clientPort", 2181 );
	}
	
	public synchronized void initialize( String tableName, String columnFamilyName, boolean createTable ) throws IOException
	{
		if( createTable )
			this.createTable( tableName, columnFamilyName );
		
		if( !this._initialized )
		{
			// Create the HTable instance here and do some optimizations for writes (disabling auto-flushing and setting the write buffer size)
			// see (http://hbase.apache.org/book/perf.writing.html)
			this._table = new HTable( this._config, tableName );
			this._table.setAutoFlush( false );
			this._table.setWriteBufferSize( 12*1024*1024 );
		
			this._initialized = true;
		}
	}
	
	public void createTable( String tableName, String columnFamilyName ) throws MasterNotRunningException, ZooKeeperConnectionException, IOException
	{
		HBaseAdmin admin = new HBaseAdmin( this._config );
		HTableDescriptor tableDesc = new HTableDescriptor( tableName );
		HColumnDescriptor columnDesc = new HColumnDescriptor( columnFamilyName );
		
		tableDesc.addFamily( columnDesc );
		try
		{
			admin.createTable( tableDesc );
		}
		catch( TableExistsException te )
		{ 
			/* If the table exists then no harm, no foul */ 
			//System.out.println( "Table exists: " + tableName );
		}
	}
	
	public void deleteTable( String tableName ) throws IOException
	{
		HBaseAdmin admin = new HBaseAdmin( this._config );
		admin.disableTable( tableName );
		admin.deleteTable( tableName );
	}
	
	public void closeTable() throws IOException
	{
		this._table.close();
	}
	
	public void dispose()
	{
		try
		{
			this.flushCommits();
			this.closeTable();
		}
		catch( Exception e )
		{}
	}
	
	public byte[] get( String columnFamilyName, String key ) throws IOException
	{
		String qualifier = "";
		Get get = new Get( Bytes.toBytes( key ) );
	    Result result = this._table.get( get );
	    byte [] savedValue = result.getValue( Bytes.toBytes( columnFamilyName ), Bytes.toBytes(  qualifier  ) );
	    return savedValue;
	}
	
	public void put( String columnFamilyName, String key, byte[] value ) throws IOException
	{
		String qualifier = "";
		// Do write
		Put put = new Put( Bytes.toBytes( key ) );
		put.add( Bytes.toBytes( columnFamilyName ), Bytes.toBytes( qualifier ), value );
		this._table.put( put );
	}
	
	public ArrayList<byte[]> scan( String startKey, String columnFamilyName, int maxRows ) throws IOException
	{
		ArrayList<byte[]> results = new ArrayList<byte[]>();
		String qualifier = "";
		
		// Do scan
		Scan scan = new Scan( Bytes.toBytes( startKey ) );
	    // Try to cache the rows in the scan result (if maxRows is large this could be a problem)
		scan.setCaching( maxRows );
		scan.addColumn( Bytes.toBytes( columnFamilyName ), Bytes.toBytes( qualifier ) );
	    ResultScanner scanner = this._table.getScanner( scan );
	    
	    
	    int rowCount = 0;
	    for( Result result : scanner )
	    {
	    	byte [] savedValue = result.getValue( Bytes.toBytes( columnFamilyName ), Bytes.toBytes(  qualifier  ) );
	    	results.add( savedValue );
	    	
	    	rowCount++;
	    	if( rowCount > maxRows )
	    		break;
	    }
	    
	    // Close the scanner once we're done
	    try
	    {
	    	scanner.close();
	    }
	    catch( Exception e )
	    {}
	    
	    return results;
	}
	
	public void flushCommits() throws IOException
	{
		this._table.flushCommits();
	}
	
	/*
	// We're supporting the key operations: get, put and scan
	public byte[] get( String tableName, String columnFamilyName, String key ) throws IOException
	{
		String qualifier = "";
		HTable table = new HTable( this._config, tableName );
		// Do read
		Get get = new Get( Bytes.toBytes( key ) );
	    Result result = table.get( get );
	    byte [] savedValue = result.getValue( Bytes.toBytes( columnFamilyName ), Bytes.toBytes(  qualifier  ) );
	    return savedValue;
	}
	
	public void put( String tableName, String columnFamilyName, String key, byte[] value ) throws IOException
	{
		String qualifier = "";
		HTable table = new HTable( this._config, tableName );
		// Move to init or ctor so we reuse the htable instance
		table.setAutoFlush( false );
		table.setWriteBufferSize( 12*1024*1024 );
		// Do write
		Put put = new Put( Bytes.toBytes( key ) );
		put.add( Bytes.toBytes( columnFamilyName ), Bytes.toBytes( qualifier ), value );
		table.put( put );
	}
	
	public ArrayList<byte[]> scan( String tableName, String columnFamilyName, int maxRows ) throws IOException
	{
		ArrayList<byte[]> results = new ArrayList<byte[]>();
		String qualifier = "";
		HTable table = new HTable( this._config, tableName );
		// Do scan
		Scan scan = new Scan();
	    scan.addColumn( Bytes.toBytes( columnFamilyName ), Bytes.toBytes( qualifier ) );
	    ResultScanner scanner = table.getScanner( scan );
	    
	    int rowCount = 0;
	    for( Result result : scanner )
	    {
	    	byte [] savedValue = result.getValue( Bytes.toBytes( columnFamilyName ), Bytes.toBytes(  qualifier  ) );
	    	results.add( savedValue );
	    	
	    	rowCount++;
	    	if( rowCount > maxRows )
	    		break;
	    }
	    
	    // Close the scanner once we're done
	    try
	    {
	    	scanner.close();
	    }
	    catch( Exception e )
	    {}
	    
	    return results;
	}
	*/
	public static void main(String[] args) throws IOException 
	{
		String server = "aqua";
		int port = 60010;//HBaseTransport.DEFAULT_HBASE_PORT;
		Random rand = new Random();
		
		String tableName = "raintbl";
		String columnFamilyName = "raincf";
				
		HBaseTransport client = new HBaseTransport( server, port );
		client.initialize( tableName, columnFamilyName, true );
		
		// Load 1000 rows, read a random subset of them, scan the table
		int numRows = 1000;
		long start;
		long end;
		double durationSecs;
		double rate;
		int size = 4096;
		byte[] value = new byte[size];
		
		start = System.currentTimeMillis();
		for( int i = 0; i < numRows; i++ )
		{
			System.out.println( "Start put" );
			String key = String.valueOf( i );
			rand.nextBytes( value );
			client.put( columnFamilyName, key, value );
			System.out.println( "End put" );
		}
		end = System.currentTimeMillis();
		durationSecs = (end - start)/1000.0;
		rate = (double) numRows/(double) durationSecs;
		System.out.println( "Sequential load rate (puts/sec) size(" + size + "): " + rate );
		
		// Flush any remaining commits
		client.flushCommits();
		
		// Do some random reads
		int numReads = 100;
		
		start = System.currentTimeMillis();
		for( int i = 0; i < numReads; i++ )
		{
			String key = String.valueOf( rand.nextInt( numRows ) );
			client.get( columnFamilyName, key );
		}
		end = System.currentTimeMillis();
		durationSecs = (end - start)/1000.0;
		rate = (double) numReads/(double) durationSecs;
		System.out.println( "Read rate (reads/sec) size(" + size + "): " + rate );
		
		// Do a 100 row scan
		int maxScanRows = 200;
		start = System.currentTimeMillis();
		client.scan( tableName, columnFamilyName, maxScanRows );
		end = System.currentTimeMillis();
		durationSecs = (end - start)/1000.0;
		rate = (double) maxScanRows/(double) durationSecs;
		System.out.println( "Scan rate (rows/sec) size(" + size + "): " + rate );
	}
}
