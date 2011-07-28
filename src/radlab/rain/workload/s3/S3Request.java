package radlab.rain.workload.s3;

public class S3Request<T> 
{
	public T key;
	public String bucket;
	public String newBucket; // to support moves
	public String newKey; // to support renames
	public int op;
	public int size;
	public byte[] value;
}
