package radlab.rain.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;

/*
 * Singleton conduit into Rain driver from the outside world.
 * Listens on a socket and waits for commands from a controller
 * */
public class RainPipe
{
	public static int DEFAULT_PORT 			= 7851;
	public static int DEFAULT_NUM_THREADS 	= 3;
	private static Object _instLock 		= new Object();
	private static RainPipe _instance 		= null;
	
	private int _port = RainPipe.DEFAULT_PORT; // Default port
	private ServerSocket _sck					= null;
	private LinkedList<LFThread> _workers 		= new LinkedList<LFThread>();
	private long _numThreads					= RainPipe.DEFAULT_NUM_THREADS;
	private boolean _threadpoolActive			= false;
	/*private Benchmark _benchmark				= null;
	
	public Benchmark getBenchmark() { return this._benchmark; }
	public void setBenchmark( Benchmark benchmark )
	{ this._benchmark = benchmark; }
	*/
	public static RainPipe getInstance()
	{
		// Double-checked locking (avoids unnecessary locking after first initialization
		// and mitigates against multiple parallel initializations)
		if( _instance == null )
		{
			synchronized( _instLock )
			{
				if( _instance == null )
					_instance = new RainPipe();
			}
		}
		
		return _instance;
	}
	
	private RainPipe()
	{}
	
	public int getPort() { return this._port; }
	public void setPort( int val ) { this._port = val; }
	
	public long getNumThreads() { return this._numThreads; }
	public void setNumThreads( long val ) { this._numThreads = val; }
	
	public void printThreadStats()
	{
		int leaders = 0;
		int followers = 0;
		int busy = 0;
				
		for( int i = 0; i < this._numThreads; i++ )
		{
			LFThread p = this._workers.get(i);
			
			if( p.getLFThreadState() == LFThread.ThreadState.Leading )
				System.out.println( "[Comm Threadpool stats] " + p.getName() + " " + p.getMessagesProcessed() + " (leader)" );
			else System.out.println( "[Comm Threadpool stats] " + p.getName() + " " + p.getMessagesProcessed() );
			
			switch( p.getLFThreadState() )
			{
				case Busy:
					busy++;
					break;
				case Leading:
					leaders++;
					break;
				case Following:
					followers++;
					break;
			}	
		}	
		System.out.println( "[Comm Threadpool stats] Leaders: " + leaders + " Busy: " + busy + " Followers: " + followers );
		System.out.println( "[Comm Threadpool stats] Total Messages received : " + LFThread.messagesReceived );
		System.out.println( "[Comm Threadpool stats] Total Messages processed: " + LFThread.messagesProccessed );
		System.out.println( "[Comm Threadpool stats] Total Messages ignored  : " + (LFThread.messagesReceived - LFThread.messagesProccessed) );
		System.out.println( " " );
	}
	
	public void start() throws IOException
	{
		// Create a new server socket for the pipe
		this._sck = new ServerSocket( this._port );		
		// Now that the socket is connected, let the threads take waiting on client
		// connections and reading messages
		this.initializeThreadPool( this._sck );
	}
	
	private void initializeThreadPool( ServerSocket sck )
	{
		if( this._threadpoolActive )
			return;
			
		for( int i = 0; i < this._numThreads; i++ )
		{
			LFThread p = new LFThread( sck );
			p.setName( "Worker-" + Integer.toString(i) );
			p.start();
			this._workers.add( p );
		}
		
		this._threadpoolActive = true;
	}
	
	private void shutdownThreadPool()
	{
		if( !this._threadpoolActive )
			return;
		
		for( int i = 0; i < this._numThreads; i++ )
		{
			LFThread p = this._workers.get(i);
			p.setDone( true );
			
			try
			{
				p.interrupt();
				p.join( 5000 );
			}
			catch( Exception e )
			{}
		}
		
		this._threadpoolActive = false;
	}
	
	public boolean stop()
	{
		return this.disconnect();
	}
	
	public boolean disconnect()
	{
		// Close the socket
		if( this._sck != null )
		{
			try
			{
				if( !this._sck.isClosed() )
				{ this._sck.close(); }
			}
			catch( Exception e )
			{}
		}
		
		// Drain/shutdown threadpool
		this.shutdownThreadPool();
				
		return true;
	}
}
