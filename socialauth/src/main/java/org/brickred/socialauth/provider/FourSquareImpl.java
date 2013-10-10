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
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provider implementation for FourSquare. This uses the oAuth API provided by
 * FourSquare
 * 
 * @author tarunn@brickred.com
 * 
 */
public class FourSquareImpl extends AbstractProvider {

	private static final long serialVersionUID = 3364430495809289118L;
	private static final String PROFILE_URL = "https://api.foursquare.com/v2/users/self";
	private static final String CONTACTS_URL = "https://api.foursquare.com/v2/users/self/friends";
	private static final String VIEW_PROFILE_URL = "http://foursquare.com/user/";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(FourSquareImpl.class);

	private Permission scope;
	private String accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private AccessGrant accessGrant;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://foursquare.com/oauth2/authenticate");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://foursquare.com/oauth2/access_token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public FourSquareImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;

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
		authenticationStrategy.setAccessTokenParameterName("oauth_token");
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
		accessToken = accessGrant.getKey();
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
	 * @return Profile object containing the profile information
	 * @param requestParams
	 *            Request Parameters, received from the provider
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
		if (requestParams.get("error") != null
				&& "access_denied".equals(requestParams.get("error"))) {
			throw new UserDeniedPermissionException();
		}

		accessGrant = authenticationStrategy.verifyResponse(requestParams);
		if (accessGrant != null) {
			accessToken = accessGrant.getKey();
			LOG.debug("Obtaining user profile");
			return getProfile();
		} else {
			throw new SocialAuthException("Access token not found");
		}
	}

	private Profile getProfile() throws Exception {
		LOG.debug("Obtaining user profile");
		Profile profile = new Profile();
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(PROFILE_URL);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL,
					e);
		}
		String res;
		try {
			res = serviceResponse.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read response from  "
					+ PROFILE_URL, exc);
		}

		JSONObject jobj = new JSONObject(res);
		JSONObject rObj;
		JSONObject uObj;
		if (jobj.has("response")) {
			rObj = jobj.getJSONObject("response");
		} else {
			throw new SocialAuthException(
					"Failed to parse the user profile json : " + res);
		}
		if (rObj.has("user")) {
			uObj = rObj.getJSONObject("user");
		} else {
			throw new SocialAuthException(
					"Failed to parse the user profile json : " + res);
		}
		if (uObj.has("id")) {
			profile.setValidatedId(uObj.getString("id"));
		}
		if (uObj.has("firstName")) {
			profile.setFirstName(uObj.getString("firstName"));
		}
		if (uObj.has("lastName")) {
			profile.setLastName(uObj.getString("lastName"));
		}
		if (uObj.has("photo")) {
			profile.setProfileImageURL(uObj.getString("photo"));
		}
		if (uObj.has("gender")) {
			profile.setGender(uObj.getString("gender"));
		}
		if (uObj.has("homeCity")) {
			profile.setLocation(uObj.getString("homeCity"));
		}
		if (uObj.has("contact")) {
			JSONObject cobj = uObj.getJSONObject("contact");
			if (cobj.has("email")) {
				profile.setEmail(cobj.getString("email"));
			}
		}
		profile.setProviderId(getProviderId());
		userProfile = profile;
		return profile;
	}

	/**
	 * Gets the list of contacts of the user.
	 * 
	 * @return List of contact objects representing Contacts. Only name and
	 *         profile URL will be available
	 */

	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.info("Fetching contacts from " + CONTACTS_URL);

		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(CONTACTS_URL);
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting contacts from "
					+ CONTACTS_URL, e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException("Error while getting contacts from "
					+ CONTACTS_URL + "Status : " + serviceResponse.getStatus());
		}
		String respStr;
		try {
			respStr = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read response from  "
					+ CONTACTS_URL, exc);
		}
		LOG.debug("User Contacts list in JSON " + respStr);
		JSONObject resp = new JSONObject(respStr);
		List<Contact> plist = new ArrayList<Contact>();
		JSONArray items = new JSONArray();
		if (resp.has("response")) {
			JSONObject robj = resp.getJSONObject("response");
			if (robj.has("friends")) {
				JSONObject fobj = robj.getJSONObject("friends");
				if (fobj.has("items")) {
					items = fobj.getJSONArray("items");
				}
			} else {
				throw new SocialAuthException(
						"Failed to parse the user profile json : " + respStr);
			}
		} else {
			throw new SocialAuthException(
					"Failed to parse the user profile json : " + respStr);
		}
		LOG.debug("Contacts Found : " + items.length());
		for (int i = 0; i < items.length(); i++) {
			JSONObject obj = items.getJSONObject(i);
			Contact c = new Contact();
			if (obj.has("firstName")) {
				c.setFirstName(obj.getString("firstName"));
			}
			if (obj.has("lastName")) {
				c.setLastName(obj.getString("lastName"));
			}
			if (obj.has("id")) {
				c.setProfileUrl(VIEW_PROFILE_URL + obj.getString("id"));
				c.setId(obj.getString("id"));
			}
			if (obj.has("photo")) {
				String photo = obj.getString("photo");
				if (photo.length() > 1) {
					c.setProfileImageURL(photo);
				}
			}
			plist.add(c);
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
		LOG.warn("WARNING: Not implemented for FourSquare");
		throw new SocialAuthException(
				"Update Status is not implemented for FourSquare");
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
		scope = p;
	}

	/**
	 * Makes HTTP request to a given URL. It attaches access token in URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Not in use for FourSquare api function. You can pass required
	 *            parameter in query string.
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
		Response response = null;
		LOG.debug("Calling URL : " + url);
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
		if (userProfile == null && accessToken != null) {
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
		LOG.warn("WARNING: Not implemented for FourSquare");
		throw new SocialAuthException(
				"Update Status is not implemented for FourSquare");
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
