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
 *
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013.
 */

package radlab.rain.workload.rubis;


/**
 * Collection of RUBiS constants.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public final class RubisConstants
{
	public static final int ANONYMOUS_USER_ID = -1;
	public static final int INVALID_CATEGORY_ID = -1;
	public static final int INVALID_REGION_ID = -1;
	public static final int INVALID_ITEM_ID = -1;
	public static final int INVALID_OPERATION_ID = -1;

	/// The set of alphanumeric characters
	public static final char[] ALNUM_CHARS = {  '0', '1', '2', '3', '4', '5',
												'6', '7', '8', '9', 'A', 'B',
												'C', 'D', 'E', 'F', 'G', 'H',
												'I', 'J', 'K', 'L', 'M', 'N',
												'O', 'P', 'Q', 'R', 'S', 'T',
												'U', 'V', 'W', 'X', 'Y', 'Z',
												'a', 'b', 'c', 'd', 'e', 'f',
												'g', 'h', 'i', 'j', 'k', 'l',
												'm', 'n', 'o', 'p', 'q', 'r',
												's', 't', 'u', 'v', 'w', 'x',
												'y', 'z'};

	public static final String[] COMMENTS = { "This is a very bad comment. Stay away from this seller!!",
											  "This is a comment below average. I don't recommend this user!!",
											  "This is a neutral comment. It is neither a good or a bad seller!!",
											  "This is a comment above average. You can trust this seller even if it is not the best deal!!",
											  "This is an excellent comment. You can make really great deals with this seller!!"};

	public static final int[] COMMENT_RATINGS = { -5, // Bad
												  -3, // Below average
												   0, // Neutral
												   3, // Average
												   5}; // Excellent

//	public static final int MAX_WORD_LEN = 12;
//	public static final int MAX_ITEM_INIT_PRICE = 5000;
//	public static final int MIN_ITEM_RESERVE_PRICE = 1000;
//	public static final int MIN_ITEM_BUY_NOW_PRICE = 1000;
//	public static final int MAX_ITEM_DURATION = 7;
	public static final int MIN_USER_ID = 0;
	public static final int MIN_ITEM_ID = 0;
	public static final int MIN_REGION_ID = 0;
	public static final int MIN_CATEGORY_ID = 0;
}
