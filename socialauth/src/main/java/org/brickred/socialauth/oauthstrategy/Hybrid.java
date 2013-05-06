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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.exception.ProviderStateException;
import org.brickred.socialauth.exception.SocialAuthConfigurationException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.HttpUtil;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.OAuthConsumer;
import org.brickred.socialauth.util.OpenIdConsumer;
import org.brickred.socialauth.util.Response;

public class Hybrid implements OAuthStrategyBase {

	private static final long serialVersionUID = -1331047094086589944L;
	private final Log LOG = LogFactory.getLog(Hybrid.class);

	private AccessGrant requestToken;
	private AccessGrant accessToken;
	private Map<String, String> endpoints;
	private String scope;
	private Permission permission;
	private String providerId;
	private OAuthConsumer oauth;
	private boolean providerState;

	public Hybrid(final OAuthConfig config, final Map<String, String> endpoints) {
		oauth = new OAuthConsumer(config);
		this.endpoints = endpoints;
		permission = Permission.DEFAULT;
		providerId = config.getId();
	}

	@Override
	public String getLoginRedirectURL(final String successUrl) throws Exception {
		String associationURL = OpenIdConsumer.getAssociationURL(endpoints
				.get(Constants.OAUTH_REQUEST_TOKEN_URL));
		Response r = HttpUtil.doHttpRequest(associationURL,
				MethodType.GET.toString(), null, null);
		StringBuffer sb = new StringBuffer();
		String assocHandle = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					r.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
				if ("assoc_handle:".equals(line.substring(0, 13))) {
					assocHandle = line.substring(13);
					break;
				}
			}
			LOG.debug("ASSOCCIATION : " + assocHandle);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read response from  ");
		}

		String realm;
		if (successUrl.indexOf("/", 9) > 0) {
			realm = successUrl.substring(0, successUrl.indexOf("/", 9));
		} else {
			realm = successUrl;
		}

		String consumerURL = realm.replace("http://", "");
		consumerURL = consumerURL.replace("https://", "");
		consumerURL = consumerURL.replaceAll(":{1}\\d*", "");

		providerState = true;
		String url = OpenIdConsumer.getRequestTokenURL(
				endpoints.get(Constants.OAUTH_REQUEST_TOKEN_URL), successUrl,
				realm, assocHandle, consumerURL, scope);
		LOG.info("Redirection to following URL should happen : " + url);
		return url;
	}

	@Override
	public AccessGrant verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		return verifyResponse(requestParams, MethodType.GET.toString());
	}

	@Override
	public AccessGrant verifyResponse(final Map<String, String> requestParams,
			final String methodType) throws Exception {
		if (!providerState) {
			throw new ProviderStateException();
		}

		LOG.debug("Running OpenID discovery");
		String reqTokenStr = "";
		if (this.scope != null) {
			if (Permission.AUTHENTICATE_ONLY.equals(this.permission)) {
				accessToken = new AccessGrant();
			} else {
				if (requestParams.get(OpenIdConsumer.OPENID_REQUEST_TOKEN) != null) {
					reqTokenStr = HttpUtil.decodeURIComponent(requestParams
							.get(OpenIdConsumer.OPENID_REQUEST_TOKEN));
				}
				requestToken = new AccessGrant();
				requestToken.setKey(reqTokenStr);
				LOG.debug("Call to fetch Access Token");
				accessToken = oauth.getAccessToken(
						endpoints.get(Constants.OAUTH_ACCESS_TOKEN_URL),
						requestToken);
				if (accessToken == null) {
					throw new SocialAuthConfigurationException(
							"Application keys may not be correct. "
									+ "The server running the application should be same that was registered to get the keys.");
				}
			}
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				accessToken.setAttribute(key, value);
			}
			if (permission != null) {
				accessToken.setPermission(permission);
			} else {
				accessToken.setPermission(Permission.DEFAULT);
			}
			accessToken.setProviderId(providerId);
		} else {
			LOG.warn("No Scope is given for the  Provider : " + providerId);
		}
		return accessToken;

	}

	@Override
	public Response executeFeed(final String url) throws Exception {
		if (accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		return oauth.httpGet(url, null, accessToken);
	}

	@Override
	public Response executeFeed(final String url, final String methodType,
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
				response = oauth.httpGet(url, headerParams, accessToken);
			} catch (Exception ie) {
				throw new SocialAuthException(
						"Error while making request to URL : " + url, ie);
			}
		} else if (MethodType.PUT.toString().equals(methodType)) {
			try {
				response = oauth.httpPut(url, params, headerParams, body,
						accessToken);
			} catch (Exception e) {
				throw new SocialAuthException(
						"Error while making request to URL : " + url, e);
			}
		}
		return response;
	}

	@Override
	public void setPermission(final Permission permission) {
		this.permission = permission;

	}

	@Override
	public void setScope(final String scope) {
		this.scope = scope;

	}

	@Override
	public void setAccessGrant(final AccessGrant accessGrant) {
		this.accessToken = accessGrant;
	}

	@Override
	public void setAccessTokenParameterName(
			final String accessTokenParameterName) {
		LOG.warn("It is not implemented for Hybrid");

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
