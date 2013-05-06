/*
 ===========================================================================
 Copyright (c) 2010 BrickRed Technologies Limited

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ===========================================================================

 */
package org.brickred.socialauth.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the keys and secret for OAuth access token as well as OAuth request
 * token.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class Token implements Serializable {

	private static final long serialVersionUID = -7120362372191191930L;
	private String key;
	private String secret;
	private Map<String, Object> _attributes;

	/**
	 * 
	 * @param key
	 *            the Key
	 * @param secret
	 *            the Secret
	 */
	public Token(final String key, final String secret) {
		this.key = key;
		this.secret = secret;
	}

	public Token() {
	}

	/**
	 * Retrieves the Token Key
	 * 
	 * @return the Token Key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Updates the Token Key
	 * 
	 * @param key
	 *            the Token Key
	 */
	public void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Retrieves the Token Secret
	 * 
	 * @return the Token Secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Updates the Token Secret
	 * 
	 * @param secret
	 *            the Token Secret
	 */
	public void setSecret(final String secret) {
		this.secret = secret;
	}

	/**
	 * Gets the attributes of this token.
	 */
	public Map<String, Object> getAttributes() {
		return _attributes;
	}

	/**
	 * Gets an attribute based from the given key.
	 */
	public Object getAttribute(final String key) {
		return _attributes == null ? null : _attributes.get(key);
	}

	/**
	 * Sets an attribute based from the given key and value.
	 */
	public void setAttribute(final String key, final Object value) {
		if (_attributes == null) {
			_attributes = new HashMap<String, Object>();
		}

		_attributes.put(key, value);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" token key : " + key + NEW_LINE);
		result.append(" token secret : " + secret + NEW_LINE);
		if (_attributes != null) {
			result.append(_attributes.toString());
		}
		result.append("}");

		return result.toString();
	}
}
