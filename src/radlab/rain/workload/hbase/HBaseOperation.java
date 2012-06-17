package radlab.rain.workload.hbase;

import java.util.ArrayList;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;


public abstract class HBaseOperation extends Operation 
{
	protected String _tableName = "";
	protected String _columnFamilyName = "";
	protected String _key = "";
	protected byte[] _value = null;
	protected int _maxScanRows = 1;
	protected HBaseTransport _hbaseClient = null;
		
	public HBaseOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		HBaseGenerator hbaseGenerator = (HBaseGenerator) generator;
		
		this._hbaseClient = hbaseGenerator.getHBaseTransport();
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
		
		this._tableName = hbaseGenerator._tableName;
		this._columnFamilyName = hbaseGenerator._columnFamilyName;
	}

	@Override
	public void cleanup() 
	{
		this._tableName = "";
		this._columnFamilyName = "";
		this._key = "";
		this._value = null;
		this._maxScanRows = 1;
	}
	
	public byte[] doGet( String key ) throws Exception
	{
		return this._hbaseClient.get( this._columnFamilyName, key );
	}
	
	public void doPut( String key, byte[] value ) throws Exception
	{
		this._operationRequest = key;
		this._hbaseClient.put( this._columnFamilyName, key, value );
	}
	
	public ArrayList<byte[]> doScan( String startKey, int maxRows ) throws Exception
	{
		return this._hbaseClient.scan( startKey, this._columnFamilyName, maxRows );
	}
}
