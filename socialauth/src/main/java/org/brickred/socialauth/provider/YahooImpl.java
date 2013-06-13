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
import org.brickred.socialauth.oauthstrategy.OAuth1;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.BirthDate;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provider implementation for Yahoo. This uses the oAuth API provided by Yahoo
 * 
 * @author abhinavm@brickred.com
 * @author tarunn@brickred.com
 * 
 */
public class YahooImpl extends AbstractProvider implements AuthProvider,
		Serializable {

	private static final long serialVersionUID = 903564874550419470L;
	private static final String PROFILE_URL = "http://social.yahooapis.com/v1/user/%1$s/profile?format=json";
	private static final String CONTACTS_URL = "http://social.yahooapis.com/v1/user/%1$s/contacts;count=max";
	private static final String UPDATE_STATUS_URL = "http://social.yahooapis.com/v1/user/%1$s/profile/status";
	private final Log LOG = LogFactory.getLog(YahooImpl.class);
	private static final Map<String, String> ENDPOINTS;

	private Permission scope;
	private AccessGrant accessToken;
	private OAuthConfig config;
	private Profile userProfile;
	private OAuthStrategyBase authenticationStrategy;

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_REQUEST_TOKEN_URL,
				"https://api.login.yahoo.com/oauth/v2/get_request_token");
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://api.login.yahoo.com//oauth/v2/request_auth");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://api.login.yahoo.com/oauth/v2/get_token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public YahooImpl(final OAuthConfig providerConfig) throws Exception {
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
		String url = null;
		try {
			url = authenticationStrategy.getLoginRedirectURL(successUrl);

		} catch (SocialAuthException ex) {
			String msg = ex.getMessage()
					+ "OR you have not set any scope while registering your application. You will have to select atlest read public profile scope while registering your application";
			throw new SocialAuthException(msg, ex);
		}
		return url;
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
		accessToken = authenticationStrategy.verifyResponse(requestParams);
		return getProfile();
	}

	private Profile getProfile() throws Exception {
		LOG.debug("Obtaining user profile");
		Profile profile = new Profile();
		String guid = (String) accessToken.getAttribute("xoauth_yahoo_guid");
		if (guid.indexOf("<") != -1) {
			guid = guid.substring(0, guid.indexOf("<")).trim();
			accessToken.setAttribute("xoauth_yahoo_guid", guid);
		}
		String url = String.format(PROFILE_URL, guid);
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
			JSONObject jobj = new JSONObject(result);
			if (jobj.has("profile")) {
				JSONObject pObj = jobj.getJSONObject("profile");
				if (pObj.has("guid")) {
					profile.setValidatedId(pObj.getString("guid"));
				}
				if (pObj.has("familyName")) {
					profile.setLastName(pObj.getString("familyName"));
				}
				if (pObj.has("gender")) {
					profile.setGender(pObj.getString("gender"));
				}
				if (pObj.has("givenName")) {
					profile.setFirstName(pObj.getString("givenName"));
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
				if (pObj.has("birthdate")) {
					String dstr = pObj.getString("birthdate");
					if (dstr != null) {
						String arr[] = dstr.split("/");
						BirthDate bd = new BirthDate();
						if (arr.length > 0) {
							bd.setMonth(Integer.parseInt(arr[0]));
						}
						if (arr.length > 1) {
							bd.setDay(Integer.parseInt(arr[1]));
						}
						profile.setDob(bd);
					}
				}
				if (pObj.has("image")) {
					JSONObject imgObj = pObj.getJSONObject("image");
					if (imgObj.has("imageUrl")) {
						profile.setProfileImageURL(imgObj.getString("imageUrl"));
					}
				}
				if (pObj.has("emails")) {
					JSONArray earr = pObj.getJSONArray("emails");
					for (int i = 0; i < earr.length(); i++) {
						JSONObject eobj = earr.getJSONObject(i);
						if (eobj.has("primary")
								&& "true".equals(eobj.getString("primary"))) {
							if (eobj.has("handle")) {
								profile.setEmail(eobj.getString("handle"));
							}
							break;
						}
					}
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
	 * Gets the list of contacts of the user and their email.
	 * 
	 * @return List of contact objects representing Contacts. Only name and
	 *         email will be available
	 */

	@Override
	public List<Contact> getContactList() throws Exception {
		String url = String.format(CONTACTS_URL,
				accessToken.getAttribute("xoauth_yahoo_guid"));
		LOG.info("Fetching contacts from " + url);

		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url);
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Failed to retrieve the contacts from " + url, ie);
		}

		List<Contact> plist = new ArrayList<Contact>();
		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(serviceResponse
					.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the contacts from response." + url, e);
		}
		NodeList contactsList = root.getElementsByTagName("contact");
		if (contactsList != null && contactsList.getLength() > 0) {
			LOG.debug("Found contacts : " + contactsList.getLength());
			for (int i = 0; i < contactsList.getLength(); i++) {
				Element contact = (Element) contactsList.item(i);
				NodeList fieldList = contact.getElementsByTagName("fields");
				if (fieldList != null && fieldList.getLength() > 0) {
					String fname = "";
					String lname = "";
					String dispName = "";
					String address = "";
					List<String> emailArr = new ArrayList<String>();
					for (int j = 0; j < fieldList.getLength(); j++) {
						Element field = (Element) fieldList.item(j);
						String type = XMLParseUtil
								.getElementData(field, "type");

						if ("email".equalsIgnoreCase(type)) {
							if (address.length() > 0) {
								emailArr.add(XMLParseUtil.getElementData(field,
										"value"));
							} else {
								address = XMLParseUtil.getElementData(field,
										"value");
							}
						} else if ("name".equals(type)) {
							fname = XMLParseUtil.getElementData(field,
									"givenName");
							lname = XMLParseUtil.getElementData(field,
									"familyName");
						} else if ("yahooid".equalsIgnoreCase(type)) {
							dispName = XMLParseUtil.getElementData(field,
									"value");
						}
					}
					if (address != null && address.length() > 0) {
						Contact p = new Contact();
						p.setFirstName(fname);
						p.setLastName(lname);
						p.setEmail(address);
						p.setDisplayName(dispName);
						if (emailArr.size() > 0) {
							String arr[] = new String[emailArr.size()];
							int k = 0;
							for (String s : emailArr) {
								arr[k] = s;
								k++;
							}
							p.setOtherEmails(arr);
						}
						p.setId(contact.getAttribute("yahoo:uri"));
						plist.add(p);
					}
				}
			}
		} else {
			LOG.debug("No contacts were obtained from : " + CONTACTS_URL);
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
		String url = String.format(UPDATE_STATUS_URL,
				accessToken.getAttribute("xoauth_yahoo_guid"));
		LOG.info("Updating status " + msg + " on " + url);
		String msgBody = "{\"status\":{\"message\":\"" + msg + "\"}}";
		Response serviceResponse = null;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url,
					MethodType.PUT.toString(), null, null, msgBody);
		} catch (Exception ie) {
			throw new SocialAuthException("Failed to update status on " + url,
					ie);
		}

		if (serviceResponse.getStatus() != 204) {
			throw new SocialAuthException(
					"Failed to update status. Return status code :"
							+ serviceResponse.getStatus());
		}
		LOG.debug("Status Updated and return status code is : "
				+ serviceResponse.getStatus());
		// return 204
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
	}

	/**
	 * Makes OAuth signed HTTP request to a given URL. It attaches GUID in URL
	 * and Authorization header to make HTTP request. URL string should contain
	 * "format specifier" for GUID.
	 * 
	 * @param url
	 *            URL to make HTTP request. It should contain format specifier
	 *            to pass GUID. E.g.
	 *            "http://social.yahooapis.com/v1/user/%1$s/profile?format=json"
	 *            . This URL contains format specifier "%1$s", which will be
	 *            replaced by GUID.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Any additional parameters whose signature we want to compute.
	 *            Only used in case of "POST" and "PUT" method type.
	 * @param headerParams
	 *            Any additional parameters need to pass as Header Parameters.
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

		String urlStr = String.format(url,
				accessToken.getAttribute("xoauth_yahoo_guid"));
		LOG.debug("Calling URL : " + urlStr);

		return authenticationStrategy.executeFeed(urlStr, methodType, params,
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
		LOG.warn("WARNING: Not implemented for Yahoo");
		throw new SocialAuthException(
				"Upload Image is not implemented for Yahoo");
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
