package radlab.rain.workload.httptest;

import java.util.LinkedList;
import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;

public class HttpTestScheduleCreator extends LoadScheduleCreator 
{
	@Override
	public LinkedList<LoadProfile> createSchedule() 
	{
		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		// Mix names used here should match what's in the behavior
		LoadProfile i1 = new LoadProfile( 310, 1, "default", 0, "first" );
		
		/*LoadProfile i2 = new LoadProfile( 15, 40, "default", 0 );
		LoadProfile i3 = new LoadProfile( 45, 100, "default", 0 );
		LoadProfile i4 = new LoadProfile( 40, 150, "default", 0 );
		LoadProfile i5 = new LoadProfile( 60, 200, "default", 0 );
		LoadProfile i6 = new LoadProfile( 40, 150, "default", 0 );
		LoadProfile i7 = new LoadProfile( 45, 100, "default", 0 );
		LoadProfile i8 = new LoadProfile( 35, 40, "default", 0 );*/
		
		loadSchedule.add( i1 );
		/*loadSchedule.add( i2 );
		loadSchedule.add( i3 );
		loadSchedule.add( i4 );
		loadSchedule.add( i5 );
		loadSchedule.add( i6 );
		loadSchedule.add( i7 );
		loadSchedule.add( i8 );*/
		
		return loadSchedule;
	}
}
