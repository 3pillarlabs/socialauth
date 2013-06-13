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
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuth1;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provider implementation for Myspace
 * 
 */
public class MySpaceImpl extends AbstractProvider {

	private static final long serialVersionUID = -4074039782095430942L;
	private static final String PROFILE_URL = "http://api.myspace.com/1.0/people/@me/@self";
	private static final String CONTACTS_URL = "http://api.myspace.com/1.0/people/@me/@all";
	private static final String UPDATE_STATUS_URL = "http://api.myspace.com/1.0/statusmood/@me/@self";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(MySpaceImpl.class);

	private Permission scope;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;

	private static final String AllPerms = "VIEWER_FULL_PROFILE_INFO|ViewFullProfileInfo|UpdateMoodStatus";
	private static final String AuthPerms = "VIEWER_FULL_PROFILE_INFO|ViewFullProfileInfo";

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"http://api.myspace.com/request_token");
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"http://api.myspace.com/authorize?myspaceid.permissions=");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"http://api.myspace.com/access_token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public MySpaceImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		if (config.getCustomPermissions() != null) {
			this.scope = Permission.CUSTOM;
		}

		if (config.getRequestTokenUrl() != null) {
			ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
					config.getRequestTokenUrl());
		} else {
			config.setRequestTokenUrl(ENDPOINTS
					.get(Constants.OAUTH_REQUEST_TOKEN_URL));
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
		authenticationStrategy = new OAuth1(config, ENDPOINTS);
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
		accessToken = accessGrant;
		scope = accessGrant.getPermission();
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

	private Profile doVerifyResponse(final Map<String, String> requestParams)
			throws Exception {
		LOG.info("Verifying the authentication response from provider");
		if (requestParams.get("oauth_problem") != null
				&& "user_refused".equals(requestParams.get("oauth_problem"))) {
			throw new UserDeniedPermissionException();
		}
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		return getProfile();
	}

	/**
	 * Gets the list of contacts of the user and their profile URL.
	 * 
	 * @return List of contact objects representing Contacts. Only name and
	 *         profile URL will be available
	 */

	@Override
	public List<Contact> getContactList() throws Exception {
		if (accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		LOG.info("Fetching contacts from " + CONTACTS_URL);

		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(CONTACTS_URL);
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Failed to retrieve the contacts from " + CONTACTS_URL, ie);
		}
		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("Contacts JSON :" + result);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read contacts from  "
					+ CONTACTS_URL, exc);
		}
		JSONArray fArr = new JSONArray();
		JSONObject resObj = new JSONObject(result);
		if (resObj.has("entry")) {
			fArr = resObj.getJSONArray("entry");
		} else {
			throw new ServerDataException(
					"Failed to parse the user Contacts json : " + result);
		}
		List<Contact> plist = new ArrayList<Contact>();
		for (int i = 0; i < fArr.length(); i++) {
			JSONObject fObj = fArr.getJSONObject(i);
			if (fObj.has("person")) {
				Contact contact = new Contact();
				JSONObject pObj = fObj.getJSONObject("person");
				if (pObj.has("displayName")) {
					contact.setDisplayName(pObj.getString("displayName"));
				}
				if (pObj.has("name")) {
					JSONObject nobj = pObj.getJSONObject("name");
					if (nobj.has("familyName")) {
						contact.setLastName(nobj.getString("familyName"));
					}
					if (nobj.has("givenName")) {
						contact.setFirstName(nobj.getString("givenName"));
					}
				}

				if (pObj.has("profileUrl")) {
					contact.setProfileUrl(pObj.getString("profileUrl"));
				}
				if (pObj.has("id")) {
					contact.setId(pObj.getString("id"));
				}
				plist.add(contact);
			}
		}
		return plist;
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
		if (msg == null || msg.trim().length() == 0) {
			throw new ServerDataException("Status cannot be blank");
		}
		LOG.info("Updating status " + msg + " on " + UPDATE_STATUS_URL);
		String msgBody = "{\"status\":\"" + msg + "\"}";
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(
					UPDATE_STATUS_URL, MethodType.PUT.toString(), null, null,
					msgBody);
		} catch (Exception ie) {
			throw new SocialAuthException("Failed to update status on "
					+ UPDATE_STATUS_URL, ie);
		}
		LOG.info("Update Status Response :" + serviceResponse.getStatus());
		return serviceResponse;
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
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
	}

	private Profile getProfile() throws Exception {
		LOG.debug("Obtaining user profile");
		Profile profile = new Profile();

		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(PROFILE_URL);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL,
					e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL
							+ ". Staus :" + serviceResponse.getStatus());
		}

		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User Profile :" + result);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read response from  "
					+ PROFILE_URL, exc);
		}
		JSONObject pObj = new JSONObject();
		JSONObject jobj = new JSONObject(result);
		if (jobj.has("person")) {
			pObj = jobj.getJSONObject("person");
		} else {
			throw new ServerDataException(
					"Failed to parse the user profile json : " + result);
		}
		if (pObj.has("displayName")) {
			profile.setDisplayName(pObj.getString("displayName"));
		}
		if (pObj.has("id")) {
			profile.setValidatedId(pObj.getString("id"));
		}
		if (pObj.has("name")) {
			JSONObject nobj = pObj.getJSONObject("name");
			if (nobj.has("familyName")) {
				profile.setLastName(nobj.getString("familyName"));
			}
			if (nobj.has("givenName")) {
				profile.setFirstName(nobj.getString("givenName"));
			}
		}
		if (pObj.has("location")) {
			profile.setLocation(pObj.getString("location"));
		}
		if (pObj.has("nickname")) {
			profile.setDisplayName(pObj.getString("nickname"));
		}
		if (pObj.has("lang")) {
			profile.setLanguage(pObj.getString("lang"));
		}
		if (pObj.has("thumbnailUrl")) {
			profile.setProfileImageURL(pObj.getString("thumbnailUrl"));
		}
		profile.setProviderId(getProviderId());
		userProfile = profile;
		return profile;
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
		return authenticationStrategy.executeFeed(url, methodType, params,
				headerParams, body);

	}

	/**
	 * Retrieves the user profile.
	 * 
	 * @return Profile object containing the profile information.
	 */
	@Override
	public Profile getUserProfile() throws Exception {
		if (userProfile == null && accessToken != null) {
			getProfile();
		}
		return userProfile;
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessToken;
	}

	@Override
	public String getProviderId() {
		return config.getId();
	}

	@Override
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for MySpace");
		throw new SocialAuthException(
				"Update Status is not implemented for MySpace");
	}

	private String getScope() {
		String scopeStr;
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			scopeStr = AuthPerms;
		} else if (Permission.CUSTOM.equals(scope)) {
			String str = config.getCustomPermissions();
			scopeStr = str.replaceAll(",", "|");
		} else {
			scopeStr = AllPerms;
		}
		String pluginScopes = getPluginsScope(config);
		if (pluginScopes != null) {
			pluginScopes = pluginScopes.replaceAll(",", "|");
			scopeStr += "|" + pluginScopes;
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
