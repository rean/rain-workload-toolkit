package radlab.rain.workload.redis;

public class RedisRequest<T> 
{
	public T key;
	public int op;
	public int size;
	public byte[] value;
}
