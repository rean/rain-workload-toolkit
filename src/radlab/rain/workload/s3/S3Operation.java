package radlab.rain.workload.s3;

import java.util.Map;

import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

public abstract class S3Operation extends Operation 
{
	public static int BUF_SIZE = 4096;
	
	public String _bucket;
	public String _key;
	public String _newBucket; // To support moves
	public String _newKey; // To support renames
	public byte[] _value;
	protected S3Transport _s3Client = null;
	
	public S3Operation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
	}

	@Override
	public void cleanup() 
	{
		this._bucket = "";
		this._key = "";
		this._newBucket = "";
		this._newKey = "";
		this._value = null;
	}

	@Override
	public void prepare(Generator generator) 
	{
		this._generator = generator;
		S3Generator s3Generator = (S3Generator) generator;
		
		this._s3Client = s3Generator.getS3Transport();
		
		LoadProfile currentLoadProfile = generator.getLatestLoadProfile();
		if( currentLoadProfile != null )
			this.setGeneratedDuringProfile( currentLoadProfile );
	}

	// Main operations
	public S3Object doGet( String bucketName, String key) throws Throwable
	{ 
		S3Object object = this._s3Client.getObject( bucketName, key );
		return object;
	}
	
	public void doPut( String bucketName, String key, byte[] value ) throws Throwable
	{ 
		this._s3Client.putObject( bucketName, key, value );	
	}
	
	public void doDelete( String bucketName, String key ) throws Throwable
	{ 
		this._s3Client.deleteObject( bucketName, key ); 	
	}
	
	public S3Object doHead( String bucketName, String key ) throws Throwable
	{ 
		return this._s3Client.headObject( bucketName, key ); 	
	}
	
	public S3Bucket doCreateBucket( String bucketName ) throws Throwable
	{ 
		return this._s3Client.createBucket( bucketName );
	}
	
	public void doDeleteBucket( String bucketName ) throws Throwable
	{ 
		this._s3Client.deleteBucket( bucketName ); 	
	}
	
	public S3Bucket[] doListAllBuckets() throws Throwable
	{ 
		return this._s3Client.listAllBuckets(); 	
	}
	
	public S3Object[] doListBucket( String bucketName ) throws Throwable
	{ 
		return this._s3Client.listBucket( bucketName );
	}
	
	public Map<String, Object> doRename( String bucketName, String oldKey, String newKey ) throws Throwable
	{ 
		return this._s3Client.renameObject( bucketName, oldKey, newKey );
	}
	
	public Map<String, Object> doMove( String oldBucketName, String oldKey, String newBucketName, String newKey ) throws Throwable
	{ 
		return this._s3Client.moveObject( oldBucketName, oldKey, newBucketName, newKey );	
	}
}
