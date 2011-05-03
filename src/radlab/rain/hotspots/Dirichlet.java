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
			//System.out.println( "g: " + g );
			sum += g;
			samples.add(g);
		}
		//System.out.println( sum );
		for (Integer i=0; i<n; i++) samples.set(i, samples.get(i)/sum);
		return(samples);
	}
	@SuppressWarnings("unused")
	public static void main( String[] args )
	{
		// Use the variance (V) to tune the entropy of the hot object set H
		// See Peter's paper: "Characterizing, Modeling, and Generating Workload Spikes for Stateful Services, in SOCC '10: Symposium on Cloud Computing, ACM, 2010" for more details.
		
		ArrayList<Double> alpha = new ArrayList<Double>();
		int N = 200; // 100 hot objects
		double low_variance = 1e-10; // All hot objects equally popular (entropy ~1)
		double mid_variance = 0.0045; // Some skew
		double high_variance = 0.5; // One object dominates (entropy ~0) 
		
		double variance = mid_variance; // Low variance high entropy (~equal popularity), High variance, low entropy (skewed popularity)
		double alpha_i = (N - 1 - (variance * Math.pow(N,2.0)) ) / (variance * Math.pow(N, 3.0));
		while( alpha_i <= 0 )
		{
			System.out.println( "Adjusting nominator" );
			variance /= 100.0;
			alpha_i = (N - 1 - (variance * Math.pow(N,2.0)) ) / (variance * Math.pow(N, 3.0));
		}
		
		// alpha_i can't be <= 0
		/*if( alpha_i < 0 )
		{
			System.out.println( "Compensating for negative alpha_i" );
			alpha_i = Math.abs( alpha_i );
		}*/
		
		System.out.println( "Alpha_i: " + alpha_i );
		
		for( int i = 0; i < N; i++ )
		{
			alpha.add( alpha_i );
		}
		
		double entropySum = 0.0;
		double sampleSum = 0.0;
		// Print the object popularities and compute the entropy as a double check to see whether we got the desired
		// result
		ArrayList<Double> samples = Dirichlet.sample( N, alpha );
		for( Double d : samples )
		{	
			//d = 1.0/20.0;
			System.out.print( d + " " );
			double temp = (d * ( Math.log10(d)/Math.log10(2) ) );
			if( !Double.isNaN( temp ) )
				entropySum += temp;
			else System.out.print( "skip " );
			
			sampleSum += d;
		}
		System.out.println( "" );
		
		entropySum *= -1;
		double entropy = entropySum/(Math.log10(N)/Math.log10(2));
		// Quick debugging
		System.out.println( "Sample sum   : " + sampleSum );
		System.out.println( "Entropy sum  : " + entropySum );
		System.out.println( "Entropy (H_0): " + entropy );
		
		// Create a multinomial using these probabilities, sample from it and check the actual distribution
		
		
		
		
		
		/*
		alpha.add( 0.3 );
		alpha.add( 0.3 );
		alpha.add( 0.3 );
		ArrayList<Double> samples = Dirichlet.sample( 3, alpha );
		for( Double d : samples )
		{
			System.out.print( d + " " );
		}
		System.out.println( "" );
		*/
	}
}
