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
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

/**
 * The DayTraderGenerator class generates operations for a single user thread
 * by producing the next operation to execute given the last operation. The
 * next operation is decided through the use of a load mix matrix. 
 */
public class DayTraderGenerator extends Generator
{
	// Starter matrix
	/*
	 {   0, 100,   0,   0,   0,   0,   0 }, // ExternalHomePage
	 {   0,   0, 100,   0,   0,   0,   0 }, // Login 
	 {   0,   0,   5,  60,  10,  15,  10 }, // UserHome
	 {   0,   0,  10,   5,  35,  40,  10 }, // ViewPortfolio
	 {   0,   0,  25,  45,   5,  20,   5 }, // SellHoldings
	 {   0,   0,  40,  30,   5,  20,   5 }, // ViewQuotes
	 {   0,   0,  25,  20,  10,  40,   5 }, // BuyStock
	 * */
	
	// Operation indices used in the mix matrix.
	public static final int EXTERNAL_HOME_PAGE	= 0;
	public static final int LOGIN				= 1;
	public static final int USER_HOME_PAGE		= 2;
	public static final int VIEW_PORTFOLIO 		= 3;
	public static final int SELL_HOLDINGS		= 4;
	public static final int VIEW_QUOTES			= 5;
	public static final int BUY_STOCKS			= 6;

	public static final int LOGOUT				= 7;
	
	private java.util.Random _randomNumberGenerator;
	private HttpTransport _http;
	
	public String _baseUrl;
	public String _loginUrl;
	public String _userHomeUrl;
	public String _viewAccountUrl;
	public String _viewAccountProfileUrl;
	public String _viewPortfolioUrl;
	public String _sellHoldingUrl;
	public String _viewQuotesUrl;
	public String _buyStockUrl;
	public String _logoutUrl;
	
	// Status/state info
	public boolean _isLoggedIn = false; // Make this private and add a method that can check whether we're logged in or not by looking at the home page
	public String _loggedInUser = "";
	public int _totalStockSymbols = 400; // Default value loaded in db, can be generator param later
	public int _totalUsersLoaded = 200; // Default value loaded in db, can be generator param later
	
	private NegativeExponential _thinkTimeGenerator  = null;
	
	/**
	 * Initialize a <code>SampleGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public DayTraderGenerator( ScenarioTrack track )
	{
		super( track );
		
		this._baseUrl 	= "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort() + "/daytrader-web-jdbc-2.2-SNAPSHOT";
		this._loginUrl 	= this._baseUrl + "/app";
		this._userHomeUrl = this._baseUrl + "/app?action=home";
		this._logoutUrl = this._baseUrl + "/app?action=logout";
		this._viewAccountUrl = this._baseUrl + "/app?action=home";
		this._viewAccountProfileUrl = this._baseUrl + "/app?action=account";
		this._viewPortfolioUrl = this._baseUrl + "/app?action=portfolio";
		// All of these are GETs not POSTS
		this._sellHoldingUrl = this._baseUrl + "/app?action=sell&holdingID="; //0
		this._viewQuotesUrl = this._baseUrl + "/app?action=quotes&symbols="; //s:0,s:1,s:2,s:3,s:4"
		this._buyStockUrl = this._baseUrl + "/app?action=buy&symbol="; //s%3A0&quantity=100";
		
		//System.out.println( "Track mean think time: " + track.getMeanThinkTime() ); 
		this._thinkTimeGenerator = new NegativeExponential( track.getMeanThinkTime() * 1000 );
	}
	
	/**
	 * Initialize this generator.
	 */
	public void initialize()
	{
		this._randomNumberGenerator = new java.util.Random();
		this._http = new HttpTransport();
	}
	
	/**
	 * Returns the next <code>Operation</code> given the <code>lastOperation</code>
	 * according to the current mix matrix.
	 * 
	 * @param lastOperation     The last <code>Operation</code> that was executed.
	 */
	public Operation nextRequest( int lastOperation )
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		int nextOperation = -1;
		
		if( lastOperation == -1 )
		{
			nextOperation = 0;
		}
		else
		{
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(currentLoad.getMixName()).getSelectionMix();
			double rand = this._randomNumberGenerator.nextDouble();
			
			int j;
			for ( j = 0; j < selectionMix.length; j++ )
			{
				if ( rand <= selectionMix[lastOperation][j] )
				{
					break;
				}
			}
			nextOperation = j;
		}
		return getOperation( nextOperation );
	}
	
	/**
	 * Returns the current think time. The think time is duration between
	 * receiving the response of an operation and the execution of its
	 * succeeding operation during synchronous execution (i.e. closed loop).
	 */
	public long getThinkTime()
	{
		long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		//System.out.println( Math.min( nextThinkTime, (5*this._thinkTime) ) );
		return Math.min( nextThinkTime, (5*this._thinkTime) );
	}
	
	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	public long getCycleTime()
	{
		return 0;
	}
	
	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	public void dispose()
	{
		// TODO: Fill me in.
	}
	
	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}
	
	
	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex   The type of operation to instantiate.
	 * @return          A prepared operation.
	 */
	public Operation getOperation( int opIndex )
	{
		switch( opIndex )
		{
			case EXTERNAL_HOME_PAGE: return this.createExternalHomePageOperation();
			case LOGIN: return this.createLoginOperation();
			case USER_HOME_PAGE: return this.createUserHomePageOperation();
			case VIEW_PORTFOLIO: return this.createViewPortfolioOperation();
			case SELL_HOLDINGS: return this.createSellHoldingOperation();
			case VIEW_QUOTES: return this.createViewQuotesOperation();
			case BUY_STOCKS: return this.createBuyStockOperation();
			
			default:         return null;
		}
	}
	
	
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared LoginOperation.
	 */
	
	public ExternalHomePageOperation createExternalHomePageOperation()
	{
		ExternalHomePageOperation op = new ExternalHomePageOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	
	public LoginOperation createLoginOperation()
	{
		LoginOperation op = new LoginOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public UserHomePageOperation createUserHomePageOperation()
	{
		UserHomePageOperation op = new UserHomePageOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;	
	}
	
	public LogoutOperation createLogoutOperation()
	{
		LogoutOperation op = new LogoutOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public ViewPortfolioOperation createViewPortfolioOperation()
	{
		ViewPortfolioOperation op = new ViewPortfolioOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public SellHoldingOperation createSellHoldingOperation()
	{
		SellHoldingOperation op = new SellHoldingOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public ViewQuotesOperation createViewQuotesOperation()
	{
		ViewQuotesOperation op = new ViewQuotesOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public BuyStockOperation createBuyStockOperation()
	{
		BuyStockOperation op = new BuyStockOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
}
