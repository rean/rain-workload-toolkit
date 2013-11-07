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

package radlab.rain.workload.rubbos;


import radlab.rain.workload.rubbos.model.RubbosCategory;


/**
 * The state of a RUBBoS user session.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public final class RubbosSessionState
{
	private RubbosCategory _category; ///< The current category
	private int _loggedUserId = RubbosUtility.ANONYMOUS_USER_ID; ///< The current logged user identifier
	private int _lastOp = RubbosUtility.INVALID_OPERATION_ID; ///< The identifier of the last terminated operation
	private int _storyId = RubbosUtility.INVALID_STORY_ID; ///< The current story identifier
	private int _lastSearchOp = RubbosUtility.INVALID_OPERATION_ID; ///< The identifier of the last terminated search operation
	private String _lastSearchWord; ///< The word looked for in the last terminated search operation
	private String _lastResponse; ///< The response of the last terminated operation


	public RubbosSessionState()
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

	public int getLoggedUserId()
	{
		return this._loggedUserId;
	}

	public void setLoggedUserId(int value)
	{
		this._loggedUserId = value;
	}

	public RubbosCategory getCategory()
	{
		return this._category;
	}

	public void setCategoryId(RubbosCategory value)
	{
		this._category = value;
	}

	public int getStoryId()
	{
		return this._storyId;
	}

	public void setStoryId(int value)
	{
		this._storyId = value;
	}

	public int getLastSearchOperation()
	{
		return this._lastSearchOp;
	}

	public void setLastSearchOperation(int value)
	{
		this._lastSearchOp = value;
	}

	public String getLastSearchWord()
	{
		return this._lastSearchWord;
	}

	public void setLastSearchWord(String value)
	{
		this._lastSearchWord = value;
	}

	public void clear()
	{
		this._category = null;
		this._loggedUserId = RubbosUtility.ANONYMOUS_USER_ID;
		this._lastOp = RubbosUtility.INVALID_OPERATION_ID;
		this._lastSearchOp = RubbosUtility.INVALID_OPERATION_ID;
		this._storyId = RubbosUtility.INVALID_STORY_ID;
		this._lastSearchWord = null;
		this._lastResponse = null;
	}
}
