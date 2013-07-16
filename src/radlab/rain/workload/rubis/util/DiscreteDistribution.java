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
 *
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013.
 */

package radlab.rain.workload.rubis.util;


import java.util.Arrays;
import java.util.Random;


/**
 * Generate random numbers according to the given probability table.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public final class DiscreteDistribution
{
	private double[] _cdf;

	public DiscreteDistribution(double[] probs)
	{
		if (probs.length > 0)
		{
			this._cdf = new double[probs.length];

			// Compute CDF
			double cumProb = probs[0];
			this._cdf[0] = probs[0];
			for (int i = 1; i < probs.length; ++i)
			{
				//this._cdf[i] = this._cdf[i-1]+probs[i];
				this._cdf[i] = cumProb;
				cumProb += probs[i];
			}
			// Normalize
			for (int i = 0; i < probs.length; ++i)
			{
				this._cdf[i] /= cumProb;
			}
		}
	}

	public int nextInt(Random rng)
	{
		double p = rng.nextDouble();

//		for (int x = 0; x < this._cdf.length; ++x)
//		{
//			if (p > this._cdf[x])
//			{
//				return x;
//			}
//		}
//		return this._cdf.length-1;

		int x = Arrays.binarySearch(this._cdf, p);

		return (x >= 0 ? x : -x);
	}
}
