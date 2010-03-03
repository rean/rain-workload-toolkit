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

/**
 * The Generator abstract class provides a default constructor, required
 * properties, and specifies the methods that must be implemented in order
 * to interface with the benchmark architecture.<br />
 * <br />
 * The basic Generator has a name, associates itself with a scenario track,
 * and keeps a reference to a scoreboard in which operation results are
 * dropped off.
 */
public abstract class Generator 
{
	/** A name with which to identify this generator (e.g. for logging). */
	protected String _name = "";
	
	/** The scenario track that identifies the load profile. */
	protected ScenarioTrack _loadTrack = null;
	/** The think time and cycle time */
	protected long _thinkTime = 0;
	protected long _cycleTime = 0;
	
	/** A reference to the scoreboard to drop results off at. */
	protected IScoreboard _scoreboard = null;
	
	public String getName() { return this._name; }
	public void setName( String val ) { this._name = val; }
	
	public ScenarioTrack getTrack() { return this._loadTrack; }
	
	public void setScoreboard( IScoreboard scoreboard ) { this._scoreboard = scoreboard; }
	public IScoreboard getScoreboard() { return this._scoreboard; }
	
	/**
	 * Creates a new Generator.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public Generator( ScenarioTrack track )
	{
		this._loadTrack = track;
		this.initialize();
	}
	
	public abstract long getThinkTime();
	public void setMeanThinkTime( long val ){ this._thinkTime = val; }
	public abstract long getCycleTime();
	public void setMeanCycleTime( long val ) { this._cycleTime = val; }
	
	public abstract Operation nextRequest( int lastOperation );
	
	public abstract void initialize();
	public abstract void dispose();
}
