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

package org.brickred.socialauth;

import java.io.Serializable;

public class Permission implements Serializable {

	private static final long serialVersionUID = 3439812589513804823L;
	public static final Permission AUTHENTICATE_ONLY = new Permission(
			"authenticate_only");
	public static final Permission ALL = new Permission("all");
	public static final Permission DEFAULT = new Permission("default");
	public static final Permission CUSTOM = new Permission("custom");
	private String scope;

	public Permission(final String scope) {
		this.scope = scope;
	}

	public Permission() {

	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Permission)) {
			return false;
		}
		Permission p = (Permission) obj;
		return p.scope.equals(this.scope);
	}

	@Override
	public String toString() {
		return scope;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(final String scope) {
		this.scope = scope;
	}
}
