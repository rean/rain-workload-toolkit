package radlab.rain.workload.cassandra;

//import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;
import java.util.Random;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;

public class CassandraTransport 
{
	public static final int DEFAULT_PORT = 9160;
	
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
	//private boolean _debug = true;
	//private HashMap<String,Integer> _failMap = new HashMap<String,Integer>();
	
	
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
	
	public synchronized void initialize( String keyspaceName, boolean createKeyspace, String columnFamilyName, boolean createColumnFamily )
	{	
		if( createKeyspace )
			this.createKeyspace( keyspaceName );
		
		this._keyspace = HFactory.createKeyspace( keyspaceName, this._cluster );
		
		if( createColumnFamily )
			this.createColumnFamily( columnFamilyName );
		
		this._initialized = true;
	}
	
	public void createKeyspace( String keyspaceName )
	{
		// If the keyspace definition does not already exist then create it
		if( this._cluster.describeKeyspace( keyspaceName ) == null )
			this._cluster.addKeyspace( HFactory.createKeyspaceDefinition( keyspaceName ) );
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
		mutator.insert( key, columnFamily, HFactory.createColumn( "value", value, StringSerializer.get(), BytesArraySerializer.get() ) );
	}
	
	public byte[] get( String columnFamilyName, String key )
	{
		ColumnQuery<String, String, byte[]> columnQuery = HFactory.createColumnQuery( this._keyspace, StringSerializer.get(), StringSerializer.get(), BytesArraySerializer.get() );
		columnQuery.setColumnFamily( columnFamilyName ).setKey( key ).setName( "value" );
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
	
	public void scan( String startKey, String columnFamilyName, int maxRows )
	{
		
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
