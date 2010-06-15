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

public class Zipf 
{
	long _lowerBound = 0;
	long _upperBound = 0;
	double _a = 0.0;
	double _r = 0.0;
	private Random _random = new Random();
	
	private boolean _first = true;
	private double _c = 0; // Normalization constant
	
	public Zipf( double a, double r, long L, long H )
	{
		this._a = a;
		this._r = r;
		this._lowerBound = L;
		this._upperBound = H + 1;
	}
	
	public double nextDouble2()
	{
		
		// Look at the gap between the upper and lower bound.
		// We will be generating
		long N = this._upperBound - this._lowerBound;
		
		if( this._first == true )
		{
			for( int i = 1; i <= N; i++ )
				this._c = this._c + (1.0 / Math.pow( (double) i, this._a ) );
			
			this._c = 1.0 / this._c;
			this._first = false;
		}
		
		double rndU = 0.0;
		double sumProb = 0.0;
		
		while( rndU == 0.0 )
			rndU = this._random.nextDouble();
		
		// Map the random value to cdf
		for( int i = 1; i <= N; i++ )
		{
			sumProb += this._c / Math.pow( (double) i , this._a );
			if( sumProb >= rndU )
				return i + this._lowerBound;
		}
		
		return 0;
	}
	
	public double nextDouble()
	{
		double k = -1;
		do 
		{
			// Sample from zipf
			k = this.sampleZipf();
			//System.out.println(k);
		} while (k > this._upperBound);
		//System.out.println(k);
				
		return Math.abs( (Double.valueOf((k+1)*this._r)).hashCode() ) % (this._upperBound-this._lowerBound) + this._lowerBound;
		//return (k % (this._upperBound - this._lowerBound) ) + this._lowerBound;
	}
	
	// Courtesy: http://osdir.com/ml/lib.gsl.general/2008-05/msg00057.html
	// and http://cg.scs.carleton.ca/~luc/chapter_ten.pdf
	private double sampleZipf() 
	{
		double b = Math.pow(2, this._a-1);
		double u, v, x, t = 0.0;
		do 
		{
			u = this._random.nextDouble();
			v = this._random.nextDouble();
			x = Math.floor( Math.pow(u,-1.0/(this._a-1.0)));
			t = Math.pow(1.0+1.0/x, this._a-1.0);
		} while ( v*x*(t-1.0)/(b-1.0) > t/b );
		return x;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		double total = 0.0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		//double a = 1.001;
		//double r = 3.456;
		
		int iterations = 1000;
		//Zipf dist = new Zipf( 1.001, 3.456, 1, 1000 );
		Zipf dist = new Zipf( 1.001, 3.456, 1, 10000000 );
		//Zipf dist = new Zipf( 1.001, 3.456, 1, 5 );
		for( int i = 0; i < iterations; i ++ )
		{
			double val = dist.nextDouble();
			if( val < min )
				min = val;
			if( val > max )
				max = val;
			total += val;
			//System.out.println( val );
		}
		
		//System.out.println( "Avg: " + (total/(double)iterations) );
		//System.out.println( "Min: " + min );
		//System.out.println( "Max: " + max );
	}

}
