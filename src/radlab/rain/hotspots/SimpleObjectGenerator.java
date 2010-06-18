package radlab.rain.hotspots;

import java.util.ArrayList;
import java.util.List;

public class SimpleObjectGenerator<O> implements IObjectGenerator<O> {

	private ArrayList<O> objects;
	private IMultinomial probabilities;
	
	public SimpleObjectGenerator(ArrayList<O> objects, IMultinomial probabilities) {
		assert objects.size()==probabilities.size() : "number of objects has to be equal to the size of the multinomial";
		
		this.objects = objects;
		this.probabilities = probabilities;
	}
	
	public O next() {
		return( objects.get( probabilities.sampleOne() ) );
	}

	public Integer numberOfObjects() {
		return(objects.size());
	}

	public List<O> objects() {
		return(new ArrayList(objects));
	}

}
