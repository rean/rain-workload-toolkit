package radlab.rain.util;

import org.json.JSONObject;

public class MetricWriterFactory 
{
	public final static String FILE_WRITER_TYPE = "file";
	public final static String SOCKET_WRITER_TYPE = "socket"; 
	//public final static String DATABASE_WRITER_TYPE = "database";
	
	private MetricWriterFactory()
	{}
	
	public static MetricWriter createMetricWriter( String writerType, JSONObject config ) throws Exception
	{
		if( writerType.equalsIgnoreCase( FILE_WRITER_TYPE ) )
			return new FileMetricWriter( config );
		else if( writerType.equalsIgnoreCase( SOCKET_WRITER_TYPE ) )
			return new SocketMetricWriter( config );
		else return null;
	}
}
