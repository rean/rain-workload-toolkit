package radlab.rain.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class Histogram<T> 
{
	public static NumberFormat Formatter = new DecimalFormat( "#0.0000" );
	private long _totalObservations = 0; 
	private TreeMap<T,Long> _hist = new TreeMap<T,Long>();
	
	// Simple accumulator
	public Histogram()
	{
		this.reset();
	}
	
	public void reset()
	{
		// Reset the observation counter
		this._totalObservations = 0;
		// Purge the tree map
		this._hist.clear();
	}
	
	public long getTotalObservations()
	{ return this._totalObservations; }
	
	public void addObservation( T obs )
	{
		// Increment the total number of observations seen
		this._totalObservations++;
		
		Long value = this._hist.get( obs );
		if( value != null )
		{
			value++;
		}
		else
		{
			value = new Long(1);
		}
		this._hist.put( obs, value );
	}
	
	public CDF<T> convertToCdf()
	{
		Iterator<T> keyIt = this._hist.keySet().iterator();
		ArrayList<T> labels = new ArrayList<T>();
		double[] frequency = new double[this._hist.size()];
		
		int i = 0;
		
		while( keyIt.hasNext() )
		{
			T current = keyIt.next();
			labels.add( current );
			Long count = this._hist.get(current);
			// Capture the number of requests for this item
			frequency[i] = count.doubleValue();
			i++;
		}
		return new CDF<T>( labels, frequency );
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		Iterator<T> it = this._hist.keySet().iterator();
		while( it.hasNext() )
		{
			T key = it.next();
			long count = this._hist.get( key );
			double frequency = (double) count / (double) this._totalObservations;
			buf.append( key ).append( " [" ).append( count ).append( "," ).append( Formatter.format( frequency ) ).append( "]\n" );
		}
		
		return buf.toString();
	}
}
