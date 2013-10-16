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
import org.brickred.socialauth.oauthstrategy.Hybrid;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.OpenIdConsumer;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provider implementation for Google. Now google supports OAuth2.0 protocol, so
 * if you are registering your application now on google, use
 * {@link GooglePlusImpl} (GoolgePlus) provider and set the key/secret in
 * configuration accordingly. Please visit <a
 * href="https://github.com/3pillarlabs/socialauth/wiki/Sample-Properties"
 * >Sample Properties</a> to configure GooglePlus key/secret.
 * 
 * @author abhinavm@brickred.com
 * @author tarun.nagpal
 * 
 */
public class GoogleImpl extends AbstractProvider {
	private static final long serialVersionUID = -6075582192266022341L;
	private static final String OAUTH_SCOPE = "https://www.google.com/m8/feeds/";
	private static final String CONTACTS_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full/?max-results=1000";
	private static final String CONTACT_NAMESPACE = "http://schemas.google.com/g/2005";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(GoogleImpl.class);

	private Permission scope;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"https://www.google.com/accounts/o8/ud");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://www.google.com/accounts/OAuthGetAccessToken");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public GoogleImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		accessToken = null;
		if (config.getCustomPermissions() != null) {
			scope = Permission.CUSTOM;
		}
		if (config.getRequestTokenUrl() != null) {
			ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
					config.getRequestTokenUrl());
		} else {
			config.setRequestTokenUrl(ENDPOINTS
					.get(Constants.OAUTH_AUTHORIZATION_URL));
		}

		if (config.getAccessTokenUrl() != null) {
			ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
					config.getAccessTokenUrl());
		} else {
			config.setAccessTokenUrl(ENDPOINTS
					.get(Constants.OAUTH_ACCESS_TOKEN_URL));
		}

		authenticationStrategy = new Hybrid(config, ENDPOINTS);
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
	}

	/**
	 * Stores access grant for the provider *
	 * 
	 * @param accessGrant
	 *            It contains the access token and other information
	 * @throws AccessTokenExpireException
	 */
	@Override
	public void setAccessGrant(final AccessGrant accessGrant)
			throws AccessTokenExpireException {
		this.accessToken = accessGrant;
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
		String url = authenticationStrategy.getLoginRedirectURL(successUrl);
		LOG.info("Redirection to following URL should happen : " + url);
		return url;

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
		LOG.debug("Verifying the authentication response from provider");
		if (requestParams.get("openid.mode") != null
				&& "cancel".equals(requestParams.get("openid.mode"))) {
			throw new UserDeniedPermissionException();
		}
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		LOG.debug("Obtaining profile from OpenID response");
		return getProfile(requestParams);
	}

	private Profile getProfile(final Map<String, String> requestParams) {
		userProfile = OpenIdConsumer.getUserInfo(requestParams);
		userProfile.setProviderId(getProviderId());
		LOG.debug("User Info : " + userProfile.toString());
		return userProfile;
	}

	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for Google");
		throw new SocialAuthException(
				"Update Status is not implemented for Google");
	}

	/**
	 * Gets the list of contacts of the user and their email.
	 * 
	 * @return List of contact objects representing Contacts. Only name and
	 *         email will be available
	 * 
	 * @throws Exception
	 */
	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.info("Fetching contacts from " + CONTACTS_FEED_URL);
		if (Permission.AUTHENTICATE_ONLY.equals(this.scope)) {
			throw new SocialAuthException(
					"You have not set Permission to get contacts.");
		}
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy
					.executeFeed(CONTACTS_FEED_URL);
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Failed to retrieve the contacts from " + CONTACTS_FEED_URL,
					ie);
		}
		List<Contact> plist = new ArrayList<Contact>();
		Element root;

		try {
			root = XMLParseUtil.loadXmlResource(serviceResponse
					.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the contacts from response."
							+ CONTACTS_FEED_URL, e);
		}
		NodeList contactsList = root.getElementsByTagName("entry");
		if (contactsList != null && contactsList.getLength() > 0) {
			LOG.debug("Found contacts : " + contactsList.getLength());
			for (int i = 0; i < contactsList.getLength(); i++) {
				Element contact = (Element) contactsList.item(i);
				String fname = "";
				NodeList l = contact.getElementsByTagNameNS(CONTACT_NAMESPACE,
						"email");
				String address = null;
				String emailArr[] = null;
				if (l != null && l.getLength() > 0) {
					Element el = (Element) l.item(0);
					if (el != null) {
						address = el.getAttribute("address");
					}
					if (l.getLength() > 1) {
						emailArr = new String[l.getLength() - 1];
						for (int k = 1; k < l.getLength(); k++) {
							Element e = (Element) l.item(k);
							if (e != null) {
								emailArr[k - 1] = e.getAttribute("address");
							}
						}
					}
				}
				String lname = "";
				String dispName = XMLParseUtil.getElementData(contact, "title");
				if (dispName != null) {
					String sarr[] = dispName.split(" ");
					if (sarr.length > 0) {
						if (sarr.length >= 1) {
							fname = sarr[0];
						}
						if (sarr.length >= 2) {
							StringBuilder sb = new StringBuilder();
							for (int k = 1; k < sarr.length; k++) {
								sb.append(sarr[k]).append(" ");
							}
							lname = sb.toString();
						}
					}
				}
				String id = XMLParseUtil.getElementData(contact, "id");

				if (address != null && address.length() > 0) {
					Contact p = new Contact();
					p.setFirstName(fname);
					p.setLastName(lname);
					p.setEmail(address);
					p.setDisplayName(dispName);
					p.setOtherEmails(emailArr);
					p.setId(id);
					plist.add(p);
				}
			}
		} else {
			LOG.debug("No contacts were obtained from the feed : "
					+ CONTACTS_FEED_URL);
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
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
	}

	/**
	 * Makes OAuth signed HTTP - GET request to a given URL. It attaches
	 * Authorization header with HTTP request.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type should be GET
	 * @param params
	 *            Not using this parameter in Google API function
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

		Response serviceResponse = null;
		if (!MethodType.GET.toString().equals(methodType)) {
			throw new SocialAuthException(
					"Only GET method is implemented in Google API function");
		}
		LOG.debug("Calling URL : " + url);
		try {
			serviceResponse = authenticationStrategy.executeFeed(url,
					methodType, params, headerParams, body);
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Error while making request to URL : " + url, ie);
		}
		return serviceResponse;
	}

	/**
	 * Retrieves the user profile.
	 * 
	 * @return Profile object containing the profile information.
	 */
	@Override
	public Profile getUserProfile() {
		if (userProfile == null && accessToken != null) {
			Map<String, String> map = new HashMap<String, String>();
			for (Map.Entry<String, Object> entry : accessToken.getAttributes()
					.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().toString();
				map.put(key, value);
			}
			if (!map.isEmpty()) {
				getProfile(map);
			}
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
		LOG.warn("WARNING: Not implemented for Google");
		throw new SocialAuthException(
				"Update Status is not implemented for Google");
	}

	private String getScope() {
		String scopeStr;
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			scopeStr = null;
		} else if (Permission.CUSTOM.equals(scope)) {
			StringBuffer sb = new StringBuffer();
			String arr[] = config.getCustomPermissions().split(",");
			sb.append(arr[0]);
			for (int i = 1; i < arr.length; i++) {
				sb.append(" ").append(arr[i]);
			}
			scopeStr = sb.toString();
		} else {
			scopeStr = OAUTH_SCOPE;
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
