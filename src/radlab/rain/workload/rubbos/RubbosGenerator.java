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

package radlab.rain.workload.rubbos;


import java.util.Arrays;
import java.util.logging.Logger;
import java.util.Random;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.json.JSONException;
import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;


/**
 * Operation generator for the RUBBoS workload.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class RubbosGenerator extends Generator
{
	// Operation indices used in the mix matrix.
	// Use the same order of the native RUBBoS mix matrix
	public static final int STORIES_OF_THE_DAY_OP = 0;
	public static final int REGISTER_OP = 1;
	public static final int REGISTER_USER_OP = 2;
	public static final int BROWSE_OP = 3;
	public static final int BROWSE_CATEGORIES_OP = 4;
	public static final int BROWSE_STORIES_BY_CATEGORY_OP = 5;
	public static final int OLDER_STORIES_OP = 6;
	public static final int VIEW_STORY_OP = 7;
	public static final int POST_COMMENT_OP = 8;
	public static final int STORE_COMMENT_OP = 9;
	public static final int VIEW_COMMENT_OP = 10;
	public static final int MODERATE_COMMENT_OP = 11;
	public static final int STORE_MODERATE_LOG_OP = 12;
	public static final int SUBMIT_STORY_OP = 13;
	public static final int STORE_STORY_OP = 14;
	public static final int SEARCH_OP = 15;
	public static final int SEARCH_IN_STORIES_OP = 16;
	public static final int SEARCH_IN_COMMENTS_OP = 17;
	public static final int SEARCH_IN_USERS_OP = 18;
	public static final int AUTHOR_LOGIN_OP = 19;
	public static final int AUTHOR_TASKS_OP = 20;
	public static final int REVIEW_STORIES_OP = 21;
	public static final int ACCEPT_STORY_OP = 22;
	public static final int REJECT_STORY_OP = 23;
	public static final int BACK_SPECIAL_OP = 24; ///< Emulate a click on the "Back" button of the browser
	public static final int EOS_SPECIAL_OP = 25; ///< Terminate the current user session

	// Static members shared among all instances
	private static Random _rng; ///< The Random Number Generator
	private static RubbosConfiguration _conf; ///< The RUBBoS-related configuration found in the JSON profile file


	private HttpTransport _http;
	private Logger _logger;
	private RubbosSessionState _sessionState; ///< Holds user session data
	private RubbosUtility _utility;
	private double _thinkTime = -1; ///< The mean think time; a value <= 0 means that no think time is used.
	private NegativeExponential _thinkTimeRng;
	private double _cycleTime = -1; ///< The mean cycle time; a value <= 0 means that no cycle time is used.
	private NegativeExponential _cycleTimeRng;
	private String _baseURL;
	private String _storiesOfTheDayURL; 
	private String _registerURL;
	private String _registerUserURL;
	private String _browseURL;
	private String _browseCategoriesURL; 
	private String _browseStoriesByCategoryURL;
	private String _olderStoriesURL;
	private String _viewStoryURL;
	private String _postCommentURL;
	private String _storeCommentURL;
	private String _viewCommentURL;
	private String _moderateCommentURL;
	private String _storeModerateLogURL;
	private String _submitStoryURL;
	private String _storeStoryURL;
	private String _searchURL;
	private String _searchInStoriesURL;
	private String _searchInCommentsURL;
	private String _searchInUsersURL;
	private String _authorLoginURL;
	private String _authorTasksURL;
	private String _reviewStoriesURL;
	private String _acceptStoryURL;
	private String _rejectStoryURL;


	/**
	 * Returns the internally used random number generator.
	 * 
	 * @return A Random object.
	 */
	public static Random getRandomGenerator()
	{
		//NOTE: this method is not "synchronized" since java.util.Random is threadsafe.
		return _rng;
	}

	/**
	 * Set the internally used random number generator.
	 * 
	 * @param value A Random object.
	 */
	protected static synchronized void setRandomGenerator(Random value)
	{
		_rng = value;
	}

	/**
	 * Get the internally used RUBBoS configuration object.
	 * 
	 * @return A RubbosConfiguration object.
	 */
	public static synchronized RubbosConfiguration getConfiguration()
	{
		return _conf;
	}

	/**
	 * Set the internally used RUBBoS configuration object.
	 * 
	 * @param value A RubbosConfiguration object.
	 */
	protected static synchronized void setConfiguration(RubbosConfiguration value)
	{
		_conf = value;
	}

	/**
	 * Initialize the shared configuration object.
	 */
	private static synchronized void initializeConfiguration(JSONObject config) throws JSONException
	{
		if (_conf == null)
		{
			_conf = new RubbosConfiguration(config);
		}
	}

	/**
	 * Initialize the shared random number generator.
	 */
	private static synchronized void initizializeRandomGenerator()
	{
		if (_rng == null)
		{
			if (getConfiguration().getRngSeed() >= 0)
			{
				_rng = new Random(getConfiguration().getRngSeed());
			}
			else
			{
				_rng = new Random();
			}
		}
	}


	/**
	 * A constructor.
	 */
	public RubbosGenerator(ScenarioTrack track)
	{
		super(track);
	}

	@Override
	public void configure(JSONObject config) throws JSONException
	{
		this.initializeConfiguration(config);
	}

	/**
	 * Initialize this generator.
	 */
	@Override
	public void initialize()
	{
		this.initizializeRandomGenerator();
		this._http = new HttpTransport();
		this._logger = Logger.getLogger(this.getName());
		this._sessionState = new RubbosSessionState();
		this._utility = new RubbosUtility(this._rng, this._conf);

		// Setup think and cycle times
		this._thinkTime = this.getTrack().getMeanThinkTime();
		if (this._thinkTime > 0)
		{
			this._thinkTimeRng = new NegativeExponential(this._thinkTime, this._rng);
		}
		this._cycleTime = this.getTrack().getMeanCycleTime();
		if (this._cycleTime > 0)
		{
			this._cycleTimeRng = new NegativeExponential(this._cycleTime, this._rng);
		}

		// Select a random user for current session (if needed)
		this.getSessionState().setLoggedUserId(this.getUtility().generateUser().id);

		// Build RUBBoS URLs
		this.initializeUrls();
	}

	/**
	 * Returns the next <code>Operation</code> given the <code>lastOperation</code>
	 * according to the current mix matrix.
	 * 
	 * @param lastOperation The last <code>Operation</code> that was executed.
	 */
	@Override
	public Operation nextRequest(int lastOperation)
	{
		LoadProfile currentLoad = this.getTrack().getCurrentLoadProfile();
		int nextOperation = -1;

		if (lastOperation == -1)
		{
			nextOperation = this.getConfiguration().getInitialOperation();
		}
		else if (lastOperation == BACK_SPECIAL_OP)
		{
			// Back to previous state
			nextOperation = Math.max(this.getConfiguration().getInitialOperation(), this._sessionState.getLastOperation());
		}
		else if (lastOperation == EOS_SPECIAL_OP)
		{
			// End-of-session

			// Start from the initial operation
			nextOperation = this.getConfiguration().getInitialOperation();

			// Clear session data
			this.getSessionState().clear();
			// Generate a new user for the new session
			this.getSessionState().setLoggedUserId(this.getUtility().generateUser().id);
		}
		else
		{
			// Get the selection matrix
			double[][] selectionMix = this.getTrack().getMixMatrix(currentLoad.getMixName()).getSelectionMix();
			double rand = this._rng.nextDouble();

			int j;
			for (j = 0; j < selectionMix.length; ++j)
			{
				if (rand <= selectionMix[lastOperation][j])
				{
					break;
				}
			}
			nextOperation = j;
		}
		return this.getOperation(nextOperation);
	}

	/**
	 * Disposes of unnecessary objects at the conclusion of a benchmark run.
	 */
	@Override
	public void dispose()
	{
	}

	/**
	 * Returns the current think time. The think time is duration between
	 * receiving the response of an operation and the execution of its
	 * succeeding operation during synchronous execution (i.e. closed loop).
	 */
	@Override
	public long getThinkTime()
	{
		if (this._thinkTime <= 0)
		{
			return 0;
		}

		return Math.round(this._thinkTimeRng.nextDouble());
	}

	/**
	 * Returns the current cycle time. The cycle time is duration between
	 * the execution of an operation and the execution of its succeeding
	 * operation during asynchronous execution (i.e. open loop).
	 */
	@Override
	public long getCycleTime()
	{
		if (this._cycleTime <= 0)
		{
			return 0;
		}

		return Math.round(this._cycleTimeRng.nextDouble());
	}

	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}

	/**
	 * Returns the logger.
	 * 
	 * @return A logger.
	 */
	public Logger getLogger()
	{
		return this._logger;
	}

	public RubbosSessionState getSessionState()
	{
		return this._sessionState;
	}

	protected void setSessionState(RubbosSessionState value)
	{
		this._sessionState = value;
	}

	public RubbosUtility getUtility()
	{
		return this._utility;
	}

	protected void setUtility(RubbosUtility value)
	{
		this._utility = value;
	}

	public boolean checkHttpResponse(String response)
	{
		return this.getUtility().checkHttpResponse(this.getHttpTransport(), response);
	}

	public String getBaseURL()
	{
		return this._baseURL;
	}

	public String getStoriesOfTheDayURL()
	{
		return this._storiesOfTheDayURL; 
	}

	public String getRegisterURL()
	{
		return this._registerURL;
	}

	public String getRegisterUserURL()
	{
		return this._registerUserURL;
	}

	public String getBrowseURL()
	{
		return this._browseURL;
	}

	public String getBrowseCategoriesURL()
	{
		return this._browseCategoriesURL; 
	}

	public String getBrowseStoriesByCategoryURL()
	{
		return this._browseStoriesByCategoryURL;
	}

	public String getOlderStoriesURL()
	{
		return this._olderStoriesURL;
	}

	public String getViewStoryURL()
	{
		return this._viewStoryURL;
	}

	public String getPostCommentURL()
	{
		return this._postCommentURL;
	}

	public String getStoreCommentURL()
	{
		return this._storeCommentURL;
	}

	public String getViewCommentURL()
	{
		return this._viewCommentURL;
	}

	public String getModerateCommentURL()
	{
		return this._moderateCommentURL;
	}

	public String getStoreModerateLogURL()
	{
		return this._storeModerateLogURL;
	}

	public String getSubmitStoryURL()
	{
		return this._submitStoryURL;
	}

	public String getStoreStoryURL()
	{
		return this._storeStoryURL;
	}

	public String getSearchURL()
	{
		return this._searchURL;
	}

	public String getSearchInStoriesURL()
	{
		return this._searchInStoriesURL;
	}

	public String getSearchInCommentsURL()
	{
		return this._searchInCommentsURL;
	}

	public String getSearchInUsersURL()
	{
		return this._searchInUsersURL;
	}

	public String getAuthorLoginURL()
	{
		return this._authorLoginURL;
	}

	public String getAuthorTasksURL()
	{
		return this._authorTasksURL;
	}

	public String getReviewStoriesURL()
	{
		return this._reviewStoriesURL;
	}

	public String getAcceptStoryURL()
	{
		return this._acceptStoryURL;
	}

	public String getRejectStoryURL()
	{
		return this._rejectStoryURL;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoriesOfTheDay.
	 */
	public StoriesOfTheDayOperation createStoriesOfTheDayOperation()
	{
		StoriesOfTheDayOperation op = new StoriesOfTheDayOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RegisterOperation.
	 */
	public RegisterOperation createRegisterOperation()
	{
		RegisterOperation op = new RegisterOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RegisterUserOperation.
	 */
	public RegisterUserOperation createRegisterUserOperation()
	{
		RegisterUserOperation op = new RegisterUserOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseOperation.
	 */
	public BrowseOperation createBrowseOperation()
	{
		BrowseOperation op = new BrowseOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseCategoriesOperation.
	 */
	public BrowseCategoriesOperation createBrowseCategoriesOperation()
	{
		BrowseCategoriesOperation op = new BrowseCategoriesOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared BrowseStoriesByCategoryOperation.
	 */
	public BrowseStoriesByCategoryOperation createBrowseStoriesByCategoryOperation()
	{
		BrowseStoriesByCategoryOperation op = new BrowseStoriesByCategoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared OlderStoriesOperation.
	 */
	public OlderStoriesOperation createOlderStoriesOperation()
	{
		OlderStoriesOperation op = new OlderStoriesOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewStoryOperation.
	 */
	public ViewStoryOperation createViewStoryOperation()
	{
		ViewStoryOperation op = new ViewStoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared PostCommentOperation.
	 */
	public PostCommentOperation createPostCommentOperation()
	{
		PostCommentOperation op = new PostCommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoreCommentOperation.
	 */
	public StoreCommentOperation createStoreCommentOperation()
	{
		StoreCommentOperation op = new StoreCommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ViewCommentOperation.
	 */
	public ViewCommentOperation createViewCommentOperation()
	{
		ViewCommentOperation op = new ViewCommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ModerateCommentOperation.
	 */
	public ModerateCommentOperation createModerateCommentOperation()
	{
		ModerateCommentOperation op = new ModerateCommentOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoreModerateLogOperation.
	 */
	public StoreModerateLogOperation createStoreModerateLogOperation()
	{
		StoreModerateLogOperation op = new StoreModerateLogOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SubmitStoryOperation.
	 */
	public SubmitStoryOperation createSubmitStoryOperation()
	{
		SubmitStoryOperation op = new SubmitStoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared StoreStoryOperation.
	 */
	public StoreStoryOperation createStoreStoryOperation()
	{
		StoreStoryOperation op = new StoreStoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchOperation.
	 */
	public SearchOperation createSearchOperation()
	{
		SearchOperation op = new SearchOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchInStoriesOperation.
	 */
	public SearchInStoriesOperation createSearchInStoriesOperation()
	{
		SearchInStoriesOperation op = new SearchInStoriesOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchInCommentsOperation.
	 */
	public SearchInCommentsOperation createSearchInCommentsOperation()
	{
		SearchInCommentsOperation op = new SearchInCommentsOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared SearchInUsersOperation.
	 */
	public SearchInUsersOperation createSearchInUsersOperation()
	{
		SearchInUsersOperation op = new SearchInUsersOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AuthorLoginOperation.
	 */
	public AuthorLoginOperation createAuthorLoginOperation()
	{
		AuthorLoginOperation op = new AuthorLoginOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AuthorTasksOperation.
	 */
	public AuthorTasksOperation createAuthorTasksOperation()
	{
		AuthorTasksOperation op = new AuthorTasksOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared ReviewStoriesOperation.
	 */
	public ReviewStoriesOperation createReviewStoriesOperation()
	{
		ReviewStoriesOperation op = new ReviewStoriesOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared AcceptStoryOperation.
	 */
	public AcceptStoryOperation createAcceptStoryOperation()
	{
		AcceptStoryOperation op = new AcceptStoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Factory method.
	 * 
	 * @return  A prepared RejectStoryOperation.
	 */
	public RejectStoryOperation createRejectStoryOperation()
	{
		RejectStoryOperation op = new RejectStoryOperation(this.getTrack().getInteractive(), this.getScoreboard());
		op.prepare(this);
		return op;
	}

	/**
	 * Creates a newly instantiated, prepared operation.
	 * 
	 * @param opIndex The type of operation to instantiate.
	 * @return A prepared operation.
	 */
	private Operation getOperation(int opIndex)
	{
		switch (opIndex)
		{
			case STORIES_OF_THE_DAY_OP:
				return this.createStoriesOfTheDayOperation();
			case REGISTER_OP:
				return this.createRegisterOperation();
			case REGISTER_USER_OP:
				return this.createRegisterUserOperation();
			case BROWSE_OP:
				return this.createBrowseOperation();
			case BROWSE_CATEGORIES_OP:
				return this.createBrowseCategoriesOperation();
			case BROWSE_STORIES_BY_CATEGORY_OP:
				return this.createBrowseStoriesByCategoryOperation();
			case OLDER_STORIES_OP:
				return this.createOlderStoriesOperation();
			case VIEW_STORY_OP:
				return this.createViewStoryOperation();
			case POST_COMMENT_OP:
				return this.createPostCommentOperation();
			case STORE_COMMENT_OP:
				return this.createStoreCommentOperation();
			case VIEW_COMMENT_OP:
				return this.createViewCommentOperation();
			case MODERATE_COMMENT_OP:
				return this.createModerateCommentOperation();
			case STORE_MODERATE_LOG_OP:
				return this.createStoreModerateLogOperation();
			case SUBMIT_STORY_OP:
				return this.createSubmitStoryOperation();
			case STORE_STORY_OP:
				return this.createStoreStoryOperation();
			case SEARCH_OP:
				return this.createSearchOperation();
			case SEARCH_IN_STORIES_OP:
				return this.createSearchInStoriesOperation();
			case SEARCH_IN_COMMENTS_OP:
				return this.createSearchInCommentsOperation();
			case SEARCH_IN_USERS_OP:
				return this.createSearchInUsersOperation();
			case AUTHOR_LOGIN_OP:
				return this.createAuthorLoginOperation();
			case AUTHOR_TASKS_OP:
				return this.createAuthorTasksOperation();
			case REVIEW_STORIES_OP:
				return this.createReviewStoriesOperation();
			case ACCEPT_STORY_OP:
				return this.createAcceptStoryOperation();
			case REJECT_STORY_OP:
				return this.createRejectStoryOperation();
			default:
		}

		return null;
	}

	/**
	 * Initialize the roots/anchors of the URLs.
	 */
	private void initializeUrls()
	{
		this.initializeCommonUrls();

		int incarnation = this.getConfiguration().getIncarnation();

		if (incarnation == RubbosConfiguration.PHP_INCARNATION)
		{
			this.initializePhpUrls();
		}
		else if (incarnation == RubbosConfiguration.SERVLET_INCARNATION)
		{
			this.initializeServletUrls();
		}
		else
		{
			this.getLogger().warning("RUBBoS incarnation '" + incarnation + "' has not been implemented yet. Default to 'servlet'");
			this.initializeServletUrls();
		}
	}

	/**
	 * Initialize the roots/anchors of the URLs that are common to all incarnations.
	 */
	private void initializeCommonUrls()
	{
		final String htmlPath = this.getConfiguration().getServerHtmlPath();

		this._baseURL = "http://" + this.getTrack().getTargetHostName() + ":" + this.getTrack().getTargetHostPort();
		this._registerURL = this._baseURL + htmlPath + "/register.html";
		this._browseURL = this._baseURL + htmlPath + "/browse.html";
		this._authorLoginURL = this._baseURL + htmlPath + "/author.html";
	}

	/**
	 * Initialize the roots/anchors of the URLs for the PHP incarnation.
	 */
	private void initializePhpUrls()
	{
		final String scriptPath = this.getConfiguration().getServerScriptPath();

		this._storiesOfTheDayURL = this._baseURL + scriptPath + "/StoriesOfTheDay.php";
		this._registerUserURL = this._baseURL + scriptPath + "/RegisterUser.php";
		this._browseCategoriesURL = this._baseURL + scriptPath + "/BrowseCategories.php";
		this._browseStoriesByCategoryURL = this._baseURL + scriptPath + "/BrowseStoriesByCategory.php";
		this._olderStoriesURL = this._baseURL + scriptPath + "/OlderStories.php";
		this._viewStoryURL = this._baseURL + scriptPath + "/ViewStory.php";
		this._postCommentURL = this._baseURL + scriptPath + "/PostComment.php";
		this._storeCommentURL = this._baseURL + scriptPath + "/StoreComment.php";
		this._viewCommentURL = this._baseURL + scriptPath + "/ViewComment.php";
		this._moderateCommentURL = this._baseURL + scriptPath + "/ModerateComment.php";
		this._storeModerateLogURL = this._baseURL + scriptPath + "/StoreModeratorLog.php";
		this._submitStoryURL = this._baseURL + scriptPath + "/SubmitStory.php";
		this._storeStoryURL = this._baseURL + scriptPath + "/StoreStory.php";
		this._searchURL = this._baseURL + scriptPath + "/Search.php";
		this._searchInStoriesURL = this._searchURL; // + "?type=0&";
		this._searchInCommentsURL = this._searchURL; // + "?type=1&";
		this._searchInUsersURL = this._searchURL; // + "?type=2&";
		this._authorTasksURL = this._baseURL + scriptPath + "/Author.php";
		this._reviewStoriesURL = this._baseURL + scriptPath + "/ReviewStories.php";
		this._acceptStoryURL = this._baseURL + scriptPath + "/AcceptStory.php";
		this._rejectStoryURL = this._baseURL + scriptPath + "/RejectStory.php";
	}

	/**
	 * Initialize the roots/anchors of the URLs for the servlet incarnation.
	 */
	private void initializeServletUrls()
	{
		final String scriptPath = this.getConfiguration().getServerScriptPath();

		this._storiesOfTheDayURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.StoriesOfTheDay";
		this._registerUserURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.RegisterUser";
		this._browseCategoriesURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.BrowseCategories";
		this._browseStoriesByCategoryURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.BrowseStoriesByCategory";
		this._olderStoriesURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.OlderStories";
		this._viewStoryURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.ViewStory";
		this._postCommentURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.PostComment";
		this._storeCommentURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.StoreComment";
		this._viewCommentURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.ViewComment";
		this._moderateCommentURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.ModerateComment";
		this._storeModerateLogURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.StoreModeratorLog";
		this._submitStoryURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.SubmitStory";
		this._storeStoryURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.StoreStory";
		this._searchURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.Search";
		this._searchInStoriesURL = this._searchURL; // + "?type=0&";
		this._searchInCommentsURL = this._searchURL; // + "?type=1&";
		this._searchInUsersURL = this._searchURL; // + "?type=2&";
		this._authorTasksURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.Author";
		this._reviewStoriesURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.ReviewStories";
		this._acceptStoryURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.AcceptStory";
		this._rejectStoryURL = this._baseURL + scriptPath + "/edu.rice.rubbos.servlets.RejectStory";
	}
}
