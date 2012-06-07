package radlab.rain;

import java.io.Serializable;

public class ResponseTimeStat extends Poolable implements Serializable 
{
	private static final long serialVersionUID = 1L;
	public static final String NAME = "ResponseTimeStat";
	
	public long _timestamp = -1;
	public long _responseTime = -1;
	public long _totalResponseTime = -1;
	public long _numObservations = -1; // totalResponseTime/numObservations => avg response time thus far
	public String _operationName = "";
	public String _operationRequest = "";
	public String _generatedDuring = "";
	
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
		this._operationRequest = ""; // Any details about the operation, e.g., what was requested
		this._generatedDuring = ""; // Interval name (if any) of when this operation was generated during a run
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "[" ).append( this._generatedDuring ).append( "] " ).append( this._timestamp ).append( " " ).append( this._operationName ).append( " " ).append( this._responseTime ).append( " [" ).append( this._operationRequest ).append( "] ").append( this._totalResponseTime ).append( " " ).append( this._numObservations );
		return buf.toString();
	}
}
