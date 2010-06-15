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

package radlab.rain.workload.cloudstoneNull;

import java.util.logging.Logger;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.ObjectPool;
import radlab.rain.util.NegativeExponential;

public class CloudstoneNullGenerator extends Generator 
{
	private java.util.Random _rng;
	//private radlab.rain.workload.cloudstone.Random _randomUtil;
	//private HttpTransport _http;
	private Logger _logger;

	public Logger getLogger()
    { return this._logger; }
    
	
	/*      @Row({  0, 11, 52, 36,  0, 1,  0 }), // Home Page
            @Row({  0,  0, 60, 20,  0, 0, 20 }), // Login
            @Row({ 21,  6, 41, 31,  0, 1,  0 }), // Tag Search
            @Row({ 72, 21,  0,  0,  6, 1,  0 }), // Event Detail
            @Row({ 52,  6,  0, 31, 11, 0,  0 }), // Person Detail
            @Row({  0,  0,  0,  0,100, 0,  0 }), // Add Person
            @Row({  0,  0,  0,100,  0, 0,  0 })  // Add Event
          */
	public static final int HOME_PAGE		= 0;
	public static final int LOGIN			= 1;
	public static final int TAG_SEARCH		= 2;
	public static final int EVENT_DETAIL	= 3;
	public static final int PERSON_DETAIL	= 4;
	public static final int ADD_PERSON		= 5;
	public static final int ADD_EVENT		= 6;

	private NegativeExponential _thinkTimeGenerator  = null;
	private NegativeExponential _cycleTimeGenerator = null;
	
	public CloudstoneNullGenerator(ScenarioTrack trk) 
	{
		super(trk);
		
		// Initalize random utility generators and HttpTransport instances
		this._rng = new java.util.Random();
		// Initialize the cycle time and think time generators. If you want non-stop
		// activity, then set mean cycle time, and mean think times to 0 and the
		// number generators should just *always* return 0 for the think/cycle time
		this._cycleTimeGenerator = new NegativeExponential( trk.getMeanCycleTime()*1000 );
		this._thinkTimeGenerator = new NegativeExponential( trk.getMeanThinkTime()*1000 );
	}

	@Override
	public void dispose() 
	{

	}

	@Override
	public long getCycleTime() 
	{
		long nextCycleTime = (long) this._cycleTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextCycleTime, (5*this._cycleTime) );
		//return 0;
	}

	@Override
	public long getThinkTime() 
	{
		long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextThinkTime, (5*this._thinkTime) );
		// return 0;
	}

	@Override
	public void initialize() 
	{
		this._logger = Logger.getLogger( this._name );
	}

	@Override
	public Operation nextRequest( int lastOperation ) 
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		int nextOperation = -1;
		
		if( lastOperation == -1 )
			nextOperation = 0;
		else
		{
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(currentLoad.getMixName()).getSelectionMix();
			double rand = this._rng.nextDouble();
		
			int j;
			for( j = 0; j < selectionMix.length; j++ )
			{
				//System.out.println( "selectmix[" + lastOperation + "][" + j + "]:" +  selectionMix[lastOperation][j] );
				if( rand <= selectionMix[lastOperation][j] )
				{
					break;
				}
			}
			nextOperation = j;
		}		
		return getOperation( nextOperation );
	}
	
	public Operation getOperation( int opIndex )
	{
		// We know about 7 high-level Cloudstone operations
		/*
		 	public static final int HOME_PAGE		= 0;
			public static final int LOGIN			= 1;
			public static final int TAG_SEARCH		= 2;
			public static final int EVENT_DETAIL	= 3;
			public static final int PERSON_DETAIL	= 4;
			public static final int ADD_PERSON		= 5;
			public static final int ADD_EVENT		= 6;
	 
		 */
		switch( opIndex )
		{
			case HOME_PAGE: return this.createHomePageOperation();
			case LOGIN: return this.createLoginOperation();
			case TAG_SEARCH: return this.createTagSearchOperation();
			case EVENT_DETAIL: return this.createEventDetailOperation();
			case PERSON_DETAIL: return this.createPersonDetailOperation();
			case ADD_PERSON: return this.createAddPersonOperation();
			case ADD_EVENT: return this.createAddEventOperation();	
			default: return null;
		}
	}
	
	// Factory methods
	public HomePageNullOperation createHomePageOperation()
	{
		HomePageNullOperation op = null;
		ObjectPool pool = this.getTrack().getObjectPool();
		op = (HomePageNullOperation) pool.rentObject( HomePageNullOperation.NAME );
		// Nothing available in pool so get an instance the tried and true way.
		if( op == null )
			op = new HomePageNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	
	public LoginNullOperation createLoginOperation()
	{
		LoginNullOperation op = null;
		ObjectPool pool = this.getTrack().getObjectPool();
		op = (LoginNullOperation) pool.rentObject( LoginNullOperation.NAME );
		
		if( op == null )
			op = new LoginNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	
	public TagSearchNullOperation createTagSearchOperation()
	{
		TagSearchNullOperation op = null; 
		ObjectPool pool = this.getTrack().getObjectPool();
		op = (TagSearchNullOperation) pool.rentObject( TagSearchNullOperation.NAME );
		// Nothing available in pool so get an instance the tried and true way.
		if( op == null )
			op = new TagSearchNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	
	public EventDetailNullOperation createEventDetailOperation()
	{
		EventDetailNullOperation op = null;
		ObjectPool pool = this.getTrack().getObjectPool();
		op = (EventDetailNullOperation) pool.rentObject( EventDetailNullOperation.NAME );
		
		if( op == null )
			op = new EventDetailNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		
		op.prepare( this );
		return op;
	}
	
	public PersonDetailNullOperation createPersonDetailOperation()
	{
		PersonDetailNullOperation op = new PersonDetailNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public AddPersonNullOperation createAddPersonOperation()
	{
		AddPersonNullOperation op = new AddPersonNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public AddEventNullOperation createAddEventOperation()
	{
		AddEventNullOperation op = new AddEventNullOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
}
