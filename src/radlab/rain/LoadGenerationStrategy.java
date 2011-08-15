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

import java.util.concurrent.ExecutorService;

/**
 * The LoadGenerationStrategy abstract class is a basic thread that keeps
 * track of its state (waiting to begin, active, or inactive) and associates
 * itself with a generator that creates operations.
 */
public abstract class LoadGenerationStrategy extends Thread
{
	/** The states in which a LoadGenerationStrategy thread can be in. */
	public enum LGState
	{
		/** This thread is waiting until the start time to begin. */
		WaitingToBegin,
		/** This thread is currently active. */
		Active,
		/** This thread is currently sleeping. */
		Inactive
	}
	
	/** An operation representing the lack of a last operation. */
	public static int NO_OPERATION_INDEX = -1;
	
	/** A value representing that a time property has not been set. */
	public static long TIME_NOT_SET = -1;
	
	/** The generator used to create operations for this thread. */
	protected Generator _generator;
	
	protected long _timeStarted      = TIME_NOT_SET;
	protected long _startSteadyState = TIME_NOT_SET;
	protected long _endSteadyState   = TIME_NOT_SET;
	protected long _timeToQuit       = TIME_NOT_SET;
	
	/** The unique ID of this thread. */
	protected long _id = -1;
	
	/** The current state of this thread. */
	protected LGState _lgState = LGState.WaitingToBegin;
	
	/** If false, requests won't be issued; only the trace is generated. */
	protected boolean _interactive = true;
	
	/** Determine whether we async requests should be limited/throttled down to a max of x/sec */
	protected long _sendNextRequest = -1;
	protected LoadProfile _lastLoadProfile = null;
	
	
	/** The shared pool of worker threads. */
	protected ExecutorService _sharedWorkPool;
	
	/**
	 * Creates a new LoadGenerationStrategy thread.
	 * 
	 * @param generator     The generator to associate with this thread.
	 * @param id            The unique ID of this thread.
	 */
	public LoadGenerationStrategy( Generator generator, long id )
	{
		this._generator = generator;
		this._id = id;
		
		StringBuffer trackName = new StringBuffer( generator.getTrack().getName() );
		if( trackName.length() > 0 )
		{
			this.setName( trackName.append( ".Generator-" ).append( this._id ).toString() );
		}
		else
		{
			this.setName( "NoTrack.Generator-" + this._id );
		}
	}
	
	/**
	 * Set the shared pool of worker threads.
	 * 
	 * @param workPool  The shared pool to use.
	 */
	public void setSharedWorkPool( ExecutorService workPool )
	{
		this._sharedWorkPool = workPool;
	}
	
	public long getTimeStarted() { return this._timeStarted; }
	public void setTimeStarted( long val ) { this._timeStarted = val; }
	
	public long getStartSteadyState() { return this._startSteadyState; }
	
	public boolean getInteractive() { return this._interactive; }
	public void setInteractive( boolean val ) { this._interactive = val; }
	
	public void resetRateLimitCounters()
	{
		this._sendNextRequest = -1;
	}
	
	public abstract void run();
	
	public abstract void dispose();
	
	// Do the operation synchronously or asynchronously
	
	public void doOperation( Operation operation )
	{
		// Set the time the operation was queued (not how long it takes).
		operation.setTimeQueued( System.currentTimeMillis() );
		
		// Execute the operation differently based on whether it should be run
		// synchronously or asynchronously.
		if( !operation.getAsync() )
		{
			operation.run();
		}
		else 
		{
			LoadProfile currentProfile = operation.getGeneratedDuringProfile();
			if( currentProfile != null )
			{				
				long activeUsers = currentProfile._numberOfUsers;
				long aggRatePerSec = currentProfile._openLoopMaxOpsPerSec;
				
				if( aggRatePerSec == 0 )
				{
					// no rate limit, just submit and leave
					this._sharedWorkPool.submit( operation );
				}
				else
				{
					// Check whether intervals have changed if they have then reset the rate counters
					long now = System.currentTimeMillis(); 
					
					if( now >= this._sendNextRequest ) 
					{
						double myRate = (aggRatePerSec)/(double) activeUsers;
						// Send at most 1 per second
						if( myRate == 0 )
							myRate = 1000.0;
						else if( myRate < 0 )
						{
							; // probabilistic send
							myRate = 1000.0;
						}
						
						double waitIntervalMsecs = (1000.0/myRate)*2;
						
						this._sendNextRequest = System.currentTimeMillis() + new Double(waitIntervalMsecs).longValue();
						//System.out.println( this.getName() + " my rate: " + myRate + " wait interval: " + waitIntervalMsecs +  " now: " + now + " next Request @ " + this._sendNextRequest );
						// Send a request now and figure out the timestamp of the next request based on the
						// rate
						this._sharedWorkPool.submit( operation );	
					}
					else
					{
						// Sleep until it's time to send another request
						long sleepTime = this._sendNextRequest - now;
						try 
						{
							Thread.sleep( sleepTime );
						} 
						catch (InterruptedException e) 
						{
							System.out.println( this.getName() + " interrupted from sleep" );
						}
						
						this._sharedWorkPool.submit( operation );
					}
				}
			}
			else this._sharedWorkPool.submit( operation );
		}
	}
}
