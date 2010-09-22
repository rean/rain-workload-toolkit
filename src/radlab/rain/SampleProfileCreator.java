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

import org.json.JSONException;
import org.json.JSONObject;

/* Example of a class that can programmatically create 1 or more tracks that describe a profile.
 * obviating the need for a profiles.config.xxx.json file. Useful if we had to generate 20+ tracks
 */
public class SampleProfileCreator extends ProfileCreator
{
	public JSONObject createProfile( JSONObject input ) throws JSONException
	{
		// We may or may not use the input
		JSONObject trackConfig = new JSONObject();
		
		// Let's try creating a track config with four tracks
		for( int i = 0; i < 4; i++ )
		{
			String trackName = "track-00" + i;
			JSONObject trackDetails = new JSONObject();
			// Fill in details
			trackDetails.put( ScenarioTrack.CFG_GENERATOR_KEY, "radlab.rain.workload.daytrader.DayTraderGenerator" ); 
			trackDetails.put( ScenarioTrack.CFG_TRACK_CLASS_KEY, "radlab.rain.DefaultScenarioTrack" );
			trackDetails.put( ScenarioTrack.CFG_RESOURCE_PATH, "resources/" );
			// Add in behavior and loadProfileCreatorClass
			
			
			
			JSONObject targetDetails = new JSONObject();
			targetDetails.put( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY, "localhost" );
			targetDetails.put( ScenarioTrack.CFG_TARGET_PORT_KEY, 8080 );
			
			trackDetails.put( ScenarioTrack.CFG_TARGET_KEY, targetDetails );
			trackDetails.put( ScenarioTrack.CFG_LOG_SAMPLING_PROBABILITY_KEY, 0.0 ); // No log sampling
			trackDetails.put( ScenarioTrack.CFG_OPEN_LOOP_PROBABILITY_KEY, 0.0 );
			trackDetails.put( ScenarioTrack.CFG_MEAN_CYCLE_TIME_KEY, 0 );
			trackDetails.put( ScenarioTrack.CFG_MEAN_THINK_TIME_KEY, 0 );
			trackDetails.put( ScenarioTrack.CFG_INTERACTIVE_KEY, true );
						
			trackConfig.put( trackName, trackDetails );
		}
		
		return trackConfig;
	}
}
