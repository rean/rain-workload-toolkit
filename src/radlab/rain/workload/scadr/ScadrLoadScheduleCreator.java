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

package radlab.rain.workload.scadr;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;
import radlab.rain.workloadtraits.WikipediaScheduleCreator;

public class ScadrLoadScheduleCreator extends LoadScheduleCreator 
{

	public ScadrLoadScheduleCreator() 
	{
	}

	@Override
	public LinkedList<LoadProfile> createSchedule( JSONObject params ) throws JSONException 
	{
		// Scale the wikipedia workload and return that
		WikipediaScheduleCreator creator = new WikipediaScheduleCreator();
		creator.setIncrementSize( 20 ); // 20 second increments
		creator.setIncrementsPerInterval( 2 ); // each interval lasts (2 * 20) seconds
		creator.setInitialWorkload( 100 ); // Use a base workload of 100 users
	
		// The schedule refers to a mix-matrix named "default", we can create that
		// or go through the schedule and change it to something else
		
		// Would like to give a duration and have the workload stretched/compressed into that
		return creator.createSchedule( new JSONObject() );
		
		
		/*
		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		// Use subclass load profile here and set all the extra special things
		// then pack it in a generic container that uses the base class, the
		// ScadrGenerator can cast it to the more specific ScadrLoadProfile
		//ScadrLoadProfile i1 = new ScadrLoadProfile( 40, 400,  "default", 0, "00000" );
		//ScadrLoadProfile i2 = new ScadrLoadProfile( 40, 1000, "default", 0, "00001" ); 
		//ScadrLoadProfile i3 = new ScadrLoadProfile( 40, 1200, "default", 0, "00002" );
		//ScadrLoadProfile i4 = new ScadrLoadProfile( 40, 900,  "default", 0, "00003" );
		//ScadrLoadProfile i5 = new ScadrLoadProfile( 40, 500,  "default", 0, "00004" );
		//ScadrLoadProfile i6 = new ScadrLoadProfile( 40, 200,  "default", 0, "00005" );
		
		ScadrLoadProfile debug = new ScadrLoadProfile( 30, 1,  "default" );
		
		//loadSchedule.add( i1 );
		//loadSchedule.add( i2 );
		//loadSchedule.add( i3 );
		//loadSchedule.add( i4 );
		//loadSchedule.add( i5 );
		//loadSchedule.add( i6 );
		
		loadSchedule.add( debug );
		return loadSchedule;*/
	}

}
