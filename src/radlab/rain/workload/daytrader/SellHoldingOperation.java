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

package radlab.rain.workload.daytrader;

import radlab.rain.IScoreboard;

import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;

public class SellHoldingOperation extends DayTraderOperation 
{
	public static String NAME = "SellHolding";
	// use case-insensitive pattern match - we're interested in one group, that one contains the holding id
	private static Pattern HOLDING_PATTERN = Pattern.compile( "<a href=\"app\\?action=sell&holdingID=(\\d+)\">", Pattern.CASE_INSENSITIVE );
				
	public SellHoldingOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = DayTraderGenerator.SELL_HOLDINGS;
	}

	@Override
	public void execute() throws Throwable 
	{
		// Login if we're not already 
		if( !this.getGenerator()._isLoggedIn )
			this.doLogin();
		
		// Get the portfolio page - this method throws an exception of the page is empty or
		// we fail to get back the expected portfolio page
		StringBuilder response = this.doViewPortfolioPage();
		// System.out.println( "Sell holding: " + response.toString() );
		// Look for things to sell
		// general pattern we're trying to match: <a href="app?action=sell&holdingID=*>
		// If nothing to sell then bail just exit, don't mark the operation as failed.
		// We can have generators keep track of trading stats.
		Matcher holdingMatcher = SellHoldingOperation.HOLDING_PATTERN.matcher( response.toString() );
		Vector<Integer> holdings = new Vector<Integer>();
		// Need to check for multiple matches within the html
		while( holdingMatcher.find() )
		{
			//String grp0 = holdingMatcher.group( 0 );
			String grp1 = holdingMatcher.group( 1 );
			//System.out.println( "Group 0: " + grp0 );
			//System.out.println( "Holding id: " + grp1 );
			holdings.add( Integer.parseInt( grp1 ) );
		}

		// If we've got holdings then randomly pick one to sell
		if( holdings.size() > 0 )
		{
			Random rnd = new Random();
			// If we've got one, then sell it
			if( holdings.size() == 1 )
			{
				response = this.doSellHolding( holdings.get(0).intValue() );
			}
			else
			{
				int holdingID = holdings.get( rnd.nextInt( holdings.size() ) ).intValue();
				response = this.doSellHolding( holdingID );
			}
			//System.out.println( "Post sale: " + response.toString() );
		}
		else
		{
			// Exit quietly or go buy some then turn around and sell one
			System.out.println( "No stocks in portfolio to sell!" );
		}
		
		this.setFailed( false );
	}

}
