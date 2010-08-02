package radlab.rain.communication;

import java.net.Socket;

public class RawMessage 
{
	public RainMessage _rainMessage; // Message (request) received
	public long _receiveTimestamp; // When we got it
	public long _completionTimestamp; // When did we finish processing it
	public boolean _canBeProcessed = false; // Can we process this message
	public Socket _clientSocket; // Who to send the reply to
}
