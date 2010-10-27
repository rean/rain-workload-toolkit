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

import java.util.Random;
import org.json.JSONException;
import java.util.LinkedList;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * The DefaultScenarioTrack class is a generic implementation of the abstract
 * <code>ScenarioTrack</code> class that supports load profiles that specify
 * the interval, number of users, mix behavior, and any transitions.
 */
public class DefaultScenarioTrack extends ScenarioTrack 
{
	private LoadManagerThread _loadManager;
	private Random _random = new Random();
	
	public DefaultScenarioTrack( Scenario parent )
	{
		super( parent );
		this._loadManager = new LoadManagerThread( this );
	}
	
	public DefaultScenarioTrack( String name, Scenario scenario )
	{
		super( scenario );
		this._name = name;
		this._loadManager = new LoadManagerThread( this );
	}
	
	/**
	 * Returns the current load profile.<br />
	 * <br />
	 * This method handles transitions by splitting each load profile interval
	 * into two parts:<br />
	 * <pre>
	 *     start                               end
	 *     [ interval proper | transition period ]
	 *                intervalEndTime    transitionEndTime
	 * </pre>
	 * During the interval proper the current load profile is simply returned.
	 * However, during the transition period, there is a probability that the
	 * next load profile (modulo the entire load profile sequence) will be
	 * returned instead. This probability is proportional to the elapsed time
	 * within the transition period (e.g. 10% into the transition period will
	 * yield the current load profile with 10% likelihood and the next load
	 * profile with 90% likelihood).
	 */
	public LoadProfile getCurrentLoadProfile() 
	{
		/*int currentLoadScheduleIndex = this._loadManager.loadScheduleIndex;
		int nextLoadScheduleIndex = ( currentLoadScheduleIndex + 1 ) % this._loadSchedule.size();
		
		LoadProfile currentProfile = this._loadSchedule.get( currentLoadScheduleIndex );
		LoadProfile nextProfile = this._loadSchedule.get( nextLoadScheduleIndex );*/
		
		// Leave it up to the load manager thread to determine the current and next load profiles
		LoadProfile currentProfile = this._loadManager.getCurrentLoadProfile();
		LoadProfile nextProfile = this._loadManager.getNextLoadProfile();
		
		// Calculate when the current interval ends and when the transition ends.
		long intervalEndTime = currentProfile.getTimeStarted() + currentProfile.getInterval();
		long transitionEndTime = intervalEndTime + currentProfile.getTransitionTime(); 
		
		long now = System.currentTimeMillis();
		
		if ( now >= currentProfile.getTimeStarted() && now <= transitionEndTime )
		{
			// Must either be in 1) interval proper, or 2) transition period.
			if ( now <= intervalEndTime )
			{
				return currentProfile;
			}
			else
			{
				double elapsedRatio = (double) ( now - intervalEndTime ) / (double) currentProfile.getTransitionTime();
				double randomDouble = this._random.nextDouble();
				
				// If elapsedTime = 90% then we'll want to select the currentProfile 10% of the time.
				// If elapsedTime = 10% then we'll want to select the currentProfile 90% of the time.
				if( randomDouble <= elapsedRatio )
				{
					return nextProfile;
				}
				else 
				{
					return currentProfile;
				}
			}
		}
		else if ( now > transitionEndTime )
		{
			/*
			 * If we make it here then the load scheduler thread has overslept
			 * and has not yet woken up to advance the load schedule. No
			 * worries, we'll just point at what should be the next profile.
			 */
			return nextProfile;
		}
		else // ( now < currentProfile.getTimestarted() )
		{
			/*
			 * If we make it here, that means the current time is before the
			 * current load profile interval. This should only happen during
			 * ramp up when we use the first load profile as the "ramp up"
			 * profile.
			 */
			return currentProfile;
		}
	}	
	
	public void submitDynamicLoadProfile( LoadProfile profile )
	{
		this._loadManager.submitDynamicLoadProfile( profile );
	}
	
	/**
	 * Checks whether a load profile is valid within a ScenarioTrack, e.g.,
	 * that the number of users is > 0, the mix name exists in the mixmap for the track,
	 * the behavioral hints are recognized by the track, etc.
	 * 
	 * @param profile	The load profile to be validated
	 * @return in	0 if the profile is considered valid, non-zero otherwise
	 */
	public int validateLoadProfile( LoadProfile profile )
	{
		// By default, all we we're interested in is whether:
		// 1) the number of users is > 0
		// 2) the mix name is in the mix map
		//boolean valid = true;
		int retVal = ScenarioTrack.VALID_LOAD_PROFILE; 
		
		if( profile._numberOfUsers <= 0 )
		{
			System.out.println( this + " Invalid load profile. Number of users <= 0. Profile details: " + profile.toString() );
			retVal = ScenarioTrack.ERROR_INVALID_LOAD_PROFILE_BAD_NUM_USERS;
		}
		
		if( profile._mixName.length() > 0 && !this._mixMap.containsKey( profile._mixName ) )
		{
			System.out.println( this + " Invalid load profile. mixname not in track's mixmap. Profile details: " + profile.toString() );
			retVal = ScenarioTrack.ERROR_INVALID_LOAD_PROFILE_BAD_MIX_NAME;
		}
		
		// Do capping of number of users, don't fail validation though
		int maxUsersForTrack = this.getMaxUsers();
		if( profile._numberOfUsers > maxUsersForTrack )
		{
			System.out.println( this + " LoadProfile validation capping number of users to " + maxUsersForTrack + "." );
			profile._numberOfUsers = maxUsersForTrack;
		}
		
		return retVal;
	}
	
	/**
	 * Initializes a ScenarioTrack with reasonable defaults. This is mainly used
	 * to initialize a testing track.
	 * 
	 * @param generatorClassName    The class name of the generator to use.
	 * @param hostname              Hostname of the target.
	 * @param port                  Port of the target.
	 * @throws JSONException 
	 */
	public void initialize( String generatorClassName, String hostname, int port ) throws JSONException
	{
		// 1) Open-Loop Probability
		this._openLoopProbability = 0.0;
		// 2) Concrete Generator
		this._generatorClassName = generatorClassName;
		try
		{
			// Create generator with no parameters by passing null
			this._generator = this.createWorkloadGenerator( this._generatorClassName, null );
		}
		catch ( Exception e )
		{
			System.out.println( "ERROR creating default generator. Reason: " + e.toString() );
			System.exit( 1 );
		}
		// 3) Target Information
		this._targetHostname = hostname;
		this._targetPort = port;
		// 4) Log Sampling Probability
		this._logSamplingProbability = 1.0;
		// 5) Mean Cycle Time in seconds
		this._meanCycleTime = 1.0;
		// 6) Mean Think Time in seconds
		this._meanThinkTime = 5.0;
		// 7) Interactive?
		this._interactive = true;
		// 8) Load Profile Array
		LoadProfile i1 = new LoadProfile( 30, 400,  "default" );
		LoadProfile i2 = new LoadProfile( 60, 1000, "default" ); 
		LoadProfile i3 = new LoadProfile( 40, 1200, "default" );
		LoadProfile i4 = new LoadProfile( 40, 900,  "default" );
		LoadProfile i5 = new LoadProfile( 40, 500,  "default" );
		LoadProfile i6 = new LoadProfile( 40, 200,  "default" );
		this._loadSchedule.clear();
		this._loadSchedule.add( i1 );
		this._loadSchedule.add( i2 );
		this._loadSchedule.add( i3 );
		this._loadSchedule.add( i4 );
		this._loadSchedule.add( i5 );
		this._loadSchedule.add( i6 );
		// 9) Load a default mix matrix.
		MixMatrix defaultMixMatrix = new MixMatrix();
		defaultMixMatrix.normalize();
		this._mixMap.put( "default", defaultMixMatrix );
	}
		
	public void start()
	{
		if( this._loadManager.isAlive() )
		{
			return;
		}
		
		System.out.println( this + " starting load scheduler" );
		this._loadManager.start();
	}
	
	public void end()
	{
		if( !this._loadManager.isAlive() )
		{
			return;
		}
		
		try
		{
			System.out.println(  this + " stopping load scheduler" );
			this._loadManager.setDone( true );
			this._loadManager.interrupt();
			this._loadManager.join();
		}
		catch( InterruptedException ie )
		{}
		
		//this._objPool.shutdown();
	}
	
	/**
	 * The LoadManagerThread class is responsible for advancing the current
	 * load profile as time passes, cycling back to the initial load profile
	 * if the run has not ended by the time the last load profile 
	 * specified has been reached.
	 */
	protected class LoadManagerThread extends Thread
	{
		/** The track for which this thread is responsible. */
		private ScenarioTrack _track = null;
		
		private LoadProfile _currentProfile = null;
		
		/** If true, this thread will stop advancing the load profile. */
		private boolean _done = false;
		
		/** The current load profile index. */
		int loadScheduleIndex = 0;
		
		/** Date formatter */
		private DateFormat formatter = null;
		private Calendar cal = null;
		
		/** Allow external agents to augment the current load schedule by providing dynamic load profiles */
		LinkedList<LoadProfile> _dynamicLoadProfiles = new LinkedList<LoadProfile>();
		
		public boolean getDone() { return this._done; }
		public void setDone( boolean val ) { this._done = val; }
		
		public LoadManagerThread( ScenarioTrack track )
		{
			this._track = track;
		}
		
		public LoadProfile getCurrentLoadProfile()
		{
			// Pull the current load profile from a local variable stash
			//return this._track._loadSchedule.get( this.loadScheduleIndex );
			return this._currentProfile;
		}
		
		public LoadProfile getNextLoadProfile()
		{
			// Compute the next load profile from what should be the current load schedule index
			int nextLoadScheduleIndex = (this.loadScheduleIndex + 1) % this._track._loadSchedule.size();
			return this._track._loadSchedule.get( nextLoadScheduleIndex );
		}
		
		public void submitDynamicLoadProfile( LoadProfile profile )
		{
			synchronized( this._dynamicLoadProfiles )
			{
				this._dynamicLoadProfiles.add( profile );
			}
		}
		
		public void run()
		{
			long now = System.currentTimeMillis();
			long rampUp = this._track.getRampUp() * 1000;
			
			// Prepare the first load profile before ramp up so that we at
			// lease have a load profile to use during the ramp up.
			this._currentProfile = this._track._loadSchedule.get( loadScheduleIndex );
			this._track._currentLoadProfile = this._currentProfile;
			this._track._currentLoadProfile.setTimeStarted( now + rampUp );
			this._track._currentLoadProfile._activeCount++;
			System.out.println( this + " ramping up for " + rampUp + "ms." );
			
			this.formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
			this.cal = Calendar.getInstance();
			
			try {
				Thread.sleep( rampUp );
			} catch (InterruptedException e1) {
				System.out.println( this + " interrupted during ramp up... exiting." );
				this._done = true;
				return;
			}
			
			System.out.println( this + " Ramp up finished!" );
			System.out.flush();
			
			cal.setTimeInMillis(now);
			System.out.println( this + " current time: " + formatter.format(cal.getTime()) + " " + this._track._currentLoadProfile.toString() );
			
			while( !this.getDone() )
			{
				try
				{
					// Sleep until the next load/behavior change.
					Thread.sleep( this._track._currentLoadProfile.getInterval() + this._track._currentLoadProfile.getTransitionTime() );
					
					// If time reading is even then push on a dynamic load profile. 
					// Simple integration testing
					/*if( now % 2 == 0 )
					{
						LoadProfile dlp = new LoadProfile( 30, 500,  "default", 0, "dlp" );
						this._dynamicLoadProfiles.push( dlp );
					}*/
					
					// Decide what to do next, either load the next profile in the schedule OR
					// load a dynamic load profile if one exists
					// Check for a dynamic load profile
					if( this._dynamicLoadProfiles.size() > 0 )
					{
						// Try to acquire a lock on the list
						System.out.println( this + " Dynamic load profile found! Attempting to load..." );
						// Grab lock before pop - only this thread pops, all other threads push, so after we see that the list is non-empty
						// its size can only increase, so pop should not fail
						LoadProfile dynProfile = null;
						synchronized( this._dynamicLoadProfiles )
						{
							dynProfile = this._dynamicLoadProfiles.pop();
						}
						
						// Just in case, make sure that we acutally got a "real"/valid load profile
						if( dynProfile != null && this._track.validateLoadProfile( dynProfile ) == ScenarioTrack.VALID_LOAD_PROFILE )
						{
							System.out.println( this + " Dynamic load profile passed validation..." );
							now = System.currentTimeMillis();
							// Store this dynProfile as the current
							this._currentProfile = dynProfile;
							// Update the track
							this._track._currentLoadProfile = this._currentProfile;
							this._track._currentLoadProfile._activeCount++;
							this._track._currentLoadProfile.setTimeStarted( now );
							System.out.println( this + " Dynamic load profile activated! Profile: " + dynProfile.toString() );
						}
						else
						{
							System.out.println( this + " Dynamic load profile failed validation. Advancing load schedule the usual way." );
							// Advance the schedule the old-fashioned way and if that returns false, then we're done
							if( !this.advanceSchedule() )
							{
								System.out.println( this + " end of load schedule... exiting." );
								this._done = true;
							}
						}// End-else loadprofile failed track validation
					}// End loading the next load profile from the dynamic profile list
					else// If not dynamic load profiles available do things the regular way
					{
						// Advance the schedule and if that returns false, then we're done
						if( !this.advanceSchedule() )
						{
							System.out.println( this + " end of load schedule... exiting." );
							this._done = true;
						}
					}// End-selecting next load profile the regular way
				}
				catch( InterruptedException ie )
				{
					System.out.println( this + " interrupted... exiting." );
					this._done = true;
				}
				catch( Exception e )
				{
					System.out.println( this + " died... exiting. Reason: " + e.toString() );
					this._done = true;
				}
			}
			System.out.println( this + " finished!" );
		}
		
		/**
		 * Default way to advance the load schedule
		 * @return	true if schedule advanced or false if at the end of the schedule
		 */
		public boolean advanceSchedule()
		{
			long now = 0;
			
			loadScheduleIndex = ( loadScheduleIndex + 1 ) % this._track._loadSchedule.size();
			// If we reach index 0, we cycled; log it.
			if ( loadScheduleIndex == 0 )
			{
				System.out.println( this + " cycling." );
			}
			
			// Update the track's reference of the current load profile.
			if ( loadScheduleIndex < this._track._loadSchedule.size() )
			{
				System.out.println( this + " advancing load schedule" );
				
				now = System.currentTimeMillis();
				
				// Save the current loadprofile locally as well as in the track
				this._currentProfile = this._track._loadSchedule.get( loadScheduleIndex );
				this._track._currentLoadProfile = this._currentProfile;
				this._track._currentLoadProfile._activeCount++;
				this._track._currentLoadProfile.setTimeStarted( now );
				
				cal.setTimeInMillis(now);
				System.out.println( this + " current time: " + formatter.format(cal.getTime()) + " " + this._track._currentLoadProfile.toString() );
				return true;
			}
			else 
			{
				//System.out.println( this + " end of load schedule... exiting." );
				//this._done = true;
				return false;
			}
		}
		
		public String toString()
		{
			return "[LOAD SCHEDULER TRACK: " + this._track._name + "]";
		}
	}
}
