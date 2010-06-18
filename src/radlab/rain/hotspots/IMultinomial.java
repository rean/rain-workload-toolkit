package radlab.rain.hotspots;

import java.util.ArrayList;

public interface IMultinomial {
	public Integer size();
	public Integer sampleOne();
	public ArrayList<Integer> sampleWithReplacement(Integer n);
	public ArrayList<Integer> sampleWithoutReplacement(Integer n);
}
