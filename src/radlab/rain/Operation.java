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

import java.util.Set;

/**
 * The Operation class is a (delegatable) encapsulation of "tasks to be done".
 * An operation contains all features of the state necessary to execute at an
 * arbitrary point in time (i.e. immediately or in the future). It follows the
 * Command pattern (GoF). It implements <code>Runnable</code> in case another
 * thread has to execute it.
 */
public abstract class Operation implements Runnable 
{
	// Describes the operation
	protected int _operationIndex       = -1;
	protected String _operationName     = "";
	protected String _operationRequest	= "";
	// Describes who generated the operation and when (during what interval)
	protected String _generatedBy       			= "";
	// LoadProfile in effect when this operation was generated/initialized
	private LoadProfile _generatedDuringProfile 	= null;
	protected long _profileStartTime		= -1;
	
	private long _generatorThreadID     = -1;
	private boolean _interactive        = true;
	private long _timeQueued            = 0;
	private long _timeStarted           = 0;
	private long _timeFinished          = 0;
		
	private long _thinkTimeUsed			= 0; // Track how much thinktime we used
	private long _cycleTimeUsed			= 0; // Track how much cycle delays we took advantage of
		
	private boolean _async              = false;
	protected boolean _mustBeSync       = false; // In some cases an operation may have to be done synchronously because it sets the state for operations that happen after it
		
	// Describes the outcome of executing the operation
	protected boolean _failed           = true;
	protected Throwable _failureReason  = null;
	protected TraceRecord _trace        = null;
	
	// Used to collect execution metrics
	protected IScoreboard _scoreboard   = null;
	protected long _actionsPerformed    = 0; // Defaults to 1, should be >= 1
	protected Generator _generator      = null;
	
	public Operation( boolean interactive, IScoreboard scoreboard )
	{
		this._interactive = interactive;
		this._scoreboard = scoreboard;
	}
	
	public int getOperationIndex() { return this._operationIndex; }
	public String getOperationName() { return this._operationName; }
	public boolean isInteractive() { return this._interactive; }
	
	public long getTimeQueued() { return this._timeQueued; }
	public void setTimeQueued( long val ){ this._timeQueued = val; }
	public long getTimeStarted() { return this._timeStarted; }
	public void setTimeStarted( long val ) { this._timeStarted = val; }
	public long getTimeFinished() { return this._timeFinished; }
	public void setTimeFinished( long val ) { this._timeFinished = val; }
	
	public long getThinkTimeUsed() { return this._thinkTimeUsed; }
	public void setThinkTimeUsed( long val ) { this._thinkTimeUsed = val; }
	public long getCycleTimeUsed() { return this._cycleTimeUsed; }
	public void setCycleTimeUsed( long val ) { this._cycleTimeUsed = val; }
	
	public boolean getAsync() { return this._async; }
	public void setAsync( boolean val ){ this._async = val; }
	public String getGeneratedBy() { return this._generatedBy; }
	public void setGeneratedBy( String val ){ this._generatedBy = val; }
	public LoadProfile getGeneratedDuringProfile() { return this._generatedDuringProfile; }
	public void setGeneratedDuringProfile( LoadProfile val )
	{ 
		// Save the load profile
		this._generatedDuringProfile = val; 
		// Save the time started now since the load manager thread updates this
		// field - we can then use timestarted+intervalduration
		// to see whether the operation finished during the interval
		this._profileStartTime = val.getTimeStarted(); 
	}
	public long getProfileStartTime(){ return this._profileStartTime; }
	
	public boolean isFailed() {return this._failed; }
	public void setFailed( boolean val ){ this._failed = val; }
	public Throwable getFailureReason(){ return this._failureReason; }
	public void setFailureReason( Throwable t ){ this._failureReason = t; }
	public long getActionsPerformed(){ return _actionsPerformed; }
	public void setActionsPerformed( long val ){ this._actionsPerformed = val; }
	public long getGeneratorThreadID() { return this._generatorThreadID; }
	public void setGeneratorThreadID( long val ) { this._generatorThreadID = val; }
	
	public void trace( String request )
	{
		if( this._trace == null )
			this._trace = new TraceRecord();
		this._trace._lstRequests.add( request );
	}
	
	public void trace( String[] requests )
	{
		if( this._trace == null )
			this._trace = new TraceRecord();
		
		for( String request : requests )
			this._trace._lstRequests.add( request );
	}
	
	public void trace( Set<String> requests )
	{
		if( this._trace == null )
			this._trace = new TraceRecord();
		
		for( String request : requests )
			this._trace._lstRequests.add( request );
	}
	
	public TraceRecord getTrace() { return this._trace; }
	
	public StringBuffer dumpTrace()
	{
		StringBuffer buf = new StringBuffer();
		TraceRecord traceRec = this._trace;
		if( traceRec == null )
			return buf;
				
		// |-----Workload details----------------|----------|-------|--------|--------------|
		// [RU] [Workload Interval#?] [Max users] Start time, opName, action#, actual request 
		int i = 0;
		for( String request : traceRec._lstRequests )
		{
			buf.append( this._timeStarted );
			buf.append( " " );
			buf.append( this._operationName );
			buf.append( " " );
			buf.append( i );
			buf.append( " " );
			buf.append( request );
			buf.append( "\n" );
			i++;
		}
		
		return buf;
	}
	
	public void disposeOfTrace() 
	{ 
		if( this._trace == null )
			return;
		
		this._trace._lstRequests.clear();
		this._trace = null;
	}
	
	/**
	 * This method is used to run this operation. By default, it records any
	 * metrics when executing. This can be overridden to make a single call to
	 * <code>execute()</code> for more fine-grained control. This method must
	 * catch any <code>Throwable</code>s.
	 */
	public void run()
	{
		// Invoke the pre-execute hook here before we start the clock to time the
		// operation's execution
		this.preExecute();
		this.setTimeStarted( System.currentTimeMillis() );
		long startNanos = System.nanoTime();
		try
		{
			this.execute();
		}
		catch( Throwable e )
		{
			this.setFailed( true );
			this.setFailureReason( e );
		}
		finally
		{
			long endNanos = System.nanoTime();
			this.setTimeFinished( System.currentTimeMillis() );
			//System.out.println( this + " " + ( this.getTimeFinished() - this.getTimeStarted() ) + " ns" );
			
			// Invoke the post-execute hook here after we stop the clock to time the
			// operation's execution
			this.postExecute();
			
			if ( this._scoreboard != null )
			{
				OperationExecution result = new OperationExecution(this);
				//System.out.println( "[EXEC-RESULT]" + this + " " + result.getExecutionTime() + " ns" );
				result.setExecutionTimeNanos( endNanos - startNanos );
				this._scoreboard.dropOff(result);
			}
		}
	}
	
	/**
	 * Prepares this operation for execution. This involves copying any features
	 * about the current state into this operation.
	 * 
	 * @param generator     The generator containing the state to copy.
	 */
	public abstract void prepare( Generator generator );
	
	/**
	 * Executes this operation. This method is responsible for saving its trace
	 * record and execution metrics.
	 * 
	 * @throws Throwable 
	 */
	public abstract void execute() throws Throwable;
	
	/** Hook method for actions to be performed right before execution starts 
	 * (before the clock starts to time the execute method). There's no throws
	 * clause on this method so if something fails the methods need to deal with it.
	 */
	public void preExecute(){}
	
	/** Hook method for actions to be performed right after execution finishes 
	 * (after the clock stops to time the execute method). There's no throws
	 * clause on this method so if something fails the methods need to deal with it.
	 */
	public void postExecute() {}
	
	/**
	 * Do any potential cleanup necessary after execution of this operation.
	 */
	public abstract void cleanup();
	
}
