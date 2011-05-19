package radlab.rain.workload.mongodb;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import radlab.rain.IScoreboard;

public class MongoGetOperation extends MongoOperation 
{
	public static String NAME = "Get";
		
	public MongoGetOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = NAME;
		this._operationIndex = MongoGenerator.READ;
	}
	
	@Override
	public void execute() throws Throwable
	{
		DBCursor cursor = this.doGet( this._key );
		try
		{
			//System.out.println( cursor.count() );
			if( cursor.count() == 0 )
				throw new Exception( "Empty cursor for key: " + this._key );
			
			while( cursor.hasNext() )
			{
				// Get the object and the value
				DBObject o = cursor.next();
				@SuppressWarnings("unused")
				byte[] value = (byte[]) o.get( "value" );
				//System.out.println( new String(value) );
			}
		}
		catch( Throwable e )
		{
			throw e;
		}
		finally
		{
			if( cursor != null )
				cursor.close();
		}
		
		this.setFailed( false );
	}
}
