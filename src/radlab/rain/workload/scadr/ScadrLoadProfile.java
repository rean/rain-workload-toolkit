package radlab.rain.workload.scadr;

import org.json.JSONException;
import org.json.JSONObject;
import radlab.rain.LoadProfile;
import java.util.Hashtable;
import radlab.rain.Generator;

public class ScadrLoadProfile extends LoadProfile {

	Hashtable<Generator,Double> _behavior = new Hashtable<Generator,Double>();
	
	public ScadrLoadProfile(JSONObject profileObj) throws JSONException {
		super(profileObj);
	}

	public ScadrLoadProfile(long interval, int numberOfUsers, String mixName) {
		super(interval, numberOfUsers, mixName);
	}

	public ScadrLoadProfile(long interval, int numberOfUsers, String mixName,
			long transitionTime) {
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public ScadrLoadProfile(long interval, int numberOfUsers, String mixName,
			long transitionTime, String name) {
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}
	
	// Override toString to control how a load profile is printed during a run
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "[Duration: " + this._interval + " Users: " + this._numberOfUsers + " Transition time: " + this._transitionTime + "]");
		return buf.toString();
	}
}
