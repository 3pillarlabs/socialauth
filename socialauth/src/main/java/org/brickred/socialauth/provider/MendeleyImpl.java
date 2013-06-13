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
import org.brickred.socialauth.oauthstrategy.OAuth1;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Mendeley implementation of the provider.
 * 
 * @author tarunn@brickred.com
 * 
 */

public class MendeleyImpl extends AbstractProvider {

	private static final long serialVersionUID = -8791307959143391316L;
	private static final String PROFILE_URL = "https://api.mendeley.com/oapi/profiles/info/me/";
	private static final String CONTACTS_URL = "https://api.mendeley.com/oapi/profiles/contacts/";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(MendeleyImpl.class);

	private Permission scope;
	private boolean isVerify;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"https://www.mendeley.com/oauth/request_token/");
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://www.mendeley.com/oauth/authorize/");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://www.mendeley.com/oauth/access_token/");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public MendeleyImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
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
	 * @return Profile object containing the profile information
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
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		isVerify = true;
		return getProfile();
	}

	private Profile getProfile() throws Exception {
		Profile profile = new Profile();
		String url = PROFILE_URL;
		LOG.debug("Obtaining user profile. Profile URL : " + url);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + url, e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + url
							+ ". Staus :" + serviceResponse.getStatus());
		}
		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User Profile :" + result);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read response from  "
					+ url, exc);
		}
		try {
			JSONObject pRes = new JSONObject(result);
			JSONObject pObj = pRes.getJSONObject("main");
			if (pObj.has("profile_id")) {
				profile.setValidatedId(pObj.getString("profile_id"));
			}
			if (pObj.has("name")) {
				String name = pObj.getString("name");
				if (name != null && name.trim().length() > 0) {
					profile.setFirstName(pObj.getString("name"));
				}
			}
			if (pObj.has("photo")) {
				String photo = pObj.getString("photo");
				if (photo != null && photo.trim().length() > 0) {
					profile.setProfileImageURL(pObj.getString("photo"));
				}
			}
			profile.setProviderId(getProviderId());
			userProfile = profile;
			return profile;
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the user profile json : " + result, e);

		}
	}

	/**
	 * Updates the status on Mendeley.
	 * 
	 * @param msg
	 *            Message to be shown as user's status
	 * @throws Exception
	 */
	@Override
	public Response updateStatus(final String msg) throws Exception {
		throw new SocialAuthException(
				"Mendeley does not support status updates");
	}

	/**
	 * Gets the list of followers of the user and their screen name.
	 * 
	 * @return List of contact objects representing Contacts. Only name, screen
	 *         name and profile URL will be available
	 */
	@Override
	public List<Contact> getContactList() throws Exception {
		if (!isVerify) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		String url = CONTACTS_URL;
		List<Contact> plist = new ArrayList<Contact>();
		LOG.info("Fetching contacts from " + url);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url);
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Failed to retrieve the contacts from " + url, ie);
		}
		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception e) {
			throw new ServerDataException("Failed to get response from " + url);
		}
		try {
			LOG.debug("User Contacts list in json : " + result);
			JSONArray data = new JSONArray(result);
			LOG.debug("Found contacts : " + data.length());
			for (int i = 0; i < data.length(); i++) {
				JSONObject obj = data.getJSONObject(i);
				Contact p = new Contact();
				String name = obj.getString("name");
				if (name != null) {
					String nameArr[] = name.split(" ");
					if (nameArr.length > 1) {
						p.setFirstName(nameArr[0]);
						p.setLastName(nameArr[1]);
					} else {
						p.setFirstName(obj.getString("name"));
					}
					p.setDisplayName(name);
				}
				p.setId(obj.getString("profile_id"));
				plist.add(p);
			}
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the user friends json : " + result, e);
		}
		return plist;
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
		LOG.warn("WARNING: Not implemented for Mendeley");
		throw new SocialAuthException(
				"Upload Image is not implemented for Mendeley");
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