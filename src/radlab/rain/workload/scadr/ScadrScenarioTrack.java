package radlab.rain.workload.scadr;


import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Scenario;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ScadrScenarioTrack extends DefaultScenarioTrack 
{
	public static final int DEFAULT_ZOOKEEPER_SESSION_TIMEOUT 	= 30000;
	public static final String APP_SERVER_LIST_SEPARATOR 		= "\n";
	
	private boolean _isConfigured 			= false;
	private ZooKeeper _zconn 				= null;
	private boolean _appServerListChanged 	= false;
	private String[] _appServers 			= null;
	
	private String _zkConnString = "";
	private String _zkPath = "";
	
	public ScadrScenarioTrack(Scenario parent) 
	{
		super(parent);
	}

	public ScadrScenarioTrack(String name, Scenario scenario) 
	{
		super(name, scenario);
	}
	
	public boolean isConfigured()
	{ return this._isConfigured; }
	
	public void setAppServerListChanged( boolean val )
	{ 
		if( val )
			System.out.println( this + " app server list changed." );
		
		this._appServerListChanged = val; 
	}
	
	public boolean getAppServerListChanged()
	{ return this._appServerListChanged; }
	
	public String[] getAppServers()
	{ 
		if( this._appServerListChanged )
			; // do update
		
		return this._appServers; 
	}
	
	public synchronized boolean configureZooKeeper( String zkConnString, String zkPath )
	{
		// Don't double configure the Scenariotrack
		if( this._isConfigured )
			return true;
	
		// Save the connection information
		this._zkConnString = zkConnString;
		this._zkPath = zkPath;
		// Update the app server list
		boolean res = this.updateAppServerList( DEFAULT_ZOOKEEPER_SESSION_TIMEOUT );
				
		if( !res )
		{
			System.out.println( this + " Error contacting zookeeper. Conn: " + zkConnString + " path: " + zkPath );
		}
		else
		{
			System.out.println( this + " Successfully contacted zookeeper. Conn: " + zkConnString + " path: " + zkPath );
			this._isConfigured = true;
		}
		
		return res;
	}
	
	public boolean updateAppServerList( int timeout )
	{
		try
		{
			this._zconn = new ZooKeeper( this._zkConnString, timeout, new ZKAppServerWatcher( this ) );
			byte[] data = this._zconn.getData( this._zkPath, true, new Stat() );
			String list = new String( data );
			
			if( list.trim().length() > 0 )
				this._appServers = list.split( APP_SERVER_LIST_SEPARATOR );
			
			return true;
		}
		catch( Exception e )
		{
			return false;
		}
	}
	
	@Override
	public String toString()
	{
		return "[SCADRTRACK: " + this._name + "]";
	}
}
