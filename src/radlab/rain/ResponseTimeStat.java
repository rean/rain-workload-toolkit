package radlab.rain;

public class ResponseTimeStat extends Poolable 
{
	public static final String NAME = "ResponseTimeStat";
	
	public long _timestamp = -1;
	public long _responseTime = -1;
	public long _totalResponseTime = -1;
	public long _numObservations = -1; // totalResponseTime/numObservations => avg response time thus far
	public String _operationName = "";
		
	public ResponseTimeStat()
	{
		super( NAME );
	}
	
	public ResponseTimeStat(String tag) 
	{
		super( tag );
	}

	@Override
	public void cleanup() 
	{
		this._timestamp = -1;
		this._responseTime = -1;
		this._totalResponseTime = -1;
		this._numObservations = -1;
		this._operationName = "";
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( this._timestamp ).append( " " ).append( this._operationName ).append( " " ).append( this._responseTime ).append( " " ).append( this._totalResponseTime ).append( " " ).append( this._numObservations );
		return buf.toString();
	}
}
