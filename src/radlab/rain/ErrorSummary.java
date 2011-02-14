package radlab.rain;

public class ErrorSummary 
{
	public String _failureClass = "";
	public long _errorCount 	= 0;
	
	public ErrorSummary( String failureClass )
	{ 
		this._failureClass = failureClass;
		this._errorCount = 0;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( this._failureClass ).append( ": " ).append( this._errorCount );
		return buf.toString();
	}
}
