package radlab.rain.workload.mapreduce;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;

public class MapReduceLoadProfile extends LoadProfile {
	
	private String _traceFilePath;

	public MapReduceLoadProfile(JSONObject profileObj) throws JSONException {
		super(profileObj);
		_traceFilePath = profileObj.getString("TraceFile");
	}

	public String get_traceFilePath() {
		return _traceFilePath;
	}

	public void set_traceFilePath(String traceFilePath) {
		_traceFilePath = traceFilePath;
	}

}
