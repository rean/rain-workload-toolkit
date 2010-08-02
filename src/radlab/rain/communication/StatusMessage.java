package radlab.rain.communication;

import java.io.Serializable;

public class StatusMessage extends RainMessage implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int _statusCode 			= MessageHeader.ERROR;
	
	public StatusMessage()
	{
		this._header = new MessageHeader( MessageHeader.VERSION_1, MessageHeader.STATUS_MSG_TYPE );
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "[ Status code: " );
		buf.append( this._statusCode );
		buf.append( " ]" );
		return buf.toString();
	}
}
