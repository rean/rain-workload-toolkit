package radlab.rain.workload.redis;

public class RedisRequest<T> 
{
	T key;
	public int op;
	public int size;
}
