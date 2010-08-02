package radlab.rain.communication;

import java.io.Serializable;
import radlab.rain.LoadProfile;

public class DynamicLoadProfileMessage extends RainMessage implements Serializable
{
	private static final long serialVersionUID = 1L;
	public String _destTrackName	= "";
	public long   _interval 		= 0;
	public long   _transitionTime 	= 0;
	public int    _numberOfUsers 	= 0;
	public String _mixName 			= "";
	public String _name				= "";
		
	public DynamicLoadProfileMessage()
	{
		this._header = new MessageHeader( MessageHeader.VERSION_1, MessageHeader.DYNAMIC_LOAD_PROFILE_MSG_TYPE );
	}
	
	public DynamicLoadProfileMessage( String destTrack, LoadProfile profile )
	{
		this._header = new MessageHeader( MessageHeader.VERSION_1, MessageHeader.DYNAMIC_LOAD_PROFILE_MSG_TYPE );
		this._destTrackName = destTrack;
		this._interval = profile.getInterval()/1000; // Gen interval returns msecs, convert to secs
		this._transitionTime = profile.getTransitionTime();
		this._numberOfUsers = profile.getNumberOfUsers();
		this._mixName = profile.getMixName();
		this._name = profile._name;
	}
		
	public LoadProfile convertToLoadProfile()
	{
		LoadProfile profile = new LoadProfile( this._interval, this._numberOfUsers, this._mixName, this._transitionTime, this._name );
		return profile;
	}
}
