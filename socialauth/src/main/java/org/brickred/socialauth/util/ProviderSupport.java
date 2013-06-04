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
package org.brickred.socialauth.util;

import java.util.Map;

import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;

/**
 * This provides the functionality to make OAuth specific HTTP call for a
 * provider
 * 
 * @author tarun.nagpal
 * 
 */
public class ProviderSupport {
	private OAuthStrategyBase authenticationStrategy;

	/**
	 * 
	 * @param strategy
	 *            OAuth strategy object
	 */
	public ProviderSupport(final OAuthStrategyBase strategy) {
		this.authenticationStrategy = strategy;
	}

	/**
	 * Makes OAuth signed HTTP request to a given URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Any additional parameters whose signature need to compute.
	 *            Only used in case of "POST" and "PUT" method type.
	 * @param headerParams
	 *            Any additional parameters need to pass as Header Parameters
	 * @param body
	 *            Request Body
	 * @return Response object
	 * @throws Exception
	 */
	public Response api(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception {
		Response response = null;
		try {
			response = authenticationStrategy.executeFeed(url, methodType,
					params, headerParams, body);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Error while making request to URL : " + url, e);
		}
		return response;
	}

	/**
	 * Makes OAuth signed HTTP GET request to a given URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @return Response object
	 * @throws Exception
	 */
	public Response api(final String url) throws Exception {
		Response response = null;
		try {
			response = authenticationStrategy.executeFeed(url,
					MethodType.GET.toString(), null, null, null);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Error while making request to URL : " + url, e);
		}
		return response;
	}

	/**
	 * Retrieves the AccessGrant object.
	 * 
	 * @return AccessGrant object.
	 */
	public AccessGrant getAccessGrant() {
		return authenticationStrategy.getAccessGrant();
	}

}
