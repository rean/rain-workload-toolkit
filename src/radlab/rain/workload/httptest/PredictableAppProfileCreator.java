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

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.ProfileCreator;
import radlab.rain.Scenario;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.ConfigUtil;
import radlab.rain.workloadtraits.WikipediaScheduleCreator;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
	// Allow specification of users per popular app and users per less-popular app.
	// If these values are specified then the userPopulation and popularHostLoadFraction
	// parameters aren't necessary
	public static String CFG_USERS_PER_POPULAR_HOST			= "usersPerPopularHost";
	public static String CFG_USERS_PER_LESS_POPULAR_HOST	= "usersPerLessPopularHost";
	
	// We'll want a mix of really busy, so-so busy and idle hosts
	// [10, 50, 40] // Simple multinomial covering the mix of activity levels
	
	// All of these are going to be configuration parameters
	private String _baseHostIP = "127.0.0.1";
	private int _hostPort = 80;
	private int _numHostTargets = 1;
	private float _popularHostFraction = 0.1f; // 10% of them
	private float _popularHostLoadFraction = 0.1f; // 10% of the traffic goes to the popular hosts
	private double _meanThinkTime = 0.0;
	private int _userPopulation = this._numHostTargets;
	private int _usersPerPopularHost  		= 10;
	private int _usersPerLessPopularHost 	= 1;
	private NumberFormat _formatter = new DecimalFormat( "#0.0000" );
	
	public PredictableAppProfileCreator() 
	{}

	public static String ipv4AddressIncrement( short[] octets, int increment )
	{
		// Treat the ipaddress as a 32-bit number
		// Add the increment
		// Parse as bytes
		// See whether we have 0 or 255 as the last octet
		// If we do increment once more
		
		// Convert to 32-bit number
		long value = (octets[0]<<24 | octets[1]<<16 | octets[2]<<8 | octets[3])&0xFFFFFFFFL;
		//System.out.println( "Pre: " + ((value & 0xFF000000L)>>24) + "." + ( (value & 0x00FF0000L)>>16 ) + "." +  ( (value & 0x0000FF00L)>>8 ) + "." + ( (value & 0x000000FFL) ) );
		
		value += increment;
		
		//System.out.println( "Incr: " + ((value & 0xFF000000L)>>24) + "." + ( (value & 0x00FF0000L)>>16 ) + "." +  ( (value & 0x0000FF00L)>>8 ) + "." + ( (value & 0x000000FFL) ) );
		// If the last octet is 0 or 255 then keep incrementing
		while( (value & 0xFF) == 0xFF || (value & 0xFF) == 0 )
		{
			//System.out.println( "    Adj: " + ((value & 0xFF000000L)>>24) + "." + ( (value & 0x00FF0000L)>>16 ) + "." +  ( (value & 0x0000FF00L)>>8 ) + "." + ( (value & 0x000000FFL) ) );
			value++;
		}
		
		// Print the result
		//System.out.println( "Post: adding: " + increment + " " + ((value & 0xFF000000L)>>24) + "." + ( (value & 0x00FF0000L)>>16 ) + "." +  ( (value & 0x0000FF00L)>>8 ) + "." + ( (value & 0x000000FFL) ) );
		String ipAddress = ((value & 0xFF000000L)>>24) + "." + ( (value & 0x00FF0000L)>>16 ) + "." + ( (value & 0x0000FF00L)>>8 ) + "." + ( (value & 0x000000FFL) );
		//System.out.println( "Post adding: " + increment + " " + ipAddress );
		
		return ipAddress;
	}
	
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
			
		// Host load fraction isn't necessary any more if we're specifying the number of users per popular app
		// and the number of users per less-popular app. Based on those choices we can compute what it effectively is
		// for a run
		//if( params.has( CFG_POPULAR_HOST_LOAD_FRACTION_KEY) )
		//	this._popularHostLoadFraction = (float) params.getDouble( CFG_POPULAR_HOST_LOAD_FRACTION_KEY );
		
		if( params.has( CFG_MEAN_THINK_TIME_KEY ) )
			this._meanThinkTime = params.getDouble( CFG_MEAN_THINK_TIME_KEY );
		
		// The number of host targets isn't optional
		this._numHostTargets = params.getInt( CFG_NUM_HOST_TARGETS_KEY );
			
		// User population isn't necessary any more if we're specifying the number of users per
		// popular app and the number of users per less-popular app. Based on those choices
		// we can compute the effective load user population
		//if( params.has( CFG_USER_POPULATION_KEY ) )
		//	this._userPopulation = params.getInt( CFG_USER_POPULATION_KEY );
		//else this._userPopulation = this._numHostTargets;
		
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
		
		/* Don't try to figure out the distribution of users per popular host and users per less popular hosts
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
		*/
		this._usersPerPopularHost = params.getInt(CFG_USERS_PER_POPULAR_HOST);
		this._usersPerLessPopularHost = params.getInt(CFG_USERS_PER_LESS_POPULAR_HOST);
		// Set the user population
		this._userPopulation = (this._usersPerPopularHost*numPopularHosts) + (this._usersPerLessPopularHost*numLessPopularHosts); 
		// Compute the popular host load fraction
		this._popularHostLoadFraction = (float)(this._usersPerPopularHost*numPopularHosts)/(float)this._userPopulation;
		
		// Print out what we've computed
		System.out.println( this + " total host targets              : " + this._numHostTargets );
		System.out.println( this + " popular hosts fraction          : " + this._formatter.format( this._popularHostFraction ) );
		System.out.println( this + " popular hosts                   : " + numPopularHosts );
		System.out.println( this + " less popular hosts              : " + numLessPopularHosts );
		System.out.println( this + " total user population           : " + this._userPopulation );
		System.out.println( this + " popular host user load fraction : " + this._formatter.format( this._popularHostLoadFraction ) );
		System.out.println( this + " users for all popular hosts     : " + this._usersPerPopularHost*numPopularHosts );
		System.out.println( this + " users for less popular hosts    : " + this._usersPerLessPopularHost*numLessPopularHosts );
		System.out.println( this + " users per popular host          : " + this._usersPerPopularHost );
		System.out.println( this + " users per less-popular host     : " + this._usersPerLessPopularHost );
		System.out.println( this + " effective user population       : " + ( (this._usersPerPopularHost*numPopularHosts)+(this._usersPerLessPopularHost*numLessPopularHosts) ) );
		
		// Change the way we compute the target's ip address
		// Store the last ip address we received,
		// Once the current ip address is different from the last ip address accept it
		// otherwise as for another one
		
		int i = 0;
		int hostIPs = 0;
		int increment = 0;
		String lastIPAddress = "";
		
		while( hostIPs < this._numHostTargets )
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
			
			// Use shorts rather than bytes since Java doesn't have unsigned bytes so
			// Byte.MAX_VALUE = 127 rather than 255
			short firstOctet = Short.parseShort( ipAddressParts[0] );
			short secondOctet = Short.parseShort( ipAddressParts[1] );
			short thirdOctet = Short.parseShort( ipAddressParts[2] );
			short fourthOctet = Short.parseShort( ipAddressParts[3] );
			
			short[] octets = {firstOctet, secondOctet, thirdOctet, fourthOctet};
			
			String targetIPAddress = PredictableAppProfileCreator.ipv4AddressIncrement( octets, increment );
			while( targetIPAddress.equals( lastIPAddress ) )
			{
				//System.out.println( "Hovering..." );
				increment++;
				targetIPAddress = PredictableAppProfileCreator.ipv4AddressIncrement( octets, increment );
			}
			lastIPAddress = targetIPAddress;
			System.out.println( "Target IP: " + targetIPAddress.toString() );
			
			
			if( popularHosts.contains( i ) )
			{
				trackName = trackName + "-p";
				trackDetails = this.createTrack( targetIPAddress.toString(), this._hostPort, this._meanThinkTime, this._usersPerPopularHost, generatorParameters );
			}
			else trackDetails = this.createTrack( targetIPAddress.toString(), this._hostPort, this._meanThinkTime, this._usersPerLessPopularHost, generatorParameters );
			
			trackConfig.put( trackName, trackDetails );
			
			i++;
			hostIPs++;
			increment++;
		}
			
		return trackConfig;
	}
	
	private JSONObject createTrack( String host, int port, double meanThinkTime, int minUsers, JSONObject generatorParameters ) throws JSONException
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
		//System.out.println( tracks.toString() );
		
		
		/*
		// Test out IP address incrementing
		String baseHostIP = "11.0.0.1";
		String[] ipAddressParts = baseHostIP.split( "\\." );
		if( ipAddressParts.length != 4 )
			throw new JSONException( "Expected numerical IPv4 address format: N.N.N.N" );
		
		// Parse all the octets so we can do the "right thing" incrementing addresses
		// Basically we want to skip broadcast addresses
		short firstOctet = Short.parseShort( ipAddressParts[0] );
		short secondOctet = Short.parseShort( ipAddressParts[1] );
		short thirdOctet = Short.parseShort( ipAddressParts[2] );
		short fourthOctet = Short.parseShort( ipAddressParts[3] );
				
		short[] octets = {firstOctet, secondOctet, thirdOctet, fourthOctet};
		//ipv4AddressIncrement( octets, 254 );
		//ipv4AddressIncrement( octets, 255 );
		//ipv4AddressIncrement( octets, 260 );
		//ipv4AddressIncrement( octets, 508 );
		//ipv4AddressIncrement( octets, 509 );
		
		//for( int i = 0; i < 602; i++ )
		//	ipv4AddressIncrement( octets, i );
		
		//ipv4AddressIncrement( octets, 301 );
		//ipv4AddressIncrement( octets, 601 );
		ipv4AddressIncrement( octets, 64770 );
		*/
		 
	}
}
