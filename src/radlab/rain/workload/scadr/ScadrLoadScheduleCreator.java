package radlab.rain.workload.scadr;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;

public class ScadrLoadScheduleCreator extends LoadScheduleCreator {

	public ScadrLoadScheduleCreator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public LinkedList<LoadProfile> createSchedule( JSONObject params ) throws JSONException 
	{
		
		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		// Use subclass load profile here and set all the extra special things
		// then pack it in a generic container that uses the base class, the
		// ScadrGenerator can cast it to the more specific ScadrLoadProfile
		ScadrLoadProfile i1 = new ScadrLoadProfile( 40, 400,  "default", 0, "00000" );
		ScadrLoadProfile i2 = new ScadrLoadProfile( 40, 1000, "default", 0, "00001" ); 
		ScadrLoadProfile i3 = new ScadrLoadProfile( 40, 1200, "default", 0, "00002" );
		ScadrLoadProfile i4 = new ScadrLoadProfile( 40, 900,  "default", 0, "00003" );
		ScadrLoadProfile i5 = new ScadrLoadProfile( 40, 500,  "default", 0, "00004" );
		ScadrLoadProfile i6 = new ScadrLoadProfile( 40, 200,  "default", 0, "00005" );
		
		//ScadrLoadProfile debug = new ScadrLoadProfile( 30, 1,  "default" );
		
		loadSchedule.add( i1 );
		loadSchedule.add( i2 );
		loadSchedule.add( i3 );
		loadSchedule.add( i4 );
		loadSchedule.add( i5 );
		loadSchedule.add( i6 );
		
		//loadSchedule.add( debug );
		return loadSchedule;
	}

}
