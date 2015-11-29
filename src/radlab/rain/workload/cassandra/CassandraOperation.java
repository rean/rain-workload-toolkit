package radlab.rain.workload.cassandra;

import java.util.List;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

public abstract class CassandraOperation extends Operation 
{
	protected String _columnFamilyName = "";
	protected String _key = "";
	protected byte[] _value = null;
	protected int _maxScanRows = 1;
	protected CassandraTransport _cassandraClient = null;
	

	public CassandraOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		CassandraGenerator cassandraGenerator = (CassandraGenerator) generator;
		
		this._cassandraClient = cassandraGenerator.getCassandraTransport();
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
		
		this._columnFamilyName = cassandraGenerator._columnFamilyName;
	}

	@Override
	public void cleanup() 
	{
		this._columnFamilyName = "";
		this._key = "";
		this._value = null;
		this._maxScanRows = 1;
	}
	
	public byte[] doGet( String key ) throws Exception
	{
		return this._cassandraClient.get( this._columnFamilyName, key );
	}
	
	public List<byte[]> doScan( String startKey, int maxRows ) throws Exception
	{
		return this._cassandraClient.scan( startKey, this._columnFamilyName, maxRows );
	}
	
	public void doPut( String key, byte[] value ) throws Exception
	{
		this._operationRequest = key;
		this._cassandraClient.put( this._columnFamilyName, key, value );
	}

	public void doDelete( String key ) throws Exception
	{
		this._cassandraClient.delete( this._columnFamilyName, key );
	}
}
