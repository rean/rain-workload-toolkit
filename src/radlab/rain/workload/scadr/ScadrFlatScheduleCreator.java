package radlab.rain.workload.scadr;

import java.util.LinkedList;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONException;
import org.json.JSONObject;

import radlab.rain.LoadProfile;
import radlab.rain.LoadScheduleCreator;

public class ScadrFlatScheduleCreator extends LoadScheduleCreator 
{
	private static NumberFormat FORMATTER = new DecimalFormat( "00000" );
	public static String CFG_INITIAL = "initialWorkload";
	public static String CFG_INCREMENT_SIZE = "incrementSize";
	public static String CFG_INCREMENTS_PER_INTERVAL = "incrementsPerInterval";

	private int _initialWorkload = 10;	
	private int _incrementSize = 60; // 60 seconds per increment
	private int _incrementsPerInterval = 1; // this gives us 10 seconds per interval
	
	
	public ScadrFlatScheduleCreator() 
	{}

	public int getInitialWorkload() { return this._initialWorkload; }
	public void setInitialWorkload( int val ){ this._initialWorkload = val; }
	
	public int getIncrementSize() { return this._incrementSize; }
	public void setIncrementSize( int val ){ this._incrementSize = val; }
	
	public int getIncrementsPerInterval() { return this._incrementsPerInterval; }
	public void setIncrementsPerInterval( int val ){ this._incrementsPerInterval = val; }
		
	@Override
	public LinkedList<LoadProfile> createSchedule(JSONObject config) throws JSONException 
	{
		// Pull out the base offset
		if( config.has( CFG_INITIAL ) )
			this._initialWorkload = config.getInt( CFG_INITIAL );
		
		if( config.has(CFG_INCREMENT_SIZE) )
			this._incrementSize = config.getInt( CFG_INCREMENT_SIZE );
		
		if( config.has( CFG_INCREMENTS_PER_INTERVAL) )
			this._incrementsPerInterval = config.getInt( CFG_INCREMENTS_PER_INTERVAL );

		LinkedList<LoadProfile> loadSchedule = new LinkedList<LoadProfile>();
		
		// Do a flat n-user workload
		loadSchedule.add( new LoadProfile( (long) 900, 200, "default", 0, FORMATTER.format(0) ) );
		
		/*
		// Add a long interval with 1 thread for debugging
		// loadSchedule.add( new LoadProfile( 300, 1, "default", 0, "debug" ) );
				
		for( int i = 0; i < this._relativeLoads.length; i++ )
		{
			long intervalLength = this._incrementSize * this._incrementsPerInterval;
			if( i == 0 )
				loadSchedule.add( new LoadProfile( intervalLength, this._initialWorkload, "default", 0, FORMATTER.format(i) ) );
			else 
			{	
				int users = 0;
				users = (int) Math.round( loadSchedule.getFirst().getNumberOfUsers() * this._relativeLoads[i] );
				
				loadSchedule.add( new LoadProfile( intervalLength, users, "default", 0, FORMATTER.format(i) ) );
			}
		}*/
		
		return loadSchedule;		
	}
	
	public static void main( String[] args ) throws JSONException
	{
		ScadrFlatScheduleCreator creator = new ScadrFlatScheduleCreator();
		
		creator.setInitialWorkload( 10 );
		
		// Would like to give a duration and have the workload stretched/compressed into that
		LinkedList<LoadProfile> profiles = creator.createSchedule( new JSONObject() );
		for( LoadProfile p : profiles )
			System.out.println( p.getNumberOfUsers() );
	}
}
