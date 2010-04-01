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

package radlab.rain.workload.scads.keys;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class ZipfKeyGenerator extends KeyGenerator
{
	protected String name = "Zipf";

	protected Random random = new Random();

	/** Lower bound of the key(s) generated. */
	protected int lowerBound;

	/** Upper bound of the key(s) generated. */
	protected int upperBound;

	/** Shape of the Zipf distribution; larger value implies taller peaks. */
	protected double a;

	/** Random number used to shuffle keys around. */
	protected double r;

	public ZipfKeyGenerator( JSONObject configObj ) throws JSONException
	{
		this( configObj.getDouble( A_CONFIG_KEY ),
			  configObj.getDouble( R_CONFIG_KEY ),
			  configObj.getInt( MIN_KEY_CONFIG_KEY ),
			  configObj.getInt( MAX_KEY_CONFIG_KEY ) );
	}

	public ZipfKeyGenerator( double a, double r, int minKey, int maxKey )
	{
		if ( a <= 1 ) {
			throw new RuntimeException( "Zipf distribution requires a > 1: a = " + a );
		}

		this.a = a;
		this.r = r;
		this.lowerBound = minKey;
		// maxKey is inclusive, upperBound is exclusive.
		this.upperBound = maxKey + 1;
	}

	public int generateKey()
	{
		int k = -1;
		do {
			k = sampleZipf();
		} while ( k > upperBound );
		return Math.abs( (Double.valueOf( ( k + 1 ) * r ) ).hashCode() ) % ( upperBound - lowerBound ) + lowerBound;
	}

	private int sampleZipf()
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
}
