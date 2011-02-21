package radlab.rain.workload.scadr;

public class AppServerStats implements Comparable<AppServerStats> 
{
	public String _appServer 			= "";
	public long _outstandingRequests 	= 0;
	
	public AppServerStats()
	{}
	
	public AppServerStats( String appServer, long outstandingRequests )
	{
		this._appServer = appServer;
		this._outstandingRequests = outstandingRequests;
	}

	@Override
	public int compareTo( AppServerStats rhs ) 
	{
		if( this._outstandingRequests == rhs._outstandingRequests )
			return 0;
		else if( this._outstandingRequests < rhs._outstandingRequests )
			return -1;
		else return 1;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( this._outstandingRequests ).append( " " ).append( this._appServer );
		return buf.toString();
	}
}
