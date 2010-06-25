package radlab.rain.workload.bookingHotspots;

import java.util.ArrayList;
import java.util.LinkedList;
import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;
import radlab.rain.hotspots.IObjectGenerator;
import radlab.rain.hotspots.Multinomial;
import radlab.rain.hotspots.SimpleObjectGenerator;

public class BookingAppHotspotScheduleCreator extends LoadScheduleCreator 
{

	@Override
	public LinkedList<LoadProfile> createSchedule() 
	{
		// create hotel objects
        String hotelSearchArray[] = {"", "W Hotel", "Marriott", "Hilton", "Doubletree", "Ritz", "Super 8", "No Tell Motel", "Conrad", "InterContinental", "Westin", "Mar", "Foo"};
        boolean expectHotelFoundArray[] = {true, true, true, true, true, true, true, false, true, true, true, true, false };
        ArrayList<Hotel> hotels = new ArrayList<Hotel>();
        for (int i=0; i<hotelSearchArray.length; i++)
        	hotels.add( new Hotel(hotelSearchArray[i], expectHotelFoundArray[i] ));
        
        // create popularity distribution of hotels (Zipfian with shape of 1.5)
        Multinomial m = Multinomial.zipf(hotels.size(), 1.5);
        
        // create hotel generator with the zipfian distribution
        IObjectGenerator<Hotel> hotelGenerator = new SimpleObjectGenerator<Hotel>(hotels, m);
        
        LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
        for (int i=1; i<=10; i++)
        	loadSchedule.add( new BookingLoadProfile(20, 10*i, "default", hotelGenerator) );
		
		return loadSchedule;
	}

}
