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

package radlab.rain.util.storage;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.util.Histogram;

public class ZipfKeyGenerator extends KeyGenerator 
{
	/** Configuration property for setting the sampling method. */
	public static final String SAMPLING_METHOD_CONFIG_KEY = "zipf.method";
	/** Value for the SAMPLING_METHOD_CONFIG_KEY used to specify the direct sampling method. */
	public static final String DIRECT_SAMPLING_METHOD_CONFIG_VALUE = "direct";
	/** Value for the SAMPLING_METHOD_CONFIG_KEY used to specify the rejection inversion sampling method. */
	public static final String REJECTION_SAMPLING_METHOD_CONFIG_VALUE = "rejection";
	/** Constant to specify the direct sampling method. */
	public static final int DIRECT_SAMPLING_METHOD = 0;
	/** Constant to specify the rejection inversion sampling method. */
	public static final int REJECTION_SAMPLING_METHOD = 1;

	protected String name = "Zipf";

	protected Random random = null;

	/** Shape of the Zipf distribution; larger value implies taller peaks. */
	protected double a;

	/** Random number used to shuffle keys around. */
	protected double r;

	protected double[] cdf;
	
	/** The sampling method (either direct or rejection sampling). */
	protected int _method = DIRECT_SAMPLING_METHOD;

	public ZipfKeyGenerator( JSONObject configObj ) throws JSONException
	{
		this( configObj.getDouble( A_CONFIG_KEY ),
			  configObj.getDouble( R_CONFIG_KEY ),
			  configObj.getInt( MIN_KEY_CONFIG_KEY ),
			  configObj.getInt( MAX_KEY_CONFIG_KEY ),
			  configObj.getLong( RNG_SEED_KEY ),
			  parseSamplingMethod( configObj.getString( SAMPLING_METHOD_CONFIG_KEY ) ) );
	}

	public ZipfKeyGenerator( double a, double r, int minKey, int maxKey, long seed )
	{
		this(a, r, minKey, maxKey, seed, DIRECT_SAMPLING_METHOD);
	}

	public ZipfKeyGenerator( double a, double r, int minKey, int maxKey, long seed, int method )
	{
		if ( a <= 1 ) {
			throw new RuntimeException( "Zipf distribution requires a > 1: a = " + a );
		}

		this.a = a;
		this.r = r;
		this.lowerBound = minKey;
		// maxKey is inclusive, upperBound is exclusive.
		this.upperBound = maxKey + 1;
		this.seed = seed;
		this.random = new Random( this.seed );
		this._method = method;
	}

	public int generateKey()
	{
		int key = -1;

		switch (this._method)
		{
			case DIRECT_SAMPLING_METHOD:
				// Generate zipf numbers directly
				key = this.generateKeyDirect();
				break;
			case REJECTION_SAMPLING_METHOD:
				// Generate zipf numbers directly
				key = this.generateKeyReject();
				break;
		}

		return key;
	}
	
	public int generateKeyReject()
	{
		int k = -1;
		do {
			k = sampleZipfReject();
		} while ( k > upperBound );
		return Math.abs( (Double.valueOf( ( k + 1 ) * r ) ).hashCode() ) % ( upperBound - lowerBound ) + lowerBound;
	}

	public int generateKeyDirect()
	{
		int k = this.sampleZipfDirect();
		// Unlike the rejection method, we won't get values out of bounds
		return Math.abs( (Double.valueOf( ( k + 1 ) * r ) ).hashCode() ) % ( upperBound - lowerBound ) + lowerBound;
	}
	
	// Compute Zipfian numbers directly
	private double[] computeZipfCdf()
	{
		// Compute the zipf probabilities directly using the shape parameter
		int keys = this.upperBound - this.lowerBound;
		double[] probabilities = new double[keys];
		double[] cdf = new double[keys];
		double sum = 0.0;
				
		for( int i = 0; i < keys; i++ )
		{
			probabilities[i] = Math.pow( 1.0/(i+1), this.a );
			sum += probabilities[i];
			//System.out.println( "i: " + i + " p(i) raw: " + p_i[i] );
		}
		
		//System.out.println( "Normalizing" );
		
		// Normalize the probabilities so they sum to 1, and compute the cdf using the normalized probabilities
		for( int i = 0; i < keys; i++ )
		{
			probabilities[i] /= sum; // Normalized probability
			
			//System.out.println( Math.log( i+1 ) + "\t" + /*"i: " + i + " p(i) norm: " +*/ Math.log(probabilities[i]) );
			
			if( i == 0 )
				cdf[i] = probabilities[i];
			else cdf[i] = cdf[i-1] + probabilities[i];
			
			//System.out.println( (i+1) + " " + probabilities[i] + " " + cdf[i] );
		}
		return cdf;
	}
	
	private int sampleZipfDirect()
	{
		if( this.cdf == null )
			this.cdf = this.computeZipfCdf();
				
		// Generate a random number
		double rndVal = this.random.nextDouble();
		int i = 0;
		for( i = 0; i < cdf.length; i++ )
		{
			if( rndVal <= cdf[i] )
				break;
		}
		
		return i + this.lowerBound;
	}
	
	// Rejection method for generating Zipfian numbers
	// See: Non-Uniform Random Variate Generation, Chapter 10: Discrete Univariate Distributions,  
	// Luc Devroye (http://luc.devroye.org/rnbookindex.html)
	private int sampleZipfReject()
	{
		double b = Math.pow( 2, a - 1 );
		double u, v, x, t = 0.0;
		do {
			u = random.nextDouble();
			v = random.nextDouble();
			x = Math.floor( Math.pow( u, -1.0 / ( a - 1.0 ) ) );
			t = Math.pow( 1.0 + 1.0 / x, a - 1.0 );
		} while ( v * x * ( t - 1.0 ) / ( b - 1.0 ) > t / b );
		return (int) x;
	}
		
	private static int parseSamplingMethod(String methodStr)
	{
		int method = DIRECT_SAMPLING_METHOD;
		if (methodStr.equalsIgnoreCase(DIRECT_SAMPLING_METHOD_CONFIG_VALUE))
		{
			method = DIRECT_SAMPLING_METHOD;
		}
		else if (methodStr.equalsIgnoreCase(REJECTION_SAMPLING_METHOD_CONFIG_VALUE))
		{
			method = REJECTION_SAMPLING_METHOD;
		}
		return method;
	}

	public static void main( String[] args )
	{
		/*double probSum = 0.0;
		int numItems = 1000;
		double[] p_i = new double[numItems];
		
		for( int i = 0; i < numItems; i++ )
		{
			p_i[i] = Math.pow( 1.0/(i+1), 1.001 );
			probSum += p_i[i];
			//System.out.println( "i: " + i + " p(i) raw: " + p_i[i] );
		}
		// Normalize
		for( int i = 0; i < numItems; i++ )
		{
			System.out.println( Math.log( i+1 ) + "\t" + /*"i: " + i + " p(i) norm: " +*/ //Math.log(p_i[i]/probSum) );
		//}
		
		ZipfKeyGenerator g1 = new ZipfKeyGenerator( 1.001, 3.456, 1, 1000, 1 );
		ZipfKeyGenerator g2 = new ZipfKeyGenerator( 1.001, 3.456, 1, 1000, 1 );
		Histogram<Integer> h1 = new Histogram<Integer>();
		Histogram<Integer> h2 = new Histogram<Integer>();
		
		int keys = 1000000;
		
		for( int i = 0; i < keys; i++ )
		{
			int key1 = g1.generateKey();
			int key2 = g2.generateKeyDirect();
			h1.addObservation( key1 );
			h2.addObservation( key2 );
			//System.out.println( key1 + " " + key2 );
		}
		
		System.out.println( h1.toString() );
		System.out.println( "-----xxxxxxxxxxxx-----" );
		System.out.println( h2.toString() );
				
		//double[] p1 = h1.getKeyPopularity();
		//double[] p2 = h2.getKeyPopularity();
		
		// Need to preserve the key rank
		/*
		for( int i = 0; i < p1.length; i++ )
		{
			System.out.println( Math.log( i+1 ) + "\t" + Math.log( p1[i] ) );
		}
		
		System.out.println( "-----xxxxxxxxxxxx-----" );
		
		for( int i = 0; i < p2.length; i++ )
		{
			System.out.println( Math.log( i+1 ) + "\t" + Math.log( p2[i] ) );
		}
		*/
		
		/*
		for( int i = 0; i < keys; i++ )
		{
			System.out.println( Math.log( i+1 ) + "\t" + Math.log( p1[i] ) + "\t" + Math.log( p2[i] ) );		
		}
		*/
	}
}
