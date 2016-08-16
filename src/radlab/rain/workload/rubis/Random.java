/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html or
 * install_dir/legal/LICENSE
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at install_dir/legal/LICENSE.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * $Id: Random.java,v 1.2 2006/06/29 19:38:39 akara Exp $
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 * 
 * Modifications by: Rean Griffith 
 * 1) Changed package to include file in Rain harness
 */
package radlab.rain.workload.rubis;

import java.util.GregorianCalendar;
import java.util.Calendar;

/**
 * Random is a random number/value generator. This is a primitive facility for
 * RandomValues. RandomValues and all subclasses generate application-specific
 * random values.
 *
 * @author Shanti Subramanyam
 */
public class Random
{
	
	private java.util.Random r;
	private static char[] alpha =
		{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
		 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
		 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
		 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
		 'u', 'v', 'w', 'x', 'y', 'z'};
	private static char[] characs =
		{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	
	/**
	 * Constructor: Initialize random number generators.
	 */
	public Random()
	{
		r = new java.util.Random();
	}
	
	/**
	 * Constructor: Initialize random number generators.
	 * 
	 * @param seed      The seed for the random number generator.
	 */
	public Random( long seed )
	{
		r = new java.util.Random( seed );
	}
	
	/**
	 * Selects a random number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, inclusive.
	 */
	public int random( int x, int y )
	{
		// Switch x and y if y less than x.
		if ( y < x )
		{
			int t = y;
			y = x;
			x = t;
		}
		return x + Math.abs( r.nextInt() % ( y - x + 1 ) );
	}
	
	/**
	 * Selects a long random number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, inclusive.
	 */
	public long lrandom( long x, long y )
	{
		// Switch x and y if y less than x.
		if ( y < x )
		{
			long t = y;
			y = x;
			x = t;
		}
		return x + Math.abs( r.nextLong() % ( y - x + 1 ) );
	}
	
	/**
	 * Selects a double random number uniformly distributed between x and y,
	 * inclusively, with a mean of (x+y)/2.
	 *
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, exclusive.
	 */
	public double drandom( double x, double y )
	{
		return ( x + ( r.nextDouble() * ( y - x ) ) );
	}
	
	/**
	 * NURand integer non-uniform random number generator.
	 * TPC-C function NURand(A, x, y) =
	 *      (((random(0,A) | random(x,y)) + C) % (y - x + 1)) + x
	 * 
	 * @param A     The A-value.
	 * @param x     The x-value.
	 * @param y     The y-value.
	 * @return      The random value between x and y, inclusive.
	 */
	public int NURand( int A, int x, int y )
	{
		int C = 123; /* Run-time constant chosen between 0, A */
		int nurand = ( ( ( random( 0, A ) | random( x, y ) ) + C ) % ( y - x + 1 ) ) + x;
		
		return nurand;
	}
	
	/**
	 * makeAString [x..y] generates a random string of alphanumeric
	 * characters of random length of mininum x, maximum y and mean (x+y)/2
	 *
	 * @param x     The minimum length.
	 * @param y     The maximum length.
	 * @return      A random string of length between x and y.
	 */
	public String makeAString( int x, int y )
	{
		int length = x;
		if (x != y)
		{
			length = this.random(x, y);
		}
		
		char[] buffer = new char[length];
		
		for ( int i = 0; i < length; i++ )
		{
			int j = random( 0, alpha.length - 1 );
			buffer[i] = alpha[j];
		}
		return new String( buffer );
	}
	
	/**
	 * makeCString [x..y] generates a random string of only alpahabet
	 * characters of random length of mininum x, maximum y and
	 * mean (x+y)/2
	 *
	 * @param x     The minimum length.
	 * @param y     The maximum length.
	 * @return      A random character string of length between x and y.
	 */
	public String makeCString( int x, int y )
	{
		int length = x;
		if (x != y)
		{
			length = this.random(x, y);
		}
		
		char[] buffer = new char[length];
		
		for ( int i = 0; i < length; i++ )
		{
			int j = random( 0, characs.length - 1 );
			buffer[i] = characs[j];
		}
		return new String( buffer );
	}
	
 	/**
 	 * makeDateInInterval generates a java.sql.Date instance representing
	 * a Date within the range specified by (input Date + x) and
	 * (inputDate + y)
	 *
	 * @param refDate   The reference date.
	 * @param x         The minimum offset from the reference date.
	 * @param y         The maximum offset from the reference date.
	 * @return          A random date.
	 */
	public java.sql.Date makeDateInInterval( java.sql.Date refDate, int x, int y )
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis( refDate.getTime() );
		
		int days = x;
		if ( x != y )
		{
			days = random( x, y );
		}
		
		calendar.add( Calendar.DATE,days );
		java.sql.Date date = new java.sql.Date( calendar.getTimeInMillis() );
		
		return date;
	}
	
	/**
	 * Creates a random calendar between time (ref + min) and (ref + max).
	 * 
	 * @param refCal    The reference calendar.
	 * @param min       The lower time offset from the reference.
	 * @param max       The upper time offset from the reference.
	 * @param units     The units of minimum and maximum, referencing the
	 *                  fields of Calendar (e.g. Calendar.YEAR).
	 * @return          A random calendar.
	 */
	public Calendar makeCalendarInInterval( Calendar refCal, int min, int max, int units ) {
		// We should not modify refCal; we clone it instead.
		Calendar baseCal = (Calendar) refCal.clone();
		baseCal.add( units, min );
		long minMs = baseCal.getTimeInMillis();
		
		Calendar maxCal = (Calendar) refCal.clone();
		maxCal.add( units, max );
		long maxMs = maxCal.getTimeInMillis();
		
		baseCal.setTimeInMillis( lrandom( minMs, maxMs ) );
		
		return baseCal;
	}
	
	/**
	 * Creates a random calendar between min and max.
	 * 
	 * @param min       The minimum time.
	 * @param max       The maximum time.
	 * @return          A random calendar.
	 */
	public Calendar makeCalendarInInterval( Calendar min, Calendar max ) {
		long minMs = min.getTimeInMillis();
		long maxMs = max.getTimeInMillis();
		
		// We use cloning so Calendar type, timezone, locale, and stuff
		// stay the same as min.
		Calendar result = (Calendar) min.clone();
		result.setTimeInMillis( lrandom( minMs, maxMs ) );
		
		return result;
	}
	
	/**
	 * makeNString [x..y] generates a random string of only numeric
	 * characters of random length of mininum x, maximum y and
	 * mean (x+y)/2.
	 * 
	 * @param min       The minimum length.
	 * @param max       The maximum length.
	 * @return          A random character string of length between x and y..
	 */
	public String makeNString( int x, int y )
	{
		int length = x;
		if ( x != y )
		{
			length = random( x, y );
		}
		
		char[] buffer = new char[length];
		
		for ( int i = 0; i < length; i++ )
		{
			buffer[i] = (char) random( '0', '9' );
		}
		
		return new String( buffer );
	}
	
}
