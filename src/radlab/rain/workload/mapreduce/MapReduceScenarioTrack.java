package radlab.rain.workload.mapreduce;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Scenario;

public class MapReduceScenarioTrack extends DefaultScenarioTrack {
	
	public static String CFG_TRACE_FILE_KEY = "traceFile";
	
	private String _traceFilePath;
		
	public MapReduceScenarioTrack(Scenario parentScenario) {
		super(parentScenario);
	}
	
	public MapReduceScenarioTrack( String name, Scenario scenario )
	{
		super( name, scenario );
	}

	public String getTraceFilePath()
	{
		return _traceFilePath;
	}

	public void setTraceFilePath(String traceFilePath)
	{
		_traceFilePath = traceFilePath;
	}
	
	@Override
	public void initialize( JSONObject config ) throws JSONException, Exception
	{
		_traceFilePath = config.getString(CFG_TRACE_FILE_KEY);
		super.initialize(config);
	}
}
