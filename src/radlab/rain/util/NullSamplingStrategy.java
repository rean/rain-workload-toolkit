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

import java.util.LinkedList;

public class NullSamplingStrategy implements ISamplingStrategy 
{
	private LinkedList<Long> _samples = new LinkedList<Long>();
	private int _currentSample = 0;
	private long _sampleSum = 0;
	
	public NullSamplingStrategy() 
	{}

	// accept always keeps each sample seen
	@Override
	public boolean accept( long value ) 
	{
		this._currentSample++;
		this._sampleSum += value;
		this._samples.add(value);
		return true;
	}

	@Override
	public double getMeanSamplingInterval() 
	{
		return 0; // Returns 0 by design, sampling interval irrelevant for NullSamplingStrategy (it accepts every sample seen)
	}

	@Override
	public long getNthPercentile(int pct) 
	{
		return PoissonSamplingStrategy.getNthPercentile( pct, this._samples );
	}

	@Override
	public LinkedList<Long> getRawSamples() 
	{
		return this._samples;
	}

	@Override
	public double getSampleMean() 
	{
		long samples = this.getSamplesCollected();
		if( samples == 0 )
			return 0.0;
		else return (double) this._sampleSum / (double) samples;
	}

	@Override
	public double getSampleStandardDeviation() 
	{
		long samples = this.getSamplesCollected();
		if( samples == 0 || samples == 1 )
			return 0.0;
		
		double sampleMean = this.getSampleMean();
		
		// Sum the deviations from the mean for all items
		double deviationSqSum = 0.0;
		for( Long value : this._samples )
		{
			// Print out value so we can debug the sd computation
			//System.out.println( value );
			deviationSqSum += Math.pow( (double)(value - sampleMean), 2 );
		}
		// Divide deviationSqSum by N-1 then return the square root
		return Math.sqrt( deviationSqSum/(double)(samples - 1) );
	}

	@Override
	public int getSamplesCollected() 
	{
		return this._samples.size();
	}

	@Override
	public int getSamplesSeen() 
	{
		return this._currentSample;
	}

	@Override
	public double getTvalue(double populationMean) 
	{
		long samples = this.getSamplesCollected();
		if( samples == 0 || samples == 1 )
			return 0.0;
		
		return ( this.getSampleMean() - populationMean ) / ( this.getSampleStandardDeviation()/Math.sqrt( this.getSamplesCollected() ) );
	}

	@Override
	public void reset() 
	{
		this._currentSample = 0;
		this._samples.clear();
		this._sampleSum = 0;
	}

	@Override
	public void setMeanSamplingInterval(double val) 
	{
		// Empty by design (sampling intervals are irrelevant for the NullSamplingStrategy: it accepts every sample)
	}

}
