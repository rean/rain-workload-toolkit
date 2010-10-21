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

package radlab.rain.workloadtraits;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;

/* Create a load schedule based on the relative load-levels from 6/25/09 - 6/26/09, the 
   date of Michael Jackson's death. Diurnal pattern with a peak-to-avg ratio of 1.26 
   (avg-to-trough ratio of 1.39). Spans 27 hours from 00:00 6/25/09 to 02:00 6/26/09 */
public class WikipediaScheduleCreator extends LoadScheduleCreator 
{
	private static NumberFormat FORMATTER = new DecimalFormat( "00000" );
	public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";
	
	// Original sequence reqs/sec: [1785L, 1692L, 1732L, 1775L, 1726L, 1665L, 1541L, 1403L, 1314L, 1318L, 1342L, 1353L, 1441L, 1602L, 1839L, 1973L, 2085L, 2100L, 2209L, 2236L, 2297L, 2290L, 2176L, 2297L, 2166L, 1972L, 1960L]
	private int _initialWorkload = 1;
	private double[] _relativeLoads = {1, 0.94789915966386551, 1.0236406619385343, 1.0248267898383372, 0.97239436619718311, 0.96465816917728853, 0.92552552552552547, 0.91044776119402981, 0.9365645046329294, 1.0030441400304415, 1.0182094081942337, 1.0081967213114753, 1.065040650406504, 1.1117279666897988, 1.1479400749063671, 1.0728656878738445, 1.0567663456664977, 1.0071942446043165, 1.0519047619047619, 1.0122227252150293, 1.0272808586762074, 0.99695254680017409, 0.95021834061135368, 1.0556066176470589, 0.94296909011754459, 0.91043397968605722, 0.99391480730223125};
	private int _incrementSize = 10; // 10 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
	
	public WikipediaScheduleCreator() 
	{}
	
	public int getIncrementSize() { return this._incrementSize; }
	public void setIncrementSize( int val ){ this._incrementSize = val; }
	
	public int getIncrementsPerInterval() { return this._incrementsPerInterval; }
	public void setIncrementsPerInterval( int val ){ this._incrementsPerInterval = val; }
	
	public LinkedList<LoadProfile> createSchedule( JSONObject config ) throws JSONException
	{
		// Pull out the base offset
		if( config.has( CFG_INITIAL ) )
			this._initialWorkload = config.getInt( CFG_INITIAL );
		
		if( config.has(CFG_INCREMENT_SIZE) )
			this._incrementSize = config.getInt( CFG_INCREMENT_SIZE );
		
		if( config.has( CFG_INCREMENTS_PER_INTERVAL) )
			this._incrementsPerInterval = config.getInt( CFG_INCREMENTS_PER_INTERVAL );

		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		for( int i = 0; i < this._relativeLoads.length; i++ )
		{
			long intervalLength = this._incrementSize * this._incrementsPerInterval;
			if( i == 0 )
				loadSchedule.add( new LoadProfile( intervalLength, this._initialWorkload, "default", 0, FORMATTER.format(i) ) );
			else 
			{	
				int users = 0;
				users = (int) Math.round( loadSchedule.getLast().getNumberOfUsers() * this._relativeLoads[i] );
				
				loadSchedule.add( new LoadProfile( intervalLength, users, "default", 0, FORMATTER.format(i) ) );
			}
		}
		
		return loadSchedule;
	}

	public static void main( String[] args ) throws JSONException
	{
		WikipediaScheduleCreator creator = new WikipediaScheduleCreator();
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
			System.out.println( p.getNumberOfUsers() );
	}
}
