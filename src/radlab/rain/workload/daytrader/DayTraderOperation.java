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

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;

/**
 * The DayTraderOperation class contains common static methods for use by the
 * operations that inherit from this abstract class.
 */
public abstract class DayTraderOperation extends Operation 
{
	// These references will be set by the Generator.
	protected HttpTransport _http;
	// 
	// <INPUT		type="hidden" name="symbol" value="s:31">
	private static Pattern QUOTE_PATTERN = Pattern.compile( "<input(\\s+)type=\"hidden\" name=\"symbol\" value=\"s:(\\d+)\">", Pattern.CASE_INSENSITIVE );
	
	
	/**
	 * Returns the SampleGenerator that created this operation.
	 * 
	 * @return      The SampleGenerator that created this operation.
	 */
	public DayTraderGenerator getGenerator()
	{
		return (DayTraderGenerator) this._generator;
	}
	
	public DayTraderOperation( boolean interactive, IScoreboard scoreboard )
	{
		super( interactive, scoreboard );
	}
	
	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		DayTraderGenerator sampleGenerator = (DayTraderGenerator) generator;
		
		this._http = sampleGenerator.getHttpTransport();
		this.setGeneratorThreadID( Thread.currentThread().getId() );
	}
	
	@Override
	public void cleanup()
	{
		
	}
	
	public void doLogout() throws Exception
	{
		if( !this.getGenerator()._isLoggedIn )
			return;
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._logoutUrl );
		this.trace( this.getGenerator()._logoutUrl );
		if ( response.length() == 0 )
		{
			String errorMessage = "Logout page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);	
		}
			// Actually check that we've logged out, e.g., by looking for the login button
	}
	
	public void doLogin() throws Exception
	{
		if( this.getGenerator()._isLoggedIn )
			return;
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._loginUrl );
		this.trace( this.getGenerator()._loginUrl );
		if ( response.length() == 0 )
		{
			String errorMessage = "Login page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);	
		}
		
		String userName = "uid:" + this.getGeneratorThreadID();
		String password = "xxx";
		
		//System.out.println( "Login: logging in as user: " + userName );
		
		// Do post
		StringBuilder postBody = new StringBuilder();
		postBody.append( "uid=" ).append( userName );
		postBody.append( "&passwd=" ).append( password );
		postBody.append("&action=login");
		
		//System.out.println( "POST URL: " + this.getGenerator().loginUrl );
		
		response = this._http.fetchUrl( this.getGenerator()._loginUrl, postBody.toString() );
		this.trace( this.getGenerator()._loginUrl + postBody.toString() );
		
		if ( response.length() == 0 )
		{
			String errorMessage = "Login page POST ERROR - Received an empty response";
			throw new IOException (errorMessage);	
		}
		
		// Actually check that we've been logged in, e.g., by looking for the log off link
		
		//System.out.println( response.toString() );
		
		this.getGenerator()._loggedInUser = userName;
		this.getGenerator()._isLoggedIn = true;
	}
	
	public StringBuilder doViewPortfolioPage() throws Exception
	{
		if( !this.getGenerator()._isLoggedIn )
			this.doLogin();
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._viewPortfolioUrl );
		this.trace( this.getGenerator()._viewPortfolioUrl ); 
		if ( response.length() == 0 )
		{
			String errorMessage = "View portfolio page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);	
		}
		
		return response;
	}
	
	public StringBuilder doSellHolding( int holdingID ) throws Exception
	{
		if( !this.getGenerator()._isLoggedIn )
			this.doLogin();
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._sellHoldingUrl + holdingID );
		this.trace( this.getGenerator()._sellHoldingUrl + holdingID );
		
		if( response.length() == 0 )
		{
			String errorMessage = "View portfolio page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);
		}
		
		return response;
	}
	
	public StringBuilder doViewQuotesPage() throws Exception
	{
		// Pick 5 stocks randomly to view
		return this.doViewQuotesPage( 5 );
	}
	
	
	public StringBuilder doViewQuotesPage( int numSymbols ) throws Exception
	{
		if( numSymbols <= 0 )
			throw new IllegalArgumentException( "Expected >=1 symbols to search for, instead got: " + numSymbols );
		
		if( !this.getGenerator()._isLoggedIn )
			this.doLogin();
		
		StringBuilder quotesUrl = new StringBuilder();
		quotesUrl.append( this.getGenerator()._viewQuotesUrl );
		// Add symbols to search for - pick some symbols randomly
		// In the generator we'll know how many stocks we have total
		// Pick n out of total without replacement.
		HashSet<Integer> symbolSearch = new HashSet<Integer>();
		Random rnd = new Random();
		
		while( symbolSearch.size() < numSymbols )
		{
			int quote = rnd.nextInt( this.getGenerator()._totalStockSymbols );
			if( !symbolSearch.contains( quote ) )
			{
				// Add it to set
				symbolSearch.add( quote );
				// Add to search url
				quotesUrl.append( "s:" + quote );
				if( symbolSearch.size() != numSymbols )
					quotesUrl.append( "," );
			}
		}
		
		//System.out.println( "Quotes url: " + quotesUrl.toString() );
		
		StringBuilder response = this._http.fetchUrl( quotesUrl.toString() );
		this.trace( quotesUrl.toString() );
		if ( response.length() == 0 )
		{
			String errorMessage = "View quotes page GET ERROR - Received an empty response";
			throw new IOException (errorMessage);	
		}
		
		return response;
	}
	
	public StringBuilder doBuyStocks() throws Exception
	{
		// By default, make 5 stock purchases from a random set of 20 retrieved
		return this.doBuyStocks( 5, 20, 100 );
	}
	
	public StringBuilder doBuyStocks( int numStocksToBuy, int numStocksToSearchFor, int numPurchaseUnits ) throws Exception
	{
		if( !this.getGenerator()._isLoggedIn )
			this.doLogin();
	
		// Do a search for quotes
		StringBuilder response = this.doViewQuotesPage( numStocksToSearchFor );
		//System.out.println( "Buy stocks: " + response.toString() );
		
		// Parse the page to get all the symbols that came back:
		Matcher quoteMatcher = DayTraderOperation.QUOTE_PATTERN.matcher( response.toString() );
		Vector<Integer> quotes = new Vector<Integer>();
		// Need to check for multiple matches within the html
		while( quoteMatcher.find() )
		{
			String quote = quoteMatcher.group(2);
			//System.out.println( "quote id: " +  quote );
			quotes.add( Integer.parseInt( quote ) );
		}
		
		// No matching quotes found
		if( quotes.size() == 0 )
		{
			throw new Exception( "Unable to find any quotes." );
		}
		
		// Select n @ random to buy (we can check our budget later if that matters for correct processing)
		if( numStocksToBuy > numStocksToSearchFor )
			numStocksToBuy = 1;
		
		Random rnd = new  Random();
		HashSet<Integer> stocksBought = new HashSet<Integer>(); 
		while( stocksBought.size() < numStocksToBuy )
		{
			// Buy something - pick a quote randomly from the list returned
			int quoteToBuy = quotes.get( rnd.nextInt( quotes.size() ) );
			if( !stocksBought.contains( quoteToBuy ) )
			{
				StringBuilder buyUrl = new StringBuilder();
				buyUrl.append( this.getGenerator()._buyStockUrl );
				buyUrl.append( "s:" );
				buyUrl.append( quoteToBuy );
				buyUrl.append( "&quantity=" );
				buyUrl.append( numPurchaseUnits );
				
				//System.out.println( "Buy url: " + buyUrl.toString() );
				
				response = this._http.fetchUrl( buyUrl.toString() );
				this.trace( buyUrl.toString() );
				
				if( this._http.getStatusCode() == HttpStatus.SC_OK )
				{
					// Add it to the list if it worked
					stocksBought.add( quoteToBuy );
				}
				else
				{
					System.out.println( "Failed to buy stock: " + buyUrl.toString() );
				}
			}
		}
		
		return response;
	}
}
