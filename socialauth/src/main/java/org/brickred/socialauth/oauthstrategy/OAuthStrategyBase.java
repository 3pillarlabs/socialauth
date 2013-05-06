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

package org.brickred.socialauth.oauthstrategy;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.brickred.socialauth.Permission;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Response;

public interface OAuthStrategyBase extends Serializable {

	/**
	 * It provides the URL which will be used for authentication with the
	 * provider
	 * 
	 * @param successUrl
	 *            the call back url on which user will be redirected after
	 *            authentication
	 * @return the authentication url
	 * @throws Exception
	 */
	public String getLoginRedirectURL(String successUrl) throws Exception;

	/**
	 * Verifies the user and get access token
	 * 
	 * @param requestParams
	 *            request parameters, received from the provider
	 * @return AccessGrant which contains access token and other information
	 * @throws Exception
	 */
	public AccessGrant verifyResponse(Map<String, String> requestParams)
			throws Exception;

	/**
	 * Verifies the user and get access token
	 * 
	 * @param requestParams
	 * @param methodType
	 * @return AccessGrant which contains access token and other attributes
	 * @throws Exception
	 */
	public AccessGrant verifyResponse(Map<String, String> requestParams,
			String methodType) throws Exception;

	/**
	 * Makes HTTP GET request to a given URL.It attaches access token in URL if
	 * required.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @return Response object
	 * @throws Exception
	 */
	public Response executeFeed(String url) throws Exception;

	/**
	 * Makes HTTP request to a given URL.It attaches access token in URL if
	 * required.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Not using this parameter in Google API function
	 * @param headerParams
	 *            Parameters need to pass as Header Parameters
	 * @param body
	 *            Request Body
	 * @throws Exception
	 */
	public Response executeFeed(String url, String methodType,
			Map<String, String> params, Map<String, String> headerParams,
			String body) throws Exception;

	/**
	 * Sets the permission
	 * 
	 * @param permission
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	public void setPermission(final Permission permission);

	/**
	 * Sets the scope string
	 * 
	 * @param scope
	 *            scope string
	 */
	public void setScope(final String scope);

	/**
	 * Stores access grant for the provider
	 * 
	 * @param accessGrant
	 *            It contains the access token and other information
	 * @throws Exception
	 */
	public void setAccessGrant(AccessGrant accessGrant);

	/**
	 * Sets the name of access token parameter which will returns by the
	 * provider. By default it is "access_token"
	 * 
	 * @param accessTokenParameterName
	 */
	public void setAccessTokenParameterName(String accessTokenParameterName);

	/**
	 * Logout
	 */
	public void logout();

	/**
	 * Makes HTTP request to upload image and status.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Parameters need to pass in request
	 * @param headerParams
	 *            Parameters need to pass as Header Parameters
	 * @param fileName
	 *            Image file name
	 * @param inputStream
	 *            Input stream of image
	 * @param fileParamName
	 *            Image Filename parameter. It requires in some provider.
	 * @return Response object
	 * @throws Exception
	 */
	public Response uploadImage(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String fileName,
			final InputStream inputStream, final String fileParamName)
			throws Exception;

	/**
	 * Retrieves the AccessGrant object.
	 * 
	 * @return AccessGrant object.
	 */
	public AccessGrant getAccessGrant();
}
