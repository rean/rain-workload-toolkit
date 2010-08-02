package radlab.rain.communication;

import java.io.Serializable;

// Basic info we expect to precede every message
public class MessageHeader implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static int VERSION_1										= 0x1;
	public static final int UNKNOWN_MSG_TYPE						= 0;
	public static final int STATUS_MSG_TYPE							= 1;
	public static final int DYNAMIC_LOAD_PROFILE_MSG_TYPE			= 3;
	public static final int BENCHMARK_START_MSG_TYPE				= 5;
	public static final int ERROR_MESSAGE_TYPE						= 7;
	public static final int TRACK_LIST_REQUEST_MSG_TYPE				= 8;
	public static final int TRACK_LIST_REPLY_MSG_TYPE				= 9;
	
	public static final int OK 											= 0;
	public static final int ERROR										= 1775;
	public static final int ERROR_UNEXPECTED_MESSAGE_TYPE				= 1779;
			
	public int _version 			= MessageHeader.VERSION_1; // Protocol version
	public int _messageType 		= 0; // Kind of message
	
	public MessageHeader( int version, int messageType )
	{
		this._version = version;
		this._messageType = messageType;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "[Version: " + this._version + " Message type: " + this._messageType + "]" );
		return buf.toString();
	}
}
