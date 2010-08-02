package radlab.rain.communication;

public class BenchmarkStartMessage extends RainMessage 
{

	public BenchmarkStartMessage()
	{
		this._header = new MessageHeader( MessageHeader.VERSION_1, MessageHeader.BENCHMARK_START_MSG_TYPE );
	}
	/**
	 * 
	 */
	private static final long serialVersionUID 	= 1L;
	public long _controllerTimestamp 			= -1;	
}
