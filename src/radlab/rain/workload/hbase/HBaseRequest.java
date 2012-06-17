package radlab.rain.workload.hbase;

public class HBaseRequest<T>
{
	public T key;
	public int op;
	public int size;
	public byte[] value;
	public int maxScanRows;
}
