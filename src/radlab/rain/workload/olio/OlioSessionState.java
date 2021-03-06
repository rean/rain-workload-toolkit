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

package radlab.rain.workload.olio;


/**
 * The state of a Olio user session.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class OlioSessionState
{
	private int _loggedPersonId = OlioUtility.ANONYMOUS_PERSON_ID; ///< The current logged person identifier
	private int _lastOp = OlioUtility.INVALID_OPERATION_ID; ///< The identifier of the last terminated operation
	private String _lastResponse; ///< The response of the last terminated operation


	public OlioSessionState()
	{
	}

	public int getLastOperation()
	{
		return this._lastOp;
	}

	public void setLastOperation(int value)
	{
		this._lastOp = value;
	}

	public void setLastResponse(String value)
	{
		this._lastResponse = value;
	}

	public String getLastResponse()
	{
		return this._lastResponse;
	}

	public int getLoggedPersonId()
	{
		return this._loggedPersonId;
	}

	public void setLoggedPersonId(int value)
	{
		this._loggedPersonId = value;
	}

	public void clear()
	{
		this._loggedPersonId = OlioUtility.ANONYMOUS_PERSON_ID;
		this._lastOp = OlioUtility.INVALID_OPERATION_ID;
		this._lastResponse = null;
	}
}
