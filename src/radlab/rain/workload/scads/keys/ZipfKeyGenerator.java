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

package radlab.rain.workload.scads.keys;

import java.util.Random;

public class ZipfKeyGenerator extends KeyGenerator {
	int minKey;
	int maxKey;
	double a; 
	double r;
	static Random rand = new Random();

	public ZipfKeyGenerator(int minKey, int maxKey) {
		this.minKey = minKey;
		this.maxKey = maxKey;
		
	}

	//double a = 1.001;
	//double r = 3.456;
	//static Random rand = new Random();
	
	public int generateKey() {
		int k = -1;
		do {
			k = sampleZipf();
		} while (k > maxKey);
		return Math.abs( (Double.valueOf((k+1)*r)).hashCode() ) % (maxKey-minKey) + minKey;
	}
	
	private int sampleZipf() {
		double b = Math.pow(2, a-1);
		double u, v, x, t = 0.0;
		do {
			u = rand.nextDouble();
			v = rand.nextDouble();
			x = Math.floor( Math.pow(u,-1.0/(a-1.0)));
			t = Math.pow(1.0+1.0/x, a-1.0);
		} while ( v*x*(t-1.0)/(b-1.0) > t/b );
		//System.out.println(x);
		return (int) x;
	}
}
