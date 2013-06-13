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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

/**
 * Encapsulates the HTTP status, headers and the content.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class Response {
	private final HttpURLConnection _connection;

	Response(final HttpURLConnection connection) {
		_connection = connection;
	}

	/**
	 * Closes the connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		_connection.disconnect();
	}

	public String getHeader(final String name) {
		return _connection.getHeaderField(name);
	}

	/**
	 * Gets the response content via InputStream.
	 * 
	 * @return response input stream
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		return _connection.getInputStream();
	}

	/**
	 * Gets the response HTTP status.
	 * 
	 * @return the HTTP status
	 */
	public int getStatus() {
		try {
			return _connection.getResponseCode();
		} catch (IOException e) {
			return 404;
		}
	}

	/**
	 * Gets the response content as String using given encoding
	 * 
	 * @param encoding
	 *            the encoding type
	 * @return Response body
	 * @throws Exception
	 */
	public String getResponseBodyAsString(final String encoding)
			throws Exception {
		String line = null;
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();

		if (Constants.GZIP_CONTENT_ENCODING.equals(_connection
				.getHeaderField(Constants.CONTENT_ENCODING_HEADER))) {
			reader = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(
							_connection.getInputStream()), encoding));
		} else {
			reader = new BufferedReader(new InputStreamReader(
					_connection.getInputStream(), encoding));
		}
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	/**
	 * Gets the error response content as String
	 * 
	 * @param encoding
	 *            the encoding type
	 * @return Error response message
	 * @throws Exception
	 */
	public String getErrorStreamAsString(final String encoding)
			throws Exception {
		String line = null;
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();

		if (Constants.GZIP_CONTENT_ENCODING.equals(_connection
				.getHeaderField(Constants.CONTENT_ENCODING_HEADER))) {
			reader = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(
							_connection.getErrorStream()), encoding));
		} else {
			reader = new BufferedReader(new InputStreamReader(
					_connection.getErrorStream(), encoding));
		}
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
}
