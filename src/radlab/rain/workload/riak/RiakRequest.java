package radlab.rain.workload.riak;

public class RiakRequest<T> 
{
	T key;
	public int op;
	public int size;
}
