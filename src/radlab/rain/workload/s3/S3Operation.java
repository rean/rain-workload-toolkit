package radlab.rain.workload.s3;

import radlab.rain.Generator;
import radlab.rain.IScoreboard;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;

public abstract class S3Operation extends Operation 
{
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
	public void doGet() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doPut() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doDelete() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doHead() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doCreateBucket() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doDeleteBucket() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doListAllBuckets() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doListBucket() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doRename() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
	
	public void doMove() throws Throwable
	{ 
		Thread.sleep( 50 ); 	
	}
}
