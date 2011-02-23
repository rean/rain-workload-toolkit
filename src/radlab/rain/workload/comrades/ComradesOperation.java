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

package radlab.rain.workload.comrades;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;

public abstract class ComradesOperation extends Operation 
{
	public static String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static double[] GPA = { 3.4, 3.5, 3.6, 3.7, 3.8, 3.8, 3.9, 3.9, 4.0, 4.0 };//, 4.1, 4.2, 4.3 };
	public static int[] INTERVIEW_SCORES = {1, 2, 3, 4, 4, 5, 5};
	//<a href="/candidates/abc%2540bar%252Eedu">
	public static String CANDIDATE_VIEW_PATTERN = "(<a href=\"/candidates/([^>]+)\">)";
	public static String[] RESEARCH_AREAS 				= { "systems", "networking", "databases", "theory", "hci", "architecture" };
	public static String[] INTERVIEW_DECISIONS = { "Offer", "Reject" };	
	
	// These references will be set by the Generator.
	protected HttpTransport _http;
	protected HashSet<String> _cachedURLs = new HashSet<String>();
	private Random _random = new Random();
	// Keep track of where this operation is supposed to go so that
	// we can update the app server traffic stats
	private String _appServerTarget = "";
		
	public ComradesOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	public ComradesGenerator getGenerator()
	{
		return (ComradesGenerator) this._generator;
	}
	
	@Override 
	public void preExecute()
	{
		if( this._appServerTarget == null || this._appServerTarget.trim().length() == 0 )
			return;
		
		ScenarioTrack track = this._generator.getTrack();
		if( track instanceof ComradesScenarioTrack )
			((ComradesScenarioTrack) track).requestIssue( this._appServerTarget );
	}
	
	@Override
	public void postExecute()
	{
		if( this._appServerTarget == null || this._appServerTarget.trim().length() == 0 )
			return;
		
		ScenarioTrack track = this._generator.getTrack();
		if( track instanceof ComradesScenarioTrack )
			((ComradesScenarioTrack) track).requestRetire( this._appServerTarget );
	}
	
	
	@Override
	public void cleanup() 
	{}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		ComradesGenerator comradesGenerator = (ComradesGenerator) generator;
		
		// Save the appServer target that's currently in the generator
		this._appServerTarget = comradesGenerator._appServerUrl;
						
		// Refresh the cache to simulate real-world browsing.
		this.refreshCache();
		
		this._http = comradesGenerator.getHttpTransport();
		LoadProfile currentLoadProfile = comradesGenerator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
	}

	/**
	 * Load the static files specified by the URLs if the current request is
	 * not cached and the file was not previously loaded and cached.
	 * 
	 * @param urls      The set of static file URLs.
	 * @return          The number of static files loaded.
	 * 
	 * @throws IOException
	 */
	protected long loadStatics( String[] urls ) throws IOException 
	{
		long staticsLoaded = 0;
		
		for ( String url : urls )
		{
			if ( this._cachedURLs.add( url ) ) 
			{
				this._http.fetchUrl( url );
				staticsLoaded++;
			}
		}
		
		return staticsLoaded;
	}
	
	/**
	 * Refreshes the cache by resetting it 40% of the time.
	 * 
	 * @return      True if the cache was refreshed; false otherwise.
	 */
	protected boolean refreshCache()
	{
		boolean resetCache = ( this._random.nextDouble() < 0.6 ); 
		if ( resetCache )
		{
			this._cachedURLs.clear();
		}
		
		return resetCache;
	}
	
	public String doHomePage() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		//System.out.println( "Starting HomePage" );
		String authToken = "";
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._homeUrl );
		this.trace( this.getGenerator()._homeUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "Home page GET ERROR - Received an empty/error response. HTTP Status Code: " + this._http.getStatusCode();
			throw new IOException( errorMessage );
		}
		
		// Get the authenticity token for login and pass it to the generator so that it can
		// be used to log in if necessary
		authToken = "";//this.parseAuthTokenRegex( response ); 
				
		this.loadStatics( this.getGenerator().homepageStatics );
		this.trace( this.getGenerator().homepageStatics );
		//System.out.println( "HomePage worked" );
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "HomePage (s): " + (end - start)/1000.0 );
		}
		
		return authToken;
	}
	
	public void doAddCandidate() throws Exception
	{
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		StringBuilder response = this._http.fetchUrl( this.getGenerator()._newCandidateUrl );
		this.trace( this.getGenerator()._homeUrl );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "New candidate page GET ERROR - Received an empty/error response. HTTP Status Code: " + this._http.getStatusCode();
			throw new IOException( errorMessage );
		}
		
		this.loadStatics( this.getGenerator().candidateStatics );
		this.trace( this.getGenerator().candidateStatics );
		
		// Now do a post to create a new candidate
		// Success message: "Candidate profile added"
		
		/* POST to /candidates
		 
		 	candidate[email]	foo@foo.com
			candidate[gpa]	3.4
			candidate[name]	test user
			candidate[research_area]	systems
			candidate[school]	Foo University
			commit	Create Candidate
		 */
		
		String commitAction = "Create Candidate";
		// Pick 
		String username = this.getUserName();
		String email = this.getEmail( username );
		double gpa = this.getGPA( username );
		String researchArea = this.getResearchArea();
		String school = this.getSchool( username );
		
		// Post the to create user results url
		HttpPost httpPost = new HttpPost( this.getGenerator()._candidatesUrl );
		// Weird things happen if we don't specify HttpMultipartMode.BROWSER_COMPATIBLE.
		// Scadr rejects the auth token as invalid without it. Not sure if this is a 
		// Scadr-specific issue or not. HTTP POSTs by the Olio (Ruby web-app) driver 
		// worked without it. 
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		//entity.addPart( "authenticity_token", new StringBody( authToken ) );
		entity.addPart( "commit", new StringBody( commitAction ) );
		entity.addPart( "candidate[email]", new StringBody( email ) );
		entity.addPart( "candidate[gpa]", new StringBody( Double.toString( gpa ) ) );
		entity.addPart( "candidate[name]", new StringBody( username ) );
		entity.addPart( "candidate[research_area]", new StringBody( researchArea ) );
		entity.addPart( "candidate[school]", new StringBody( school ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		response = this._http.fetch( httpPost );
		
		//System.out.println( response );
		
		// Look at the response for the string 'Candidate profile added'
		StringBuilder successMessage = new StringBuilder();
		successMessage.append( "Candidate profile added" );
		
		if(! (response.toString().contains( successMessage.toString() ) ) )
			throw new Exception( "Creating new candidate: " + username.toString() + " failed! No success message found. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
		
		this.trace( this.getGenerator()._baseUrl );
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "AddCandidate (s): " + (end - start)/1000.0 );
		}
	}
	
	private String getUserName()
	{ 
		// Use pre-loaded names (if available)
		if( ComradesScenarioTrack.NAMES.size() > 0 )
			return ComradesScenarioTrack.NAMES.get( this._random.nextInt( ComradesScenarioTrack.NAMES.size() ) );
		else // Use random names if we can't find pre-loaded names 
		{
			// Make firstnames with up to 6 characters
			// Make lastnames with up to 9 characters
			String firstname = this.randomString( 6 );
			String lastname = this.randomString( 9 );
			
			StringBuffer buf = new StringBuffer();
			buf.append( firstname );
			buf.append( " " );
			buf.append( lastname );
			
			return buf.toString();
		}
	}
	
	private String randomString( int length )
	{
		StringBuilder value = new StringBuilder();
		int maxCount = length;
		while( maxCount > 0 )
		{
			char rndChar = ComradesOperation.ALPHABET.charAt( this._random.nextInt( ComradesOperation.ALPHABET.length() ) );
			value.append( rndChar );
			maxCount--;
		}
		return value.toString();
	}
	
	private String getEmail( String username )
	{ 
		StringBuffer buf = new StringBuffer();
		buf.append( username.replace( " ", "" ) );
		buf.append( "@" );
		buf.append( this.randomString( 5 ) );
		buf.append( ".com" );	
		return buf.toString(); 
	}
	
	private String getSchool( String username )
	{ 
		StringBuffer buf = new StringBuffer();
		buf.append( this.randomString( 3 ).toUpperCase() );
		buf.append( " " );
		buf.append( "University" );
		return buf.toString(); 
	}
	
	private String getResearchArea()
	{ 
		return RESEARCH_AREAS[this._random.nextInt( RESEARCH_AREAS.length )]; 
	}
	
	private double getGPA( String username )
	{ 
		return GPA[this._random.nextInt( GPA.length )];
	}
	
	public int doSubmitInterview() throws Exception
	{
		int score = -1;
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		// Pick a candidate, view their details
		// check whether we can submit an interview for them
		// If we can then do it, otherwise just exit
		boolean foundInterviewee = false;
		// Try three times to find a candidate that's available for interview
		int maxSearches = 3;
		
		while( maxSearches > 0 )
		{
			InterviewStatus status = this.doCandidateDetails();
			if( status != null && status._canInterview )
			{
				StringBuffer interviewDetailsUrl = new StringBuffer();
				//interviewDetailsUrl.append( this.getGenerator()._candidatesUrl ).append( "/" );
				interviewDetailsUrl.append( status._interviewDetailsUrl );
				
				//System.out.println( "Found interviewee: " + interviewDetailsUrl.toString() );
				score = this.doSubmitInterview( interviewDetailsUrl.toString() );
							
				foundInterviewee = true;
				break;
			}
			maxSearches--;
		}
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "Submit Interview (s) [found interviewee (" +  foundInterviewee + ")]: " + (end - start)/1000.0 );
		}		
		
		return score;
	}
	
	private int doSubmitInterview( String interviewDetailsUrl ) throws Exception
	{
		// Do POST
		HttpPost httpPost = new HttpPost( interviewDetailsUrl );
		// Weird things happen if we don't specify HttpMultipartMode.BROWSER_COMPATIBLE.
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
		
		// POST to view interview details url, e.g.,: http://localhost:3000/candidates/bar%2540bar%252Ecom/interviews/1298399477
		/*
		 _method	put
		commit	Update Interview
		interview[comments]	awesome!!!
		interview[interviewer]	me
		interview[score]	5
		 */
		
		int score = INTERVIEW_SCORES[this._random.nextInt(INTERVIEW_SCORES.length)];
		entity.addPart( "_method", new StringBody( "put" ) );
		entity.addPart( "commit", new StringBody( "Update Interview" ) );
		entity.addPart( "interview[comments]", new StringBody( this.randomString( 100 ) ) );
		entity.addPart( "interview[interviewer]", new StringBody( this.getUserName() ) );
		entity.addPart( "interview[score]", new StringBody( Integer.toString( score ) ) );
		httpPost.setEntity( entity );
		
		// Make the POST request and verify that it succeeds.
		StringBuilder response = this._http.fetch( httpPost );
		
		// Look for success message
		if( !( response.toString().contains( "Candidate interviewed." ) ) )
			throw new Exception( "Interviewing candidate: " + interviewDetailsUrl + " failed! No success message found. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
		
		this.trace( interviewDetailsUrl.toString() );
		
		return score;
	}
	
	// TODO: Rewrite this method so we pick from top-rated only
	public void doUpdateInterview() throws Exception
	{
		// Make an offer or reject
		// Find a candidate that we've already interviewed
		// Make them an offer
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		// Pick a candidate, view their details
		// check whether we can submit an interview for them
		// If we can then do it, otherwise just exit
		boolean foundInterviewee = false;
		// Try three times to find a candidate that's available for interview
		int maxSearches = 5;
		
		while( maxSearches > 0 )
		{
			InterviewStatus status = this.doCandidateDetails();
			// 50% of the time force a decision
			if( status != null && status._canInterview && this._random.nextDouble() < 0.5 )
			{
				// Force an interview
				//System.out.println( "Forcing interview..." );
				/*int score =*/ this.doSubmitInterview( status._interviewDetailsUrl );
				status._canInterview = false;
			}
			
			if( !status._canInterview )
			{
				StringBuffer interviewDecisionUrl = new StringBuffer();
				//interviewDecisionUrl.append( this.getGenerator()._candidatesUrl ).append( "/" );
				interviewDecisionUrl.append( status._interviewDetailsUrl );
				interviewDecisionUrl.append( "/decide" );
				
				//System.out.println( "Found offeree: " + interviewDecisionUrl.toString() );
				
				HttpPost httpPost = new HttpPost( interviewDecisionUrl.toString() );
				// Weird things happen if we don't specify HttpMultipartMode.BROWSER_COMPATIBLE.
				MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
				
				// POST URL: http://localhost:3000/candidates/abc%2540bar%252Eedu/interviews/1298396844/decide
				/* Params: 
				 *
				 * _method	put
				 * commit	Offer
				 * interview[decision]	OFFER
				 * 
				 * _method	put
				 * commit	Reject
				 * interview[decision]	REJECT
				*/
				// Success message: "Offer sent to candidate testuser."
				// 
				String decision = "";
				if( this._random.nextDouble() < 0.25 )
					decision = "Offer";
				else decision = "Reject";
				
				entity.addPart( "_method", new StringBody( "put" ) );
				entity.addPart( "commit", new StringBody( decision ) );
				entity.addPart( "interview[decision]", new StringBody( decision.toUpperCase() ) );
				httpPost.setEntity( entity );
				
				// Make the POST request and verify that it succeeds.
				StringBuilder response = this._http.fetch( httpPost );
				
				// Look for success message
				if( response.toString().contains( "Rejected candidate" ) || response.toString().contains( "Offer sent to candidate" ) )
				{
					
				}
				else throw new Exception( "Deciding candidate fate: " + interviewDecisionUrl.toString() + " failed! No success message found. HTTP Status Code: " + this._http.getStatusCode() + " Response: " + response.toString() );
				
				this.trace( interviewDecisionUrl.toString() );
				
				foundInterviewee = true;
				break;
			}	
			maxSearches--;
		}
		
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "Update Interview (s) [found interviewee (" +  foundInterviewee + ")]: " + (end - start)/1000.0 );
		}
	}
	
	public InterviewStatus doCandidateDetails() throws Exception
	{
		InterviewStatus status = null;
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();
		
		Vector<String> candidates = this.searchResearchAreasForCandidate(); 
		if( candidates.size() > 0 )
		{
			String candidate = "";
			// Pick a candidate at random
			if( candidates.size() == 1 )
				candidate = candidates.firstElement();
			else candidate = candidates.get( this._random.nextInt( candidates.size() ) );
			status = this.getCandidateInterviewDetails( candidate );
		}
				
		if( debug )
		{
			end = System.currentTimeMillis();
			System.out.println( "CandidateDetails (s) [found(" + (candidates.size() > 0) + ")]: " + (end - start)/1000.0 );
		}
		
		return status;
	}
	
	private Vector<String> searchResearchAreasForCandidate() throws Exception
	{
		// Put all the areas into a vector
		Vector<String> areas = new Vector<String>();
		for( int i = 0; i < RESEARCH_AREAS.length; i++ )
			areas.add( RESEARCH_AREAS[i] ) ;
		
		// Randomly permute the list of research areas
		Collections.shuffle( areas );
		Vector<String> candidates = null;
		
		// Search through the research areas until we find one with some candidates
		for( String researchArea : areas )
		{	
			candidates = this.doSearchCandidates( researchArea );
			if( candidates.size() > 0 )
				break;
		}
		
		return candidates;
	}
	
	private InterviewStatus getCandidateInterviewDetails( String candidate ) throws IOException
	{
		InterviewStatus status = null;
		// Go get the details
		StringBuffer candidateDetailsUrl = new StringBuffer();
		candidateDetailsUrl.append( this.getGenerator()._candidatesUrl );
		candidateDetailsUrl.append( "/" ).append( candidate );
		
		StringBuilder response = this._http.fetchUrl( candidateDetailsUrl.toString() );
		// Parse the page for any candidates
		this.trace( candidateDetailsUrl.toString() );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "Candidate details GET ERROR - Received an empty/error response. HTTP Status Code: " + this._http.getStatusCode();
			throw new IOException( errorMessage );
		}
		
		// Parse for interview scheduling or viewing interview details
		Pattern candiateInterviewPattern = Pattern.compile( CANDIDATE_VIEW_PATTERN, Pattern.CASE_INSENSITIVE );
		Matcher candidateInterviewMatch = candiateInterviewPattern.matcher( response.toString() );
				
		status = new InterviewStatus();
		// Assume that we can't interview the candidate, we can just view their interview details
		//System.out.println( "Groups: " + match.groupCount() );
		while( candidateInterviewMatch.find() )
		{
			//System.out.println( buffer.substring( match.start(), match.end()) );
			String interview  = candidateInterviewMatch.group(2);
			if( interview.contains( "interview" ) )
			{
				StringBuilder fullQUrl = new StringBuilder();
				fullQUrl.append( this.getGenerator()._candidatesUrl ).append( "/" );
				
				//System.out.println( "Interview: " + interview );
				if( interview.contains( "/edit" ) )
				{
					status._interviewDetailsUrl = fullQUrl.append( interview.replace( "/edit", "" ) ).toString();
					//System.out.println( "Interview (editable): " + status._interviewDetailsUrl );
					status._canInterview = true;
				}
				else status._interviewDetailsUrl = fullQUrl.append( interview ).toString();
			}
		}
		return status;
	}
	
	public Vector<String> doSearchCandidates() throws Exception
	{
		// Pick an area to search for
		String researchArea = this.getResearchArea();
		return this.doSearchCandidates( researchArea );
	}
	
	public Vector<String> doSearchCandidates( String researchArea ) throws Exception
	{
		Vector<String> candidates = new Vector<String>();
		// Force all the searches to be for systems candidates (for testing)
		//if( true )
			//researchArea = "systems";
		
		// Issue a get for the home page ? researcharea
		// ?research_area=systems
		long start = 0;
		long end = 0;
		boolean debug = this.getGenerator().getIsDebugMode();
			
		if( debug )
			start = System.currentTimeMillis();

		StringBuffer searchUrl = new StringBuffer();
		searchUrl.append( this.getGenerator()._homeUrl );
		searchUrl.append( "/?research_area=" );
		searchUrl.append( researchArea );
		
		StringBuilder response = this._http.fetchUrl(  searchUrl.toString() );
		// Parse the page for any candidates
		this.trace( searchUrl.toString() );
		if( response.length() == 0 || this._http.getStatusCode() > 399 )
		{
			String errorMessage = "Search candidate GET ERROR - Received an empty/error response. HTTP Status Code: " + this._http.getStatusCode();
			throw new IOException( errorMessage );
		}
		
		this.loadStatics( this.getGenerator().homepageStatics );
		this.trace( this.getGenerator().homepageStatics );
		
		Pattern candiatePattern = Pattern.compile( CANDIDATE_VIEW_PATTERN, Pattern.CASE_INSENSITIVE );
		Matcher candidateMatch = candiatePattern.matcher( response.toString() );
		//System.out.println( "Groups: " + match.groupCount() );
		while( candidateMatch.find() )
		{
			//System.out.println( buffer.substring( match.start(), match.end()) );
			String candidate = candidateMatch.group(2);
			if( !candidate.equalsIgnoreCase( "new" ) )
			{	
				//System.out.println( "Candidate: " + candidateMatch.group(2) );
				candidates.add( candidate );
			}
		}
				
		if( debug )
		{
			System.out.println( "Found " + candidates.size() + " candidates in research area: " + researchArea) ;
			end = System.currentTimeMillis();
			System.out.println( "SearchCandidate (s): " + (end - start)/1000.0 );
		}
		
		return candidates;
	}
}
