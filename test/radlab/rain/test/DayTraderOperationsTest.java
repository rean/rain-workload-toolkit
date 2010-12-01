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
import radlab.rain.workload.daytrader.DayTraderGenerator;

public class DayTraderOperationsTest 
{
	private static DefaultScenarioTrack track;
	private DayTraderGenerator generator;
	
	@BeforeClass
	public static void scenarioSetup() {
		String generatorClassName = "radlab.rain.workload.daytrader.DayTraderGenerator";
		String hostname = null;
		int port = -1;
		
		try
		{
			String filename = "config/profiles.config.daytrader.json";
			String fileContents = ConfigUtil.readFileAsString( filename );
			JSONObject jsonConfigRoot = new JSONObject( fileContents );
			JSONObject jsonTrack = jsonConfigRoot.getJSONObject( "daytrader-001" );
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
	public void setUp() 
	{		
		generator = new DayTraderGenerator( track );
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
	public void testLoginOperation()
	{
		runOp( generator.createLoginOperation() );
	}

	@Test 
	public void testLogoutOperation()
	{
		runOp( generator.createLoginOperation() );
		runOp( generator.createLogoutOperation() );
	}
	
	@Test 
	public void testViewPortfolioOperation()
	{
		runOp( generator.createViewPortfolioOperation() );
	}
	
	@Test 
	public void testSellHoldingOperation()
	{
		runOp( generator.createSellHoldingOperation() );
	}
	
	@Test 
	public void testViewQuotesOperation()
	{
		runOp( generator.createViewQuotesOperation() );
	}
	
	@Test 
	public void testBuyStockOperation()
	{
		runOp( generator.createBuyStockOperation() );
	}
}
