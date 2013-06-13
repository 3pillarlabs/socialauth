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

package org.brickred.socialauth.provider;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.exception.AccessTokenExpireException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONObject;

/**
 * Provider implementation for SalesForce
 * 
 * 
 */

public class SalesForceImpl extends AbstractProvider implements AuthProvider,
		Serializable {

	private static final long serialVersionUID = 6929330230703360670L;
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(SalesForceImpl.class);

	private OAuthConfig config;
	private Permission scope;
	private AccessGrant accessGrant;
	private Profile userProfile;
	private String profileURL;
	private OAuthStrategyBase authenticationStrategy;

	// set this to the list of extended permissions you want
	private static final String AllPerms = new String("full");
	private static final String AuthPerms = new String("api");

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://login.salesforce.com/services/oauth2/authorize");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://login.salesforce.com/services/oauth2/token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public SalesForceImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
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
	 * @throws AccessTokenExpireException
	 */
	@Override
	public void setAccessGrant(final AccessGrant accessGrant)
			throws AccessTokenExpireException {
		this.accessGrant = accessGrant;
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
		if (!successUrl.startsWith("https")) {
			throw new SocialAuthException(
					"To implement SalesForce provider your web application should run on a secure port. Please use an https URL instead of http.");
		}
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
	public Profile verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		return doVerifyResponse(requestParams);
	}

	/**
	 * @param requestParams
	 * @return
	 * @throws Exception
	 */
	private Profile doVerifyResponse(final Map<String, String> requestParams)
			throws Exception {
		LOG.info("Retrieving Access Token in verify response function");
		if (requestParams.get("error") != null
				&& "access_denied".equals(requestParams.get("error"))) {
			throw new UserDeniedPermissionException();
		}
		accessGrant = authenticationStrategy.verifyResponse(requestParams,
				MethodType.POST.toString());

		if (accessGrant != null) {
			LOG.debug("Obtaining user profile");
			return getProfile();
		} else {
			throw new SocialAuthException("Access token not found");
		}

	}

	/**
	 * Gets the list of contacts of the user. this may not be available for all
	 * providers.
	 * 
	 * @return List of contact objects representing Contacts. Only name will be
	 *         available
	 */

	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.warn("WARNING: Not implemented for SalesForce");
		throw new SocialAuthException(
				"Retrieving contacts is not implemented for SalesForce");

	}

	/**
	 * Updates the status on the chosen provider if available. This may not be
	 * implemented for all providers.
	 * 
	 * @param msg
	 *            Message to be shown as user's status
	 * @throws Exception
	 */

	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for SalesForce");
		throw new SocialAuthException(
				"Update Status is not implemented for SalesForce");

	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		accessGrant = null;
		authenticationStrategy.logout();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Profile getProfile() throws Exception {
		if (accessGrant.getAttribute("id") != null) {
			profileURL = (String) accessGrant.getAttribute("id");
		}
		LOG.debug("Profile URL : " + profileURL);
		Profile p = new Profile();
		Map<String, String> headerParam = new HashMap<String, String>();
		headerParam.put("Authorization", "OAuth " + accessGrant.getKey());
		headerParam.put("Content-Type", "application/json");
		headerParam.put("Accept", "application/json");
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(profileURL,
					MethodType.GET.toString(), null, headerParam, null);
			// HttpUtil.doHttpRequest(profileURL, "GET", null, headerParam);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + profileURL,
					e);
		}

		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User Profile :" + result);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to read response from  "
					+ profileURL, e);
		}
		try {
			JSONObject resp = new JSONObject(result);
			if (resp.has("user_id")) {
				p.setValidatedId(resp.getString("user_id"));
			}
			if (resp.has("first_name")) {
				p.setFirstName(resp.getString("first_name"));
			}
			if (resp.has("last_name")) {
				p.setLastName(resp.getString("last_name"));
			}
			p.setDisplayName(resp.getString("display_name"));

			p.setEmail(resp.getString("email"));
			String locale = resp.getString("locale");
			if (locale != null) {
				String a[] = locale.split("_");
				p.setLanguage(a[0]);
				p.setCountry(a[1]);
			}
			if (resp.has("photos")) {
				JSONObject photosResp = resp.getJSONObject("photos");

				if (p.getProfileImageURL() == null
						|| p.getProfileImageURL().length() <= 0) {
					p.setProfileImageURL(photosResp.getString("thumbnail"));
				}
			}
			serviceResponse.close();
			p.setProviderId(getProviderId());
			userProfile = p;
			return p;
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to parse the user profile json : " + result, e);

		}
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
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
	}

	/**
	 * Makes HTTP request to a given URL.It attaches access token in URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 * @param headerParams
	 *            Parameters need to pass as Header Parameters
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

	/**
	 * Retrieves the user profile.
	 * 
	 * @return Profile object containing the profile information.
	 */
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
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for SalesForce");
		throw new SocialAuthException(
				"Upload Image is not implemented for SalesForce");
	}

	private String getScope() {
		String scopeStr = null;
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			scopeStr = AuthPerms;
		} else if (Permission.CUSTOM.equals(scope)) {
			scopeStr = config.getCustomPermissions();
		} else {
			scopeStr = AllPerms;
		}
		return scopeStr;
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
