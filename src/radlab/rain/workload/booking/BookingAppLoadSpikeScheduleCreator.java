package radlab.rain.workload.booking;

import java.util.LinkedList;
import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;

public class BookingAppLoadSpikeScheduleCreator extends LoadScheduleCreator 
{

	@Override
	public LinkedList<LoadProfile> createSchedule() 
	{
		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		/*LoadProfile i1 = new LoadProfile( 30, 400,  "default" );
		LoadProfile i2 = new LoadProfile( 60, 1000, "default" ); 
		LoadProfile i3 = new LoadProfile( 40, 1200, "default" );
		LoadProfile i4 = new LoadProfile( 40, 900,  "default" );
		LoadProfile i5 = new LoadProfile( 40, 500,  "default" );
		LoadProfile i6 = new LoadProfile( 40, 200,  "default" );
		
		loadSchedule.add( i1 );
		loadSchedule.add( i2 );
		loadSchedule.add( i3 );
		loadSchedule.add( i4 );
		loadSchedule.add( i5 );
		loadSchedule.add( i6 );*/
		
		// 4X increase over 100 seconds, sustained for 10 minutes followed by a return to the original load level over 100 seconds
		LoadProfile i1 = new LoadProfile( 50, 100, "default", 5 );
		LoadProfile i2 = new LoadProfile( 20, 160, "default", 5 );
		LoadProfile i3 = new LoadProfile( 20, 220, "default", 5 );
		LoadProfile i4 = new LoadProfile( 20, 280, "default", 5 );
		LoadProfile i5 = new LoadProfile( 20, 340, "default", 5 );
		LoadProfile i6 = new LoadProfile( 20, 400, "default", 5 );
		LoadProfile i7 = new LoadProfile( 600, 400, "default", 5 );
		LoadProfile i8 = new LoadProfile( 30, 300, "default", 5 );
		LoadProfile i9 = new LoadProfile( 30, 200, "default", 5 );
		LoadProfile i10 = new LoadProfile( 30, 100, "default", 5 );
		LoadProfile i11 = new LoadProfile( 400, 100, "default", 5 );
		
		loadSchedule.add( i1 );
		loadSchedule.add( i2 );
		loadSchedule.add( i3 );
		loadSchedule.add( i4 );
		loadSchedule.add( i5 );
		loadSchedule.add( i6 );
		loadSchedule.add( i7 );
		loadSchedule.add( i8 );
		loadSchedule.add( i9 );
		loadSchedule.add( i10 );
		loadSchedule.add( i11 );
		
		return loadSchedule;
	}

}
