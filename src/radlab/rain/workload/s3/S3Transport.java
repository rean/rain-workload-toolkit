package radlab.rain.workload.s3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

public class S3Transport 
{
	private String _awsAccessKey 	= null;
	private String _awsSecretKey 	= null;
	private S3Service _s3 			= null;
		
	public S3Transport( String awsAccessKey, String awsSecretKey ) throws S3ServiceException 
	{
		this._awsAccessKey = awsAccessKey;
		this._awsSecretKey = awsSecretKey;
		AWSCredentials credentials = new AWSCredentials( this._awsAccessKey, this._awsSecretKey );
		this._s3 = new RestS3Service( credentials );
	}
	
	public S3Service getS3Client()
	{
		return this._s3;
	}
	
	public HttpClient getHttpClient()
	{ 
		return this._s3.getHttpClient(); 
	}
	
	public HttpConnectionManager getConnectionManager()
	{
		return this._s3.getHttpConnectionManager();
	}
	
	// Flesh out the API
	public S3Bucket[] listAllBuckets() throws S3ServiceException
	{
		S3Bucket[] buckets = this._s3.listAllBuckets();
		return buckets;
	}
	
	public S3Bucket createBucket( String name ) throws S3ServiceException
	{
		return this._s3.createBucket( name );
	}

	// Create a bucket in a specific region
	public S3Bucket createBucket( String name, String location ) throws S3ServiceException
	{
		return this._s3.createBucket( name, location );
	}
	
	public void deleteBucket( String name ) throws ServiceException
	{
		this._s3.deleteBucket( name );
	}
	
	public S3Object putObject( String bucketName, String key, String value ) throws S3ServiceException, IOException, NoSuchAlgorithmException
	{
		S3Object obj = new S3Object( key, value );
		return this._s3.putObject( bucketName, obj );
	}
	
	public S3Object putObject( String bucketName, String key, byte[] value ) throws S3ServiceException
	{
		S3Object obj = new S3Object( key );
		ByteArrayInputStream input = new ByteArrayInputStream( value );
		obj.setDataInputStream( input );
		obj.setContentLength( value.length );
		obj.setContentType( "application/binary" );
		return this._s3.putObject( bucketName, obj );
	}
	
	public S3Object putObject( String bucketName, File file ) throws NoSuchAlgorithmException, IOException, S3ServiceException
	{
		S3Object obj = new S3Object( file );
		return this._s3.putObject( bucketName, obj );
	}
	
	public S3Object getObject( String bucketName, String key ) throws S3ServiceException
	{
		S3Object obj = this._s3.getObject( bucketName, key );
		return obj;
	}
	
	@SuppressWarnings("deprecation")
	public S3Object headObject( String bucketName, String key ) throws S3ServiceException
	{
		S3Bucket bucket = new S3Bucket( bucketName );
		S3Object obj = this._s3.getObjectDetails( bucket, key );
		return obj;
	}
	
	public S3Object[] listBucket( String bucketName ) throws S3ServiceException
	{
		return this._s3.listObjects( bucketName );
	}
	
	public void deleteObject( String bucketName, String key ) throws ServiceException
	{
		this._s3.deleteObject( bucketName, key );
	}
	
	public Map<String, Object> renameObject( String bucketName, String oldKey, String newKey ) throws NoSuchAlgorithmException, IOException, ServiceException
	{
		S3Object destObj = new S3Object( newKey );
		return this._s3.renameObject( bucketName, oldKey, destObj );
	}
	
	public Map<String, Object> moveObject( String oldBucketName, String oldKey, String newBucketName, String newKey ) throws ServiceException
	{
		S3Object destObj = new S3Object( newKey );
		return this._s3.moveObject( oldBucketName, oldKey, newBucketName, destObj, false );
	}
}
