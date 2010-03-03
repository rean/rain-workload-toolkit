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

package radlab.rain;

import radlab.rain.util.PoissonSamplingStrategy;

public class OperationSummary 
{
	public long succeeded 					= 0;
	public long failed 						= 0;
	public long totalActions				= 0;
	public long totalResponseTime 			= 0;
	public long totalAsyncInvocations		= 0;
	public long totalSyncInvocations		= 0;
	public long minResponseTime				= Long.MAX_VALUE;
	public long maxResponseTime				= Long.MIN_VALUE;
	// Sample the response times so that we can give a "reasonable" 
	// estimate of the 90th and 99th percentiles.	
	private PoissonSamplingStrategy responseTimeSampler; 
	
	public OperationSummary( PoissonSamplingStrategy strategy )
	{
		this.responseTimeSampler = strategy;
	}
	
	public long getNthPercentileResponseTime( int pct )
	{
		return this.responseTimeSampler.getNthPercentile( pct );
	}
	
	public boolean acceptSample( long respTime )
	{
		return this.responseTimeSampler.accept( respTime );
	}
	
	public void resetSamples()
	{
		this.responseTimeSampler.reset();
	}
	
	public int getSamplesSeen()
	{
		return this.responseTimeSampler.getSamplesSeen();
	}
	
	public int getSamplesCollected()
	{
		return this.responseTimeSampler.getSamplesCollected();
	}
	public double getAverageResponseTime()
	{
		if( this.succeeded == 0 )
			return 0.0;
		else return (double) this.totalResponseTime/(double)this.succeeded;
	}
}
