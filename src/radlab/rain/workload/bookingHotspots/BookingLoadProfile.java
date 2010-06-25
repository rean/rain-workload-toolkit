package radlab.rain.workload.bookingHotspots;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.hotspots.IObjectGenerator;

public class BookingLoadProfile extends LoadProfile {

	private IObjectGenerator<Hotel> hotelGenerator;
	
	public BookingLoadProfile( long interval, int numberOfUsers, String mixName, IObjectGenerator<Hotel> hotelGenerator ) {
		super(interval, numberOfUsers, mixName);
		this.hotelGenerator = hotelGenerator;
	}
	
	public BookingLoadProfile( long interval, int numberOfUsers, String mixName ) {
		super(interval, numberOfUsers, mixName);
	}

	public Hotel nextHotel() {
		return( hotelGenerator.next() );
	}
	
}
