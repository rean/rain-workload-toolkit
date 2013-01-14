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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubis.model.RubisCategory;


/**
 * Search-Items-By-Category operation.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class SearchItemsByCategoryOperation extends RubisOperation 
{
	public SearchItemsByCategoryOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = "Search Items by Category";
		this._operationIndex = RubisGenerator.SEARCH_ITEMS_BY_CATEGORY_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		RubisCategory category = this.getGenerator().generateCategory();

		Map<String,String> headers = new HashMap<String,String>();
		headers.put("category", Integer.toString(category.id));
		headers.put("categoryName", category.name);
		//headers.put("page", Integer.toString(this.getUtility.extractPageFromHTML(this.getLastHTML())));
		headers.put("page", Integer.toString(1));
		headers.put("nbOfItems", Integer.toString(this.getGenerator().getNumItemsPerPage()));
		StringBuilder response = this.getHttpTransport().fetchUrl(this.getGenerator().getSearchItemsByCategoryURL());
		this.trace(this.getGenerator().getSearchItemsByCategoryURL());
		if (response.length() == 0)
		{
			throw new IOException("Received empty response");
		}

		this.setFailed(false);
	}
}
