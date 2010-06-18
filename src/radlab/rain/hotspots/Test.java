package radlab.rain.hotspots;

import java.util.ArrayList;

import radlab.rain.workload.booking.Hotel;

public class Test {

	public static void main(String[] args) {
		System.out.println("Uniform multinomial distribution");
		Multinomial m = Multinomial.uniform(100);
		System.out.println(m);
		
		System.out.println("100 samples with replacement:  ");
		for (Integer i: m.sampleWithReplacement(100))
			System.out.print(i+" ");
		System.out.println();
		
		System.out.println("5 samples without replacement:  ");
		for (Integer i: m.sampleWithoutReplacement(5))
			System.out.print(i+" ");
		System.out.println();
		System.out.println();
		
		System.out.println("Multinomial distribution following Zipf's law:");
		Multinomial z = Multinomial.zipf(100, 1.5);
		System.out.println(z);
		System.out.println("distribution with same probabilities, but sorted (notice that the probabilities sharply fall off):");
		System.out.println(z.sort(false)+"\n");
		
		System.out.println("Multinomial with most of the probabilities set to 0:");
		Multinomial s = Multinomial.sparse(4,20);
		System.out.println(s);
		
		
		// create hotel generator and sample from it
		ArrayList<Hotel> hotels = new ArrayList<Hotel>();
		hotels.add( new Hotel("Motel 8",true) );
		hotels.add( new Hotel("Best Western",true) );
		hotels.add( new Hotel("Hilton",true) );
		Multinomial hotelM = Multinomial.zipf(hotels.size(), 1.5);
		IObjectGenerator<Hotel> hotelGenerator = new SimpleObjectGenerator<Hotel>(hotels, hotelM);
		System.out.println("\nHotel generator:");
		System.out.println("Hotels: "+hotels);
		System.out.println("Hotel distribution: "+hotelM);
		System.out.print("Samples: ");
		for (int i=0; i<100; i++)
			System.out.print(hotelGenerator.next()+", ");
	}

	
	public class User {
		public String firstName;
		public String lastName;
		public User(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
	}
}
