/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package radlab.rain.util;

import java.util.TreeMap;
import java.util.Random;

public class EmpiricalCDF 
{
	// <cdf summary> = [<pctilemark,value>,...,<pctilemark,value>]
	private TreeMap<Double,Double> _cdfSummary = null;
	private double[] _rawCdf = null;
	private Random _random;
	
	public EmpiricalCDF( double[] rawCdf )
	{
		this._rawCdf = rawCdf;
		this._random = new Random();
	}
	
	public EmpiricalCDF( TreeMap<Double,Double> cdfSummary )
	{
		this._cdfSummary = cdfSummary;
		this._random = new Random();
	}
	
	public EmpiricalCDF( double[] rawCdf, Random rng )
	{
		this._rawCdf = rawCdf;
		this._random = rng;
	}
	
	public EmpiricalCDF( TreeMap<Double,Double> cdfSummary, Random rng )
	{
		this._cdfSummary = cdfSummary;
		this._random = rng;
	}
	
	public double nextDouble()
	{
		double rndValU = this._random.nextDouble();
		// Use either the rawcdf or cdf summary
		if( this._cdfSummary != null )
		{
			// Look at where this random number puts us on the cdf (percentile wise)
			// then interpolate between percentiles if necessary
			Object[] keys = this._cdfSummary.keySet().toArray();
			Double prevPctileMark = (Double) keys[0];
			
			// Return the value at the first percentile mark
			if( rndValU < ((Double) keys[0]).doubleValue() )
				return this._cdfSummary.get( (Double) keys[0] );
			
			// Start from the second percentile marker
			for( int i = 1; i < keys.length; i++ )
			{
				// Save the current percentile mark
				Double currentPctileMark = (Double) keys[i];
				// Get the previous and current percentile values
				Double prevPctileVal = this._cdfSummary.get( prevPctileMark );
				Double currentPctileVal = this._cdfSummary.get( currentPctileMark );
				
				// Are we somewhere between the previous percentile mark and the current one?
				if( rndValU < currentPctileMark )
				{
					// Interpolate between percentile marks and their associated values
					return (prevPctileVal  + (currentPctileVal - prevPctileVal )/(currentPctileMark - prevPctileMark)*(rndValU - prevPctileMark));
				}
				// Save where we were before the next iteration kicks in
				prevPctileMark = (Double) keys[i];
			}
			
			// If we get here then return the upper bound of the cdf
			return this._cdfSummary.get( keys[keys.length-1] );
		}
		else if( this._rawCdf != null )
		{
			
		}
		
		return 0.0;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		TreeMap<Double,Double> cdfSummary = new TreeMap<Double,Double>();
		// Load up percentiles
		cdfSummary.put( new Double(0.01), new Double( 10 ) );
		cdfSummary.put( new Double(0.25), new Double( 15 ) );
		cdfSummary.put( new Double(0.50), new Double( 17 ) );
		cdfSummary.put( new Double(0.75), new Double( 19 ) );
		cdfSummary.put( new Double(0.99), new Double( 30 ) );
		
		double total = 0.0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		EmpiricalCDF dist = new EmpiricalCDF( cdfSummary );
		int iterations = 10000;
		
		System.out.println( "Start" );
		for( int i = 0; i < iterations; i ++ )
		{
			double val = dist.nextDouble();
			if( val < min )
				min = val;
			if( val > max )
				max = val;
			total += val;
			System.out.println( val );
		}
		
		System.out.println( "Avg: " + (total/(double)iterations) );
		System.out.println( "Min: " + min );
		System.out.println( "Max: " + max );
	}

}
