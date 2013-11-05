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

public class ParetoBounded 
{
	private double _alpha = 0.0;
	private double _lowerBound = 0.0;
	private double _upperBound = 0.0;
	private Random _random;
	
	public ParetoBounded( double alpha, double L, double H )
	{
		this._alpha = alpha;
		this._lowerBound = L;
		this._upperBound = H;
		this._random = new Random();
	}

	public ParetoBounded( double alpha, double L, double H, Random rng )
	{
		this._alpha = alpha;
		this._lowerBound = L;
		this._upperBound = H;
		this._random = rng;
	}

	// Courtesy: http://en.wikipedia.org/wiki/Pareto_distribution
	public double nextDouble()
	{
		double rndValU = this._random.nextDouble();
		double numerator = (rndValU * Math.pow( this._upperBound,this._alpha )) - (rndValU * Math.pow( this._lowerBound,this._alpha )) - Math.pow( this._upperBound, this._alpha );  
		double denominator = Math.pow( this._upperBound,this._alpha ) * Math.pow( this._lowerBound,this._alpha );
		double next = Math.pow( (-1 * (numerator/denominator)), -1.0/this._alpha );
		return next;
	}
	
	public static void main(String[] args) 
	{
		double total = 0.0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		int iterations = 1000;
		ParetoBounded dist = new ParetoBounded( 0.1, 1, 2000 );
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
		
		System.out.println( "Avg: " + (total/(double)iterations) );
		System.out.println( "Min: " + min );
		System.out.println( "Max: " + max );
	}

}
