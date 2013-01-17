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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import radlab.rain.IScoreboard;
import radlab.rain.workload.rubis.model.RubisRegion;


/**
 * Browse-Regions operation.
 *
 * Emulates the following requests:
 * 1. Go to the browse page
 * 2. Click on the "Browse all in a region"
 * 3. Click on a region name to browse all categories in that region
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class BrowseRegionsOperation extends RubisOperation 
{
	public BrowseRegionsOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);

		this._operationName = "Browse Regions";
		this._operationIndex = RubisGenerator.BROWSE_REGIONS_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = null;

		// Go to the browse home page
		response = this.getHttpTransport().fetchUrl(this.getGenerator().getBrowseURL());
		this.trace(this.getGenerator().getBrowseURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{   
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getBrowseURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Emulate a click on the "Browse all in a region" link
		response = this.getHttpTransport().fetchUrl(this.getGenerator().getBrowseRegionsURL());
		this.trace(this.getGenerator().getBrowseRegionsURL());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + this.getGenerator().getBrowseRegionsURL() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		// Emulate a click on a region name to browse all related categories
		// Generate a random region
		RubisRegion region = this.getGenerator().generateRegion();
		// Send a request with the region as parameter
		URIBuilder uri = new URIBuilder(this.getGenerator().getBrowseCategoriesInRegionURL());
		uri.setParameter("region", region.name);
		HttpGet reqGet = new HttpGet(uri.build());
		response = this.getHttpTransport().fetch(reqGet);
		this.trace(reqGet.getURI().toString());
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			throw new IOException("Problems in performing request to URL: " + reqGet.getURI() + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.setFailed(false);
	}
}
