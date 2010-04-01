package radlab.rain.workload.scads;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;

public class ScadsLoadProfile extends LoadProfile
{
	public static String CFG_LOAD_PROFILE_KEY_GENERATOR_KEY        = "keyGenerator";
	public static String CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY = "keyGeneratorConfig";

	private String _keyGeneratorClass;
	private JSONObject _keyGeneratorConfig;

	public ScadsLoadProfile( JSONObject profileObj ) throws JSONException
	{
		super( profileObj );

		this._keyGeneratorClass = profileObj.getString( CFG_LOAD_PROFILE_KEY_GENERATOR_KEY );
		this._keyGeneratorConfig = profileObj.getJSONObject( CFG_LOAD_PROFILE_KEY_GENERATOR_CONFIG_KEY );
	}

	public String getKeyGeneratorName() { return this._keyGeneratorClass; }
	public void setKeyGeneratorName( String val ) { this._keyGeneratorClass = val; }

	public JSONObject getKeyGeneratorConfig() { return this._keyGeneratorConfig; }
	public void setKeyGeneratorConfig( JSONObject val ) { this._keyGeneratorConfig = val; }

}
