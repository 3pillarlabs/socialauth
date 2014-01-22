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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Twitter implementation of the provider.
 * 
 * @author tarunn@brickred.com
 * 
 */

public class TwitterImpl extends AbstractProvider implements AuthProvider,
		Serializable {

	private static final long serialVersionUID = 1908393649053616794L;
	private static final String PROFILE_URL = "https://api.twitter.com/1.1/users/show.json?screen_name=";
	private static final String CONTACTS_URL = "https://api.twitter.com/1.1/friends/ids.json?screen_name=%1$s&cursor=-1";
	private static final String LOOKUP_URL = "https://api.twitter.com/1.1/users/lookup.json?user_id=";
	private static final String UPDATE_STATUS_URL = "https://api.twitter.com/1.1/statuses/update.json?status=";
	private static final String IMAGE_UPLOAD_URL = "https://api.twitter.com/1.1/statuses/update_with_media.json";

	private static final String PROPERTY_DOMAIN = "twitter.com";
	private static final Map<String, String> ENDPOINTS;
	private static final Pattern IMAGE_FILE_PATTERN = Pattern.compile(
			"(jpg|jpeg|gif|png)$", Pattern.CASE_INSENSITIVE);

	private final Log LOG = LogFactory.getLog(TwitterImpl.class);

	private Permission scope;
	private boolean isVerify;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"https://api.twitter.com/oauth/request_token");
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://api.twitter.com/oauth/authenticate");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://api.twitter.com/oauth/access_token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public TwitterImpl(final OAuthConfig providerConfig) throws Exception {
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
		if (requestParams.get("denied") != null) {
			throw new UserDeniedPermissionException();
		}
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		isVerify = true;
		return getProfile();
	}

	private Profile getProfile() throws Exception {
		Profile profile = new Profile();
		String url = PROFILE_URL + accessToken.getAttribute("screen_name");
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
							+ ". Status :" + serviceResponse.getStatus());
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
			JSONObject pObj = new JSONObject(result);
			if (pObj.has("id_str")) {
				profile.setValidatedId(pObj.getString("id_str"));
			}
			if (pObj.has("name")) {
				profile.setFullName(pObj.getString("name"));
			}
			if (pObj.has("location")) {
				profile.setLocation(pObj.getString("location"));
			}
			if (pObj.has("screen_name")) {
				profile.setDisplayName(pObj.getString("screen_name"));
			}
			if (pObj.has("lang")) {
				profile.setLanguage(pObj.getString("lang"));
			}
			if (pObj.has("profile_image_url")) {
				profile.setProfileImageURL(pObj.getString("profile_image_url"));
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
	 * Updates the status on Twitter.
	 * 
	 * @param msg
	 *            Message to be shown as user's status
	 * @throws Exception
	 */
	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.info("Updatting status " + msg);
		if (!isVerify) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		if (msg == null || msg.trim().length() == 0) {
			throw new ServerDataException("Status cannot be blank");
		}
		String message = msg;
		if (message.length() > 140) {
			LOG.debug("Truncating message up to 140 characters");
			message = message.substring(0, 140);
		}

		String url = UPDATE_STATUS_URL
				+ URLEncoder.encode(message, Constants.ENCODING);
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url,
					MethodType.POST.toString(), null, null, null);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to update status on " + url,
					e);
		}
		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException("Failed to update status on " + url
					+ ". Status :" + serviceResponse.getStatus());
		}
		return serviceResponse;
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
		String url = String.format(CONTACTS_URL,
				accessToken.getAttribute("screen_name"));
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
		LOG.debug("User friends ids : " + result);
		try {
			JSONObject jobj = new JSONObject(result);
			if (jobj.has("ids")) {
				JSONArray idList = jobj.getJSONArray("ids");
				int flength = idList.length();
				int ids[] = new int[flength];
				for (int i = 0; i < idList.length(); i++) {
					ids[i] = idList.getInt(i);
				}
				if (flength > 0) {
					if (flength > 100) {
						int i = flength / 100;
						int temparr[];
						for (int j = 1; j <= i; j++) {
							temparr = new int[100];
							for (int k = (j - 1) * 100, c = 0; k < j * 100; k++, c++) {
								temparr[c] = ids[k];
							}
							plist.addAll(lookupUsers(temparr));
						}
						if (flength > i * 100) {
							temparr = new int[flength - i * 100];
							for (int k = i * 100, c = 0; k < flength; k++, c++) {
								temparr[c] = ids[k];
							}
							plist.addAll(lookupUsers(temparr));
						}
					} else {
						plist.addAll(lookupUsers(ids));
					}
				}
			}
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the user friends json : " + result, e);
		}
		return plist;
	}

	private List<Contact> lookupUsers(final int fids[]) throws Exception {
		StringBuilder strb = new StringBuilder();
		List<Contact> plist = new ArrayList<Contact>();
		for (int value : fids) {
			if (strb.length() != 0) {
				strb.append(",");
			}
			strb.append(value);
		}
		String url = LOOKUP_URL + strb.toString();
		LOG.debug("Fetching info of following users : " + url);
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
			throw new ServerDataException("Failed to get response from " + url,
					e);
		}
		LOG.debug("Users info : " + result);
		try {
			JSONArray jarr = new JSONArray(result);
			for (int i = 0; i < jarr.length(); i++) {
				JSONObject jobj = jarr.getJSONObject(i);
				Contact cont = new Contact();
				if (jobj.has("name")) {
					cont.setFirstName(jobj.getString("name"));
				}
				if (jobj.has("screen_name")) {
					cont.setDisplayName(jobj.getString("screen_name"));
					cont.setProfileUrl("https://" + PROPERTY_DOMAIN + "/"
							+ jobj.getString("screen_name"));
				}
				cont.setProfileImageURL(jobj.optString("profile_image_url"));
				if (jobj.has("id_str")) {
					cont.setId(jobj.getString("id_str"));
				}
				plist.add(cont);
			}
		} catch (Exception e) {
			throw e;
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
		LOG.info("Uploading Image :: " + fileName + ", message :: " + message);
		if (!IMAGE_FILE_PATTERN.matcher(fileName).find()) {
			throw new SocialAuthException(
					"Twitter supports only PNG, JPG and GIF image formats");
		}
		String fileNameParam = "media[]";
		Map<String, String> map = new HashMap<String, String>();
		map.put("status", message);
		Response response = authenticationStrategy.uploadImage(
				IMAGE_UPLOAD_URL, MethodType.POST.toString(), map, null,
				fileName, inputStream, fileNameParam);
		LOG.info("Upload Image status::" + response.getStatus());
		return response;
	}

	@Override
	protected List<String> getPluginsList() {
		List<String> list = new ArrayList<String>();
		list.add("org.brickred.socialauth.plugin.twitter.FeedPluginImpl");
		list.add("org.brickred.socialauth.plugin.twitter.AlbumsPluginImpl");
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