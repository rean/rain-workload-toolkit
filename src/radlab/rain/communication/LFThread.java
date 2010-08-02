package radlab.rain.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

//import radlab.rain.Benchmark;
import radlab.rain.Benchmark;
import radlab.rain.LoadProfile;
import radlab.rain.ScenarioTrack;

/*
 * Socket handling threadpool based on the leader-followers pattern
 * */
public class LFThread extends Thread 
{
	public enum ThreadState
	{
		Leading,
		Following,
		Busy
	}
	
	// Each thread gets their private reference to the shared socket (the firehose) to monitor,
	// all their accesses MUST go through the shared firehose lock
	private ServerSocket _firehose = null;
	// Class level shared lock around the firehose
	static Object firehoseLock = new Object();
	
	// State information for each thread
	private boolean _done = false;
	private ThreadState _state = ThreadState.Following;
	private long _myMessages = 0;
	private RawMessage _rawMessage = new RawMessage();
	
	// Bookeeping information for threadpool
	public static Object statsLock = new Object();
	// Only the leader updates the number of messages received
	public static long messagesReceived = 0;
	// Followers update the counters below
	public static long messagesProccessed = 0;
	
	// Properties
	public boolean getDone() { return this._done; }
	public void setDone( boolean value ){ this._done = value; }
	public ThreadState getLFThreadState() { return this._state; }
	public long getMessagesProcessed() { return this._myMessages; }

	public LFThread( ServerSocket sck )
	{
		this._firehose = sck;
	}
	
	public void run()
	{
		while( !this._done )
		{
			// Needs leader?
			synchronized( firehoseLock )
			{
				// I am the leader
				try
				{
					this._state = ThreadState.Leading;
					// Hang out until we get a new message
					Socket clientSocket = this._firehose.accept();			
					ObjectInputStream clientStream = new ObjectInputStream( clientSocket.getInputStream() );
					Object o = clientStream.readObject();
					RainMessage msg = (RainMessage) o;
					this._rawMessage._rainMessage = msg;
					this._rawMessage._receiveTimestamp = System.currentTimeMillis();
					// Save the socket so we can reply to the client
					this._rawMessage._clientSocket = clientSocket;
					this._rawMessage._canBeProcessed = true;
										
					// Only the leader updates the messages received
					LFThread.messagesReceived++;
				}
				catch( IOException e )
				{
					this._rawMessage._canBeProcessed = false;
				} 
				catch( ClassNotFoundException cne )
				{
					this._rawMessage._canBeProcessed = false;
				}
			}// No-longer the leader
						
			// Got work (or something broke), let someone else lead
			this._state = ThreadState.Busy;
				
			// Only work on valid raw messages (if an exception occurred above we don't want
			// to touch it )
			if( this._rawMessage._canBeProcessed )
			{
				// Debugging: print out the message header received
				if( this._rawMessage._rainMessage._header != null )
					System.out.println( this + " Received message header: " + this._rawMessage._rainMessage._header.toString() );
				else System.out.println( this + " Received headerless message: " + this._rawMessage._rainMessage.toString() );
				
				ObjectOutputStream clientOutput = null;
				try
				{
					clientOutput = new ObjectOutputStream( this._rawMessage._clientSocket.getOutputStream() );
					if( this._rawMessage._rainMessage instanceof DynamicLoadProfileMessage )
					{
						// Extract message
						DynamicLoadProfileMessage msg = (DynamicLoadProfileMessage) this._rawMessage._rainMessage;
						// Find the track it should go to and validate it. 
						// We should make Scenarios singletons since there's only one
						// Scenario ever (a Scenario holds one or more ScenarioTracks)
						ScenarioTrack track = Benchmark.getBenchmarkScenario().getTracks().get( msg._destTrackName );
						if( track != null )
						{
							System.out.println( this + " Found target track" );
							LoadProfile profile = msg.convertToLoadProfile();
							int validationResult = track.validateLoadProfile( profile ); 
							// Try to validate and submit to the track's load scheduler
							if( validationResult == ScenarioTrack.VALID_LOAD_PROFILE )
							{
								System.out.println( this + " Profile validated" );
								// Submit to load scheduler thread
								track.submitDynamicLoadProfile( profile );
								
								StatusMessage reply = new StatusMessage();
								reply._statusCode = MessageHeader.OK;
								clientOutput.writeObject( reply );
							}
							else // Dynamic LoadProfile failed validation
							{
								System.out.println( this + " Profile validation failed!" );
								StatusMessage reply = new StatusMessage();
								reply._statusCode = validationResult;
								clientOutput.writeObject( reply );
							}
						}
						else // Could not find track
						{
							System.out.println( this + " Target track not found: " + msg._destTrackName );
							StatusMessage reply = new StatusMessage();
							reply._statusCode = ScenarioTrack.ERROR_TRACK_NOT_FOUND;
							clientOutput.writeObject( reply );
						}	
					}
					else if( this._rawMessage._rainMessage instanceof BenchmarkStartMessage )
					{
						System.out.println( this + " Received benchmark start message." );
						// Extract message
						Benchmark.getBenchmarkInstance().waitingForStartSignal = false;
						// Send an OK message back to the client
						StatusMessage reply = new StatusMessage();
						reply._statusCode = MessageHeader.OK;
						clientOutput.writeObject( reply );
					}
					else if( this._rawMessage._rainMessage instanceof TrackListRequestMessage )
					{
						System.out.println( this + " Received track list request message." );
						TrackListReplyMessage reply = new TrackListReplyMessage();
						
						for( ScenarioTrack track : Benchmark.BenchmarkScenario.getTracks().values() )
						{
							System.out.println( this + " Adding track name: " + track.getName() );
							reply._trackNames.add( track.getName() );
						}
						
						System.out.println( this + " Sending track list reply message." );
						clientOutput.writeObject( reply );
						System.out.println( this + " Sent track list reply message." );
					}
					else
					{
						// No idea what kind of message the client sent, bail.
						StatusMessage reply = new StatusMessage();
						reply._statusCode = MessageHeader.ERROR_UNEXPECTED_MESSAGE_TYPE;
						clientOutput.writeObject( reply );
					}
					
					synchronized( statsLock )
					{
						LFThread.messagesProccessed++;
					}
					
					// Track how many message a thread successfully processes
					this._myMessages++;
				}
				catch( Exception e )
				{
					// Send an error message back to client
					StatusMessage reply = new StatusMessage();
					reply._statusCode = MessageHeader.ERROR;
					System.out.println( this + " Error processing message" );
					e.printStackTrace( System.out );
					try
					{
						clientOutput.writeObject( reply );
					}
					catch( Exception ex )
					{
						System.out.println( this + " Error sending failure message to client" );
						ex.printStackTrace( System.out );
					}
				}
				finally
				{
					if( this._rawMessage._clientSocket.isConnected() )
					{
						try
						{
							clientOutput.flush();
							clientOutput.close();
							this._rawMessage._clientSocket.close();
						}
						catch( IOException ioe )
						{
							System.out.println( this + " Error closing client socket " );
							ioe.printStackTrace( System.out );
						}
					}
				}
			}// end-if message can be processed
			
			// Mark the raw message as already processed
			this._rawMessage._canBeProcessed = false;
			// No longer busy, go back to being a follower
			this._state = ThreadState.Following;
		}
	}
	
	public String toString()
	{
		return "[PIPE LFThread " + this.getName() + "]";
	}
}
