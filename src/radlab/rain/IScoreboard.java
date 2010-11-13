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

import java.io.FileWriter;
import java.io.PrintStream;

/**
 * The IScoreboard interface allows an implemented scoreboard to interface
 * with the benchmark architecture.<br />
 * <br />
 * The interface requires that the scoreboard be able to start, stop, receive
 * results, show statistics, and register log handles, among other things.
 */
public interface IScoreboard
{
	/**
	 * Gets the time when this scoreboard starts recording results.
	 */
	long getStartTimestamp();
	
	/**
	 * Sets the time when this scoreboard starts recording results.
	 * 
	 * @param val   The time to use as the start time.
	 */
	void setStartTimestamp( long val );
	
	/**
	 * Gets the time when this scoreboard stops recording results.
	 */
	long getEndTimestamp();
	
	/**
	 * Sets the time when this scoreboard stops recording results.
	 * 
	 * @param val   The time to use as the end time.
	 */
	void setEndTimestamp( long val );
	
	/**
	 * Initializes the start and end times and resets the results.
	 * 
	 * @param startTime     The time to start receiving results.
	 * @param endTime       The time to stop receiving results.
	 */
	void initialize( long startTime, long endTime );
	
	/**
	 * Resets the results recorded by this scoreboard.
	 */
	void reset();
	
	/**
	 * Starts this scoreboard for recording results.
	 */
	void start();
	
	/**
	 * Stops this scoreboard from recording any more results.
	 */
	void stop();
	
	/**
	 * Receives the results of an operation execution.
	 * 
	 * @param result    The result to record.
	 */
	void dropOff( OperationExecution result );

	void dropOffWaitTime( long time, String opName, long waitTime );
	
	/**
	 * Prints the statistics processed by this scoreboard.
	 */
	void printStatistics( PrintStream out );
	
	/**
	 * Registers a log handler for a string identifier.
	 * 
	 * @param owner         The string to identify this log handler by.
	 * @param logHandle     The log handler to register.
	 */
	void registerLogHandle( String owner, FileWriter logHandle );
	
	/**
	 * Deregisters a log handler identified by the given string.
	 * 
	 * @param owner         The string identifying the log handler.
	 */
	void deRegisterLogHandle( String owner );

	/**
	 * Registers an error log handler for a string identifier.
	 * 
	 * @param owner         The string to identify this error log handler by.
	 * @param logHandle     The error log handler to register.
	 */
	void registerErrorLogHandle( String owner, FileWriter logHandle );

	/**
	 * Deregisters an error log handler identified by the given string.
	 * 
	 * @param owner         The string identifying the error log handler.
	 */
	void deRegisterErrorLogHandle( String owner );
	
	double getLogSamplingProbability();
	void setLogSamplingProbability( double val );
	
	long getMetricSnapshotInterval();
	void setMetricSnapshotInterval( long val );
	
	ScenarioTrack getScenarioTrack();
	void setScenarioTrack( ScenarioTrack val );
	
	long getMeanResponseTimeSamplingInterval();
	void setMeanResponseTimeSamplingInterval( long val );

	String getTargetHost();
	void setTargetHost( String val );
}
