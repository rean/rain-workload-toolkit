package radlab.rain.test;

import static org.junit.Assert.fail;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Operation;
import radlab.rain.Scenario;
import radlab.rain.util.ConfigUtil;
import radlab.rain.workload.redis.RedisGenerator;
import radlab.rain.workload.redis.RedisRequest;

public class RedisOperationsTest 
{
	private static DefaultScenarioTrack track;
	private RedisGenerator generator;
	
	@BeforeClass
	public static void scenarioSetup() 
	{
		String generatorClassName = "radlab.rain.workload.redis.RedisGenerator";
		String hostname = null;
		int port = -1;
		
		try
		{
			String filename = "config/profiles.config.redis.json";
			String fileContents = ConfigUtil.readFileAsString( filename );
			JSONObject jsonConfigRoot = new JSONObject( fileContents );
			JSONObject jsonTrack = jsonConfigRoot.getJSONObject( "redis-001" );
			JSONObject jsonConfig = jsonTrack.getJSONObject( "target" );
			
			hostname = jsonConfig.getString( "hostname" );
			port = jsonConfig.getInt( "port" );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
		
		Scenario testScenario = new Scenario();
		testScenario.setRampUp( 10 );
		testScenario.setDuration( 600 );
		testScenario.setRampDown( 10 );
		track = new DefaultScenarioTrack( testScenario );
		try
		{
			track.initialize( generatorClassName, hostname, port );
		}
		catch ( JSONException e )
		{
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws JSONException 
	{		
		generator = new RedisGenerator( track );
		// Turn off pooling
		generator.setUsePooling( false );
		generator.configure( new JSONObject() );
	}

	private void runOp( Operation op )
	{
		op.setGeneratorThreadID( 0 );
		op.run();
		if ( op.isFailed() ) {
			op.getFailureReason().printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testGetOperation()
	{
		RedisRequest<String> request = new RedisRequest<String>();
		request.key = "UnitTestKey";
		request.op = RedisGenerator.GET;
		
		// Save the value first then retrieve it
		testSetOperation();
		runOp( generator.createGetOperation( request ) );
	}
	
	@Test
	public void testSetOperation()
	{
		RedisRequest<String> request = new RedisRequest<String>();
		request.key = "UnitTestKey";
		request.op = RedisGenerator.SET;
		request.value = "The Brown Fox Jumped Over Something...".getBytes();
		request.size = request.value.length;
		
		runOp( generator.createSetOperation( request ) );
	}
}
