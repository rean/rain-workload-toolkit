package radlab.rain.communication;

public class TrackListRequestMessage extends RainMessage 
{
	public TrackListRequestMessage()
	{
		this._header = new MessageHeader( MessageHeader.VERSION_1, MessageHeader.TRACK_LIST_REQUEST_MSG_TYPE );
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
