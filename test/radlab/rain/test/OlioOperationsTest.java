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

package radlab.rain.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import radlab.rain.Operation;
import radlab.rain.DefaultScenarioTrack;
import radlab.rain.Scenario;
import radlab.rain.util.ConfigUtil;
import radlab.rain.workload.olio.OlioGenerator;
import radlab.rain.workload.olio.OlioOperation;

public class OlioOperationsTest
{
	
	private static DefaultScenarioTrack track;
	private OlioGenerator generator;
	
	@BeforeClass
	public static void scenarioSetup() {
		String generatorClassName = "radlab.rain.workload.olio.OlioGenerator";
		String hostname = null;
		int port = -1;
		
		try
		{
			String filename = "config/junit.config.json";
			String fileContents = ConfigUtil.readFileAsString( filename );
			JSONObject jsonConfig = new JSONObject( fileContents );
			 
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
		track.initialize( generatorClassName, hostname, port );
	}
	
	@Before
	public void setUp() {		
		generator = new OlioGenerator( track );
	}
	
	public static final int HOME_PAGE       = 0;
	public static final int LOGIN           = 1;
	public static final int TAG_SEARCH      = 2;
	public static final int EVENT_DETAIL    = 3;
	public static final int PERSON_DETAIL   = 4;
	public static final int ADD_PERSON      = 5;
	public static final int ADD_EVENT       = 6;
	
	private void runOp( Operation op )
	{
		op.setGeneratorThreadID( 1 );
		op.run();
		if ( op.isFailed() ) {
			op.getFailureReason().printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testHomePageOperation()
	{
		runOp( generator.getOperation( HOME_PAGE ) );
	}
	
	@Test
	public void testLoginOperation()
	{
		runOp( generator.getOperation( LOGIN ) );
	}
	
	@Test
	public void testTagSearchOperation()
	{
		runOp( generator.getOperation( TAG_SEARCH ) );
	}
	
	@Test
	public void testEventDetailOperation()
	{
		runOp( generator.getOperation( EVENT_DETAIL ) );
	}
	
	@Test
	public void testPersonDetailOperation()
	{
		runOp( generator.getOperation( LOGIN ) );
		runOp( generator.getOperation( PERSON_DETAIL ) );
	}
	
	@Test
	public void testAddPersonOperation()
	{
		runOp( generator.getOperation( ADD_PERSON ) );
	}
	
	@Test
	public void testAddEventOperation()
	{
		runOp( generator.getOperation( LOGIN ) );
		runOp( generator.getOperation( ADD_EVENT ) );
	}
	
	@Test
	public void testSessionPersistence()
	{
		OlioOperation op = (OlioOperation) generator.getOperation( HOME_PAGE );
		
		Operation loginOp = generator.getOperation( LOGIN );
		
		assertTrue( !op.checkIsLoggedIn() );
		
		runOp( loginOp );
		
		loginOp = generator.getOperation( LOGIN );
		
		runOp( loginOp );

		assertTrue( op.checkIsLoggedIn() );
		
		try
		{
			op.getGenerator().getHttpTransport().fetchUrl( op.getGenerator().logoutURL );
		}
		catch ( IOException e )
		{}
		
		assertTrue( !op.checkIsLoggedIn() );
	}
	
	@Test
	public void testParseImages()
	{
		// HOME_PAGE is arbitrary; need to instantiate OlioOperation.
		OlioOperation op = (OlioOperation) generator.getOperation( HOME_PAGE );
		
		String imageName = "fileService.test.jpg";
		String imagePath = op.getGenerator().baseURL + "/" + imageName;
		
		String htmlContent = "<img src=\"" + imageName + "\" />";
		
		Set<String> imageUrls = op.parseImages( new StringBuilder( htmlContent ) );
		assertTrue( imageUrls.size() == 1 );
		assertTrue( imageUrls.contains( imagePath ) );
	}
	
	@Test
	public void testParseAuthToken()
	{
		// HOME_PAGE is arbitrary; need to instantiate OlioOperation.
		OlioOperation op = (OlioOperation) generator.getOperation( HOME_PAGE );
		
		String token = "/006HAv7H5MymkgE4NWGJN>aQpQl+KDiiwJ707K8/W6c=";
		
		String htmlContent = "<input name=\"authenticity_token\" type=\"hidden\" value=\"" + token + "\" />";
		
		try
		{
			String result = op.parseAuthToken( new StringBuilder( htmlContent ) );
			assertTrue( result.equals( token ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
