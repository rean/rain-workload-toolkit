package radlab.rain.workload.scadr;


import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Scenario;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

public class ScadrScenarioTrack extends DefaultScenarioTrack 
{
	public static final int DEFAULT_ZOOKEEPER_SESSION_TIMEOUT 	= 30000;
	public static final String APP_SERVER_LIST_SEPARATOR 		= "\n";
	public static int DEFAULT_RETRIES							= 3;
	public static long DEFAULT_RETRY_TIMEOUT					= 3000; // 3 sec retry timeout
	
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
		//if( this._appServerListChanged )
			//; // do update
		
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
		boolean res = this.initializeAppServerList( DEFAULT_ZOOKEEPER_SESSION_TIMEOUT );
				
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
	
	private boolean initializeAppServerList( int timeout )
	{
		try
		{
			// Establist a zookeeper connection
			this._zconn = new ZooKeeper( this._zkConnString, timeout, new ZKAppServerWatcher( this ) );
			
			int retries = DEFAULT_RETRIES;
			long retryTimeout = DEFAULT_RETRY_TIMEOUT;
			
			byte[] data = ScadrScenarioTrack.readZooKeeperData( this._zconn, this._zkPath, retries, retryTimeout );//this._zconn.getData( this._zkPath, true, new Stat() );
			if( data == null )
				throw new Exception( "No data returned from ZooKeeper path: " + this._zkPath + " after: " + retries + " retries." );
				
			String list = new String( data );
			
			if( list.trim().length() > 0 )
			{
				this._appServers = list.split( APP_SERVER_LIST_SEPARATOR );
				return true; // Signal that we've initialized the app server list
			}
			else return false;
		}
		catch( Exception e )
		{
			System.out.println( this + " Error initializing app server list. Reason: " + e.toString() );
			e.printStackTrace();
			return false;
		}
	}
	
	public synchronized boolean updateAppServerList()
	{
		try
		{
			int retries = DEFAULT_RETRIES;
			long retryTimeout = DEFAULT_RETRY_TIMEOUT;
			
			byte[] data = ScadrScenarioTrack.readZooKeeperData( this._zconn, this._zkPath, retries, retryTimeout );//this._zconn.getData( this._zkPath, true, new Stat() );
			if( data == null )
				throw new Exception( "No data returned from ZooKeeper path: " + this._zkPath + " after: " + retries + " retries." );
				
			String list = new String( data );
			
			if( list.trim().length() > 0 )
			{
				this._appServers = list.split( APP_SERVER_LIST_SEPARATOR );
				this._appServerListChanged = false; // Now that we have the new list of appservers squelch the change
				return true; // Signal that we've updated the app server list
			}
			else return false;
		}
		catch( Exception e )
		{
			System.out.println( this + " Error updating app server list. Reason: " + e.toString() );
			e.printStackTrace();
			return false;
		}
	}
	
	public static byte[] readZooKeeperData( ZooKeeper zkConn, String zkPath, int retries, long retryTimeout )
	{
		if( retries <= 0 )
			retries = DEFAULT_RETRIES;
		if( retryTimeout < 0 )
			retryTimeout = DEFAULT_RETRY_TIMEOUT;
		
		byte[] data = null;
		
		int i = 0;
		while( i < retries )
		{
			try
			{
				i++;
				data = zkConn.getData( zkPath, true, new Stat() );
				// Check whether we found data at that path in ZooKeeper.
				// If we find data there then break otherwise try again
				if( data != null)
					break;
				else Thread.sleep( retryTimeout ); // Sleep for a while before retrying
			}
			catch( KeeperException ke )
			{
				if( ke.code() == Code.CONNECTIONLOSS )
				{
					try
					{
						Thread.sleep( retryTimeout ); // Sleep for a while before retrying
					}
					catch( InterruptedException ie ){}
					
					continue; // try again if we can
				}
			}
			catch( InterruptedException ie )
			{
				try
				{
					Thread.sleep( retryTimeout ); // Sleep for a while before retrying
				}
				catch( InterruptedException nie )
				{}
				
				continue; // try the transaction again if we can
			}
		}
		
		return data;
	}
	
	@Override
	public String toString()
	{
		return "[SCADRTRACK: " + this._name + "]";
	}
}
