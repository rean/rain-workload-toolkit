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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.ProfileCreator;
import radlab.rain.ScenarioTrack;

public class ScadrDemoProfileCreator extends ProfileCreator 
{
		
	public ScadrDemoProfileCreator() 
	{}

	@Override
	public JSONObject createProfile(JSONObject params) throws JSONException 
	{
		// We may or may not use the input
		JSONObject trackConfig = new JSONObject();
		
		// Let's try creating a track config with 1 track
		for( int i = 0; i < 1; i++ )
		{
			String trackName = "SCADr";//"scadr-00" + i;
			JSONObject trackDetails = new JSONObject();
			// Fill in details
			trackDetails.put( ScenarioTrack.CFG_GENERATOR_KEY, "radlab.rain.workload.scadr.ScadrGenerator" );
			// Construct generator parameters
			JSONObject generatorParams = new JSONObject();
			
			generatorParams.put( ScadrGenerator.CFG_ZOOKEEPER_APP_SERVER_PATH, "/demo/apps/scadr/webServerList" );
			generatorParams.put( ScadrGenerator.CFG_USE_POOLING_KEY, true );
			generatorParams.put( ScadrGenerator.CFG_DEBUG_KEY, false );
			
			trackDetails.put( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY, generatorParams );
			
			trackDetails.put( ScenarioTrack.CFG_TRACK_CLASS_KEY, "radlab.rain.workload.scadr.ScadrScenarioTrack" );
			trackDetails.put( ScenarioTrack.CFG_RESOURCE_PATH, "resources/" );
			// Add in behavior and loadProfileCreatorClass
			
			/*
			"behavior": {
	            "default" : [
					[ 25.0, 75.0, 0.0,  0.0,   0.0, 0.0],
					[ 0.0,  0.0, 100.0, 0.0,   0.0, 0.0],
					[ 0.0,  0.0, 20.0,  5.0,   55.0, 20.0],
					[10.0,  0.0, 50.0,  0.0,   20.0, 20.0],
					[ 0.0,  0.0,  0.0,  10.0,  40.0, 50.0],
					[ 0.0,  0.0,  0.0,  20.0, 50.0, 30.0]
				]
	        }*/
						
			
			JSONObject behaviorDetails = new JSONObject();
						
			// Create an array for each row
			JSONArray row1 = new JSONArray( new int[] {25,    75,     0,     0,     0,     0} );
			JSONArray row2 = new JSONArray( new int[] { 0,     0,   100,     0,     0,     0} );
			JSONArray row3 = new JSONArray( new int[] { 0,     0,    20,     5,    55,    20} );
			JSONArray row4 = new JSONArray( new int[] {10,     0,    50,     0,    20,    20} );
			JSONArray row5 = new JSONArray( new int[] { 0,     0,     0,    10,    40,    50} );
			JSONArray row6 = new JSONArray( new int[] { 0,     0,     0,    20,    50,    30} );
			
			
			// Now create a JSONArray which stores each row
			JSONArray mix1 = new JSONArray();
			mix1.put( row1 );
			mix1.put( row2 );
			mix1.put( row3 );
			mix1.put( row4 );
			mix1.put( row5 );
			mix1.put( row6 );
						
			// Associate a mix matrix with a tag/name
			behaviorDetails.put( "default", mix1 );
			
			// Store the behavior details in the track config
			trackDetails.put( ScenarioTrack.CFG_BEHAVIOR_KEY, behaviorDetails );
			
			// Specifiy the load creator class
			trackDetails.put( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_KEY, "radlab.rain.workload.scadr.ScadrDemoScheduleCreator" );
						
			JSONObject targetDetails = new JSONObject();
			targetDetails.put( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY, "ec2-50-16-105-73.compute-1.amazonaws.com" );
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
			trackDetails.put( ScenarioTrack.CFG_METRIC_SNAPSHOT_INTERVAL, 60 ); // Specify the metric snapshot interval in seconds
			trackDetails.put( ScenarioTrack.CFG_METRIC_SNAPSHOTS, true );
			trackDetails.put( ScenarioTrack.CFG_METRIC_DB, "dev-mini-demosql.cwppbyvyquau.us-east-1.rds.amazonaws.com,radlab_dev,randyAndDavelab" );
			trackConfig.put( trackName, trackDetails );
		}
		
		return trackConfig;
	}

}
