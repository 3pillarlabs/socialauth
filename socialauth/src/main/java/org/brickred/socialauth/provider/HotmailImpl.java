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
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

/**
 * Implementation of Hotmail provider. This implementation is based on the
 * sample provided by Microsoft. Currently no elements in profile are available
 * and this implements only getContactList() properly
 * 
 * 
 * @author tarunn@brickred.com
 * 
 */

public class HotmailImpl extends AbstractProvider {

	private static final long serialVersionUID = 4559561466129062485L;
	private static final String PROFILE_URL = "https://apis.live.net/v5.0/me";
	private static final String CONTACTS_URL = "https://apis.live.net/v5.0/me/contacts";
	private static final String UPDATE_STATUS_URL = "https://apis.live.net/v5.0/me/share";
	private static final String PROFILE_PICTURE_URL = "https://apis.live.net/v5.0/me/picture?access_token=%1$s";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(HotmailImpl.class);

	private Permission scope;
	private boolean isVerify;
	private OAuthConfig config;
	private Profile userProfile;
	private AccessGrant accessGrant;
	private OAuthStrategyBase authenticationStrategy;

	// set this to the list of extended permissions you want
	private static final String AllPerms = new String(
			"wl.basic,wl.emails,wl.share,wl.birthday");
	private static final String AuthenticateOnlyPerms = new String(
			"wl.basic,wl.emails");

	static {
		ENDPOINTS = new HashMap<String, String>();
//		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
//				"https://oauth.live.com/authorize");
//		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
//				"https://oauth.live.com/token");
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://login.live.com/oauth20_authorize.srf");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://login.live.com/oauth20_token.srf");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public HotmailImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		if (config.getCustomPermissions() != null) {
			this.scope = Permission.CUSTOM;
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
		//Hotmail / live needs two properties for access grant
		Map<String, String> props = new HashMap<String, String>();
		props.put("response_type", "token");
		props.put("scope", getScope());
		config.setCustomProperties(props);
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
		LOG.info("Retrieving Access Token in verify response function");

		if (requestParams.get("wrap_error_reason") != null
				&& "user_denied".equals(requestParams.get("wrap_error_reason"))) {
			throw new UserDeniedPermissionException();
		}

		accessGrant = authenticationStrategy.verifyResponse(requestParams);

		if (accessGrant != null) {
			isVerify = true;
			LOG.debug("Obtaining user profile");
			return getProfile();
		} else {
			throw new SocialAuthException("Unable to get Access token");
		}
	}

	/**
	 * Gets the list of contacts of the user and their email.
	 * 
	 * @return List of profile objects representing Contacts. Only name and
	 *         email will be available
	 * @throws Exception
	 */

	@Override
	public List<Contact> getContactList() throws Exception {

		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			throw new SocialAuthException(
					"You have not set permission to get contacts");
		}
		LOG.info("Fetching contacts from " + CONTACTS_URL);
		return getContacts(CONTACTS_URL);
	}

	private List<Contact> getContacts(final String url) throws Exception {
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url);
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting contacts from "
					+ url, e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException("Error while getting contacts from "
					+ url + "Status : " + serviceResponse.getStatus());
		}
		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception e) {
			throw new ServerDataException("Failed to get response from " + url,
					e);
		}
		LOG.debug("User Contacts list in JSON " + result);
		JSONObject resp = new JSONObject(result);
		List<Contact> plist = new ArrayList<Contact>();
		if (resp.has("data")) {
			JSONArray addArr = resp.getJSONArray("data");
			LOG.debug("Contacts Found : " + addArr.length());
			for (int i = 0; i < addArr.length(); i++) {
				JSONObject obj = addArr.getJSONObject(i);
				Contact p = new Contact();
				if (obj.has("email_hashes")) {
					JSONArray emailArr = obj.getJSONArray("email_hashes");
					if (emailArr.length() > 0) {
						p.setEmailHash(emailArr.getString(0));
					}
				}
				p.setDisplayName(obj.optString("name", null));
				p.setFirstName(obj.optString("first_name", null));
				p.setLastName(obj.optString("last_name", null));
				p.setId(obj.optString("id", null));
				if (config.isSaveRawResponse()) {
					p.setRawResponse(obj.toString());
				}
				plist.add(p);
			}
		}
		serviceResponse.close();
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
		LOG.info("Updating status : " + msg);
		if (!isVerify) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		if (msg == null || msg.trim().length() == 0) {
			throw new ServerDataException("Status cannot be blank");
		}

		Map<String, String> headerParam = new HashMap<String, String>();
		headerParam.put("Authorization", "Bearer " + accessGrant.getKey());
		headerParam.put("Content-Type", "application/json");
		String body = "{message:\"" + msg + "\"}";
		Response serviceResponse;
		serviceResponse = authenticationStrategy.executeFeed(UPDATE_STATUS_URL,
				MethodType.POST.toString(), null, headerParam, body);

		int code = serviceResponse.getStatus();
		LOG.debug("Status updated and return status code is :" + code);
		// return 201
		return serviceResponse;
	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		accessGrant = null;
		authenticationStrategy.logout();
	}

	private Profile getProfile() throws Exception {
		Profile p = new Profile();
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(PROFILE_URL);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL,
					e);
		}

		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User Profile :" + result);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to read response from  "
					+ PROFILE_URL, e);
		}
		try {
			JSONObject resp = new JSONObject(result);
			if (resp.has("id")) {
				p.setValidatedId(resp.get("id").toString());
			}
			p.setFullName(resp.optString("name", null));
			p.setFirstName(resp.optString("first_name", null));
			p.setLastName(resp.optString("last_name", null));
			p.setLocation(resp.optString("Location", null));
			p.setGender(resp.optString("gender", null));
			p.setProfileImageURL(resp.optString("ThumbnailImageLink", null));

			if (resp.has("birth_day") && !resp.isNull("birth_day")) {
				BirthDate bd = new BirthDate();
				bd.setDay(resp.getInt("birth_day"));
				if (resp.has("birth_month") && !resp.isNull("birth_month")) {
					bd.setMonth(resp.getInt("birth_month"));
				}
				if (resp.has("birth_year") && !resp.isNull("birth_year")) {
					bd.setYear(resp.getInt("birth_year"));
				}
				p.setDob(bd);
			}

			if (resp.has("emails")) {
				JSONObject eobj = resp.getJSONObject("emails");
				String email = null;
				if (eobj.has("preferred")) {
					email = eobj.getString("preferred");
				}
				if ((email == null || email.isEmpty()) && eobj.has("account")) {
					email = eobj.getString("account");
				}
				if ((email == null || email.isEmpty()) && eobj.has("personal")) {
					email = eobj.getString("personal");
				}
				p.setEmail(email);

			}
			if (resp.has("locale")) {
				p.setLanguage(resp.optString("locale", null));
			}
			serviceResponse.close();
			p.setProviderId(getProviderId());
			String picUrl = String.format(PROFILE_PICTURE_URL,
					accessGrant.getKey());
			p.setProfileImageURL(picUrl);
			if (config.isSaveRawResponse()) {
				p.setRawResponse(result);
			}
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
		this.scope = p;
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
	}

	/**
	 * Makes HTTP request to a given URL.
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
		LOG.debug("Calling URL : " + url);
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url,
					methodType, params, headerParams, body);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Error while making request to URL : " + url, e);
		}
		if (serviceResponse.getStatus() != 200) {
			LOG.debug("Return statuc for URL " + url + " is "
					+ serviceResponse.getStatus());
			throw new SocialAuthException("Error while making request to URL :"
					+ url + "Status : " + serviceResponse.getStatus());
		}
		return serviceResponse;
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
		LOG.warn("WARNING: Not implemented for Hotmail");
		throw new SocialAuthException(
				"Update Status is not implemented for Hotmail");
	}

	private String getScope() {
		String scopeStr = null;
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			scopeStr = AuthenticateOnlyPerms;
		} else if (Permission.CUSTOM.equals(scope)) {
			scopeStr = config.getCustomPermissions();
		} else {
			scopeStr = AllPerms;
		}
		String pluginScopes = getPluginsScope(config);
		if (pluginScopes != null) {
			if (scopeStr != null) {
				scopeStr += "," + pluginScopes;
			} else {
				scopeStr = pluginScopes;
			}
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
