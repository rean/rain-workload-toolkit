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
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013
 */

package radlab.rain.workload.olio;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONException;


/**
 * Handle the configuration related to Olio.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class OlioConfiguration
{
	// Possible Java incarnation
	public static final int JAVA_INCARNATION = 0;
	public static final int PHP_INCARNATION = 1;
	public static final int RAILS_INCARNATION = 2;

	// Configuration keys
	private static final String CFG_INCARNATION_KEY = "olio.incarnation";
	private static final String CFG_RNG_SEED_KEY = "olio.rngSeed";
	private static final String CFG_NUM_PRELOADED_EVENTS_KEY = "olio.numPreloadedEvents";
	private static final String CFG_NUM_PRELOADED_PERSONS_KEY = "olio.numPreloadedPersons";
	private static final String CFG_NUM_PRELOADED_TAGS_KEY = "olio.numPreloadedTags";

	// Default values
	private static final int DEFAULT_INCARNATION = RAILS_INCARNATION;
	private static final long DEFAULT_RNG_SEED = -1;
	private static final int DEFAULT_NUM_PRELOADED_EVENTS = 0;
	private static final int DEFAULT_NUM_PRELOADED_PERSONS = 0;
	private static final int DEFAULT_NUM_PRELOADED_TAGS = 0;


	// Members to hold configuration values
	private int _incarnation = DEFAULT_INCARNATION; ///< Olio incarnation
    private long _rngSeed = DEFAULT_RNG_SEED; ///< The seed used for the Random Number Generator; a value <= 0 means that no special seed is used.
	private int _numPreloadEvents = DEFAULT_NUM_PRELOADED_EVENTS; ///< Number of events that have been already preloaded in the Olio database
	private int _numPreloadPersons = DEFAULT_NUM_PRELOADED_PERSONS; ///< Number of persons that have been already preloaded in the Olio database
	private int _numPreloadTags = DEFAULT_NUM_PRELOADED_TAGS; ///< Number of tags that have been already preloaded in the Olio database


	public OlioConfiguration()
	{
	}

	public OlioConfiguration(JSONObject config) throws JSONException
	{
		configure(config);
	}

	public void configure(JSONObject config) throws JSONException
	{
		if (config.has(CFG_INCARNATION_KEY))
		{
			String str = config.getString(CFG_INCARNATION_KEY).toLowerCase();
			if (str.equals("java"))
			{
				this._incarnation = JAVA_INCARNATION;
			}
			else if (str.equals("php"))
			{
				this._incarnation = PHP_INCARNATION;
			}
			else if (str.equals("rails"))
			{
				this._incarnation = RAILS_INCARNATION;
			}
			else
			{
				throw new JSONException("Unknown Olio incarnation");
			}
		}
		if (config.has(CFG_RNG_SEED_KEY))
		{
			this._rngSeed = config.getLong(CFG_RNG_SEED_KEY);
		}
		if (config.has(CFG_NUM_PRELOADED_EVENTS_KEY))
		{
			this._numPreloadEvents = config.getInt(CFG_NUM_PRELOADED_EVENTS_KEY);
		}
		if (config.has(CFG_NUM_PRELOADED_PERSONS_KEY))
		{
			this._numPreloadPersons = config.getInt(CFG_NUM_PRELOADED_PERSONS_KEY);
		}
		if (config.has(CFG_NUM_PRELOADED_TAGS_KEY))
		{
			this._numPreloadTags = config.getInt(CFG_NUM_PRELOADED_TAGS_KEY);
		}

		//TODO: check parameters values
		if (this._rngSeed <= 0)
		{
			this._rngSeed = DEFAULT_RNG_SEED;
		}
	}

	/**
	 * Get the Olio incarnation type.
	 *
	 * @return the Olio incarnation type.
	 */
	public int getIncarnation()
	{
		return this._incarnation;
	}

	/**
	 * Get the seed for the random number generator used by the Olio generator.
	 *
	 * @return the seed for the random number generator
	 */
	public long getRngSeed()
	{
		return this._rngSeed;
	}

	/**
	 * Get the number of events that have been already preloaded inside the
	 * Olio database.
	 *
	 * @return the number of preloaded events.
	 */
	public int getNumOfPreloadedEvents()
	{
		return this._numPreloadEvents;
	}

	/**
	 * Get the number of persons that have been already preloaded inside the
	 * Olio database.
	 *
	 * @return the number of preloaded persons.
	 */
	public int getNumOfPreloadedPersons()
	{
		return this._numPreloadPersons;
	}

	/**
	 * Get the number of tags that have been already preloaded inside the
	 * Olio database.
	 *
	 * @return the tags of preloaded persons.
	 */
	public int getNumOfPreloadedTags()
	{
		return this._numPreloadTags;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append( " Incarnation: " + this.getIncarnation());
		sb.append(", Random Number Generator Seed: " + this.getRngSeed());
		sb.append(", Number of Preloaded Events: " + this.getNumOfPreloadedEvents());
		sb.append(", Number of Preloaded Persons: " + this.getNumOfPreloadedPersons());
		sb.append(", Number of Preloaded Tags: " + this.getNumOfPreloadedTags());

		return sb.toString();
	}
}
