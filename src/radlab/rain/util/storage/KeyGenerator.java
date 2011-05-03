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

package radlab.rain.util.storage;

import java.lang.reflect.Constructor;

import org.json.JSONObject;


public abstract class KeyGenerator
{
	protected static final long DEFAULT_RNG_SEED		= 1;
	
	// Configuration constants
	public static final String RNG_SEED_KEY			= "rngSeed";
	public static final String MIN_KEY_CONFIG_KEY 	= "minKey";
	public static final String MAX_KEY_CONFIG_KEY 	= "maxKey";
	public static final String A_CONFIG_KEY 			= "a";
	public static final String R_CONFIG_KEY 			= "r";
	public static final String KEY_FILE_KEY			= "keyFile";
	public static final String NUMBER_OF_KEYS_KEY	= "numKeys";
		
	/** The name of this generator. */
	protected String name;

	/**
	 * Generates a key, usually based on a distribution represented
	 * by this key generator. Subsequent invocations of this method may not
	 * return the same value.
	 * 
	 * @return  An integral key.
	 */
	public abstract int generateKey();
	
	/**
	 * Returns the name of this generator.
	 * 
	 * @return  The name of this generator.
	 */
	public String getName()
	{
		return this.name;
	}

	@SuppressWarnings("unchecked")
	public static KeyGenerator createKeyGenerator( String name, JSONObject keyGeneratorConfig ) throws Exception
	{
		KeyGenerator keyGenerator = null;
		Class<KeyGenerator> keyGeneratorClass = (Class<KeyGenerator>) Class.forName( name );
		Constructor<KeyGenerator> keyGeneratorCtor = keyGeneratorClass.getConstructor( new Class[]{ JSONObject.class } );
		keyGenerator = (KeyGenerator) keyGeneratorCtor.newInstance( new Object[] { keyGeneratorConfig } );
		return keyGenerator;
	}
}
