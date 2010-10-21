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

package radlab.rain.workload.httptest;

import radlab.rain.ProfileCreator;
import radlab.rain.ScenarioTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* Example of a class that can programmatically create 1 or more tracks that describe a profile.
 * obviating the need for a profiles.config.xxx.json file. Useful if we had to generate 20+ tracks
 */
public class HttpTestProfileCreator extends ProfileCreator
{
	public static String CFG_BASE_HOST_IP_KEY 		= "baseHostIp";
	public static String CFG_NUM_HOST_TARGETS_KEY	= "numHostTargets";
	
	public JSONObject createProfile( JSONObject params ) throws JSONException
	{
		String baseHostIP = "127.0.0.1";
		int numHostTargets = 1;
		
		// See whether the input is non-null and has parameters for the base host ip prefix and/or
		// the number of ip targets
		if( params != null )
		{
			if( params.has( CFG_BASE_HOST_IP_KEY ) )
				baseHostIP = params.getString( CFG_BASE_HOST_IP_KEY );
			
			if( params.has( CFG_NUM_HOST_TARGETS_KEY) )
				numHostTargets = params.getInt( CFG_NUM_HOST_TARGETS_KEY );
		}
		
		// We may or may not use the input
		JSONObject trackConfig = new JSONObject();
		
		// Create a track config with "numHostTargets" tracks
		for( int i = 0; i < numHostTargets; i++ )
		{
			String trackName = "";
			
			if( i < 10 )
				trackName = "track-000" + i;
			else trackName = "track-00" + i;
			
			JSONObject trackDetails = new JSONObject();
			// Fill in details
			trackDetails.put( ScenarioTrack.CFG_GENERATOR_KEY, "radlab.rain.workload.httptest.HttpTestGenerator" ); 
			trackDetails.put( ScenarioTrack.CFG_TRACK_CLASS_KEY, "radlab.rain.DefaultScenarioTrack" );
			trackDetails.put( ScenarioTrack.CFG_RESOURCE_PATH, "resources/" );
			// Add in behavior and loadProfileCreatorClass
			
			/*"behavior": {
			"default" : [
					[0, 100,   0,   0,   0,   0,   0], 
					[0,   0, 100,   0,   0,   0,   0],
					[0,   0,   5,  60,  10,  15,  10],
					[0,   0,  10,   5,  35,  40,  10],
					[0,   0,  25,  45,   5,  20,   5],
					[0,   0,  40,  30,   5,  20,   5],
					[0,   0,  25,  20,  10,  40,   5]
			]
			},*/
						
			
			JSONObject behaviorDetails = new JSONObject();
						
			// Create an array for each row
			JSONArray row1 = new JSONArray( new int[] {0, 100,   0,   0,   0,   0,   0} );
			JSONArray row2 = new JSONArray( new int[] {0,   0, 100,   0,   0,   0,   0} );
			JSONArray row3 = new JSONArray( new int[] {0,   0,   5,  60,  10,  15,  10} );
			JSONArray row4 = new JSONArray( new int[] {0,   0,  10,   5,  35,  40,  10} ); 
			JSONArray row5 = new JSONArray( new int[] {0,   0,  25,  45,   5,  20,   5} );
			JSONArray row6 = new JSONArray( new int[] {0,   0,  40,  30,   5,  20,   5} );
			JSONArray row7 = new JSONArray( new int[] {0,   0,  25,  20,  10,  40,   5} );
			
			// Now create a JSONArray which stores each row
			JSONArray mix1 = new JSONArray();
			mix1.put( row1 );
			mix1.put( row2 );
			mix1.put( row3 );
			mix1.put( row4 );
			mix1.put( row5 );
			mix1.put( row6 );
			mix1.put( row7 );
			
			// Associate a mix matrix with a tag/name
			behaviorDetails.put( "default", mix1 );
			
			// Store the behavior details in the track config
			trackDetails.put( ScenarioTrack.CFG_BEHAVIOR_KEY, behaviorDetails );
			
			// Specifiy the load creator class
			trackDetails.put( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_KEY, "radlab.rain.workload.httptest.HttpTestScheduleCreator" );
						
			JSONObject targetDetails = new JSONObject();
			
			// Get base IP, split on . get last octet convert to int then add i
			String[] ipAddressParts = baseHostIP.split( "\\." );
			if( ipAddressParts.length != 4 )
				throw new JSONException( "Expected numerical IPv4 address format: N.N.N.N" );
			
			int lastOctet = Integer.parseInt( ipAddressParts[3] );
			StringBuffer targetIPAddress = new StringBuffer();
			targetIPAddress.append( ipAddressParts[0] );
			targetIPAddress.append( "." );
			targetIPAddress.append( ipAddressParts[1] );
			targetIPAddress.append( "." );
			targetIPAddress.append( ipAddressParts[2] );
			targetIPAddress.append( "." );
			targetIPAddress.append( (lastOctet+i) );
			
			System.out.println( "Target IP: " + targetIPAddress.toString() );
			
			targetDetails.put( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY, targetIPAddress.toString() );
			targetDetails.put( ScenarioTrack.CFG_TARGET_PORT_KEY, 8080 );
			
			trackDetails.put( ScenarioTrack.CFG_TARGET_KEY, targetDetails );
			trackDetails.put( ScenarioTrack.CFG_LOG_SAMPLING_PROBABILITY_KEY, 0.0 ); // No log sampling
			trackDetails.put( ScenarioTrack.CFG_OPEN_LOOP_PROBABILITY_KEY, 0.0 );
			trackDetails.put( ScenarioTrack.CFG_MEAN_CYCLE_TIME_KEY, 0 );
			trackDetails.put( ScenarioTrack.CFG_MEAN_THINK_TIME_KEY, 0 );
			trackDetails.put( ScenarioTrack.CFG_INTERACTIVE_KEY, true );

			// Set response time sampling interval - should be tuned based on the expected 
			// order of the expected number of operations/requests that will be issued/served
			// e.g. lower values if we're doing a short run with few operations and
			// larger values if we're doing a long run with many operations so we reduce
			// memory overhead of storing samples
			trackDetails.put( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL, 50 );
			
			trackConfig.put( trackName, trackDetails );
		}
		
		return trackConfig;
	}
}
