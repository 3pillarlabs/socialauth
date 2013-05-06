package org.brickred.socialauth;

import java.util.Date;

public class Feed {
	String id;
	String from;
	String message;
	String screenName;
	Date createdAt;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(final String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getScreenName() {
		return screenName;
	}

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
