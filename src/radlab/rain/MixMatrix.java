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

public class MixMatrix 
{
	// Default cloudstone mix, but could be anything
	
	private double [][] _mix = { {  0.0, 11, 52, 36,  0.0, 1,  0.0 }, 
							     {  0.0,  0.0, 60, 20,  0.0, 0.0, 20 }, 
							     { 21,  6, 41, 31,  0.0, 1,  0.0 }, 
							     { 72, 21,  0,  0,  6, 1,  0.0 }, 
							     { 52,  6,  0, 31, 11, 0,  0 }, 
							     {  0.0,  0.0,  0.0, 0.0, 100, 0.0,  0.0 },
							     { 0.0,  0.0,  0.0, 100, 0.0, 0.0, 0.0 }  };
	//private double [][] _normalizedMix = null;
	private double [][] _selectionMix = null;
	
	public MixMatrix()
	{
		this.normalize();
		this.createSelectionMatrix();
	}
	
	public MixMatrix( double[][] data )
	{
		this._mix = data.clone();
		this.normalize();
		this.createSelectionMatrix();
	}
	
	public boolean isSelectionMixAvailable()
	{ return this._selectionMix != null; }
	
	public void normalize()
	{
		for( int i = 0; i < this._mix.length; i++ )
		{
			double rowSum = 0.0;
			for( int j = 0; j < this._mix.length; j++ )
			{
				rowSum += this._mix[i][j];
			}
			
			//System.out.println( rowSum );
			
			for( int k = 0; k < this._mix.length; k++ )
			{
				this._mix[i][k] /= rowSum;
			}
		}
	}
	
	public void createSelectionMatrix()
	{
		if( this.isSelectionMixAvailable() )
			return;
		
		this._selectionMix = new double[this._mix.length][this._mix.length];
		
		for( int i = 0; i < this._mix.length; i++ ) 
		{
            this._selectionMix[i][0] = this._mix[i][0];
            for (int j = 1; j < this._selectionMix.length; j++) 
            {
                this._selectionMix[i][j] = this._mix[i][j] + this._selectionMix[i][j - 1];
            }
        }
	}
	
	public double[][] getSelectionMix()
	{
		return this._selectionMix;
	}
	
	public void printMix()
	{
		for( int i = 0; i < this._mix.length; i++ )
		{
			for( int j = 0; j < this._mix.length; j++ )
			{
				System.out.print( this._mix[i][j] );
        		System.out.print( " " );
			}
			System.out.println( "" );
		}
		System.out.println( "" );
	}
	
	public void printSelectionMix()
	{
		for( int i = 0; i < this._selectionMix.length; i++ )
		{
			for( int j = 0; j < this._selectionMix.length; j++ )
			{
				System.out.print( this._selectionMix[i][j] );
        		System.out.print( " " );
			}
			System.out.println( "" );
		}
		System.out.println( "" );
	}
	
	public boolean converges( long steps, double tolerance )
	{
		boolean converged = false;
			
		return converged;
	}
	
	public static void main( String[] args )
	{
		MixMatrix m = new MixMatrix();
		m.printMix();
		m.printSelectionMix();
	}
}
