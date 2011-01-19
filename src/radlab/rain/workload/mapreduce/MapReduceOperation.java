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

package radlab.rain.workload.mapreduce;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

public abstract class MapReduceOperation extends Operation 
{
	protected String _hdfsRoot = "";
	protected String _inputPath = "";
	protected String _outputPath = "";
	protected String _jobTracker = "";
	protected float _shuffleInputRatio = 1.0f;
	protected float _outputShuffleRatio = 1.0f;
	protected long _interarrival = 0;
	protected String _jobName = "";
	
	public String getJobName() { return this._jobName; }
	public void setJobName( String val ) { this._jobName = val; }
	
	public String getInputPath() { return this._inputPath; }
	public void setInputPath( String val ) { this._inputPath = val; }
	
	public String getOutputPath() { return this._outputPath; }
	public void setOutputPath( String val ) { this._outputPath = val; }
	
	public float getShuffleInputRatio() { return this._shuffleInputRatio; }
	public void setShuffleInputRatio( float val ) { this._shuffleInputRatio = val; }
	
	public float getOutputShuffleRatio() { return this._outputShuffleRatio; }
	public void setOutputShuffleRatio( float val ) { this._outputShuffleRatio = val; }
	
	public long getInterarrival() { return this._interarrival; }
	public void setInterarrival( long val ) { this._interarrival = val; }
	
	public String getJobTracker() { return this._jobTracker; }
	public void setJobTracker( String val ){ this._jobTracker = val; }
	
	public MapReduceOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	@Override
	public void cleanup() 
	{
		// Once the job is done, ditch the output to keep
		// HDFS from filling up
		try
		{
			HdfsUtil.deletePath( this._outputPath );
		}
		catch( Exception e )
		{}
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
	}
}
