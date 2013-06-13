/*
 ===========================================================================
 Copyright (c) 2012 3Pillar Global

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
import java.util.Date;

/**
 * Data bean for Feed info
 * 
 * @author tarun.nagpal
 * 
 */
public class Feed implements Serializable {

	private static final long serialVersionUID = 6654181691044247887L;

	String id;
	String from;
	String message;
	String screenName;
	Date createdAt;

	/**
	 * Returns Feed id
	 * 
	 * @return feed id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Updates Feed Id
	 * 
	 * @param id
	 *            the feed id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Returns the name who has posted this feed
	 * 
	 * @return the name who has posted this feed
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Updates the name who has posted feed
	 * 
	 * @param from
	 *            the name who has posted feed
	 */
	public void setFrom(final String from) {
		this.from = from;
	}

	/**
	 * Returns feed message
	 * 
	 * @return the feed message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Updates the feed message
	 * 
	 * @param message
	 *            the feed message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * Returns the posted date
	 * 
	 * @return the posted date
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * Updates the feed posted date
	 * 
	 * @param createdAt
	 *            the feed posted date
	 */
	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Returns the screen name (if available) who has posted this feed
	 * 
	 * @return
	 */
	public String getScreenName() {
		return screenName;
	}

	/**
	 * Updates the screen name who has posted this feed
	 * 
	 * @param screenName
	 */
	public void setScreenName(final String screenName) {
		this.screenName = screenName;
	}

	/**
	 * Retrieves the feed as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" id: " + id + NEW_LINE);
		result.append(" from: " + from + NEW_LINE);
		result.append(" screenName: " + screenName + NEW_LINE);
		result.append(" message: " + message + NEW_LINE);
		result.append(" createdAt: " + createdAt + NEW_LINE);
		result.append("}");
		return result.toString();
	}

}
