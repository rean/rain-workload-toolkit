package radlab.rain.workload.scadr;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.IScoreboard;
import radlab.rain.Operation;
import radlab.rain.util.HttpTransport;

public abstract class ScadrOperation extends Operation 
{
	// These references will be set by the Generator.
	protected HttpTransport _http;
	protected HashSet<String> _cachedURLs = new HashSet<String>();
	private Random _random = new Random();	
	
	public ScadrOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	public ScadrGenerator getGenerator()
	{
		return (ScadrGenerator) this._generator;
	}
	
	@Override
	public void cleanup() 
	{}

	@Override
	public void prepare(Generator generator) {
		this._generator = generator;
		ScadrGenerator scadrGenerator = (ScadrGenerator) generator;
		
		// Refresh the cache to simulate real-world browsing.
		this.refreshCache();
		
		this._http = scadrGenerator.getHttpTransport();
		LoadProfile currentLoadProfile = scadrGenerator.getLatestLoadProfile();
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
	
	//<form action="/user_session" class="new_user_session" id="user_session_3558" method="post"><div style="margin:0;padding:0;display:inline"><input name="authenticity_token" type="hidden" value="XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=" /></div>
	//<input name="authenticity_token" type="hidden" value="XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=" />
	public String parseAuthTokenRegex( StringBuilder buffer ) throws IOException
	{
		String token = "";
		System.out.println( buffer.toString() );
		Pattern authTokenPattern = Pattern.compile( ".*<input name=\"authenticity_token\" (.)* value=\"(.*).*\"", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS );
		Matcher match = authTokenPattern.matcher( buffer.toString() );
		System.out.println( "Groups: " + match.groupCount() );
		if( match.find() )
		{
			
			System.out.println( match.group(1) );
		}
		
		
		if( match.groupCount() == 2 )
			token = match.group(1);
				
		return token;
	}
	
	/**
	 * Parses an HTML document for an authenticity token used by the Ruby on
	 * Rails framework to authenticate forms.
	 * 
	 * @param buffer    The HTTP response; expected to be an HTML document.
	 * @return          The authenticity token if found; otherwise, null.
	 * 
	 * @throws IOException
	 */
	public String parseAuthTokenSAX( StringBuilder buffer ) throws IOException 
	{
		String token = "";
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			factory.setValidating(false);
			factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
			
			Document document = factory.newDocumentBuilder().parse( new InputSource( new StringReader( buffer.toString() ) ) );
			
			NodeList inputList = document.getElementsByTagName("input");
			for ( int i = 0; i < inputList.getLength(); i++ )
			{
				Element input = (Element) inputList.item(i);
				String name = input.getAttribute("name");
				if ( name.equals("authenticity_token") )
				{
					token = input.getAttribute("value");
					break;
				}
			}
		}
		catch ( Exception e )
		{
			//e.printStackTrace();
		}
		
		return token;
	}
	
	public static void main( String[] args )
	{
		String input = "<form action=\"/user_session\" class=\"new_user_session\" id=\"user_session_3558\" method=\"post\"><div style=\"margin:0;padding:0;display:inline\"><input name=\"authenticity_token\" type=\"hidden\" value=\"XiJqPX2XOX0y3RHpVHLbhrjxcuDDnUTcvkrGVP7yDXk=\" /></div>";
		Pattern authTokenPattern = Pattern.compile( "<input name=\"authenticity_token\" (.)* value=\"(.*)\"", Pattern.CASE_INSENSITIVE  );
		Matcher match = authTokenPattern.matcher( input );
		match.find();
		System.out.println( "Groups: " + match.groupCount() );
		System.out.println( match.group(2) );
		
	}
}
