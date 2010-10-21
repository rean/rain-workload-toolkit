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

import java.io.IOException;

import org.apache.http.HttpStatus;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public class PredictableAppOperation extends Operation 
{
	public static String NAME_PREFIX = "PredicatableOp_";
	
	// Parameters we need 
	public int _workDone;
	public int _busyPct;
	public String _memorySize;
	
	// These references will be set by the Generator.
	protected HttpTransport _http;
	
	public PredictableAppOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		// Set the operation index, but don't set the name until execution time
		this._operationIndex = PredictableAppGenerator.PREDICTABLE_OP;
	}

	public void setName( String val )
	{
		this._operationName = val;
	}
	
	/**
	 * Returns the Generator that created this operation.
	 * 
	 * @return      The Generator that created this operation.
	 */
	public PredictableAppGenerator getGenerator()
	{
		return (PredictableAppGenerator) this._generator;
	}
	
	@Override
	public void cleanup() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() throws Throwable 
	{
		Thread.sleep( this._workDone );
		StringBuilder url = new StringBuilder();
		url.append( this.getGenerator()._baseUrl );
		url.append( "/task1/spring/worker/busy/" );
		url.append( "1" ); // total num iterations?
		url.append( "/" );
		url.append( this._workDone );
		url.append( "/" );
		url.append( this._busyPct );
		url.append( "/" );
		url.append( this._memorySize );
		
		//System.out.println( this + " " + url.toString() );
		StringBuilder response = this._http.fetchUrl( url.toString() );
		
		this.trace( url.toString() );
		if( response.length() == 0 || this._http.getStatusCode() != HttpStatus.SC_OK )
		{
			String errorMessage = "Home page GET ERROR - Received an empty response or non 200 http status code.";
			throw new IOException (errorMessage);
		}
		
		this.setFailed( false );
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		PredictableAppGenerator predictableGenerator = (PredictableAppGenerator) generator;
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this._generatedDuringProfile = currentLoadProfile;
		
		this._http = predictableGenerator.getHttpTransport();
	}
}
