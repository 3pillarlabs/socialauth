/*
 ===========================================================================
 Copyright (c) 2014 BrickRed Technologies Limited

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

import java.io.IOException;
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
import org.brickred.socialauth.util.BirthDate;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of LinkedIn OAuth2 provider. This uses the OAuth2 API provided
 * by LinkedIn
 *
 *
 * @author vineet.aggarwal@3pillarglobal.com
 * @author tarun.nagpal
 *
 */

public class LinkedInOAuth2Impl extends AbstractProvider {

	private static final long serialVersionUID = 3389564715902769183L;
	private static final String CONNECTION_URL = "https://api.linkedin.com/v2/people/~/connections:(id,first-name,last-name,public-profile-url,picture-url)?oauth2_access_token=";
	private static final String UPDATE_STATUS_URL = "https://api.linkedin.com/v2/people/~/shares?oauth2_access_token=";
	private static final String PROFILE_URL = "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture)&oauth2_access_token=";
	private static final String STATUS_BODY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><share><comment>%1$s</comment><visibility><code>anyone</code></visibility></share>";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(LinkedInOAuth2Impl.class);

	private Permission scope;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;
	private String state;

	private static final String[] AllPerms = new String[] { "r_liteprofile"};
	private static final String[] AuthPerms = new String[] { "r_fullprofile",
			"r_emailaddress" };

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://www.linkedin.com/oauth/v2/authorization");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://www.linkedin.com/oauth/v2/accessToken");
	}

	/**
	 * Stores configuration for the provider
	 *
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public LinkedInOAuth2Impl(final OAuthConfig providerConfig)
			throws Exception {
		config = providerConfig;
		state = "SocialAuth" + System.currentTimeMillis();
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
		Map<String, String> map = new HashMap<String, String>();
		map.put(Constants.STATE, state);
		return authenticationStrategy.getLoginRedirectURL(successUrl, map);
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
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		return getProfile();
	}

	/**
	 * Gets the list of contacts of the user and their email.
	 *
	 * @return List of profile objects representing Contacts. Only name and
	 *         email will be available
	 */

	@Override
	public List<Contact> getContactList() throws Exception {
		LOG.info("Fetching contacts from " + CONNECTION_URL);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(CONNECTION_URL
					+ authenticationStrategy.getAccessGrant().getKey());
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Failed to retrieve the contacts from " + CONNECTION_URL,
					ie);
		}
		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(serviceResponse
					.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the contacts from response."
							+ CONNECTION_URL, e);
		}
		List<Contact> contactList = new ArrayList<Contact>();
		if (root != null) {
			NodeList pList = root.getElementsByTagName("person");
			if (pList != null && pList.getLength() > 0) {
				LOG.debug("Found contacts : " + pList.getLength());
				for (int i = 0; i < pList.getLength(); i++) {
					Element p = (Element) pList.item(i);
					String fname = XMLParseUtil.getElementData(p, "first-name");
					String lname = XMLParseUtil.getElementData(p, "last-name");
					String id = XMLParseUtil.getElementData(p, "id");
					String profileUrl = XMLParseUtil.getElementData(p,
							"public-profile-url");
					String pictureUrl = XMLParseUtil.getElementData(p,
							"picture-url");
					if (id != null) {
						Contact cont = new Contact();
						if (fname != null) {
							cont.setFirstName(fname);
						}
						if (lname != null) {
							cont.setLastName(lname);
						}
						if (profileUrl != null) {
							cont.setProfileUrl(profileUrl);
						}
						if (pictureUrl != null) {
							cont.setProfileImageURL(pictureUrl);
						}
						cont.setId(id);
						if (config.isSaveRawResponse()) {
							cont.setRawResponse(XMLParseUtil
									.getStringFromElement(p));
						}
						contactList.add(cont);
					}
				}
			} else {
				LOG.debug("No connections were obtained from : "
						+ CONNECTION_URL);
			}
		}
		return contactList;
	}

	@Override
	public Response updateStatus(final String msg) throws Exception {
		if (msg == null || msg.trim().length() == 0) {
			throw new ServerDataException("Status cannot be blank");
		}
		String message = msg;
		if (msg.length() > 700) {
			LOG.warn("Message length can not be greater than 700 characters. So truncating it to 700 chars");
			message = msg.substring(0, 700);
		}
		// message = URLEncoder.encode(message, Constants.ENCODING);
		message = message.replace("&", "&amp;");
		LOG.info("Updating status " + message + " on " + UPDATE_STATUS_URL);
		Map<String, String> headerParams = new HashMap<String, String>();
		headerParams.put("Content-Type", "text/xml;charset=UTF-8");
		String msgBody = String.format(STATUS_BODY, message);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(
					UPDATE_STATUS_URL
							+ authenticationStrategy.getAccessGrant().getKey(),
					MethodType.POST.toString(), null, headerParams, msgBody);
		} catch (Exception ie) {
			throw new SocialAuthException("Failed to update status on "
					+ UPDATE_STATUS_URL, ie);
		}
		LOG.debug("Status Updated and return status code is : "
				+ serviceResponse.getStatus());
		// return 201
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

	private Profile getProfile() throws Exception {
		LOG.debug("Obtaining user profile");
		Profile profile = new Profile();
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(PROFILE_URL
					+ authenticationStrategy.getAccessGrant().getKey());
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL,
					e);
		}
		if (serviceResponse.getStatus() != 200) {
			LOG.info("response from provider error: " +serviceResponse.getErrorStreamAsString("UTF-8"));
			LOG.info("response from provider response: " +serviceResponse.getErrorStreamAsString("UTF-8"));

			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL
							+ ". Staus :" + serviceResponse.getStatus());

		}

		String result;
		try {
			result = serviceResponse.getResponseBodyAsString(Constants.ENCODING);
			JSONObject jObj = new JSONObject(result);
			if (jObj.has("id")) {
				profile.setFirstName(jObj.getJSONObject("firstName").getJSONObject("localized").getString("en_US"));
				profile.setLastName(jObj.getJSONObject("lastName").getJSONObject("localized").getString("en_US"));
				profile.setValidatedId(jObj.getString("id"));
				profile.setProviderId(getProviderId());

				if (config.isSaveRawResponse()) {
					profile.setRawResponse(result);
				}
				LOG.debug("User Profile :" + profile.toString());
				userProfile = profile;
			}
		} catch (IOException io) {
			throw new SocialAuthException(io);
		}

		return profile;
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
		LOG.debug("Calling URL : " + url);
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
		LOG.warn("WARNING: Not implemented for LinkedIn");
		throw new SocialAuthException(
				"Update Image is not implemented for LinkedIn");
	}

	@Override
	protected List<String> getPluginsList() {
		List<String> list = new ArrayList<String>();
		list.add("org.brickred.socialauth.plugin.linkedin.oauth2.FeedPluginImpl");
		list.add("org.brickred.socialauth.plugin.linkedin.oauth2.CareerPluginImpl");
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
		return result.toString();
	}
}

