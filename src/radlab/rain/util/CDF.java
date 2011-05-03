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
package radlab.rain.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

public class CDF<T> 
{
	public static NumberFormat Formatter = new DecimalFormat( "#0.0000" );
	
	private Random _random 	= null;
	ArrayList<T> _labels	= new ArrayList<T>();
	
	private double _sum = 0.0;
	private long[] _rawCounts 	= null; 
	private double[] _cdf 		= null;
	
	public CDF( JSONArray labels, JSONArray counts ) throws JSONException
	{
		this( System.currentTimeMillis(), labels, counts );
	}
	
	@SuppressWarnings("unchecked")
	public CDF( long seed, JSONArray labels, JSONArray counts ) throws JSONException
	{
		this._random = new Random( seed );
		this._rawCounts = new long[counts.length()];
		this._cdf = new double[counts.length()];
		
		this._sum = 0.0;
		for( int i = 0; i < counts.length(); i++ )
		{
			this._labels.add( (T) labels.get(i) );
			this._rawCounts[i] = counts.getLong(i);
			this._sum += counts.getDouble(i);
			this._cdf[i] = this._sum;
		}
		this.normalize();
	}
	
	public CDF( T[] labels, double[] counts )
	{
		this( System.currentTimeMillis(), labels, counts );
	}
	
	public CDF( long seed, T[] labels, double[] counts )
	{
		this._random = new Random( seed );
		this._rawCounts = new long[counts.length];
		this._cdf = new double[counts.length];
		this._sum = 0.0;
		for( int i = 0; i < counts.length; i++ )
		{
			this._labels.add( (T) labels[i] );
			this._rawCounts[i] = new Double(counts[i]).longValue();
			this._sum += counts[i];
			this._cdf[i] = this._sum;
		}
		this.normalize();
	}
	
	public CDF( ArrayList<T> labels, double[] counts )
	{
		this( System.currentTimeMillis(), labels, counts );
	}
		
	public CDF( long seed,  ArrayList<T> labels, double[] counts )
	{
		this._labels = labels;
		this._rawCounts = new long[counts.length];
		this._cdf = new double[counts.length];
		this._sum = 0.0;
		for( int i = 0; i < counts.length; i++ )
		{
			this._sum += counts[i];
			this._rawCounts[i] = new Double(counts[i]).longValue();
			this._cdf[i] = this._sum;
		}
		this.normalize();
	}
	
	public void normalize()
	{
		double sum = this._cdf[this._cdf.length-1];
		for( int i = 0; i < this._cdf.length; i++ )
			this._cdf[i] /= sum;
	}
	
	// Return the index of the next item to fetch
	public int nextObjectIndex()
	{
		double rndVal = this._random.nextDouble();
		int i = 0;
		
		for( i = 0; i < this._cdf.length; i++ )
		{
			if( rndVal <= this._cdf[i] )
				break;
		}
		
		return i;
	}
	
	// Return the object at that index
	public T nextObject()
	{
		return this._labels.get( this.nextObjectIndex() );
	}
	
	public boolean compare( CDF<T> rhs )
	{
		// Assume 5% tolerance by default
		return this.compare( rhs, 0.05 );
	}
	
	public void print()
	{
		for( int i = 0; i < this._labels.size(); i++ )
		{
			double frequency = this._rawCounts[i]/this._sum;
			System.out.println( this._labels.get(i).toString() + " [" + this._rawCounts[i] + "/" + this._sum + "] " + Formatter.format( frequency ) );
		}
	}
	
	//public void compare( )
	// CDFs equivalent if:
	// they have the same entries
	// proportions of each entry is within 5%
	// Fuzzy matching/comparison of CDFs
	public boolean compare( CDF<T> rhsCdf, double tolerance )
	{
		boolean result = true;
		
		for( int i = 0; i < this._labels.size(); i++ )
		{
			T lhs = this._labels.get( i );
			T rhs = rhsCdf._labels.get( i );
			long lhsCount = this._rawCounts[i];
			long rhsCount = rhsCdf._rawCounts[i];
			
			// Check that the types are equivalent
			if( this._labels.get( i ).equals( rhsCdf._labels.get( i ) ) )
			{
				// Compute the discrepancy between item popularities
				double lhsFrequency = lhsCount/this._sum; //this._cdf[i];
				double rhsFrequency = rhsCount/rhsCdf._sum;//rhsCdf._cdf[i];
				
				System.out.println( "LHS: " + lhs.toString() + " RHS: " + rhs.toString() + "[" + Formatter.format(lhsFrequency) + " vs " + Formatter.format(rhsFrequency) + "]" );
								
				double diff = Math.abs( lhsFrequency - rhsFrequency );
				double discrepancy = diff/lhsFrequency; 
				if( discrepancy > tolerance )
				{
					System.out.println( "CDF discrepancy at Object: " + lhs.toString() + " " + Formatter.format( discrepancy ) + " > tolerance: " + Formatter.format( tolerance ) );
					return false;
				}
			}
			else 
			{
				System.out.println( "Misaligned" );
				// Mis-alignment
				return false;
			}
		}		
		return result;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

}
