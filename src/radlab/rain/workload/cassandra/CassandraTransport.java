package radlab.rain.workload.cassandra;

//import java.util.HashMap;
//import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;

//[FIXME] Experimental: use templates instead of mutators
//import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
//import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
//import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
//[/FIXME]

public class CassandraTransport 
{
	public static final int DEFAULT_PORT = 9160;
	private static final String DEFAULT_COLUMN_NAME = "value";
	
	private String _clusterName = "";
	//private String _clusterHost = "";
	//private int _clusterPort = DEFAULT_PORT;
	private String _clusterHosts = "";
	private Cluster _cluster = null;
	private boolean _initialized = false;
	private int _maxUsers = 100;
	private int _socketTimeout = 10000;
	private Keyspace _keyspace = null;
	private CassandraHostConfigurator _config = null;
	private int _replicationFactor = 1;
	//private boolean _debug = true;
	//private HashMap<String,Integer> _failMap = new HashMap<String,Integer>();
	//[FIXME] Experimental: use of templates instead of mutators
	//private ColumnFamilyTemplate<String, String> _template;
	//[/FIXME]
	
	
	public CassandraTransport( String clusterName, String clusterHosts, int maxUsers )
	{
		this._clusterName = clusterName;
		this._maxUsers = maxUsers;
		this._clusterHosts = clusterHosts;
		// Use a comma separated list of host:port pairs		
		this._config = new CassandraHostConfigurator( clusterHosts );
		this._config.setMaxActive( this._maxUsers );
		this._config.setCassandraThriftSocketTimeout( this._socketTimeout );
		this._config.setAutoDiscoverHosts( true );
		this._config.setAutoDiscoveryDelayInSeconds( 30 );
		this._cluster = HFactory.getOrCreateCluster( this._clusterName, this._config );
	}
	
	public CassandraTransport( String clusterName, String clusterHost, int clusterPort, int maxUsers )
	{
		this._clusterName = clusterName;
		//this._clusterHost = clusterHost;
		//this._clusterPort = clusterPort;
		this._maxUsers = maxUsers;
		
		StringBuffer hostIp = new StringBuffer();
		hostIp.append( clusterHost ).append( ":" ).append( clusterPort );
		this._clusterHosts = hostIp.toString();
		this._config = new CassandraHostConfigurator( this._clusterHosts );
		this._config.setMaxActive( this._maxUsers );
		this._config.setCassandraThriftSocketTimeout( this._socketTimeout );
		this._config.setAutoDiscoverHosts( true );
		this._config.setAutoDiscoveryDelayInSeconds( 30 );
		
		this._cluster = HFactory.getOrCreateCluster( this._clusterName, this._config );
	}
	
	public int getReplicationFactor() { return this._replicationFactor; }
	public void setReplicationFactor( int value ) { this._replicationFactor = value; }
	
	public synchronized void initialize( String keyspaceName, boolean createKeyspace, String columnFamilyName, boolean createColumnFamily )
	{	
		if( createKeyspace )
			this.createKeyspace( keyspaceName, this._replicationFactor );
		
		this._keyspace = HFactory.createKeyspace( keyspaceName, this._cluster );
		
		if( createColumnFamily )
			this.createColumnFamily( columnFamilyName );
		
		//[FIXME] Experimental: use templates instead of mutators
        //this._template = new ThriftColumnFamilyTemplate<String, String>(this._keyspace,
		//																columnFamilyName,
		//																StringSerializer.get(),
		//																StringSerializer.get());
		//[/FIXME]

		this._initialized = true;
	}
	
	public void createKeyspace( String keyspaceName, int replicationFactor )
	{
		// If the keyspace definition does not already exist then create it
		if( this._cluster.describeKeyspace( keyspaceName ) == null )
		{
			KeyspaceDefinition ksDefn = HFactory.createKeyspaceDefinition( keyspaceName, ThriftKsDef.DEF_STRATEGY_CLASS, replicationFactor, new ArrayList<ColumnFamilyDefinition>() );//HFactory.createKeyspaceDefinition( keyspaceName );
			this._cluster.addKeyspace( ksDefn );
		}
	}
	
	public void deleteKeyspace( String keyspaceName )
	{
		// If the keyspace definition exists then delete it
		if( this._cluster.describeKeyspace( keyspaceName ) != null )
			this._cluster.dropKeyspace( keyspaceName );
	}
	
	public void createColumnFamily( String columnFamilyName )
	{
		if( this.columnFamilyExists( columnFamilyName ) )
			return;
				
		this._cluster.addColumnFamily( HFactory.createColumnFamilyDefinition( this._keyspace.getKeyspaceName(), columnFamilyName ) );
	}
	
	public void deleteColumnFamily( String columnFamilyName )
	{
		if( this.columnFamilyExists( columnFamilyName ) )
			this._cluster.dropColumnFamily( this._keyspace.getKeyspaceName(), columnFamilyName );
	}
	
	public boolean columnFamilyExists( String columnFamilyName )
	{
		List<ColumnFamilyDefinition> lstColumnFamily = this._cluster.describeKeyspace( this._keyspace.getKeyspaceName() ).getCfDefs();
		// if the column family exists then exit
		for( ColumnFamilyDefinition cfDef : lstColumnFamily )
		{
			if( cfDef.getName().equals( columnFamilyName ) )
				return true;
		}
		return false;
	}
	
	public void put( String columnFamily, String key, byte[] value )
	{	
		Mutator<String> mutator = HFactory.createMutator( this._keyspace, StringSerializer.get() );
		mutator.insert( key, columnFamily, HFactory.createColumn( DEFAULT_COLUMN_NAME, value, StringSerializer.get(), BytesArraySerializer.get() ) );
	}
	
//[FIXME] Experimental: use templates instead of mutators
//	public void put( String columnFamily, String key, byte[] value )
//	{	
//		ColumnFamilyUpdater<String, String> updater = this._template.createUpdater(key);
//		updater.setByteArray(DEFAULT_COLUMN_NAME, value);
 //   	this._template.update(updater);
//	}
//[/FIXME]
	
	public byte[] get( String columnFamilyName, String key )
	{
		ColumnQuery<String, String, byte[]> columnQuery = HFactory.createColumnQuery( this._keyspace, StringSerializer.get(), StringSerializer.get(), BytesArraySerializer.get() );
		columnQuery.setColumnFamily( columnFamilyName ).setKey( key ).setName( DEFAULT_COLUMN_NAME );
		QueryResult<HColumn<String, byte[]>> result = columnQuery.execute();
		
		HColumn<String, byte[]> res = result.get(); 
		if( res != null )
		{
			//System.out.println( "Get success" );
			byte[] value = res.getValue(); 
			
			/*
			if( this._debug )
			{
				if( this._failMap.containsKey( key ) )
					System.out.println( "Successful get for previously failed key: " + key );
			}*/
			return value;
		}
		else 
		{
			/*
			if( this._debug )
			{
				// Track the keys that fail and check whether we ever see a key for a value that failed before
				if( this._failMap.containsKey( key ) )
				{
					int count = this._failMap.get( key );
					count++;
					this._failMap.put( key, count );
				}
				else this._failMap.put( key, 1 );
			}*/
			//System.out.println( "Get failure" );
			return null;
		}
	}
	
//[FIXME] Experimental: use templates instead of mutators
//	public byte[] get( String columnFamilyName, String key )
//	{
//		ColumnFamilyResult<String, String> res = this._template.queryColumns(key);
//		return res.getByteArray(DEFAULT_COLUMN_NAME);
//	}
//[/FIXME]
	
	public List<byte[]> scan( String startKey, String columnFamilyName, int maxRows )// throws IOException
	{
		ArrayList<byte[]> results = new ArrayList<byte[]>();

		MultigetSliceQuery<String, String, byte[]> multigetSliceQuery = HFactory.createMultigetSliceQuery(this._keyspace, StringSerializer.get(), StringSerializer.get(), BytesArraySerializer.get());
		multigetSliceQuery.setColumnFamily(columnFamilyName);
		//multigetSliceQuery.setKey(startKey);
		multigetSliceQuery.setRange(startKey, "", false, maxRows);
		QueryResult<Rows<String, String, byte[]>> queryResult = multigetSliceQuery.execute();
		Rows<String, String, byte[]> rows = queryResult.get();
		for (Row<String, String, byte[]> row : rows)
		{
			boolean fail = true;
			HColumn<String, byte[]> column = row.getColumnSlice().getColumnByName(DEFAULT_COLUMN_NAME);
			if (column != null)
			{
				byte[] value = column.getValue();

				if (value != null)
				{
					results.add(value);
					fail = false;
					/*
					if (this._debug)
					{
						String key = rows.getKey();

						if (this._failMap.containsKey(key))
						{
							System.out.println("Successful scan for previously failed key: " + key);
						}
					}
					*/
				}
			}
			if (fail)
			{
				/*
				if (this._debug)
				{
					String key = rows.getKey();

					// Track the keys that fail and check whether we ever see a key for a value that failed before
					if (this._failMap.containsKey(key))
					{
						int count = this._failMap.get(key);
						count++;
						this._failMap.put(key, count);
					}
					else
					{
						this._failMap.put(key, 1);
					}
				}
				*/
			}
		}

		return results;
	}

	//[FIXME] Experimental: use templates instead of mutators
	//public List<byte[]> scan( String startKey, String columnFamilyName, int maxRows )// throws IOException
	//{
	//	ArrayList<byte[]> results = new ArrayList<byte[]>();
	//
	//	HSlicePredicate<String> predicate = new HSlicePredicate<String>(StringSerializer.get());
	//	predicate.setStartOn(startkey);
	//	predicate.setCount(maxRows);
	//
	//	ColumnFamilyResult<String, String> rows = this._template.queryColumns(startkey, predicate);
	//
	//	while (rows.hasNext())
	//	{
	//		ColumnFamilyResult<String, String> row = rows.next();
	//		byte[] value = row.getByteArray(DEFAULT_COLUMN_NAME);
	//		if (value != null)
	//		{
	//			results.add(value);
	//
	//			/*
	//			if (this._debug)
	//			{
	//				String key = row.getKey();
	//
	//				if (this._failMap.containsKey(key))
	//				{
	//					System.out.println("Successful scan for previously failed key: " + key);
	//				}
	//			}
	//			*/
	//		}
	//		else
	//		{
	//			/*
	//			if (this._debug)
	//			{
	//				String key = row.getKey();
	//
	//				// Track the keys that fail and check whether we ever see a key for a value that failed before
	//				if (this._failMap.containsKey(key))
	//				{
	//					int count = this._failMap.get(key);
	//					count++;
	//					this._failMap.put(key, count);
	//				}
	//				else
	//				{
	//					this._failMap.put(key, 1);
	//				}
	//			}
	//			*/
	//		}
	//	}
	//
	//	return results;
	//}
	
	public void delete( String columnFamily, String key )
	{	
		Mutator<String> mutator = HFactory.createMutator( this._keyspace, StringSerializer.get() );
		mutator.delete(key, columnFamily, null, StringSerializer.get());
		//mutator.addDeletion(key, columnFamily);
		//mutator.execute();
	}

	public void dispose()
	{
		/*
		// Any cleanup that we need to do
		if( this._debug )
		{
			// Dump fail map
			Iterator<String> keyIt = this._failMap.keySet().iterator();
			while( keyIt.hasNext() )
			{
				String failedKey = keyIt.next();
				System.out.println( failedKey + ", " + this._failMap.get( failedKey ) );
			}
		}*/
	}
	
	public static void main(String[] args) 
	{
		int port = CassandraTransport.DEFAULT_PORT;
		String host = "localhost";
		String clusterName = "test";
		String keyspaceName = "testks";
		String columnFamilyName = "testcf";
		int minKey = 1;
		int maxKey = 100000;
		int size = 4096;
		Random rnd = new Random(5);
		byte[] value = new byte[size]; 
		rnd.nextBytes( value );
		
		CassandraTransport client = new CassandraTransport( clusterName, host, port, 1 );
		client.deleteKeyspace( keyspaceName );
		client.initialize( keyspaceName, true, columnFamilyName, true );
		client.put( columnFamilyName, String.valueOf( minKey ), value );
		byte[] result = client.get( columnFamilyName, String.valueOf( minKey ) );
		System.out.println( result.length );
	}

}
