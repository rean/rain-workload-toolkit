package radlab.rain.workload.scadr;

import radlab.rain.Generator;
import radlab.rain.LoadProfile;
import radlab.rain.Operation;
import radlab.rain.ScenarioTrack;
import radlab.rain.util.HttpTransport;
import radlab.rain.util.NegativeExponential;

import java.util.Random;

@SuppressWarnings("unused")
public class ScadrGenerator extends Generator {

	// Operation indices - each operation has a unique index 
	public static final int LOGIN		 		= 0;
	public static final int CREATE_SUBSCRIPTION = 1;
	public static final int CREATE_THOUGHT		= 2;
	public static final int READ_THOUGHTSTREAM	= 3;
	
	private HttpTransport _http;
	private Random _rand = new Random();
	private NegativeExponential _thinkTimeGenerator  = null;
	private NegativeExponential _cycleTimeGenerator = null;
	
	public ScadrGenerator(ScenarioTrack track) {
		super(track);
		// Initialize think/cycle time random number generators (if you need/want them)
		this._cycleTimeGenerator = new NegativeExponential( track.getMeanCycleTime()*1000 );
		this._thinkTimeGenerator = new NegativeExponential( track.getMeanThinkTime()*1000 );
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public long getCycleTime() {
		/* Example cycle time generator
		long nextCycleTime = (long) this._cycleTimeGenerator.nextDouble(); 
		// Truncate at 5 times the mean (arbitrary truncation)
		return Math.min( nextCycleTime, (5*this._cycleTime) );
		*/
		
		return 0; // No pause
	}

	@Override
	public long getThinkTime() {
		
		/* Example think time generator
		 long nextThinkTime = (long) this._thinkTimeGenerator.nextDouble(); 
		 // Truncate at 5 times the mean (arbitrary truncation)
		 return Math.min( nextThinkTime, (5*this._thinkTime) );
		 */	
		return 0; // No think time
	}

	@Override
	public void initialize() {
		this._http = new HttpTransport();
	}

	/* Pass in index of the last operation */
	
	@Override
	public Operation nextRequest(int lastOperation) {
		
		// Get the current load profile if we need to look inside of it to decide
		// what to do next
		ScadrLoadProfile currentLoad = (ScadrLoadProfile) this.getTrack().getCurrentLoadProfile();
		
		// Pick a random number between 0 and 3
		int nextOpIndex = Math.abs( this._rand.nextInt() ) % 4;
		
		Operation op = this.getOperation( nextOpIndex );
		return op;
	}

	private ScadrOperation getOperation( int opIndex )
	{
		// We know about 4 high-level Cloudstone operations
		/*public static final int LOGIN		 		= 0;
		public static final int CREATE_SUBSCRIPTION = 1;
		public static final int CREATE_THOUGHT		= 2;
		public static final int READ_THOUGHTSTREAM	= 3;*/
		
		switch( opIndex )
		{
			case LOGIN: return this.createLoginOperation();
			case CREATE_SUBSCRIPTION: return this.createSubscriptionOperation();
			case CREATE_THOUGHT: return this.createThoughtOperation();
			case READ_THOUGHTSTREAM: return this.createReadThoughtstreamOperation();
			default: return null;
		}
	}
	
	// Factory methods for creating operations
	public LoginOperation createLoginOperation()
	{
		LoginOperation op = new LoginOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;		
	}

	public CreateSubscriptionOperation createSubscriptionOperation()
	{
		CreateSubscriptionOperation op = new CreateSubscriptionOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;
	}
	
	public CreateThoughtOperation createThoughtOperation()
	{
		CreateThoughtOperation op = new CreateThoughtOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;		
	}
	
	public ReadThoughtstreamOperation createReadThoughtstreamOperation()
	{
		ReadThoughtstreamOperation op = new ReadThoughtstreamOperation( this.getTrack().getInteractive(), this.getScoreboard() );
		op.prepare( this );
		return op;		
	}
	
	/**
	 * Returns the pre-existing HTTP transport.
	 * 
	 * @return          An HTTP transport.
	 */
	public HttpTransport getHttpTransport()
	{
		return this._http;
	}
}
