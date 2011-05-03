package radlab.rain.util.storage;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class UniformKeyGenerator extends KeyGenerator 
{	
	protected String name = "Uniform";

	protected Random random = null;

	/** Lower bound of the key(s) generated, inclusive. */
	protected int lowerBound;

	/** Upper bound of the key(s) generated, exclusive. */
	protected int upperBound;

	public UniformKeyGenerator( JSONObject configObj ) throws JSONException
	{
		this( configObj.getInt( MIN_KEY_CONFIG_KEY ),
			  configObj.getInt( MAX_KEY_CONFIG_KEY ), 
			  configObj.getLong( RNG_SEED_KEY ) );
	}

	public UniformKeyGenerator( int minKey, int maxKey, long seed )
	{
		this.lowerBound = minKey;
		// maxKey is inclusive, upperBound is exclusive.
		this.upperBound = maxKey + 1;
		this.random = new Random( seed );
	}
	
	@Override
	public int generateKey()
	{
		return random.nextInt( upperBound - lowerBound ) + lowerBound;
	}
}
