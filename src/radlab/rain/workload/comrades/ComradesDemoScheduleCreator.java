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

package radlab.rain.workload.comrades;

import java.util.LinkedList;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;

public class ComradesDemoScheduleCreator extends LoadScheduleCreator 
{
	private static NumberFormat FORMATTER = new DecimalFormat( "00000" );
	public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";

	private int _initialWorkload = 10;
	private double[] _relativeLoads = {1,
			   2,
			   3,
			   4,
			   5,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6,
			   6
	};
	private int _incrementSize = 360; // 60 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
	
	
	public ComradesDemoScheduleCreator() 
	{}

	public int getInitialWorkload() { return this._initialWorkload; }
	public void setInitialWorkload( int val ){ this._initialWorkload = val; }
	
	public int getIncrementSize() { return this._incrementSize; }
	public void setIncrementSize( int val ){ this._incrementSize = val; }
	
	public int getIncrementsPerInterval() { return this._incrementsPerInterval; }
	public void setIncrementsPerInterval( int val ){ this._incrementsPerInterval = val; }
		
	@Override
	public LinkedList<LoadProfile> createSchedule(JSONObject config) throws JSONException 
	{
		// Pull out the base offset
		if( config.has( CFG_INITIAL ) )
			this._initialWorkload = config.getInt( CFG_INITIAL );
		
		if( config.has(CFG_INCREMENT_SIZE) )
			this._incrementSize = config.getInt( CFG_INCREMENT_SIZE );
		
		if( config.has( CFG_INCREMENTS_PER_INTERVAL) )
			this._incrementsPerInterval = config.getInt( CFG_INCREMENTS_PER_INTERVAL );

		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		// Add a long interval with 1 thread for debugging
		// loadSchedule.add( new LoadProfile( 300, 1, "default", 0, "debug" ) );
		
		// Set the transition time to be half of the interval
		long transitionTime = (this._incrementSize * this._incrementsPerInterval)/2;
		
		for( int i = 0; i < this._relativeLoads.length; i++ )
		{
			long intervalLength = this._incrementSize * this._incrementsPerInterval;
			if( i == 0 )
				loadSchedule.add( new LoadProfile( intervalLength, this._initialWorkload, "default", transitionTime, FORMATTER.format(i) ) );
			else 
			{	
				int users = 0;
				users = (int) Math.round( loadSchedule.getFirst().getNumberOfUsers() * this._relativeLoads[i] );
				
				loadSchedule.add( new LoadProfile( intervalLength, users, "default", transitionTime, FORMATTER.format(i) ) );
			}
		}
		
		return loadSchedule;		
	}
	
	public static void main( String[] args ) throws JSONException
	{
		ComradesDemoScheduleCreator creator = new ComradesDemoScheduleCreator();
		
		creator.setInitialWorkload( 10 );
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
			System.out.println( p.getNumberOfUsers() );
	}
}
