package radlab.rain.workload.cassandra;

public class CassandraRequest<T>
{
	public T key;
	public int op;
	public int size;
	public byte[] value;
	public int maxScanRows;
}
