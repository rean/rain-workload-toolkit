package radlab.rain.workloadtraits;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.util.storage.StorageLoadProfile;
import radlab.rain.workload.mongodb.MongoLoadProfile;

public class MongoFlatHotspotScheduleCreator extends FlatHotspotScheduleCreator 
{

	public MongoFlatHotspotScheduleCreator() 
	{}

	@Override
	public LinkedList<LoadProfile> createSchedule(JSONObject config) throws JSONException 
	{
		// Let the superclass create a generic schedule of StorageLoadProfiles
		LinkedList<LoadProfile> genericSchedule = super.createSchedule( config );
		
		LinkedList<LoadProfile> mongoSchedule = new LinkedList<LoadProfile>();		
		// Make a pass through the generic list, converting each StorageLoadProfile to a
		// MongoLoadProfile. Conversion is simple, just use the saved JSONObject config object
		for( LoadProfile p : genericSchedule )
		{
			if( p instanceof StorageLoadProfile )
			{
				MongoLoadProfile m = new MongoLoadProfile( p.getConfig() );
				m._name = p._name;
				mongoSchedule.add( m );
			}
		}
		
		if( mongoSchedule.size() == 0 )
			throw new JSONException( "Unable to convert generic load schedule to mongodb-specific load schedule" );
		
		return mongoSchedule;
	}
}
