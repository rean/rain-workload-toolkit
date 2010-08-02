package radlab.rain.communication;

import java.io.Serializable;

public abstract class RainMessage implements Serializable 
{
	private static final long serialVersionUID = 1L;
	public MessageHeader _header;
}
