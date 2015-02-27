/*
 ===========================================================================
 Copyright (c) 2014 3PillarGlobal

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
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONObject;

/**
 * Provider implementation for Amazon
 * 
 * @author tarun.nagpal
 * 
 */
public class AmazonImpl extends AbstractProvider {

	private static final long serialVersionUID = -4484000112933480313L;
	private static final String PROFILE_URL = "https://api.amazon.com/user/profile";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(AmazonImpl.class);

	private Permission scope;
	private OAuthConfig config;
	private Profile userProfile;
	private AccessGrant accessGrant;
	private OAuthStrategyBase authenticationStrategy;
	private String state;

	private static final String[] AuthPerms = new String[] { "profile",
			"postal_code" };

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://www.amazon.com/ap/oa");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://api.amazon.com/auth/o2/token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public AmazonImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		state = "SocialAuth" + System.currentTimeMillis();
		String authURL = ENDPOINTS.get(Constants.OAUTH_AUTHORIZATION_URL) + "?"
				+ Constants.STATE + "=" + state;
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL, authURL);
		// Need to pass scope while fetching RequestToken from LinkedIn for new
		// keys
		if (config.getCustomPermissions() != null) {
			scope = Permission.CUSTOM;
		}

		if (config.getAuthenticationUrl() != null) {
			ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
					config.getAuthenticationUrl());
		} else {
			config.setAuthenticationUrl(ENDPOINTS
					.get(Constants.OAUTH_AUTHORIZATION_URL));
		}

		if (config.getAccessTokenUrl() != null) {
			ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
					config.getAccessTokenUrl());
		} else {
			config.setAccessTokenUrl(ENDPOINTS
					.get(Constants.OAUTH_ACCESS_TOKEN_URL));
		}

		authenticationStrategy = new OAuth2(config, ENDPOINTS);
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
	}

	/**
	 * Stores access grant for the provider
	 * 
	 * @param accessGrant
	 *            It contains the access token and other information
	 * @throws Exception
	 */
	@Override
	public void setAccessGrant(AccessGrant accessGrant)
			throws AccessTokenExpireException, SocialAuthException {
		this.accessGrant = accessGrant;
		authenticationStrategy.setAccessGrant(accessGrant);
	}

	/**
	 * This is the most important action. It redirects the browser to an
	 * appropriate URL which will be used for authentication with the provider
	 * that has been set using setId()
	 * 
	 */
	@Override
	public String getLoginRedirectURL(String successUrl) throws Exception {
		return authenticationStrategy.getLoginRedirectURL(successUrl);
	}

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * 
	 * @param requestParams
	 *            request parameters, received from the provider
	 * @return Profile object containing the profile information
	 * @throws Exception
	 */
	@Override
	public Profile verifyResponse(Map<String, String> requestParams)
			throws Exception {
		if (requestParams.containsKey(Constants.STATE)) {
			String stateStr = requestParams.get(Constants.STATE);
			if (!state.equals(stateStr)) {
				throw new SocialAuthException(
						"State parameter value does not match with expected value");
			}
		}
		return doVerifyResponse(requestParams);
	}

	private Profile doVerifyResponse(final Map<String, String> requestParams)
			throws Exception {
		LOG.info("Verifying the authentication response from provider");
		accessGrant = authenticationStrategy.verifyResponse(requestParams,
				MethodType.POST.toString());
		return getProfile();
	}

	private Profile getProfile() throws Exception {
		String presp;

		try {
			Response response = authenticationStrategy.executeFeed(PROFILE_URL);
			presp = response.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting profile from "
					+ PROFILE_URL, e);
		}
		try {
			LOG.debug("User Profile : " + presp);
			JSONObject resp = new JSONObject(presp);
			Profile p = new Profile();
			if (resp.has("name")) {
				p.setFullName(resp.getString("name"));
			}
			if (resp.has("email")) {
				p.setEmail(resp.getString("email"));
			}
			if (resp.has("user_id")) {
				p.setValidatedId(resp.getString("user_id"));
			}
			if (resp.has("postal_code")) {
				p.setLocation(resp.getString("postal_code"));
			}
			if (config.isSaveRawResponse()) {
				p.setRawResponse(presp);
			}
			p.setProviderId(getProviderId());
			if (config.isSaveRawResponse()) {
				p.setRawResponse(presp);
			}
			userProfile = p;
			return p;

		} catch (Exception ex) {
			throw new ServerDataException(
					"Failed to parse the user profile json : " + presp, ex);
		}
	}

	@Override
	public Response updateStatus(String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for Amazon");
		throw new SocialAuthException(
				"Update Status is not implemented for Amazon");
	}

	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.warn("WARNING: Not implemented for Amazon");
		throw new SocialAuthException(
				"Get contact list is not implemented for Amazon");
	}

	@Override
	public void logout() {
		accessGrant = null;
		authenticationStrategy.logout();
	}

	@Override
	public void setPermission(Permission p) {
		LOG.debug("Permission requested : " + p.toString());
		this.scope = p;
		authenticationStrategy.setPermission(this.scope);
	}

	/**
	 * Makes HTTP request to a given URL.It attaches access token in URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Parameter need to pass with request
	 * @param headerParams
	 *            Parameters need to pass as Header Parameters
	 * @param body
	 *            Request Body
	 * @return Response object
	 * @throws Exception
	 */
	@Override
	public Response api(String url, String methodType,
			Map<String, String> params, Map<String, String> headerParams,
			String body) throws Exception {
		LOG.info("Calling api function for url	:	" + url);
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

	@Override
	public Profile getUserProfile() throws Exception {
		if (userProfile == null && accessGrant != null) {
			getProfile();
		}
		return userProfile;
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessGrant;
	}

	@Override
	public String getProviderId() {
		return config.getId();
	}

	@Override
	public Response uploadImage(String message, String fileName,
			InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for Amazon");
		throw new SocialAuthException(
				"Upload Image is not implemented for Amazon");
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

	private String getScope() {
		StringBuffer result = new StringBuffer();
		String arr[] = AuthPerms;
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			arr = AuthPerms;
		} else if (Permission.CUSTOM.equals(scope)
				&& config.getCustomPermissions() != null) {
			arr = config.getCustomPermissions().split(",");
		}
		result.append(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			result.append(" ").append(arr[i]);
		}
		String pluginScopes = getPluginsScope(config);
		if (pluginScopes != null) {
			result.append(" ").append(pluginScopes);
		}
		return result.toString();
	}

    @Override
    public List<Contact> getContactList(int startIndex, int pageSize) throws Exception {
        throw new SocialAuthException("This method has not been implemented yet.");
    }
}
