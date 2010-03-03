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

public class LoadProfile 
{
	private long _interval      	= 0;
	private int _numberOfUsers  	= 0;
	private String _mixName     	= "";
	private long _transitionTime	= 0;
	private long _timeStarted		= -1; // LoadManagerThreads need to update this every time they advance the "clock"
	
	public LoadProfile( long interval, int users, String mix )
	{
		this._interval = interval;
		this._numberOfUsers = users;
		this._mixName = mix;
		this._transitionTime = 0;
	}
	
	public LoadProfile( long interval, int users, String mix, long transitionTime )
	{
		this._interval = interval;
		this._numberOfUsers = users;
		this._mixName = mix;
		this._transitionTime = transitionTime;	
	}
		
	// Converts to milliseconds
	public long getInterval() { return (this._interval * 1000); }
	public void setInterval( long val ) { this._interval = val; }
	
	public int getNumberOfUsers() { return this._numberOfUsers; }
	public void setNumberOfUsers( int val ) { this._numberOfUsers = val; }
	
	public String getMixName() { return this._mixName; }
	public void setMixName( String val ) { this._mixName = val; }
	
	public long getTransitionTime() { return (this._transitionTime * 1000); }
	public void setTransitionTime( long val ) { this._transitionTime = val; }
	
	public long getTimeStarted() { return this._timeStarted; }
	public void setTimeStarted( long val ) { this._timeStarted = val; }
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "[Duration: " + this._interval + " Users: " + this._numberOfUsers + " Mix: " + this._mixName + " Transition time: " + this._transitionTime + "]");
		return buf.toString();
	}
}
