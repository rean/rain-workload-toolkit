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
			sum += g;
			samples.add(g);
		}
		for (Integer i=0; i<n; i++) samples.set(i, samples.get(i)/sum);
		return(samples);
	}
	
}
