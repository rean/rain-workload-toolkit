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

import java.util.Random;

public class NegativeExponential 
{
	private Random _random;
	private double _mean = 0.0;
	
	public NegativeExponential( double mean )
	{
		this._mean = mean;
		this._random = new Random();
	}
	
	public NegativeExponential( double mean, Random rng )
	{
		this._mean = mean;
		this._random = rng;
	}
	
	// Courtesy: http://www.sitmo.com/eq/513 - Generating an Exponential distributed random number
	// Note: we don't do any truncation e.g. to cap random numbers generated to n*mean or anything
	// clients/consumers can do any capping.
	public double nextDouble()
	{
		if( this._mean == 0 )
			return 0.0;
		
		double rndValU = this._random.nextDouble();
		double next = -1 * this._mean * Math.log( rndValU );	
		return next;
	}
	
	public double getMean() { return this._mean; }
	public void setMean( double val ) { this._mean = val; }
	
	public static void main( String[] args )
	{
		double total = 0.0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		int iterations = 100;//00000;
		NegativeExponential nexp = new NegativeExponential( 1/5.0 );
		for( int i = 0; i < iterations; i ++ )
		{
			double val = nexp.nextDouble();
			if( val < min )
				min = val;
			if( val > max )
				max = val;
			total += val;
			//System.out.println( val );
		}
		
		System.out.println( "Avg: " + (total/(double)iterations) );
		System.out.println( "Min: " + min );
		System.out.println( "Max: " + max );
	}
}
