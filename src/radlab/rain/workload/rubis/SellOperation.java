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
 * Sell operation.
 */
public class SellOperation extends RubisOperation 
{

	public SellOperation( boolean interactive, IScoreboard scoreboard ) 
	{
		super( interactive, scoreboard );
		this._operationName = "Sell";
		this._operationIndex = RubisGenerator.SELL;
		this._mustBeSync = true;
	}
	
	@Override
	public void execute() throws Throwable
	{
		StringBuilder response = this._http.fetchUrl( this.getGenerator().sellURL );
		this.trace( this.getGenerator().sellURL );
		if ( response.length() == 0 )
		{
			throw new IOException( "Received empty response" );
		}

		String nickname = "cercs";
		String password = "cercs";

		// Decide on the username and password; parse the authenticity token.
		StringBuilder postBody = new StringBuilder();
		postBody.append( "nickname=" ).append( nickname );
		postBody.append( "&password=" ).append( password );
				
		StringBuilder postResponse = this._http.fetchUrl( this.getGenerator().browseCategoriesURL, postBody.toString());
		this.trace(this.getGenerator().browseCategoriesURL);
		
		// Check that the user was successfully signed in for selling item 
		if ( postResponse.length() < 0 ) {
			System.out.println( "Did not login for selling items properly." );
			throw new Exception( "Browse Categories for Sell did not appear for unknown reason");
		}

		String category = "2";
		String user = "1001850";

		String sellItemFormUrl = this.getGenerator().sellItemFormURL + "?category="+category + "&user=" + user;
		
		StringBuilder sellItemForm = this._http.fetchUrl(sellItemFormUrl);
		this.trace( this.getGenerator().sellItemFormURL );
		if ( sellItemForm.length() == 0 )
		{
			System.out.println( "Did not get sell item form properly." );
			throw new Exception( "Sell Item Form for category and user failed for unknown reason");
		}

		String name = "AOS";
		String description = "AOS";
		String initialPrice = "100";
		String reservePrice = "50";
		String buyNow = "70";
		String quantity = "10";
		String duration = "7";
		
		String categoryId = "2";
		String userId = "1001850";
		
		// Make the POST request to log in.
		StringBuilder registerItem = new StringBuilder();
		registerItem.append( "name=" ).append( name );
		registerItem.append( "&description=" ).append( description );
		registerItem.append( "&initialPrice=" ).append( initialPrice );
		registerItem.append( "&reservePrice=" ).append( reservePrice );
		registerItem.append( "&buyNow=" ).append( buyNow );
		registerItem.append( "&quantity=" ).append( quantity );
		registerItem.append( "&duration=" ).append( duration );
		registerItem.append( "&categoryId=" ).append( categoryId );
		registerItem.append( "&userId=" ).append( userId );
		
		StringBuilder registerItemResponse = this._http.fetchUrl( this.getGenerator().postRegisterItemURL, registerItem.toString());
		this.trace(this.getGenerator().postRegisterItemURL);

		if ( registerItemResponse.length() == 0 ) {
			System.out.println( "Did not register sell item properly." );
			throw new Exception( "Post for Registering Sell Item failed for unknown reason");
		}

		this.setFailed( false );
	}
	
}
