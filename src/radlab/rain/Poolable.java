package radlab.rain;

public abstract class Poolable implements IPoolable
{
	protected String _poolTag = ""; // Used to identify the type/name of object being pooled

	protected Poolable()
	{}
	
	// Base ctor that sets the pool tag
	public Poolable( String tag )
	{
		this._poolTag = tag;
	}
	
	public abstract void cleanup();
}
