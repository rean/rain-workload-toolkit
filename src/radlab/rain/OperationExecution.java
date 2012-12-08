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
 * The OperationExecution class is a wrapper for the results recorded from the
 * execution of an operation. This wrapper can be passed off to an IScoreboard
 * to be recorded and presented at a later time.
 */
public class OperationExecution implements Comparable<OperationExecution>
{
	/** The owner operation for/in which the results are stored. */
	private Operation _owner = null;
	private boolean _interactive = true;
	private String _traceLabel = Scoreboard.NO_TRACE_LABEL;
	private long _timeStarted = 0;
	private long _timeFinished = 0;
	private long _executionTimeNanos = 0;
	public String _operationName;
	public String _operationRequest;
	public LoadProfile _generatedDuring = null;
	public long _profileStartTime = -1;
	public long _actionsPerformed = 1;
	public boolean _async = false;
	public boolean _failed = true;
	public Operation getOperation() { return this._owner; }
	
	public String getTraceLabel() { return this._traceLabel; }
	public void setTraceLabel( String val ) { this._traceLabel = val; }
	
	/**
	 * Creates a new OperationExecution for the specified operation.
	 * 
	 * @param operation     The operation for/in which results are stored.
	 */
	public OperationExecution( Operation operation )
	{ 
		this._owner = operation;
		this._interactive = operation.isInteractive();
		this._timeStarted = operation.getTimeStarted();
		this._timeFinished = operation.getTimeFinished();
		this._operationName = operation._operationName;
		this._operationRequest = operation._operationRequest;
		this._async = operation.getAsync();
		this._failed = operation._failed;
		// Pull out any info on when this operation was created
		this._generatedDuring = operation.getGeneratedDuringProfile();
		this._profileStartTime = operation.getProfileStartTime();
		
		TraceRecord traceRec = operation.getTrace();
		if ( traceRec != null && traceRec._lstRequests.size() > 0 )
		{
			this._actionsPerformed = traceRec._lstRequests.size();
		}
		else
		{
			this._actionsPerformed = 1;
		}
	}
	
	/* Delegate to get the execution statistics */
	
	//public long getTimeQueued()   { return (this._owner != null) ? this._owner.getTimeQueued()   : 0; }
	public long getTimeStarted()  { return this._timeStarted; }
	public long getTimeFinished() { return this._timeFinished; }
	
	public long getActionsPerformed() { return this._actionsPerformed; }
	
	//public long getDelayTime() { return (this._owner != null) ? this.getDelayTime() : 0; }
	
	public boolean isAsynchronous() { return this._async; }
	public boolean isFailed()       { return this._failed; }
	public boolean isInteractive() { return this._interactive; }
	
	//public long getWaitTime()      { return this.getTimeStarted()  - this.getTimeQueued(); }
	// Return nanos instead
	public long getExecutionTimeMsecs() { return this.getTimeFinished() - this.getTimeStarted(); }
	public long getExecutionTimeNanos() { return this._executionTimeNanos; }
	public void setExecutionTimeNanos( long val ) { this._executionTimeNanos = val; }
	
	//public long getTotalTime()     { return this.getWaitTime()     + this.getExecutionTime() + this.getDelayTime(); }
	
	/**
	 * Compares the start time of this OperationExecution with the provided
	 * OperationExecution; used to construct a timeline of what happened.
	 * 
	 * @param other     The other OperationExecution.
	 */
	public int compareTo( OperationExecution other ) 
	{
		// TODO: Implement me.
		return 0;
	}
}
