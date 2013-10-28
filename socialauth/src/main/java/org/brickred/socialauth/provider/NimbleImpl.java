/*
 ===========================================================================
 Copyright (c) 2013 3PillarGlobal

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
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provider implementation for Nimble
 * 
 * @author tarun.nagpal
 * 
 */
public class NimbleImpl extends AbstractProvider {

	private static final long serialVersionUID = 8942981661253696430L;

	private static final String CONTACTS_URL = "https://api.nimble.com/api/v1/contacts?per_page=200";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(NimbleImpl.class);

	private Permission scope;
	private OAuthConfig config;
	private AccessGrant accessGrant;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://api.nimble.com/oauth/authorize");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://api.nimble.com/oauth/token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public NimbleImpl(final OAuthConfig providerConfig) throws Exception {
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
	 * @throws Exception
	 */
	@Override
	public void setAccessGrant(final AccessGrant accessGrant)
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
		if (requestParams.get("error") != null
				&& "access_denied".equals(requestParams.get("error"))) {
			throw new UserDeniedPermissionException();
		}
		accessGrant = authenticationStrategy.verifyResponse(requestParams,
				MethodType.POST.toString());

		if (accessGrant != null) {
			LOG.debug("Obtaining user profile");
			return null;
		} else {
			throw new SocialAuthException("Access token not found");
		}
	}

	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for Nimble");
		throw new SocialAuthException(
				"Update Status is not implemented for Nimble");
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
		List<Contact> plist = new ArrayList<Contact>();
		LOG.info("Fetching contacts from " + CONTACTS_URL);
		String respStr;
		try {
			Response response = authenticationStrategy
					.executeFeed(CONTACTS_URL);
			respStr = response.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting contacts from "
					+ CONTACTS_URL, e);
		}
		try {
			LOG.debug("User Contacts list in json : " + respStr);
			JSONObject resp = new JSONObject(respStr);
			JSONArray responses = resp.getJSONArray("resources");
			LOG.debug("Found contacts : " + responses.length());
			for (int i = 0; i < responses.length(); i++) {
				JSONObject obj = responses.getJSONObject(i);
				JSONObject fields = obj.getJSONObject("fields");
				Contact p = new Contact();
				if (obj.has("record_type")) {
					if ("company".equals(obj.getString("record_type"))) {
						if (fields.has("company name")) {
							JSONArray arr = fields.getJSONArray("company name");
							JSONObject jobj = arr.getJSONObject(0);
							if (jobj.has("value")) {
								p.setFirstName(jobj.getString("value"));
							}
						}
						if (obj.has("avatar_url")) {
							p.setProfileImageURL(obj.getString("avatar_url"));
						}
						if (fields.has("URL")) {
							JSONArray arr = fields.getJSONArray("URL");
							JSONObject jobj = arr.getJSONObject(0);
							if (jobj.has("value")) {
								p.setProfileUrl(jobj.getString("value"));
							}
						}
						plist.add(p);
					} else if ("person".equals(obj.getString("record_type"))) {
						if (fields.has("last name")) {
							JSONArray arr = fields.getJSONArray("last name");
							JSONObject jobj = arr.getJSONObject(0);
							if (jobj.has("value")) {
								p.setLastName(jobj.getString("value"));
							}
						}
						if (fields.has("first name")) {
							JSONArray arr = fields.getJSONArray("first name");
							JSONObject jobj = arr.getJSONObject(0);
							if (jobj.has("value")) {
								p.setFirstName(jobj.getString("value"));
							}
						}
						if (obj.has("avatar_url")) {
							p.setProfileImageURL(obj.getString("avatar_url"));
						}
						if (fields.has("URL")) {
							JSONArray arr = fields.getJSONArray("URL");
							if (arr.length() == 1) {
								JSONObject jobj = arr.getJSONObject(0);
								if (jobj.has("value")) {
									p.setProfileUrl(jobj.getString("value"));
								}
							} else {
								String url = null;
								for (int k = 0; k < arr.length(); k++) {
									JSONObject jobj = arr.getJSONObject(k);
									url = jobj.optString("value");
									if ("personal".equals(jobj
											.optString("modifier"))) {
										break;
									}
								}
								if (url != null) {
									p.setProfileUrl(url);
								}
							}
						}
						plist.add(p);
					}
				}
			}
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the contacts json : " + respStr, e);
		}
		return plist;
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
	 * 
	 * @param p
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	@Override
	public void setPermission(final Permission p) {
		LOG.debug("Permission requested : " + p.toString());
		this.scope = p;
		authenticationStrategy.setPermission(this.scope);
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
	 *            Not using this parameter in Google API function
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
		LOG.warn("WARNING: Get Profile function not implemented for Nimble");
		return null;
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
		LOG.warn("WARNING: Not implemented for Nimble");
		throw new SocialAuthException(
				"Upload Image is not implemented for Nimble");
	}

	private String getScope() {
		return null;
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