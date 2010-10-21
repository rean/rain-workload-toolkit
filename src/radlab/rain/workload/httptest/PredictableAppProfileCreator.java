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

//import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.ProfileCreator;
import radlab.rain.Scenario;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.ConfigUtil;
import radlab.rain.workloadtraits.WikipediaScheduleCreator;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;

public class PredictableAppProfileCreator extends ProfileCreator 
{
	public static String CFG_BASE_HOST_IP_KEY 				= "baseHostIp";
	public static String CFG_HOST_PORT_KEY					= "hostPort";
	public static String CFG_NUM_HOST_TARGETS_KEY			= "numHostTargets";
	public static String CFG_POPULAR_HOST_FRACTION_KEY		= "popularHostFraction";
	public static String CFG_POPULAR_HOST_LOAD_FRACTION_KEY = "popularHostLoadFraction";
	public static String CFG_MEAN_THINK_TIME_KEY			= "meanThinkTime";
	public static String CFG_USER_POPULATION_KEY			= "userPopulation";
	
	// We'll want a mix of really busy, so-so busy and idle hosts
	// [10, 50, 40] // Simple multinomial covering the mix of activity levels
	
	// All of these are going to be configuration parameters
	private String _baseHostIP = "127.0.0.1";
	private int _hostPort = 80;
	private int _numHostTargets = 1;
	private float _popularHostFraction = 0.1f; // 10% of them
	private float _popularHostLoadFraction = 0.1f; // 10% of the traffic goes to the popular hosts
	private int _meanThinkTime = 0;
	private int _userPopulation = this._numHostTargets;
	
	public PredictableAppProfileCreator() 
	{}

	@Override
	public JSONObject createProfile(JSONObject params) throws JSONException 
	{
		// Pull out all the parameters we need:
		if( params.has( CFG_BASE_HOST_IP_KEY) )
			this._baseHostIP = params.getString( CFG_BASE_HOST_IP_KEY );
		
		if( params.has( CFG_HOST_PORT_KEY ) )
			this._hostPort = params.getInt( CFG_HOST_PORT_KEY );
		
		if( params.has( CFG_POPULAR_HOST_FRACTION_KEY ) );
			this._popularHostFraction = (float) params.getDouble( CFG_POPULAR_HOST_FRACTION_KEY );
			
		if( params.has( CFG_POPULAR_HOST_LOAD_FRACTION_KEY) )
			this._popularHostLoadFraction = (float) params.getDouble( CFG_POPULAR_HOST_LOAD_FRACTION_KEY );
		
		if( params.has( CFG_MEAN_THINK_TIME_KEY ) )
			this._meanThinkTime = params.getInt( CFG_MEAN_THINK_TIME_KEY );
		
		if( params.has( CFG_NUM_HOST_TARGETS_KEY ) )
			this._numHostTargets = params.getInt( CFG_NUM_HOST_TARGETS_KEY );
			
		if( params.has( CFG_USER_POPULATION_KEY ) )
			this._userPopulation = params.getInt( CFG_USER_POPULATION_KEY );
		else this._userPopulation = this._numHostTargets;
		
		JSONObject generatorParameters = null;
		if( params.has( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY ) )
			generatorParameters = params.getJSONObject( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY );
		else generatorParameters = new JSONObject();
		
		this.printConfig( System.out );
		
		// Determine the set of hosts to touch - could all be contiguous or randomly selected
		Random random = new Random();
		JSONObject trackConfig = new JSONObject();
		
		
		
		// Determine the number of popular and less popular hosts
		int numPopularHosts =  Math.min( this._numHostTargets, Math.max( 0, (int) Math.ceil( this._numHostTargets * this._popularHostFraction ) ) );
		int numLessPopularHosts = this._numHostTargets - numPopularHosts;
		
		HashSet<Integer> popularHosts = new HashSet<Integer>();
		if( numLessPopularHosts != 0 )
		{
			// Pick the popular hosts
			while( popularHosts.size() != numPopularHosts )
			{
				int nextHost = random.nextInt( this._numHostTargets );
				if( popularHosts.contains( nextHost ) )
					continue;
				else popularHosts.add( nextHost );
			}
		}
		else // If numLessPopularHosts == 0 then everything is going to be popular
		{
			for( int i = 0; i < this._numHostTargets; i++ )
				popularHosts.add( i );
		}
		
		// Look at the total users/threads we have to give to the popular and less popular hosts
		int usersForPopularHosts = 0;
		
		// If there are no less popular hosts then don't do the load fraction calculation
		if( numLessPopularHosts == 0 )
			usersForPopularHosts = this._userPopulation;
		else if( numPopularHosts > 0 )
			usersForPopularHosts = Math.max(0, Math.round( this._popularHostLoadFraction * this._userPopulation ) );
				
		int usersForLessPopularHosts = Math.max(0, ( this._userPopulation - usersForPopularHosts ) );
		
		// User load per popular host
		int usersPerPopularHost = 0;
		if( numPopularHosts > 0 )
			usersPerPopularHost = (int) Math.round( usersForPopularHosts / (float) numPopularHosts );
		
		// User load per less popular host
		int usersPerLessPopularHost = 0;
		if( numLessPopularHosts > 0 )
			usersPerLessPopularHost = (int) Math.round( usersForLessPopularHosts / (float) numLessPopularHosts );   
				
		// Print out what we've computed
		System.out.println( this + " total host targets              : " + this._numHostTargets );
		System.out.println( this + " popular hosts fraction          : " + this._popularHostFraction );
		System.out.println( this + " popular hosts                   : " + numPopularHosts );
		System.out.println( this + " less popular hosts              : " + numLessPopularHosts );
		System.out.println( this + " total user population           : " + this._userPopulation );
		System.out.println( this + " popular host user load fraction : " + this._popularHostLoadFraction );
		System.out.println( this + " users for all popular hosts     : " + usersForPopularHosts );
		System.out.println( this + " users for less popular hosts    : " + usersForLessPopularHosts );
		System.out.println( this + " users per popular host          : " + usersPerPopularHost );
		System.out.println( this + " users per less-popular host     : " + usersPerLessPopularHost );
		System.out.println( this + " effective user population       : " + ( (usersPerPopularHost*numPopularHosts)+(usersPerLessPopularHost*numLessPopularHosts) ) );
		
		for( int i = 0; i < this._numHostTargets; i++ )
		{
			JSONObject trackDetails = null;
			
			String trackName = "";
			if( i < 10 )
				trackName = "predictable-000" + i;
			else trackName = "predictable-00" + i;
		
			// Get base IP, split on . get last octet convert to int then add i
			String[] ipAddressParts = this._baseHostIP.split( "\\." );
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
			
			//System.out.println( "Target IP: " + targetIPAddress.toString() );
							
			if( popularHosts.contains( i ) )
			{
				trackName = trackName + "-p";
				trackDetails = this.createTrack( targetIPAddress.toString(), this._hostPort, this._meanThinkTime, usersPerPopularHost, generatorParameters );
			}
			else trackDetails = this.createTrack( targetIPAddress.toString(), this._hostPort, this._meanThinkTime, usersPerLessPopularHost, generatorParameters );
			
			trackConfig.put( trackName, trackDetails );
		}
	
		return trackConfig;
	}
	
	private JSONObject createTrack( String host, int port, int meanThinkTime, int minUsers, JSONObject generatorParameters ) throws JSONException
	{
		JSONObject trackDetails = new JSONObject();
		// Fill in details
		trackDetails.put( ScenarioTrack.CFG_GENERATOR_KEY, "radlab.rain.workload.httptest.PredictableAppGenerator" );
		// Fill in any generator parameters
		trackDetails.put( ScenarioTrack.CFG_GENERATOR_PARAMS_KEY, generatorParameters );
		
		trackDetails.put( ScenarioTrack.CFG_TRACK_CLASS_KEY, "radlab.rain.DefaultScenarioTrack" );
		trackDetails.put( ScenarioTrack.CFG_RESOURCE_PATH, "resources/" );
		// Add in loadProfileCreatorClass
		JSONObject behaviorDetails = new JSONObject();
		// Store the behavior details in the track config
		trackDetails.put( ScenarioTrack.CFG_BEHAVIOR_KEY, behaviorDetails );
		// Specifiy the load creator class - we're going to use Wikipedia for now
		trackDetails.put( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_KEY, "radlab.rain.workloadtraits.WikipediaScheduleCreator" );
		JSONObject scheduleCreatorParams = new JSONObject();
		// Fill in the parameters for the schedule creator
		// Just set the minUsers so the workload can be scaled
		scheduleCreatorParams.put( WikipediaScheduleCreator.CFG_INITIAL, minUsers );
				
		trackDetails.put( ScenarioTrack.CFG_LOAD_SCHEDULE_CREATOR_PARAMS_KEY, scheduleCreatorParams );
		
		JSONObject targetDetails = new JSONObject();
				
		targetDetails.put( ScenarioTrack.CFG_TARGET_HOSTNAME_KEY, host );
		targetDetails.put( ScenarioTrack.CFG_TARGET_PORT_KEY, port );
		
		trackDetails.put( ScenarioTrack.CFG_TARGET_KEY, targetDetails );
		trackDetails.put( ScenarioTrack.CFG_LOG_SAMPLING_PROBABILITY_KEY, 0.0 ); // No log sampling
		trackDetails.put( ScenarioTrack.CFG_OPEN_LOOP_PROBABILITY_KEY, 0.0 );
		trackDetails.put( ScenarioTrack.CFG_MEAN_CYCLE_TIME_KEY, 0 );
		trackDetails.put( ScenarioTrack.CFG_MEAN_THINK_TIME_KEY, meanThinkTime );
		trackDetails.put( ScenarioTrack.CFG_INTERACTIVE_KEY, true );

		// Set response time sampling interval - should be tuned based on the expected 
		// order of the expected number of operations/requests that will be issued/served
		// e.g. lower values if we're doing a short run with few operations and
		// larger values if we're doing a long run with many operations so we reduce
		// memory overhead of storing samples
		trackDetails.put( ScenarioTrack.CFG_MEAN_RESPONSE_TIME_SAMPLE_INTERVAL, 100 );

		return trackDetails;
	}
	
	private void printConfig( PrintStream out )
	{
		out.println( this + " Base Host IP                    : " + this._baseHostIP );
		out.println( this + " Host port                       : " + this._hostPort );
		out.println( this + " Mean think time                 : " + this._meanThinkTime );
	}
	
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append( "[PredictableAppProfileCreator]" );
		return buf.toString();
	}
	
	public static void main( String[] args ) throws Exception
	{
		// Read in an example rain.xxx.json file and see what we get
		String filename = "config/rain.config.ac_predictable.json";
		String fileContents = ConfigUtil.readFileAsString( filename );
		JSONObject jsonConfigRoot = new JSONObject( fileContents );
		JSONObject params = jsonConfigRoot.getJSONObject( Scenario.CFG_PROFILES_CREATOR_CLASS_PARAMS_KEY );
		
		PredictableAppProfileCreator creator = new PredictableAppProfileCreator(); 
		JSONObject tracks = creator.createProfile( params );
		System.out.println( tracks.toString() );
	}
}
