/*
 ===========================================================================
 Copyright (c) 2012 BrickRed Technologies Limited

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
package org.brickred.socialauth.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.exception.AccessTokenExpireException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuth1;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;

/**
 * Generic OAuth1 provider implementation. By just adding configuration it can
 * be used for any provider which supports OAuth1 protocol. Following
 * configuration required for using this provider:<br>
 * socialauth.myoauth1 = org.brickred.socialauth.provider.GenericOAuth1Provider<br>
 * <code>
 * myoauth1.consumer_key=XXX myoauth1.consumer_secret=XXX
 * myoauth1.request_token_url=provider Request_Token_URL
 * myoauth1.authentication_url=provider Authenticate_URL
 * myoauth1.access_token_url=provider Access_Token_URL
 * </code> <br>
 * Here myoauth1 will be the provider id.
 * 
 * @author tarun.nagpal
 * 
 */
public class GenericOAuth1Provider extends AbstractProvider {

	private static final long serialVersionUID = 1L;

	private final Log LOG = LogFactory.getLog(GenericOAuth1Provider.class);

	private Permission scope;
	private boolean isVerify;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private OAuthStrategyBase authenticationStrategy;
	private final Map<String, String> ENDPOINTS;

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public GenericOAuth1Provider(final OAuthConfig providerConfig)
			throws Exception {
		config = providerConfig;
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				providerConfig.getRequestTokenUrl());
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				providerConfig.getAuthenticationUrl());
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				providerConfig.getAccessTokenUrl());
		authenticationStrategy = new OAuth1(config, ENDPOINTS);
	}

	/**
	 * Stores access grant for the provider
	 * 
	 * @param accessGrant
	 *            It contains the access token and other information
	 * @throws AccessTokenExpireException
	 */
	@Override
	public void setAccessGrant(final AccessGrant accessGrant)
			throws AccessTokenExpireException {
		accessToken = accessGrant;
		isVerify = true;
		authenticationStrategy.setAccessGrant(accessGrant);
	}

	/**
	 * This is the most important action. It redirects the browser to an
	 * appropriate URL which will be used for authentication with the provider
	 * that has been set using setId()
	 * 
	 * @throws Exception
	 */
	@Override
	public String getLoginRedirectURL(final String successUrl) throws Exception {
		LOG.info("Determining URL for redirection");
		return authenticationStrategy.getLoginRedirectURL(successUrl);
	}

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * @return Profile object containing the profile information. But in Generic
	 *         Provider, it will be null.
	 * @param requestParams
	 *            request parameters, received from the provider
	 * @throws Exception
	 */

	@Override
	public Profile verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		return doVerifyResponse(requestParams);
	}

	private Profile doVerifyResponse(final Map<String, String> requestParams)
			throws Exception {
		LOG.info("Verifying the authentication response from provider");
		if (requestParams.get("denied") != null) {
			throw new UserDeniedPermissionException();
		}
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		isVerify = true;
		return null;
	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		accessToken = null;
		authenticationStrategy.logout();
	}

	/**
	 * 
	 * @param p
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	@Override
	public void setPermission(final Permission p) {
		LOG.debug("Permission requested : " + p.toString());
		this.scope = p;
	}

	/**
	 * Makes OAuth signed HTTP request to a given URL. It attaches Authorization
	 * header with HTTP request.
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
	@Override
	public Response api(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception {
		if (!isVerify) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		Response response = null;
		LOG.debug("Calling URL : " + url);
		response = authenticationStrategy.executeFeed(url, methodType, params,
				headerParams, body);
		return response;
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessToken;
	}

	@Override
	public String getProviderId() {
		return config.getId();
	}

	/**
	 * This method is not implemented for GenericOAuth1 provider. Use
	 * <code>api()</code> method instead to update user status.
	 */
	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for GenericOauth1Provider");
		throw new SocialAuthException(
				"Update Status is not implemented for GenericOauth1Provider");
	}

	/**
	 * This method is not implemented for GenericOAuth1 provider. Use
	 * <code>api()</code> method instead to get user contacts.
	 */
	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.warn("WARNING: Not implemented for GenericOauth1Provider");
		throw new SocialAuthException(
				"Get Contacts is not implemented for GenericOauth1Provider");
	}

	/**
	 * This method is not implemented for GenericOAuth1 provider. Use
	 * <code>api()</code> method instead to get user profile.
	 */
	@Override
	public Profile getUserProfile() throws Exception {
		LOG.warn("WARNING: Not implemented for GenericOauth1Provider");
		throw new SocialAuthException(
				"Get Profile is not implemented for GenericOauth1Provider");
	}

	/**
	 * This method is not implemented for GenericOAuth1 provider.
	 */
	@Override
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for GenericOauth1Provider");
		throw new SocialAuthException(
				"Upload Image is not implemented for GenericOauth1Provider");
	}

	@Override
	protected List<String> getPluginsList() {
		List<String> list = new ArrayList<String>();
		if (config.getRegisteredPlugins() != null
				&& config.getRegisteredPlugins().length > 0) {
			list.addAll(Arrays.asList(config.getRegisteredPlugins()));
		}
		return list;
	}

	@Override
	protected OAuthStrategyBase getOauthStrategy() {
		return authenticationStrategy;
	}

}
