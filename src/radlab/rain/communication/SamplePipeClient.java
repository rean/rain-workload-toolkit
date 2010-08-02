package radlab.rain.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import radlab.rain.LoadProfile;

public class SamplePipeClient 
{
	public static String DRIVER_PIPE_ADDRESS = "127.0.0.1";
	public static int DRIVER_PIPE_PORT = RainPipe.DEFAULT_PORT;
	
	
	// Communication protocol with Rain drivers over pipes is request/reply. 
	
	/*
	 * Generic send-message-to-Driver-pipe method
	 * */
	public static RainMessage sendMessage( RainMessage message )
	{
		Socket sck = null;
		RainMessage replyMessage = null;
		try
		{
			sck = new Socket( DRIVER_PIPE_ADDRESS, DRIVER_PIPE_PORT );
			// Send request
			ObjectOutputStream out = new ObjectOutputStream( sck.getOutputStream() );	
			out.writeObject( message );
			// Read reply
			ObjectInputStream in = new ObjectInputStream( sck.getInputStream() );
			replyMessage = (RainMessage) in.readObject();	
		}
		catch( IOException ioe )
		{
			System.out.println( "Error communicating with Driver pipe: " + ioe.toString() );
			ioe.printStackTrace( System.out );
		}
		catch( ClassNotFoundException cne )
		{
			System.out.println( "Error reading reply from Driver pipe: " + cne.toString() );
			cne.printStackTrace( System.out );
		}
		finally // Cleanup socket
		{
			if( sck != null && sck.isConnected() )
			{
				try
				{
					sck.close();
				}
				catch( IOException ioe )
				{
					System.out.println( "Error closing socket." );
					ioe.printStackTrace( System.out );
				}
			}
		}
		
		return replyMessage;
	}
	
	
	public static void main(String[] args) 
	{
		// 1) Start Rain drivers with config option (in rain.config.<app name>.json): "waitForStartSignal": true
		// 2) Connect a socket to the driver pipe (use expected default port 7851), see SimplePipeClient::sendMessage
		// 3) Send Rain driver a track list message
		// 4) Send a benchmark start message
		// 5) Periodically send a dynamic load profile message
		
		try
		{
			RainMessage replyMessage = null;
			
			// 3) Send Rain driver a track list message
			replyMessage = SamplePipeClient.sendMessage( new TrackListRequestMessage() );
			// Expect a TrackListReplyMessage back
			if( replyMessage == null || !( replyMessage instanceof TrackListReplyMessage ) )
			{
				System.out.println( "Received unexpected or null reply to track list request!" );
				System.exit( -1 );
			}
			
			// If we're here, we got a track list reply message, so pick the track we want
			TrackListReplyMessage trackList = (TrackListReplyMessage) replyMessage;
			if( trackList._trackNames.size() == 0 )
			{
				System.out.println( "Empty track list received!" );
				System.exit( -1 );
			}
			
			// 4) Send a benchmark start message
			replyMessage = SamplePipeClient.sendMessage( new BenchmarkStartMessage() );
			// Expect a StatusMessage back
			if( replyMessage == null || !( replyMessage instanceof StatusMessage ) )
			{
				System.out.println( "Received unexpected or null reply to benchmark start message!" );
				System.exit( -1 );
			}
			
			for( int i = 0; i < 100; i++ )
			{	
				// Build a dynamic load profile message
				LoadProfile dlp = new LoadProfile( 30, 500,  "default", 0, "dlp-" + i );
				// Create a load profile message to send, indicate which track should get it
				DynamicLoadProfileMessage dynLoadProfileMessage = new DynamicLoadProfileMessage( "cloudstoneNull-002", dlp );
				replyMessage = SamplePipeClient.sendMessage( dynLoadProfileMessage );
				// Expect a status message back
				if( replyMessage == null || !( replyMessage instanceof StatusMessage ) )
				{
					System.out.println( "Received unexpected or null reply to dynamic load profile message!" );
					//System.exit( -1 );
				}
				// Check on whether the submission succeeded or not
				StatusMessage status = (StatusMessage) replyMessage;
				if( status._statusCode == MessageHeader.OK )
				{
					System.out.println( "Dynamic load profile accepted by driver!" );
				}
				else System.out.println( "Dynamic load profile rejected by driver! Status code returned: " + status._statusCode );
				
				// Sleep for 40 seconds
				Thread.sleep( 40000 );
			}
		}
		catch( Exception e )
		{
			
		}
		finally
		{
		}
	}

}
