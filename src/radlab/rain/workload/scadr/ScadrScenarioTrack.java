/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package radlab.rain.workload.scadr;


import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Scenario;
import radlab.rain.util.AppServerStats;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Iterator;

public class ScadrScenarioTrack extends DefaultScenarioTrack 
{
	public static final int DEFAULT_ZOOKEEPER_SESSION_TIMEOUT 	= 30000;
	public static final String APP_SERVER_LIST_SEPARATOR 		= "\n";
	public static int DEFAULT_RETRIES							= 3;
	public static long DEFAULT_RETRY_TIMEOUT					= 3000; // 3 sec retry timeout
	
	private NumberFormat _formatter 				 = new DecimalFormat( "#0.0000" );
	private boolean _isConfigured 					 = false;
	private ZooKeeper _zconn 						 = null;
	private boolean _appServerListChanged 			 = false;
	private String[] _appServers 					 = null;
	
	// Mechanisms associated with keeping traffic stats for request-gating.
	// We want to get the generators to send requests to least loaded server
	private long _totalTrafficLockWaitTime 				= 0;
	private long _totalTrafficLockRequestCount			= 0;
	private long _maxTrafficLockWaitTime 				= 0;
	private Object _trafficLock 					 	= new Object();
	//private Hashtable<String,Long> _appServerTraffic 	= new Hashtable<String,Long>();
	private Hashtable<String, AppServerStats> _appServerTraffic = new Hashtable<String, AppServerStats>();
		
	// Accessor methods such that operations can indicate that they're targeting a 
	// specific server (based on the base url in the generator that created the operation)
	public void requestIssue( String appServer )
	{
		long start = System.currentTimeMillis();
		synchronized( this._trafficLock )
		{
			long end = System.currentTimeMillis();
			// How long did we wait for the lock?
			long lockWaitTime = end - start;
			// Keep track of the total time waiting on the traffic lock
			this._totalTrafficLockWaitTime += lockWaitTime; 
			// Keep counting the lock requests
			this._totalTrafficLockRequestCount++;
			// Track the worst case lock wait time seen so far
			if( lockWaitTime >  this._maxTrafficLockWaitTime )
				this._maxTrafficLockWaitTime = lockWaitTime;
			
			// Get the request counter for this server
			//Long outstandingRequests = this._appServerTraffic.get( appServer );
			AppServerStats stats = this._appServerTraffic.get( appServer );
			if( stats == null )
			{
				// We might want to know whether requests are coming in for servers
				// not in the Hashtable, that would mean that we're messing up somewhere
				// re: keeping the Hashtable up-to-date with the latest info from
				// ZooKeeper, e.g., the list changes before we get a chance to update the
				// Hashtable
				this._appServerTraffic.put( appServer, new AppServerStats(appServer, 0L ) );
			}
			else stats._outstandingRequests++;
		}
	}
	
	public void requestRetire( String appServer )
	{
		long start = System.currentTimeMillis();
		synchronized( this._trafficLock )
		{
			long end = System.currentTimeMillis();
			// How long did we wait for the lock?
			long lockWaitTime = end - start;
			// Keep track of the total time waiting on the traffic lock
			this._totalTrafficLockWaitTime += lockWaitTime; 
			// Keep counting the lock requests
			this._totalTrafficLockRequestCount++;
			// Track the worst case lock wait time seen so far
			if( lockWaitTime >  this._maxTrafficLockWaitTime )
				this._maxTrafficLockWaitTime = lockWaitTime;
			
			// Get the request counter for this server
			//Long outstandingRequests = this._appServerTraffic.get( appServer );
			AppServerStats stats = this._appServerTraffic.get( appServer );
			
			// outstandingRequests should never be null on request retire (frequently) - 
			// a request can't be retired from a server that's not in the traffic stats hashtable 
			// unless a request was sent to a slow app server and by the time the request 
			// finished the slow server was purged from a recent ZooKeeper list. In that
			// eventuality it's safe to ignore the retire message
			if( stats != null )
				stats._outstandingRequests--;	
		}
	}
	
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
	
	/*public String[] getAppServers()
	{ 
		//if( this._appServerListChanged )
			//; // do update
		
		return this._appServers; 
	}*/
	
	public Hashtable<String, AppServerStats> getAppServers()
	{ return this._appServerTraffic; }
	
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
				System.out.println( this + " Appserver list initialized, " + this._appServers.length + " app servers found." );
				for( String s : this._appServers )
				{
					// Set up empty stats
					this._appServerTraffic.put( s, new AppServerStats( s, 0L ) );
					//System.out.println( this + " Appserver: " + s );
				}
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
				System.out.println( this + " Appserver list updated, " + this._appServers.length + " app servers found." );
								
				// Here's where we'd want to purge the traffic table of entries that are not in
				// this list - we need to purge so that non-existent servers don't get
				// picked as the least loaded
				
				Hashtable<String,AppServerStats> newTrafficSnapshot = new Hashtable<String,AppServerStats>();
				for( String s : this._appServers )
				{
					//System.out.println( this + " Appserver: " + s );
					// Create a stats snapshot with just the server names, 
					// but null stats values
					newTrafficSnapshot.put( s, new AppServerStats( s, 0L ) );
				}
				// Now we that we have the latest list of server names, but no stats
				// copy over the latest stats from traffic stats table
				synchronized( this._trafficLock )
				{
					// Go through the current stats, if that server is in the new snapshot
					// then copy its stats otherwise ignore it
					Iterator<String> appIt = this._appServerTraffic.keySet().iterator();
					while( appIt.hasNext() )
					{
						AppServerStats currentServerStats = this._appServerTraffic.get( appIt.next() );
						// If an existing server is still on the latest list of servers
						// then copy over its stats
						if( newTrafficSnapshot.containsKey( currentServerStats._appServer ) )
						{
							// Copy over the latest stats for this server
							newTrafficSnapshot.put( currentServerStats._appServer, currentServerStats );
						}
					}
					// Replace the current appServerTraffic table with the new version
					this._appServerTraffic = newTrafficSnapshot;
				}
							
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
	
	@Override
	public void end()
	{
		// Dump traffic lock stats
		if( this._totalTrafficLockRequestCount > 0 )
			System.out.println( this + " Gating stats - Average traffic lock wait time (ms) : " + this._formatter.format( (double) this._totalTrafficLockWaitTime / (double) this._totalTrafficLockRequestCount ) );
		else System.out.println( this + " Gating stats - Average traffic lock wait time (ms) : " + this._formatter.format( 0.0 ) );
		
		System.out.println( this + " Gating stats - Total traffic lock requests         : " + this._totalTrafficLockRequestCount );
		System.out.println( this + " Gating stats - Max traffic lock wait time (ms)     : " + this._formatter.format( this._maxTrafficLockWaitTime ) );
		// Let the base class finish its regular cleanup
		super.end();
	}
}
