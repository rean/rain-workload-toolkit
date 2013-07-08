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

package radlab.rain.workload.rubis;


/**
 * The state of a RUBiS user session.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public final class RubisSessionState
{
	private int _loggedUserId; ///< The current logged user identifier
	private int _lastOp; ///< The identifier of the last terminated operation
//	private int _categoryId; ///< The current category identifier
//	private int _regionId; ///< The current region identifier
	private int _itemId; ///< The current item identifier
	private String _lastResponse; ///< The response of the last terminated operation


	public RubisSessionState()
	{
		this._loggedUserId = RubisConstants.ANONYMOUS_USER_ID;
		this._lastOp = RubisConstants.INVALID_OPERATION_ID;
//		this._categoryId = RubisConstants.INVALID_CATEGORY_ID;
//		this._regionId = RubisConstants.INVALID_REGION_ID;
		this._itemId = RubisConstants.INVALID_ITEM_ID;
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

//	public int getCategoryId()
//	{
//		return this._categoryId;
//	}
//
//	public void setCategoryId(int value)
//	{
//		this._categoryId = value;
//	}
//
//	public int getRegionId()
//	{
//		return this._regionId;
//	}
//
//	public void setRegionId(int value)
//	{
//		this._regionId = value;
//	}

	public int getItemId()
	{
		return this._itemId;
	}

	public void setItemId(int value)
	{
		this._itemId = value;
	}

    public boolean isUserLoggedIn()
    {
        return RubisConstants.ANONYMOUS_USER_ID != this._loggedUserId;
    }
}
