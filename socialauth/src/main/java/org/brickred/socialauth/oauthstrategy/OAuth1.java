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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.exception.ProviderStateException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.OAuthConsumer;
import org.brickred.socialauth.util.Response;

public class OAuth1 implements OAuthStrategyBase {

	private static final long serialVersionUID = -447820298609650347L;
	private final Log LOG = LogFactory.getLog(OAuth1.class);

	private AccessGrant accessToken;
	private AccessGrant requestToken;
	private OAuthConsumer oauth;
	private boolean providerState;
	private Map<String, String> endpoints;
	private String scope;
	private Permission permission;
	private String providerId;

	public OAuth1(final OAuthConfig config, final Map<String, String> endpoints) {
		oauth = new OAuthConsumer(config);
		this.endpoints = endpoints;
		permission = Permission.ALL;
		providerId = config.getId();
	}

	@Override
	public String getLoginRedirectURL(final String successUrl) throws Exception {
		LOG.info("Determining URL for redirection");
		providerState = true;
		LOG.debug("Call to fetch Request Token");
		requestToken = oauth.getRequestToken(
				endpoints.get(Constants.OAUTH_REQUEST_TOKEN_URL), successUrl);
		String authUrl = endpoints.get(Constants.OAUTH_AUTHORIZATION_URL);
		if (scope != null) {
			if (scope.contains("=")) {
				authUrl += "?" + scope;
			} else {
				authUrl += "?scope=" + scope;
			}
		}
		StringBuilder urlBuffer = oauth.buildAuthUrl(authUrl, requestToken,
				successUrl);
		LOG.info("Redirection to following URL should happen : "
				+ urlBuffer.toString());
		return urlBuffer.toString();
	}

	@Override
	public AccessGrant verifyResponse(final Map<String, String> requestParams,
			final String methodType) throws Exception {
		LOG.info("Verifying the authentication response from provider");
		if (!providerState) {
			throw new ProviderStateException();
		}
		if (requestToken == null) {
			throw new SocialAuthException("Request token is null");
		}
		String verifier = requestParams.get(Constants.OAUTH_VERIFIER);
		if (verifier != null) {
			requestToken.setAttribute(Constants.OAUTH_VERIFIER, verifier);
		}
		LOG.debug("Call to fetch Access Token");
		accessToken = oauth.getAccessToken(
				endpoints.get(Constants.OAUTH_ACCESS_TOKEN_URL), requestToken);
		accessToken.setPermission(permission);
		accessToken.setProviderId(providerId);
		return accessToken;
	}

	@Override
	public AccessGrant verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		return verifyResponse(requestParams, MethodType.GET.toString());
	}

	@Override
	public void setScope(final String scope) {
		this.scope = scope;
	}

	@Override
	public void setPermission(final Permission permission) {
		this.permission = permission;
	}

	@Override
	public Response executeFeed(final String url) throws Exception {
		return oauth.httpGet(url, null, accessToken);
	}

	@Override
	public Response executeFeed(final String urlStr, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception {
		Response response = null;
		if (accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		if (MethodType.GET.toString().equals(methodType)) {
			try {
				response = oauth.httpGet(urlStr, headerParams, accessToken);
			} catch (Exception ie) {
				throw new SocialAuthException(
						"Error while making request to URL : " + urlStr, ie);
			}
		} else if (MethodType.PUT.toString().equals(methodType)) {
			try {
				response = oauth.httpPut(urlStr, params, headerParams, body,
						accessToken);
			} catch (Exception e) {
				throw new SocialAuthException(
						"Error while making request to URL : " + urlStr, e);
			}
		} else if (MethodType.POST.toString().equals(methodType)) {
			try {
				response = oauth.httpPost(urlStr, params, headerParams, body,
						accessToken);
			} catch (Exception e) {
				throw new SocialAuthException(
						"Error while making request to URL : " + urlStr, e);
			}
		}
		return response;
	}

	@Override
	public void setAccessGrant(final AccessGrant accessGrant) {
		this.accessToken = accessGrant;
	}

	@Override
	public void setAccessTokenParameterName(
			final String accessTokenParameterName) {
		LOG.warn("It is not implemented for OAuth1");

	}

	@Override
	public void logout() {
		accessToken = null;
		providerState = false;
	}

	@Override
	public Response uploadImage(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String fileName,
			final InputStream inputStream, final String fileParamName)
			throws Exception {
		return oauth.uploadImage(url, params, headerParams, inputStream,
				fileParamName, fileName, methodType, accessToken, true);
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessToken;
	}

}
