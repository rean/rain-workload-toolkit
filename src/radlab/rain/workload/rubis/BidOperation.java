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

package radlab.rain.workload.rubis;

import java.io.IOException;

import radlab.rain.IScoreboard;

/**
 * BidOperation implementation.
 */
public class BidOperation extends RubisOperation 
{

	public BidOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Bid";
		this._operationIndex = RubisGenerator.BID;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		StringBuilder browseResp = this._http.fetchUrl( this.getGenerator().browseURL );
		this.trace( this.getGenerator().browseURL );
		if ( browseResp.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}

		
		StringBuilder browseCatResp = this._http.fetchUrl( this.getGenerator().browseCategoriesURL );
		this.trace( this.getGenerator().browseCategoriesURL );
		if ( browseCatResp.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}

		String category = "2";
		String categoryName = "Books";

		//GET /rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByCategory?category=2&categoryName=Books
		
		String searchItemsByCategoryUrl = this.getGenerator().searchItemsByCategoryURL + "?category="+category + "&categoryName=" + categoryName;
		
		StringBuilder searchItemsByCategoryResponse = this._http.fetchUrl(searchItemsByCategoryUrl);
		this.trace( this.getGenerator().searchItemsByCategoryURL );
		if ( searchItemsByCategoryResponse.length() == 0 )
		{
			System.out.println( "Did not find items to bid by category." );
			throw new Exception( "search items by category failed for unknown reason");
		}

		//"GET /rubis_servlets/servlet/edu.rice.rubis.servlets.PutBidAuth?itemId=533724	

		String putBidAuthUrl = this.getGenerator().putBidAuthURL + "?itemId="+533724;
		
		StringBuilder putBidAuthResponse = this._http.fetchUrl(putBidAuthUrl);
		this.trace( this.getGenerator().putBidAuthURL );
		if ( putBidAuthResponse.length() == 0 )
		{
			System.out.println( "Put bid authentication failed ." );
			throw new Exception( "Put Bid Authentication failed for unknown reason");
		}
		
		String itemId = "533724";
		String nickname = "cercs";
		String password = "cercs";

		// Decide on the username and password; parse the authenticity token.
		StringBuilder postBody = new StringBuilder();
		postBody.append( "itemId=" ).append( itemId );
		postBody.append( "&nickname=" ).append( nickname );
		postBody.append( "&password=" ).append( password );
				
		StringBuilder postPutBidResponse = this._http.fetchUrl( this.getGenerator().postPutBidURL, postBody.toString());
		this.trace(this.getGenerator().postPutBidURL);
		
		// Check that the user was successfully signed in for selling item 
		if ( postPutBidResponse.length() == 0 ) {
			System.out.println( "Did not login for selling items properly." );
			throw new Exception( "Browse Categories for Sell did not appear for unknown reason");
		}

		// String itemId - value already initialized above.
		String userId = "1001850";		
		String minBid = "10";
		String bid = "50";
		String maxBid = "50";
		String qty = "1";
		String maxQty = "10";
		
		// Make the POST request to log in.
		StringBuilder storeBid = new StringBuilder();
		storeBid.append( "itemId=" ).append( itemId );
		storeBid.append( "&userId=" ).append( userId );
		storeBid.append( "&minBid=" ).append( minBid );
		storeBid.append( "&bid=" ).append( bid );
		storeBid.append( "&maxBid=" ).append( maxBid );
		storeBid.append( "&qty=" ).append( qty );
		storeBid.append( "&maxQty=" ).append( maxQty );
		
		StringBuilder storeBidResponse = this._http.fetchUrl( this.getGenerator().postStoreBidURL, storeBid.toString());
		this.trace(this.getGenerator().postStoreBidURL);

		if ( storeBidResponse.length() == 0 ) {
			System.out.println( "Did not place bid on item ." );
			throw new Exception( "Post for putting bid on item failed for unknown reason");
		}

		this.setFailed( false );
	}
	
}
