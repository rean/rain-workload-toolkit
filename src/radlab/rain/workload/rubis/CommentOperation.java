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
 * Comment operation.
 */
public class CommentOperation extends RubisOperation 
{

	public CommentOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Comment";
		this._operationIndex = RubisGenerator.COMMENT;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		StringBuilder aboutMeResp = this._http.fetchUrl( this.getGenerator().postAboutMeURL );
		this.trace( this.getGenerator().postAboutMeURL );
		if ( aboutMeResp.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}

		String itemId = "533724";
		String viewItemURL = this.getGenerator().viewItemURL + "?itemId="+ itemId;

		StringBuilder viewItemResp = this._http.fetchUrl( viewItemURL);
		this.trace( viewItemURL);
		if ( viewItemResp.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}

		String to = "1001850";
		// Item id is provided above

		//GET /rubis_servlets/servlet/edu.rice.rubis.servlets.PutCommentAuth?to=1001850&itemId=533725
				
		String putCommentAuthUrl = this.getGenerator().putCommentAuthURL + "?to="+to + "&itemId=" + itemId;
		
		StringBuilder putCommentAuthResponse = this._http.fetchUrl(putCommentAuthUrl);
		this.trace( putCommentAuthUrl );
		if ( putCommentAuthResponse.length() == 0 )
		{
			System.out.println( "Did not find items to bid by category." );
			throw new Exception( "search items by category failed for unknown reason");
		}
	
		// POST /rubis_servlets/servlet/edu.rice.rubis.servlets.PutComment
				
		//String itemId = "533724";
		String nickname = "cercs";
		String password = "cercs";

		// Decide on the username and password; parse the authenticity token.
		StringBuilder postBody = new StringBuilder();
		postBody.append( "itemId=" ).append( itemId );
		postBody.append( "&nickname=" ).append( nickname );
		postBody.append( "&password=" ).append( password );
				
		StringBuilder postPutBidResponse = this._http.fetchUrl( this.getGenerator().postPutCommentURL, postBody.toString());
		this.trace(this.getGenerator().postPutCommentURL);
		
		// Check that the user was successfully signed in for selling item 
		if ( postPutBidResponse.length() == 0 ) {
			System.out.println( "Did not login for selling items properly." );
			throw new Exception( "Browse Categories for Sell did not appear for unknown reason");
		}

		// String itemId - value already intialized above.
		String userId = "1001850";		
		String minComment = "You can place your comment here...";
		String maxQty = "50";
		String comment = "Here I am writing a comment which no one will read...";
		String maxComment = "Here I am writing a comment which no one will read...";
		String qty = "5";
		
		// Make the POST request to log in.
		StringBuilder storeComment = new StringBuilder();
		storeComment.append( "itemId=" ).append( itemId );
		storeComment.append( "&userId=" ).append( userId );
		storeComment.append( "&minComment=" ).append( minComment );
		storeComment.append( "&maxQty=" ).append( maxQty );
		storeComment.append( "&comment=" ).append( comment );
		storeComment.append( "&maxComment=" ).append( maxComment );
		storeComment.append( "&qty=" ).append( qty );
		
		StringBuilder storeCommentResponse = this._http.fetchUrl( this.getGenerator().postStoreCommentURL, storeComment.toString());
		this.trace(this.getGenerator().postStoreCommentURL);

		if ( storeCommentResponse.length() == 0 ) {
			System.out.println( "Did not place bid on item ." );
			throw new Exception( "Post for putting bid on item failed for unknown reason");
		}

		this.setFailed( false );
	}	
}