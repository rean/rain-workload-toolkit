package radlab.rain.hotspots;

import java.util.ArrayList;

import cern.jet.random.Gamma;
import cern.jet.random.engine.MersenneTwister;

public class Dirichlet {

	private static Gamma gamma = new Gamma(1, 1, new MersenneTwister()); 
	
	public static ArrayList<Double> sample(Integer n, ArrayList<Double> alpha) {
		ArrayList<Double> samples = new ArrayList<Double>();
		Double sum = 0.0;
		for (Integer i=0; i<n; i++) {
			Double g = gamma.nextDouble(alpha.get(i),1.0);
			//System.out.println( g );
			sum += g;
			samples.add(g);
		}
		//System.out.println( sum );
		for (Integer i=0; i<n; i++) samples.set(i, samples.get(i)/sum);
		return(samples);
	}
	
	public static void main( String[] args )
	{
		ArrayList<Double> alpha = new ArrayList<Double>();
		alpha.add( 0.3 );
		alpha.add( 0.3 );
		alpha.add( 0.3 );
		ArrayList<Double> samples = Dirichlet.sample( 3, alpha );
		for( Double d : samples )
		{
			System.out.print( d + " " );
		}
		System.out.println( "" );
	}
}
