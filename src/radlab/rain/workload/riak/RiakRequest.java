package radlab.rain.workload.riak;

public class RiakRequest<T> 
{
	public T key;
	public int op;
	public int size;
	public byte[] value;
}
