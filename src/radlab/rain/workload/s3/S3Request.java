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
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		String opName = "";
		switch( this.op )
		{
			case S3Generator.CREATE_BUCKET:  opName = "CreateBucket"; break;
			case S3Generator.DELETE: opName =  "Delete"; break;
			case S3Generator.DELETE_BUCKET: opName = "Delete Bucket"; break;
			case S3Generator.GET: opName = "Get"; break;
			case S3Generator.HEAD: opName = "Head"; break;
			case S3Generator.LIST_ALL_BUCKETS: opName = "ListAll Buckets"; break;
			case S3Generator.LIST_BUCKET: opName = "List Bucket"; break;
			case S3Generator.MOVE: opName = "Move"; break;
			case S3Generator.PUT: opName = "Put"; break;
			case S3Generator.RENAME: opName = "Rename"; break;
		}
		
		buf.append( "Op: ").append( opName.toUpperCase() ).append( " bucket: " ).append( bucket ).append( " key: " ).append( key ).append( " size: " ).append( size ).append( " new bucket: " ).append( newBucket ).append( " new key: " ).append( newKey );
		
		return buf.toString();
	}
}
