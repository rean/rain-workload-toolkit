/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: ScaleFactors.java,v 1.1.1.1 2008/09/29 22:33:08 sp208304 Exp $
 * 
 * Modifications by: Rean Griffith 
 * 1) Changed package to include file in Rain harness
 * 
 * Modifications by: Timothy Yung
 * 1) Renamed users to activeUsers and tagCount to tags.
 * 2) Renamed cumuHalfLogistic to cumulativeHalfLogistic.
 * 3) Added documentation.
 */
package radlab.rain.workload.olio;

/**
 * This static class contains all the scale factors used for loading the data
 * and load generation.
 */
public class ScaleFactors 
{
	/** The ratio between loaded and active users. */
	public static final int USERS_RATIO = 100;
	
	/** The number of active users. */
	public static int activeUsers = -1;
	
	/** The number of loaded users. */
	public static int loadedUsers;
	
	/** The number of loaded events. */
	public static int events;
	
	/** The number of tags. */
	public static int tags;

	/**
	 * Sets the number of users for the run/load.
	 * 
	 * @param userCount
	 */
	public static void setActiveUsers( int userCount )
	{
		// TODO: Why don't we update all values on every call?
		if ( activeUsers == -1 )
		{
			activeUsers = userCount;
			loadedUsers = activeUsers * USERS_RATIO;
			
			tags = getTagCount( loadedUsers );
			events = tags * 3;
		}
	}

	/**
	 * Returns the estimated number of tags given the number of loaded users.<br />
	 * <br />
	 * From http://tagsonomy.com/index.php/dynamic-growth-of-tag-clouds/
	 * "As of this writing, a little over 700 users have tagged it, with 450+
	 * unique tags, roughly two-thirds of which tags were (of course) used by
	 * one and only one user."<br />
	 * <br />
	 * This function uses a cumulative half logistic distribution to determine
	 * the tag growth. We have tested the distribution to be close to the
	 * quote above. I.e. 175 multi-user tags for 700 users. The quote above
	 * gives us one-third of 450 which is 150. Close enough.
	 *
	 * @param loadedUsers   The number of users loaded.
	 * @return              The number of tags loaded.
	 */
	public static int getTagCount( int loadedUsers )
	{
		double prob = cumulativeHalfLogistic( loadedUsers, 10000 );
		// We limit to 5000 tags
		return (int) Math.round( 5000 * prob );
	}

	/**
	 * Computes the cumulative half logistic distribution.
	 * 
	 * @param x         The x-axis (i.e. the number of users).
	 * @param scale     Determines the x-stretch of the curve (i.e. how far it
	 *                  takes for the probability to converge to 1).
	 * @return          The resulting probability (i.e. y-axis).
	 */
	private static double cumulativeHalfLogistic( double x, double scale )
	{
		double power = -x / scale;
		return ( 1d - Math.exp( power ) ) / ( 1d + Math.exp( power ) );
	}
}
