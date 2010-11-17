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

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;

/**
 * The SampleGenerator class generates operations for a single user thread
 * by producing the next operation to execute given the last operation. The
 * next operation is decided through the use of a load mix matrix. 
 */
public class RubisGenerator extends Generator
{
	
	// Operation indices used in the mix matrix.
	//public static final int DO_NOTHING = 0;
	public static final int HOME_PAGE = 0;
	public static final int BROWSE_CATEGORIES = 1;
	public static final int REGISTER = 2;
	public static final int SELL = 3;
	public static final int BID = 4;
	public static final int COMMENT = 5;
	
	private java.util.Random _randomNumberGenerator;
	private HttpTransport _http;
	
	// URL roots/anchors for each request
	public String baseURL;
	public String homepageURL; 
	public String browseURL;
	public String browseCategoriesURL; 
	public String registerURL;
	public String postRegisterURL;
	public String sellURL;
	public String sellItemFormURL;
	public String postRegisterItemURL;
	public String searchItemsByCategoryURL;
	public String putBidAuthURL;
	public String postPutBidURL;
	public String postStoreBidURL;

	public String postAboutMeURL;
	public String viewItemURL;
	public String putCommentAuthURL;
	public String postPutCommentURL;
	public String postStoreCommentURL;
	
	/**
	 * Initialize a <code>SampleGenerator</code> given a <code>ScenarioTrack</code>.
	 * 
	 * @param track     The track configuration with which to run this generator.
	 */
	public RubisGenerator( ScenarioTrack track )
	{
		super( track );
		
		this.initializeUrlAnchors();
		// TODO: Fill me in.
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
	 * Initialize the roots/anchors of the URLs.
	 */
	public void initializeUrlAnchors()
	{
		this.baseURL = "http://" + this._loadTrack.getTargetHostName() + ":" + this._loadTrack.getTargetHostPort();
		this.homepageURL        = this.baseURL + "/rubis/";
		this.browseURL = this.baseURL + "/rubis_servlets/browse.html";
		this.browseCategoriesURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.BrowseCategories";
		this.registerURL = this.baseURL + "/rubis_servlets/register.html";
		this.postRegisterURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.RegisterUser";
		this.sellURL = this.baseURL + "/rubis_servlets/sell.html";
		this.sellItemFormURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SellItemForm";
		this.postRegisterItemURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.RegisterItem";
		this.searchItemsByCategoryURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.SearchItemsByCategory";
		this.putBidAuthURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutBidAuth";
		this.postPutBidURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutBid";
		this.postStoreBidURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreBid";
		this.postAboutMeURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.AboutMe";
		this.viewItemURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.ViewItem";
		this.putCommentAuthURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutCommentAuth";
		this.postPutCommentURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.PutComment";	
		this.postStoreCommentURL = this.baseURL + "/rubis_servlets/servlet/edu.rice.rubis.servlets.StoreComment";
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
		return 0;
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
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}

	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	public void dispose()
	{
		// TODO: Fill me in.
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
			//case DO_NOTHING: return this.createDoNothingOperation();
			case HOME_PAGE: return this.createHomePageOperation();
			case BROWSE_CATEGORIES: return this.createBrowseCategoriesOperation();
			case REGISTER: return this.createRegisterOperation();
			case SELL: return this.createSellOperation();
			case BID: return this.createBidOperation();
			case COMMENT: return this.createCommentOperation();
			default: return null;
		}
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared Homepage.
	 */
	public HomePageOperation createHomePageOperation()
	{
		HomePageOperation op = new HomePageOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseCategoriesOperation.
	 */
	public BrowseCategoriesOperation createBrowseCategoriesOperation()
	{
		BrowseCategoriesOperation op = new BrowseCategoriesOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RegisterOperation.
	 */
	public RegisterOperation createRegisterOperation()
	{
		RegisterOperation op = new RegisterOperation( this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SellOperation.
	 */
	public SellOperation createSellOperation()
	{
		SellOperation op = new SellOperation( this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BidOperation.
	 */
	public BidOperation createBidOperation()
	{
		BidOperation op = new BidOperation( this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare( this );
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared CommentOperation.
	 */
	public CommentOperation createCommentOperation()
	{
		CommentOperation op = new CommentOperation( this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare( this );
		return op;
	}
}
