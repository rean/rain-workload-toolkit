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


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Collection of utilities for the RUBiS workload.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
public class RubisUtility
{
	/**
	 * Extract the value of the given form parameter.
	 *
	 * @param paramName the pattern to look for.
	 * @return the value corresponding to the given parameter name, or @c null if not found.
	 */
	public static String extractFormParamFromHtml(String html, String paramName)
	{
		if (html == null)
		{
			throw new IllegalArgumentException("HTML text cannot be null");
		}
		if (paramName == null)
		{
			throw new IllegalArgumentException("Parameter name cannot be null");
		}

		// Look for the key
		Pattern rePatt = Pattern.compile("^.*<input\\s+[^>]*?name=" + Pattern.quote(paramName) + "\\s+[^>]*?value=([^\\s>]*).*$",
										 Pattern.CASE_INSENSITIVE);
		// Alternative
		//Pattern rePatt = Pattern.compile("^.*<input\\s+[^>]*?name\\s*=\\s*['\"]?" + Pattern.quote(paramName) + "['\"]?\\s+[^>]*?value\\s*=\\s*['\"]?(.*?)['\"]?(?:\\s|>).*$", Pattern.CASE_INSENSITIVE);
		Matcher reMatch = rePatt.matcher(html);

		if (reMatch.matches())
		{
			return reMatch.group(1);
		}

		return "";
	}

	/**
	 * Extract the value of the given link parameter.
	 *
	 * @param paramName the pattern to look for.
	 * @return the value corresponding to the given parameter name, or @c null if not found.
	 */
	public static String extractLinkParamFromHtml(String html, String paramName)
	{
		if (html == null)
		{
			throw new IllegalArgumentException("HTML text cannot be null");
		}
		if (paramName == null)
		{
			throw new IllegalArgumentException("Parameter name cannot be null");
		}

		// Look for the key
		//Pattern rePatt = Pattern.compile("^.*<a\\s+[^>]*?href\\s*=\\s*(?:'|\")[^'\"&>]*?" + Pattern.quote(paramName) + "\\s*=\\s*([^'\"&>]*?).*$",
		Pattern rePatt = Pattern.compile("^.*<a\\s+[^>]*?href\\s*=\\s*(?:'|\")[^?&]*?" + Pattern.quote(paramName) + "=([^'\"&>]*?).*$",
										 Pattern.CASE_INSENSITIVE);
		Matcher reMatch = rePatt.matcher(html);

		if (reMatch.matches())
		{
			return reMatch.group(1);
		}

		return "";
	}
}
