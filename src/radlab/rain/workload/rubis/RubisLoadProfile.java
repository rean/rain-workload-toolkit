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
 *
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013.
 */

package radlab.rain.workload.rubis;


import org.json.JSONException;
import org.json.JSONObject;
import radlab.rain.LoadProfile;


public class RubisLoadProfile extends LoadProfile
{
	public static String CFG_LOAD_PROFILE_MEAN_THINK_TIME_KEY = "meanThinkTime";
	public static String CFG_LOAD_PROFILE_MEAN_CYCLE_TIME_KEY = "meanCycleTime";


	protected double _meanThinkTime = -1.0; // A negative number means "not set"
	protected double _meanCycleTime = -1.0; // A negative number means "not set"


	public RubisLoadProfile(JSONObject profileObj) throws JSONException
	{
		super(profileObj);

		if (profileObj.has(CFG_LOAD_PROFILE_MEAN_THINK_TIME_KEY))
		{
			this._meanThinkTime = profileObj.getDouble(CFG_LOAD_PROFILE_MEAN_THINK_TIME_KEY);
		}
		if (profileObj.has(CFG_LOAD_PROFILE_MEAN_CYCLE_TIME_KEY))
		{
			this._meanCycleTime = profileObj.getDouble(CFG_LOAD_PROFILE_MEAN_CYCLE_TIME_KEY);
		}
	}

	public RubisLoadProfile(long interval, int numberOfUsers, String mixName)
	{
		super(interval, numberOfUsers, mixName);
	}

	public RubisLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime)
	{
		super(interval, numberOfUsers, mixName, transitionTime);
	}

	public RubisLoadProfile(long interval, int numberOfUsers, String mixName, long transitionTime, String name)
	{
		super(interval, numberOfUsers, mixName, transitionTime, name);
	}

	public double getMeanThinkTime() { return this._meanThinkTime; }
	public void setMeanThinkTime(double val) { this._meanThinkTime = val; }

	public double getMeanCycleTime() { return this._meanCycleTime; }
	public void setMeanCycleTime(double val) { this._meanCycleTime = val; }
}
