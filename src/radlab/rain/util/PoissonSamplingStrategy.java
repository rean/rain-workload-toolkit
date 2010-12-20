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

import java.util.Collections;
import java.util.LinkedList;

public class PoissonSamplingStrategy implements ISamplingStrategy
{
	public static long getNthPercentile( int pct, LinkedList<Long> samples )
	{
		if( samples.size() == 0 )
			return 0;
		Collections.sort( samples );
		int index = (int)  Math.round( (double) ( pct*( samples.size()+1 ) )/100.0 );
		if( index < samples.size() )
			return samples.get( index ).longValue();
		else return samples.get( samples.size() - 1 ); // Return the second last sample
	}
		
	private LinkedList<Long> _samples = new LinkedList<Long>();
	private int _nextSampleToAccept = 1;
	private int _currentSample = 0;
	private double _meanSamplingInterval = 1.0;
	private NegativeExponential _expRandom = null;
	private long _sampleSum = 0;
	
	public PoissonSamplingStrategy(double meanSamplingInterval) 
	{
		this._meanSamplingInterval = meanSamplingInterval;
		this._expRandom = new NegativeExponential(this._meanSamplingInterval);
		this.reset();
	}

	public double getMeanSamplingInterval()
	{
		return this._meanSamplingInterval;
	}
	
	public void setMeanSamplingInterval( double val )
	{
		this._meanSamplingInterval = val;
	}
	
	public void reset() 
	{
		this._currentSample = 0;
		this._nextSampleToAccept = 1;
		this._samples.clear();
		this._sampleSum = 0;
	}

	public int getSamplesCollected()
	{
		return this._samples.size();
	}
	
	public int getSamplesSeen()
	{
		return this._currentSample;
	}
	
	public long getNthPercentile( int pct )
	{
		return PoissonSamplingStrategy.getNthPercentile( pct, this._samples );
	}
	
	public double getSampleMean()
	{
		long samples = this.getSamplesCollected();
		if( samples == 0 )
			return 0.0;
		else return (double) this._sampleSum / (double) samples;
	}
	
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
	
	public double getTvalue( double populationMean )
	{
		long samples = this.getSamplesCollected();
		if( samples == 0 || samples == 1 )
			return 0.0;
		
		return ( this.getSampleMean() - populationMean ) / ( this.getSampleStandardDeviation()/Math.sqrt( this.getSamplesCollected() ) );
	}
	
	public boolean accept(long value) 
	{
		this._currentSample++;

		if (this._currentSample == this._nextSampleToAccept) 
		{
			this._sampleSum += value;
			this._samples.add(value);
			// Update the nextSampleToAccept
			double randExp = this._expRandom.nextDouble();
			//System.out.println( "Random exp: " + randExp );
			this._nextSampleToAccept = this._currentSample + (int) Math.ceil( randExp ) ;
			//System.out.println("Next sample to accept: " + this._nextSampleToAccept);
			return true;
		}
		return false;
	}

	/*
	public static void main(String[] args) 
	{
		// Generate 11,000,000 numbers - try uniform random and expRandom numbers
		// Sort them and compute the 90th and 99th percentiles for the ground truth
		// Pass each random number generated to a sampler and let it keep the ones it chooses and compute the percentiles on the captured samples
		// Compare the ground truth 90th and 99th percentiles with those computed by the sampler
		int maxNumbers = 1000000; //100000;//1000000;
		double meanSamplingInterval = 1000.0;
		LinkedList<Long> allSamplesUniform = new LinkedList<Long>();
		LinkedList<Long> allSamplesExp = new LinkedList<Long>();
		// Sampling strategies to compare
		PoissonSamplingStrategy expSamplerAllUniform = new PoissonSamplingStrategy( meanSamplingInterval );
		PoissonSamplingStrategy expSamplerExp = new PoissonSamplingStrategy( meanSamplingInterval );
		
		// Random number generators
		double populationSampleMean = 1000.0;
		Random random = new Random();
		NegativeExponential expRandom = new NegativeExponential( populationSampleMean );
		
		long totalUniform = 0;
		long maxUniform = Long.MIN_VALUE;
		long minUniform = Long.MAX_VALUE;
		
		long totalExp = 0;
		long maxExp = Long.MIN_VALUE;
		long minExp = Long.MAX_VALUE;
		
		// Generate numbers (according to a uniform and exponential distribution) with same mean
		for( int i = 0; i < maxNumbers; i++ )
		{
			long valUniform = Math.round( random.nextDouble()*(2*populationSampleMean) );
			totalUniform += valUniform;
			if( valUniform > maxUniform )
				maxUniform = valUniform;
			if( valUniform < minUniform )
				minUniform = valUniform;
			
			
			// Sample uniformly distributed numbers
			// expSamplerAllUniform.accept( valUniform );
			allSamplesUniform.add( valUniform );
						
			long valExp = (long) Math.ceil( expRandom.nextDouble() );
			totalExp += valExp;
			if( valExp > maxExp )
				maxExp = valExp;
			if( valExp <  minExp )
				minExp = valExp;
			
			// Sample exp distributed numbers
			// expSamplerExp.accept( valExp );
			allSamplesExp.add( valExp );
		}
		
		double meanUniform = (double)totalUniform/(double)maxNumbers;
		double meanExp = (double)totalExp/(double)maxNumbers;
		
		// Compute percentiles for normally distributed numbers and exponentially distributed numbers
		long uniform90th = PoissonSamplingStrategy.getNthPercentile( 90, allSamplesUniform );
		long uniform99th = PoissonSamplingStrategy.getNthPercentile( 90, allSamplesUniform );
		long exp90th = PoissonSamplingStrategy.getNthPercentile( 90, allSamplesExp );
		long exp99th = PoissonSamplingStrategy.getNthPercentile( 99, allSamplesExp );
		
		System.out.println( "Max uniform      : " + maxUniform );
		System.out.println( "Mean uniform     : " + meanUniform );
		System.out.println( "Min uniform      : " + minUniform );
		System.out.println( "90th uniform gt  : " + uniform90th );
		System.out.println( "90th uniform smp : " +  expSamplerAllUniform.getNthPercentile( 90 ) );
		System.out.println( "99th uniform gt  : " + uniform99th );
		System.out.println( "99th uniform smp : " +  expSamplerAllUniform.getNthPercentile( 99 ) );
		System.out.println( "# samples seen   : " + expSamplerAllUniform.getSamplesSeen() );
		System.out.println( "# samples saved  : " + expSamplerAllUniform.getSamplesCollected() );
		
		System.out.println( "" );
		System.out.println( "Max exp          : " + maxExp );
		System.out.println( "Mean exp         : " + meanExp );
		System.out.println( "Min exp          : " + minExp );
		System.out.println( "90th exp gt      : " + exp90th );
		System.out.println( "90th uniform smp : " + expSamplerExp.getNthPercentile( 90 ) );
		System.out.println( "99th exp gt      : " + exp99th );
		System.out.println( "99th uniform smp : " + expSamplerExp.getNthPercentile( 99 ) );
		System.out.println( "# samples seen   : " + expSamplerExp.getSamplesSeen() );
		System.out.println( "# samples saved  : " + expSamplerExp.getSamplesCollected() );
		
		// Use bootstrapping to quantify the variance in our samples
		int numTrials = 1000;
		long[] arrU90 = new long[numTrials];
		long[] arrU99 = new long[numTrials];
		long[] arrE90 = new long[numTrials];
		long[] arrE99 = new long[numTrials];
		
		long[] arrE90Usample = new long[numTrials];
		long[] arrE99Usample = new long[numTrials];
		
		LinkedList<Long> randomSamples = new LinkedList<Long>();
		
		System.out.println( "Starting bootstrapping..." );
		for( int i = 0; i < numTrials; i++ )
		{
			System.out.println( "Trial: " + i );
			// Create samplers
			randomSamples.clear();
			expSamplerAllUniform.reset();
			expSamplerExp.reset();
						
			// Go through the datasets allSamplesUniform and allSamplesExp and select a sample,
			for( int j = 0; j < maxNumbers; j++ )
			{
				if( j%100000 == 0 )
					System.out.println( "Number: " + j );
				
				expSamplerAllUniform.accept( allSamplesUniform.get(j) );
				expSamplerExp.accept( allSamplesExp.get( j ) );
				
				double randomVal = random.nextDouble();
				if( randomVal <= 0.001 )
					randomSamples.add( allSamplesExp.get( j ) );
			}
			
			System.out.println( "Computing percentiles for trial: " + i );
			arrU90[i] = expSamplerAllUniform.getNthPercentile( 90 );
			arrU99[i] = expSamplerAllUniform.getNthPercentile( 99 );
			arrE90[i] = expSamplerExp.getNthPercentile( 90 );
			arrE99[i] = expSamplerExp.getNthPercentile( 99 );
			arrE90Usample[i] = PoissonSamplingStrategy.getNthPercentile( 90, randomSamples );
			arrE99Usample[i] = PoissonSamplingStrategy.getNthPercentile( 99, randomSamples );
			
			System.out.println( "Poisson sampler uniform data : " + expSamplerAllUniform.getSamplesCollected() );
			System.out.println( "Poisson sampler exp data     : " + expSamplerExp.getSamplesCollected() );
			System.out.println( "Uniform sampler exp data     : " + randomSamples.size() );
			System.out.println( arrU90[i] + " " + arrU99[i] + " " + arrE90[i] + " " + arrE99[i] + " " + arrE90Usample[i] + " " + arrE99Usample[i] );
		}
		
		System.out.println( "Final results..." );
		System.out.println( "Poisson sampler uniform data : " + expSamplerAllUniform.getSamplesCollected() );
		System.out.println( "Poisson sampler exp data     : " + expSamplerExp.getSamplesCollected() );
		System.out.println( "Uniform sampler exp data     : " + randomSamples.size() );
		
		for( int i = 0; i < numTrials; i++ )
		{
			System.out.println( arrU90[i] + " " + arrU99[i] + " " + arrE90[i] + " " + arrE99[i] + " " + arrE90Usample[i] + " " + arrE99Usample[i] );
		}
	}*/
}
