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

package radlab.rain.workload.sample;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;

/**
 * The SampleGenerator class generates operations for a single user thread
 * by producing the next operation to execute given the last operation. The
 * next operation is decided through the use of a load mix matrix. 
 */
public class SampleGenerator extends Generator
{
	
	// Operation indices used in the mix matrix.
	public static final int OPERATION1 = 0;
	public static final int OPERATION2 = 1;
	
	private java.util.Random _randomNumberGenerator;
	private HttpTransport _http;
	/**
	 * Initialize a <code>SampleGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public SampleGenerator( ScenarioTrack track )
	{
		super( track );
		
		// TODO: Fill me in.
	}
	
	/**
	 * Initialize this generator.
	 */
	public void initialize()
	{
		this._randomNumberGenerator = new java.util.Random();
		this._http = new HttpTransport();
	}
	
	/**
	 * Returns the next <code>Operation</code> given the <code>lastOperation</code>
	 * according to the current mix matrix.
	 * 
	 * @param lastOperation     The last <code>Operation</code> that was executed.
	 */
	public Operation nextRequest( int lastOperation )
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		int nextOperation = -1;
		
		if( lastOperation == -1 )
		{
			nextOperation = 0;
		}
		else
		{
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(currentLoad.getMixName()).getSelectionMix();
			double rand = this._randomNumberGenerator.nextDouble();
			
			int j;
			for ( j = 0; j < selectionMix.length; j++ )
			{
				if ( rand <= selectionMix[lastOperation][j] )
				{
					break;
				}
			}
			nextOperation = j;
		}
		return getOperation( nextOperation );
	}
	
	/**
	 * Returns the current think time. The think time is duration between
	 * receiving the response of an operation and the execution of its
	 * succeeding operation during synchronous execution (i.e. closed loop).
	 */
	public long getThinkTime()
	{
		return 0;
	}
	
	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	public long getCycleTime()
	{
		return 0;
	}
	
	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	public void dispose()
	{
		// TODO: Fill me in.
	}
	
	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}
	
	
	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex   The type of operation to instantiate.
	 * @return          A prepared operation.
	 */
	public Operation getOperation( int opIndex )
	{
		switch( opIndex )
		{
			case OPERATION1: return this.createOperation1();
			case OPERATION2: return this.createOperation2();
			default:         return null;
		}
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared Operation1.
	 */
	public Operation1 createOperation1()
	{
		Operation1 op = new Operation1( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared Operation2.
	 */
	public Operation2 createOperation2()
	{
		Operation2 op = new Operation2( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
}
