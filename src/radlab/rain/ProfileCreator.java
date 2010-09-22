package radlab.rain;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ProfileCreator 
{
	public abstract JSONObject createProfile( JSONObject params ) throws JSONException;
}

