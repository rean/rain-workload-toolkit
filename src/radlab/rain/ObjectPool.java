/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package radlab.rain;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.io.PrintStream;

public class ObjectPool
{
	private long _maxPoolSize = 100000; // Upper-bound on number of pooled objects we keep around
	private Hashtable<String,LinkedList<Operation>> _pool = new Hashtable<String,LinkedList<Operation>>(); 
	// Pool stats
	private long _timeStarted;
	private long _timeShutdown;
	
	private long _misses = 0;
	private long _hits = 0;
	
	private long _totalRentQTime = 0;
	private long _totalRentServiceTime = 0;
	private long _totalRentRequests = 0;
	
	private long _totalReturnQTime = 0;
	private long _totalReturnServiceTime = 0;
	private long _totalReturnRequests = 0;
	private long _totalSuccessfulReturns = 0;
	
	private long _totalCleanupDiscards = 0;
	private long _totalOverflowDiscards = 0;

	private String _trackName = "None";
	private NumberFormat _formatter = new DecimalFormat( "#0.0000" );
	
	public ObjectPool( long maxPoolSize )
	{
		this._maxPoolSize = maxPoolSize;
	}
	
	public boolean isActive()
	{
		return this._totalRentRequests > 0;
	}
	
	public void resetStatistics()
	{
		_misses = 0;
		_hits = 0;
		
		_totalRentQTime = 0;
		_totalRentServiceTime = 0;
		_totalRentRequests = 0;
		
		_totalReturnQTime = 0;
		_totalReturnServiceTime = 0;
		_totalReturnRequests = 0;
		_totalSuccessfulReturns = 0;
		
		_totalCleanupDiscards = 0;
		_totalOverflowDiscards = 0;	
	}

	public String getTrackName() { return this._trackName; }
	public void setTrackName( String val ) { this._trackName = val; }
	
	public void printStatistics( PrintStream out)
	{
		//String trackName = 
		out.println( "[OBJECTPOOL " + this._trackName + "] Start time                       : " + this._timeStarted );
		out.println( "[OBJECTPOOL " + this._trackName + "] Total rent requests              : " + this._totalRentRequests );
		out.println( "[OBJECTPOOL " + this._trackName + "] Request rate                     : " + this._formatter.format( ( (double)this._totalRentRequests/(double)(this._timeShutdown - this._timeStarted) ) * 1000 ) );
		out.println( "[OBJECTPOOL " + this._trackName + "] Hits                             : " + this._hits + "(" + this._formatter.format( ( (double)this._hits/(double)this._totalRentRequests ) * 100 ) + "%)" );
		out.println( "[OBJECTPOOL " + this._trackName + "] Misses                           : " + this._misses + "(" + this._formatter.format( ( (double)this._misses/(double)this._totalRentRequests ) * 100 ) + "%)" );
		out.println( "[OBJECTPOOL " + this._trackName + "] Average rentQ time (ms)          : " + this._formatter.format((double)this._totalRentQTime/(double)this._totalRentRequests) );
		out.println( "[OBJECTPOOL " + this._trackName + "] Average rent service time (ms)   : " + this._formatter.format((double)this._totalRentServiceTime/(double)this._totalRentRequests) );
		
		out.println( "[OBJECTPOOL " + this._trackName + "] Total return requests            : " + this._totalReturnRequests );
		out.println( "[OBJECTPOOL " + this._trackName + "] Total successful returns         : " + this._totalSuccessfulReturns );
		out.println( "[OBJECTPOOL " + this._trackName + "] Total overflow discards          : " + this._totalOverflowDiscards );
		out.println( "[OBJECTPOOL " + this._trackName + "] Total cleanup discards           : " + this._totalCleanupDiscards );
		out.println( "[OBJECTPOOL " + this._trackName + "] Average returnQ time (ms)        : " + this._formatter.format( (double)this._totalReturnQTime/(double)(this._totalSuccessfulReturns+this._totalOverflowDiscards) ) );
		out.println( "[OBJECTPOOL " + this._trackName + "] Average return service time (ms) : " + this._formatter.format( (double)this._totalReturnServiceTime/(double)(this._totalSuccessfulReturns+this._totalOverflowDiscards) ) );		
	}
	
	public Operation rentObject( String tag )
	{
		Operation obj = null;
		this._totalRentRequests++;
		
		if( this._totalRentRequests == 1 )
		{
			this._timeStarted = System.currentTimeMillis();
			this._timeShutdown = -1;
		}
		
		long qStart = System.currentTimeMillis();
		synchronized( this._pool )
		{
			long qEnd = System.currentTimeMillis();
						
			LinkedList<Operation> objs = this._pool.get( tag );
			if( objs == null || objs.size() == 0 )
			{
				this._misses++;
			}
			else 
			{
				this._hits++;
				obj = objs.remove(); // hit
			}
			this._totalRentQTime += ( qEnd - qStart ); // Total time waiting for pool look
			this._totalRentServiceTime += (System.currentTimeMillis() - qEnd); // Total time to get an answer
		}
		return obj;
	}
	
	public void returnObject( Operation op )
	{
		this._totalReturnRequests++;
		try
		{
			op.cleanup(); // Reset object
		}
		catch( Throwable t )
		{
			// Don't pool an object that didn't cleanup properly
			this._totalCleanupDiscards++;
			op = null;
			return;
		}
		
		// If no one is asking for objects then the pool is probably inactive.
		// We don't just want to hold onto objects that won't be re-used/recycled
		if( this._totalRentRequests == 0 )
			return;
		
		long qStart = System.currentTimeMillis();
		synchronized( this._pool )
		{
			long qEnd = System.currentTimeMillis();
			LinkedList<Operation> objs = this._pool.get( op._operationName );
			if( objs == null )
			{
				objs = new LinkedList<Operation>();
				this._pool.put( op._operationName, objs ); 
			}
		
			if( objs.size() + 1 <= this._maxPoolSize )
			{
				objs.add( op );
				this._totalSuccessfulReturns++;
			}
			else 
			{
				op = null; // Toss it
				this._totalOverflowDiscards++;
			}
			
			this._totalReturnQTime += qEnd - qStart;
			this._totalReturnServiceTime += System.currentTimeMillis() - qEnd;
		}
	}
	
	public void shutdown()
	{
		this._timeShutdown = System.currentTimeMillis();
		// Dump each linked list in the hashtable
		synchronized( this._pool )
		{
			Enumeration<LinkedList<Operation>> it = this._pool.elements();
			while( it.hasMoreElements() )
			{
				LinkedList<Operation> objs = it.nextElement();
				objs.clear();
			}
			
			this._pool.clear();
		}
		this.printStatistics( System.out );
		this.resetStatistics();
	}
}
