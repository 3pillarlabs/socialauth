package org.brickred.socialauth.exception;

public class AccessTokenExpireException extends Exception {
	
	private static final long serialVersionUID = 4864931488910235154L;
	
	private static final String errorMessage = "AccessToken is expired.";
	private static final String resolution = "Please call refreshToken() method first before calling connect()";
	
	public AccessTokenExpireException() {
		super();
	}

	/**
	 * @param message
	 */
	public AccessTokenExpireException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AccessTokenExpireException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AccessTokenExpireException(final String message,
			final Throwable cause) {
		super(message, cause);
	}
	
	@Override
	public String toString() {
		return errorMessage + resolution;
	}
}
