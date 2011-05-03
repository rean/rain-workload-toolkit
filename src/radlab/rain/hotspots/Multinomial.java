package radlab.rain.hotspots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class Multinomial implements IMultinomial {
	private ArrayList<Double> probabilities;
	private ArrayList<Double> cdf;
	private Random rnd = new Random();
	
	public void shrink( int targetSize )
	{
		while( this.size() > targetSize )
		{
			this.probabilities.remove( (this.probabilities.size() - 1) );
		}
		this.probabilities = normalize( this.probabilities );
		this.cdf = computeCDF(this.probabilities);
	}
	
	public Multinomial( double[] arrProbabilities )
	{
		ArrayList<Double> rawData = new ArrayList<Double>();
		for( int i = 0; i < arrProbabilities.length; i++ )
			rawData.add( arrProbabilities[i] );
		this.probabilities = normalize( rawData );
		this.cdf = computeCDF(this.probabilities);
	}	
	
	public Multinomial(ArrayList<Double> probabilities) {
		this.probabilities = normalize(probabilities);
		this.cdf = computeCDF(this.probabilities);
	}
	
	public static Multinomial uniform(Integer n) {
		ArrayList<Double> p = new ArrayList<Double>();
		for (Integer i=0; i<n; i++) p.add(1.0);
		return(new Multinomial(p));
	}
	
	public static Multinomial zipf(Integer n, Double shape) {
		ArrayList<Double> p = new ArrayList<Double>();
		for (Integer i=0; i<n; i++)
			p.add(Math.pow(1.0/(i+1), shape));
		return(new Multinomial(p).shuffle());
	}
	
	public static Multinomial sparse(Integer k, Integer n) {
		ArrayList<Double> p = new ArrayList<Double>(n);
		for (Integer i=0; i<n; i++) 
			if (i<k) p.add(1.0);
			else p.add(0.0);
		return(new Multinomial(p).shuffle());
	}
	
	public static Multinomial sparse(ArrayList<Double> p, Integer n) {
		ArrayList<Double> s = new ArrayList<Double>(n);
		for (Integer i=0; i<n; i++) 
			if (i<p.size()) s.add(p.get(i));
			else p.add(0.0);
		return(new Multinomial(s).shuffle());
	}
	
	private ArrayList<Double> normalize(ArrayList<Double> p) {
		Double sum = 0.0;
		for (Double d: p) sum += d;
		
		ArrayList<Double> normalized = new ArrayList<Double>();
		for (Double d: p) normalized.add(d/sum);
		return(normalized);
	}
	
	private ArrayList<Double> computeCDF(ArrayList<Double> p) {
		ArrayList<Double> cdf = new ArrayList<Double>();
		Double sum = 0.0;
		for (Double d: p) {
			sum += d;
			cdf.add(sum);
		}
		return(cdf);
	}

	public Multinomial shuffle() {
		ArrayList<Double> shuffled = new ArrayList<Double>(probabilities);
		for (Integer i=0; i<size(); i++) {
			Integer j = rnd.nextInt(size()-i)+i;
			Double tmp = shuffled.get(i);
			shuffled.set(i, shuffled.get(j));
			shuffled.set(j, tmp);
		}
		return(new Multinomial(shuffled));
	}
	
	public Multinomial sort(Boolean increasing) {
		ArrayList<Double> sorted = new ArrayList<Double>(probabilities);
		if (increasing)
			Collections.sort(sorted);
		else
			Collections.sort(sorted, new Decreasing());
		return(new Multinomial(sorted));
	}
	
	public Integer size() { return(probabilities.size()); }
	
	/**
	 * Sampling from the multinomial distribution without replacement. This is a rejection method so
	 * it could be really slow when taking too many samples.
	 * 
	 * @param n Number of samples to draw (n should be less or equal to size of the Multinomial).
	 * @return
	 */
	public ArrayList<Integer> sampleWithoutReplacement(Integer n) {
		assert n<=size() : "can't sample that many values without replacement";
		
		HashSet<Integer> samples = new HashSet<Integer>();
		while (samples.size()<n) {
			Boolean haveNew = false;
			while (!haveNew) {
				Integer s = sampleOne();
				if (!samples.contains(s)) {
					haveNew=true;
					samples.add(s);
				}
			}
		}
		return(new ArrayList<Integer>(samples));
	}
	
	public ArrayList<Integer> sampleWithReplacement(Integer n) { 
		ArrayList<Integer> samples = new ArrayList<Integer>();
		for (Integer i=0; i<n; i++) samples.add(sampleOne());
		return(samples);
	}
	
	public Integer sampleOne() {
		Integer i0 = -1;
		Integer i1 = cdf.size();
		Integer i = 0;
		Double r = rnd.nextDouble() * cdf.get(cdf.size()-1);
		Boolean found = false;
		
		while (!found) {
			i = (i0+i1)/2;
//			System.out.println(r+" "+i0+" "+i+" "+i1);
			if ( (i==0&&r<=cdf.get(0)) || (i>0&&cdf.get(i-1)<r&&r<=cdf.get(i)) ) found = true;
			else if (i>0&&r<=cdf.get(i-1)) i1=i;
			else i0=i;			
		}
		return(i);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("probabilities: (");
		for (Double d: probabilities) sb.append(d+",");
		sb.append(")");
		return(sb.toString());
	}
	
	public class Decreasing implements java.util.Comparator<Double> {
		public int compare(Double d1, Double d2) { return( d2>d1 ? 1 : (d2==d1?0:-1) ); }
	} 
}
