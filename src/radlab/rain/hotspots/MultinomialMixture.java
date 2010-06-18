package radlab.rain.hotspots;

import java.util.ArrayList;

public class MultinomialMixture implements IMultinomial {

	private Multinomial mix;
	private ArrayList<Multinomial> multinomials;
	
	public MultinomialMixture(ArrayList<Multinomial> multinomials, ArrayList<Double> weights) {
		assert multinomials.size()>0 : "need as least one Multinomial";
		assert multinomials.size()==weights.size() : "multinomials has to be the same length as weights";
		
		Integer size = multinomials.get(0).size();
		for (Integer i=0; i<size; i++)
			assert multinomials.get(i).size()==size : "all multinomials need to have the same size";
		
		this.mix = new Multinomial(weights);
		this.multinomials = multinomials;
	}

	public Integer sampleOne() { 
		return( multinomials.get( mix.sampleOne() ).sampleOne() ); 
	}

	public ArrayList<Integer> sampleWithReplacement(Integer n) {
		return( multinomials.get( mix.sampleOne() ).sampleWithReplacement(n) ); 
	}

	public ArrayList<Integer> sampleWithoutReplacement(Integer n) {
		return( multinomials.get( mix.sampleOne() ).sampleWithoutReplacement(n) ); 
	}

	public Integer size() {
		return( multinomials.get( mix.sampleOne() ).size() ); 
	}

}
