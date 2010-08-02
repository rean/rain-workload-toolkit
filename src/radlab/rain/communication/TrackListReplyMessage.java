package radlab.rain.communication;

import java.util.LinkedList;

public class TrackListReplyMessage extends RainMessage 
{
	public TrackListReplyMessage()
	{
		this._header = new MessageHeader( MessageHeader.VERSION_1, MessageHeader.TRACK_LIST_REPLY_MSG_TYPE );
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LinkedList<String> _trackNames = new LinkedList<String>();
}
