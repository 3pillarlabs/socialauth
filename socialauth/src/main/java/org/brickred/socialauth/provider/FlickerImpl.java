/*
 ===========================================================================
 Copyright (c) 2013 3Pillar Global

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

import java.io.ByteArrayInputStream;
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
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuth1;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Flicker implementation of the provider.
 * 
 * @author vineet.aggarwal@3pillarglobal.com
 * 
 */

public class FlickerImpl extends AbstractProvider implements AuthProvider,
		Serializable {

	private static final long serialVersionUID = 1908393649053616794L;
	private static final String PROFILE_URL = "http://api.flickr.com/services/rest/?method=flickr.people.getInfo&user_id=%1$s&apikey=%2$s";
	private static final String CONTACT_URL = "http://api.flickr.com/services/rest/?method=flickr.contacts.getList&user_id=%1$s&apikey=%2$s";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(FlickerImpl.class);

	private Permission scope;
	private boolean isVerify;
	private AccessGrant accessToken;
	private final OAuthConfig config;
	private Profile userProfile;
	private final OAuthStrategyBase authenticationStrategy;

	private static final String[] AllPerms = new String[] { "delete" };
	private static final String[] AuthPerms = new String[] { "read" };

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"http://www.flickr.com/services/oauth/request_token");
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"http://www.flickr.com/services/oauth/authorize");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"http://www.flickr.com/services/oauth/access_token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public FlickerImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;

		if (config.getCustomPermissions() != null) {
			scope = Permission.CUSTOM;
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
		if (requestParams.get("denied") != null) {
			throw new UserDeniedPermissionException();
		}
		accessToken = authenticationStrategy.verifyResponse(requestParams);

		isVerify = true;
		return getProfile();
	}

	private Profile getProfile() throws Exception {
		Profile profile = new Profile();

		String profileUrl = String
				.format(PROFILE_URL, accessToken.getAttribute("user_nsid"),
						config.get_consumerKey());

		LOG.info("Obtaining user profile. Profile URL : " + profileUrl);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(profileUrl);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + profileUrl,
					e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + profileUrl
							+ ". Status :" + serviceResponse.getStatus());
		}

		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(serviceResponse
					.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the profile from response." + profileUrl,
					e);
		}

		if (root != null) {
			NodeList pList = root.getElementsByTagName("person");
			if (pList != null && pList.getLength() > 0) {
				Element p = (Element) pList.item(0);
				if (p != null) {
					profile.setFullName(XMLParseUtil.getElementData(p,
							"realname"));
					profile.setDisplayName(XMLParseUtil.getElementData(p,
							"username"));
					profile.setCountry(XMLParseUtil.getElementData(p,
							"location"));
					String id = p.getAttribute("id");
					String iconfarm = p.getAttribute("iconfarm");
					String iconserver = p.getAttribute("iconserver");
					String buddyurl = "http://farm" + iconfarm
							+ ".staticflickr.com/" + iconserver
							+ "/buddyicons/" + id + ".jpg";
					profile.setValidatedId(id);
					if (iconserver.equalsIgnoreCase("0")) {
						profile.setProfileImageURL("http://www.flickr.com/images/buddyicon.gif");
					} else {
						profile.setProfileImageURL(buddyurl);
					}
					userProfile = profile;
				}
			}
		}
		return profile;
	}

	/**
	 * Updates the status on Twitter.
	 * 
	 * @param msg
	 *            Message to be shown as user's status
	 * @throws Exception
	 */
	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for Flickr");
		throw new SocialAuthException(
				"Update Status is not implemented for Flickr");
	}

	/**
	 * Gets the list of followers of the user and their screen name.
	 * 
	 * @return List of contact objects representing Contacts. Only name, screen
	 *         name and profile URL will be available
	 */
	@Override
	public List<Contact> getContactList() throws Exception {
		String contactUrl = String
				.format(CONTACT_URL, accessToken.getAttribute("user_nsid"),
						config.get_consumerKey());

		LOG.info("Obtaining user contacts. Contact URL : " + contactUrl);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(contactUrl);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to retrieve contacts from  "
					+ contactUrl, e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException("Failed to retrieve contacts from  "
					+ contactUrl + ". Status :" + serviceResponse.getStatus());
		}
		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to read response from  "
					+ contactUrl, exc);
		}

		Element root;

		try {
			InputStream is = new ByteArrayInputStream(result.getBytes());
			root = XMLParseUtil.loadXmlResource(is);
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the user contacts xml : " + result, e);
		}

		List<Contact> contactList = new ArrayList<Contact>();
		if (root != null) {
			NodeList cList = root.getElementsByTagName("contacts");
			if (cList != null && cList.getLength() > 0) {
				Element contacts = (Element) cList.item(0);
				NodeList contactNodes = contacts
						.getElementsByTagName("contact");
				if (contactNodes != null && contactNodes.getLength() > 0) {
					LOG.debug("Found contacts : " + contactNodes.getLength());
					for (int i = 0; i < contactNodes.getLength(); i++) {
						Element contact = (Element) contactNodes.item(i);
						String id = contact.getAttribute("nsid");
						String userName = contact.getAttribute("username");
						String realName = contact.getAttribute("realname");
						String iconfarm = contact.getAttribute("iconfarm");
						String iconserver = contact.getAttribute("iconserver");
						String buddyurl = "http://farm" + iconfarm
								+ ".staticflickr.com/" + iconserver
								+ "/buddyicons/" + id + ".jpg";

						if (id != null) {
							Contact contactObj = new Contact();
							if (realName != null) {
								contactObj.setFirstName(realName);
							}
							if (userName != null) {
								contactObj.setDisplayName(userName);
							}

							if (iconserver != null) {
								if (iconserver.equalsIgnoreCase("0")) {
									contactObj
											.setProfileImageURL("http://www.flickr.com/images/buddyicon.gif");
								} else {
									contactObj.setProfileImageURL(buddyurl);
								}
							}
							contactObj.setId(id);
							contactList.add(contactObj);
						}
					}
				}
			} else {
				LOG.debug("No contacts were obtained from : " + contactUrl);
			}
		}
		return contactList;
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

	/**
	 * Updates the image and message on Twitter. Twitter supports only PNG,JPG
	 * and GIF image formats. Animated GIFs are not supported.
	 * 
	 * @param message
	 *            Status Message
	 * @param fileName
	 *            Image file name
	 * @param inputStream
	 *            Input Stream of image
	 * @return Response object
	 * @throws Exception
	 */
	@Override
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for Flickr");
		throw new SocialAuthException("Not implemented for Flickr");
	}

	private String getScope() {
		StringBuffer result = new StringBuffer();
		String arr[] = null;
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			arr = AuthPerms;
		} else if (Permission.CUSTOM.equals(scope)
				&& config.getCustomPermissions() != null) {
			arr = config.getCustomPermissions().split(",");
		} else {
			arr = AllPerms;
		}
		result.append(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			result.append(" ").append(arr[i]);
		}
		List<String> scopes = config.getPluginsScopes();
		if (scopes != null && !scopes.isEmpty()) {
			String scopesStr = scopes.get(0);
			for (int i = 1; i < scopes.size(); i++) {
				scopesStr += " " + scopes.get(i);
			}
			result.append(" ").append(scopesStr);
		}
		String scope = null;
		if (result.length() > 0) {
			scope = "perms=" + result.toString();
		}
		return scope;
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