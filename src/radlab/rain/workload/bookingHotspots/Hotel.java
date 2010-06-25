package radlab.rain.workload.bookingHotspots;

public class Hotel {
	
	private String name;
	private Boolean expectedFound;
	
	public Hotel(String name, Boolean expectedFound) {
		this.name = name;
		this.expectedFound = expectedFound;
	}
	
	public String getName() { return name; }
	public Boolean expectedFound() { return expectedFound; }
	public String toString() { return name+" (expected:"+expectedFound+")"; }
}
