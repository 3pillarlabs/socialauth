/*
===========================================================================
Copyright (c) 2013 National Technical University of Athens

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

Authors: Konstantinos Psychas
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

public class InstagramImpl extends AbstractProvider {

	/**
	 * Provider implementation for Instagram
	 * 
	 * @author Psychas Konstantinos
	 */
	private static final long serialVersionUID = 6073346132625871229L;
	public static final String CLASSID = "instagram";

	private static final String PROFILE_URL = "https://api.instagram.com/v1/users/self";
	private static final String CONTACTS_URL = "https://api.instagram.com/v1/users/self/follows";
	private static final String VIEW_PROFILE_URL = "http://instagram.com/";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(InstagramImpl.class);

	private Permission scope;
	private OAuthConfig config;
	private AccessGrant accessGrant;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;//OAuth2 strategy

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://api.instagram.com/oauth/authorize");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://api.instagram.com/oauth/access_token");
	}

	public InstagramImpl(final OAuthConfig providerConfig) throws Exception{
		config = providerConfig;
		/* scope should include relationships */
		authenticationStrategy = new OAuth2(config, ENDPOINTS);
		/* no need to set access token name, default access_token */
		config.setAuthenticationUrl(ENDPOINTS
				.get(Constants.OAUTH_AUTHORIZATION_URL));
		config.setAccessTokenUrl(ENDPOINTS
				.get(Constants.OAUTH_ACCESS_TOKEN_URL));
	}

	@Override
	public Response api(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
					throws Exception {

		LOG.debug("Calling URL : " + url);
		try {
			return authenticationStrategy.executeFeed(url, methodType,
					params, headerParams, body);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Error : " + e.getMessage() + "- while making request to URL : " + url, e);
		}
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessGrant;
	}

	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.info("Fetching contacts from " + CONTACTS_URL);
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(CONTACTS_URL);
		} catch (Exception e) {
			throw new SocialAuthException("Error : " + e.getMessage() + " - while getting contacts from "
					+ CONTACTS_URL, e);
		}

		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException("Error while getting contacts from "
					+ CONTACTS_URL + "Status : " + serviceResponse.getStatus());
		}

		String respStr = serviceResponse
				.getResponseBodyAsString(Constants.ENCODING);
		List<Contact> plist = new ArrayList<Contact>();

		JSONObject resp = new JSONObject(respStr);
		JSONArray data = resp.optJSONArray("data");
		if (data != null)
			for (int i = 0; i < data.length(); i++) {
				JSONObject obj = data.getJSONObject(i);
				Contact p = new Contact();
				String id = obj.optString("id");
				p.setId(id);
				String full_name = obj.optString("full_name");
				p.setDisplayName(full_name);
				if (full_name!=null) {
					String[] names = full_name.split(" ");
					if (names.length > 1) {
						p.setFirstName(names[0]);
						p.setLastName(names[1]);
					} else {
						p.setFirstName(full_name);
					}
				}
				String username = obj.optString("username");
				p.setProfileUrl(VIEW_PROFILE_URL + username);
				plist.add(p);
			}
		return plist;
	}

	@Override
	public String getLoginRedirectURL(final String successUrl) throws Exception {
		return authenticationStrategy.getLoginRedirectURL(successUrl);
	}

	@Override
	public String getProviderId() {
		/* ="instagram" */
		return config.getId();
	}

	@Override
	public Profile getUserProfile() throws Exception {
		if (userProfile == null && accessGrant != null) {
			getProfile();
		}
		return userProfile;
	}

	private void getProfile() throws Exception {
		LOG.debug("Obtaining user profile");
		Response response;
		try {
			response = authenticationStrategy.executeFeed(PROFILE_URL);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from " + PROFILE_URL, e);
		}

		if (response.getStatus() == 200) {
			String respStr = response.getResponseBodyAsString(Constants.ENCODING);
			JSONObject data = new JSONObject(respStr);
			Profile p = new Profile();

			p.setValidatedId(data.optString("id"));
			String full_name = data.optString("full_name");
			p.setDisplayName(full_name);
			if (full_name!=null) {
				String[] names = full_name.split(" ");
				if (names.length > 1) {
					p.setFirstName(names[0]);
					p.setLastName(names[1]);
				} else {
					p.setFirstName(full_name);
				}
			}
			p.setProfileImageURL(data.optString("profile_picture"));
			p.setProviderId(getProviderId());
			this.userProfile = p;
		}
	}

	@Override
	public void logout() {
		accessGrant = null;
		authenticationStrategy.logout();
	}

	@Override
	public void setAccessGrant(AccessGrant accessGrant) throws Exception {
		this.accessGrant = accessGrant;
		authenticationStrategy.setAccessGrant(accessGrant);
	}

	@Override
	public void setPermission(final Permission p) {
		LOG.debug("Permission requested : " + p.toString());
		this.scope = p;
	}

	@Override
	public void updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for Instagram");
		throw new SocialAuthException(
				"Update Status is not implemented for Instagram");
	}

	@Override
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for Instagram");
		throw new SocialAuthException(
				"Upload Image is not implemented for Instagram");
	}

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
			LOG.debug("Obtaining user profile");
			getProfile();
			return this.userProfile;
		} else {
			throw new SocialAuthException("Access token not found");
		}
	}

	@Override
	protected OAuthStrategyBase getOauthStrategy() {
		return authenticationStrategy;
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

}

