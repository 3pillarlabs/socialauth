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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.brickred.socialauth.exception.ProviderStateException;
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.exception.SocialAuthConfigurationException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.BirthDate;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.HttpUtil;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class YammerImpl extends AbstractProvider implements AuthProvider,
		Serializable {
	private static final long serialVersionUID = 8671863515161132392L;
	private static final String AUTHORIZATION_URL = "https://www.yammer.com/dialog/oauth?client_id=%1$s&redirect_uri=%2$s";
	private static final String ACCESS_TOKEN_URL = "https://www.yammer.com/oauth2/access_token.json?client_id=%1$s&client_secret=%2$s&code=%3$s";
	private static final String UPDATE_STATUS_URL = "https://www.yammer.com/api/v1/messages.json";
	private static final String PROFILE_URL = "https://www.yammer.com/api/v1/users/%1$s.json?access_token=%2$s";
	private static final String CONTACTS_URL = "https://www.yammer.com/api/v1/users.json?sort_by=followers&access_token=%1$s";

	private final Log LOG = LogFactory.getLog(YammerImpl.class);

	private String accessToken;
	private String successUrl;
	private boolean isVerify;
	private OAuthConfig config;
	private Permission scope;
	private AccessGrant accessGrant;
	private Profile userProfile;
	private String profileId;
	private boolean providerState = false;

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public YammerImpl(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		if (config.getCustomPermissions() != null) {
			scope = Permission.CUSTOM;
		}
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
		isVerify = true;
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
		providerState = true;
		try {
			this.successUrl = URLEncoder.encode(successUrl, Constants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			this.successUrl = successUrl;
		}
		String url = String.format(AUTHORIZATION_URL, config.get_consumerKey(),
				this.successUrl);
		String scopeStr = getScope();
		if (scopeStr != null) {
			url += "&scope=" + scopeStr;
		}
		LOG.info("Redirection to following URL should happen : " + url);
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

	/**
	 * @param requestParams
	 * @return
	 * @throws Exception
	 */
	private Profile doVerifyResponse(final Map<String, String> requestParams)
			throws Exception {
		LOG.info("Retrieving Access Token in verify response function");
		if (requestParams.get("error") != null
				&& "access_denied".equals(requestParams.get("error"))) {
			throw new UserDeniedPermissionException();
		}
		if (!providerState) {
			throw new ProviderStateException();
		}
		String code = requestParams.get("code");
		if (code == null || code.length() == 0) {
			throw new SocialAuthException("Verification code is null");
		}
		String url = String.format(ACCESS_TOKEN_URL, config.get_consumerKey(),
				config.get_consumerSecret(), code);
		LOG.debug("Verification Code : " + code);
		StringBuilder strb = new StringBuilder();
		strb.append("code=").append(code);
		strb.append("&client_secret=").append(config.get_consumerSecret());

		LOG.debug("Parameters for access token : " + strb.toString());
		Response response;
		try {
			response = HttpUtil.doHttpRequest(url, MethodType.GET.toString(),
					null, null);
		} catch (Exception e) {
			throw new SocialAuthException("Error in url : " + e);
		}
		String result = null;
		if (response.getStatus() == 200) {
			try {
				result = response.getResponseBodyAsString(Constants.ENCODING);
			} catch (Exception exc) {
				throw new SocialAuthException("Failed to parse response", exc);
			}
		}
		if (result == null || result.length() == 0) {
			throw new SocialAuthConfigurationException(
					"Problem in getting Access Token. Application key or Secret key may be wrong."
							+ "The server running the application should be same that was registered to get the keys.");
		}

		JSONObject resp = new JSONObject(result);
		JSONObject accessTokenObject = resp.getJSONObject("access_token");
		accessToken = accessTokenObject.getString("token");
		LOG.debug("Access Token : " + accessToken);

		if (accessToken != null) {
			isVerify = true;
			accessGrant = new AccessGrant();
			accessGrant.setKey(accessToken);
			if (scope != null) {
				accessGrant.setPermission(scope);
			} else {
				accessGrant.setPermission(Permission.ALL);
			}

			if (accessTokenObject.has("user_id")) {
				profileId = accessTokenObject.getString("user_id");
			}
			accessGrant.setAttribute("profileId", profileId);
			accessGrant.setProviderId(getProviderId());

			return getProfile();
		} else {
			throw new SocialAuthException(
					"Access token and expires not found from "
							+ ACCESS_TOKEN_URL);
		}
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
		if (!isVerify || accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		List<Contact> plist = new ArrayList<Contact>();
		String contactURL = String.format(CONTACTS_URL, accessToken);
		LOG.info("Fetching contacts from " + contactURL);
		String respStr;
		try {
			Response response = HttpUtil.doHttpRequest(contactURL,
					MethodType.GET.toString(), null, null);
			respStr = response.getResponseBodyAsString(Constants.ENCODING);
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting contacts from "
					+ contactURL, e);
		}
		try {
			LOG.debug("User Contacts list in json : " + respStr);
			JSONArray resp = new JSONArray(respStr);
			for (int i = 0; i < resp.length(); i++) {
				JSONObject obj = resp.getJSONObject(i);
				Contact p = new Contact();
				String name = obj.getString("full_name");
				p.setDisplayName(name);
				JSONObject userContactDetails = obj.getJSONObject("contact");
				JSONArray emailArr = userContactDetails
						.getJSONArray("email_addresses");
				JSONObject eobj = emailArr.getJSONObject(0);
				if (eobj.has("address")) {
					p.setEmail(eobj.getString("address"));
				}
				p.setId(obj.getString("id"));
				p.setProfileUrl(obj.getString("web_url"));
				plist.add(p);
			}
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the user profile json : " + respStr, e);
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
		LOG.info("Updating status : " + msg);
		if (!isVerify || accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token and then update status");
		}
		if (msg == null || msg.trim().length() == 0) {
			throw new ServerDataException("Status cannot be blank");
		}
		Map<String, String> headerParam = new HashMap<String, String>();
		headerParam.put("Authorization", "Bearer " + accessToken);
		headerParam.put("Content-Type", "application/json");
		headerParam.put("Accept", "application/json");
		String msgBody = "{\"body\" : \"" + msg + "\"}";
		Response serviceResponse;
		try {
			serviceResponse = HttpUtil.doHttpRequest(UPDATE_STATUS_URL,
					MethodType.POST.toString(), msgBody, headerParam);

			if (serviceResponse.getStatus() != 201) {
				throw new SocialAuthException(
						"Status not updated. Return Status code :"
								+ serviceResponse.getStatus());
			}
		} catch (Exception e) {
			throw new SocialAuthException(e);
		}
		return serviceResponse;
	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		accessToken = null;
		accessGrant = null;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Profile getProfile() throws Exception {
		if (!isVerify || accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token and then update status");
		}
		Profile p = new Profile();
		Response serviceResponse;
		if (profileId == null) {
			profileId = (String) accessGrant.getAttribute("profileId");
		}
		String profileURL = String.format(PROFILE_URL, profileId, accessToken);
		try {

			serviceResponse = HttpUtil.doHttpRequest(profileURL, "GET", null,
					null);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + profileURL,
					e);
		}

		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User Profile :" + result);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to read response from  "
					+ profileURL, e);
		}
		try {
			JSONObject resp = new JSONObject(result);
			if (resp.has("full_name")) {
				p.setFullName(resp.getString("full_name"));
			}
			if (resp.has("location")) {
				p.setLocation(resp.getString("location"));
			}
			if (resp.has("mugshot_url")) {
				p.setProfileImageURL(resp.getString("mugshot_url"));
			}
			if (resp.has("birth_date")) {
				String dstr = resp.getString("birth_date");
				if (dstr != null) {
					String arr[] = dstr.split("\\s+");
					BirthDate bd = new BirthDate();
					if (arr.length == 1) {
						Calendar currentDate = Calendar.getInstance();
						bd.setMonth(currentDate.get(Calendar.MONTH) + 1);
						bd.setDay(currentDate.get(Calendar.DAY_OF_MONTH));
					} else {
						if (arr.length > 0) {
							bd.setDay(Integer.parseInt(arr[1]));
						}
						if (arr.length > 1) {
							bd.setMonth(new Integer(SocialAuthUtil
									.getMonthInInt(arr[0])));
						}
					}
					p.setDob(bd);
				}
			}
			JSONObject userContactDetails = resp.getJSONObject("contact");
			JSONArray emailArr = userContactDetails
					.getJSONArray("email_addresses");

			JSONObject eobj = emailArr.getJSONObject(0);
			if (eobj.has("address")) {
				p.setEmail(eobj.getString("address"));
			}

			p.setProviderId(getProviderId());
			userProfile = p;
			return userProfile;
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
		LOG.debug("Permission requested : " + p.toString());
		this.scope = p;
	}

	/**
	 * Makes HTTP request to a given URL.It attaches access token in URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
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
		if (!isVerify || accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		Map<String, String> headerParam = new HashMap<String, String>();
		headerParam.put("Content-Type", "application/json");
		headerParam.put("Accept", "application/json");
		if (headerParams != null) {
			headerParam.putAll(headerParams);
		}
		headerParam.put("Authorization", "Bearer " + accessToken);
		Response serviceResponse;
		LOG.debug("Calling URL : " + url);
		LOG.debug("Header Params : " + headerParam.toString());
		try {
			serviceResponse = HttpUtil.doHttpRequest(url, methodType, body,
					headerParam);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Error while making request to URL : " + url, e);
		}
		if (serviceResponse.getStatus() != 200
				&& serviceResponse.getStatus() != 201) {
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
		if (userProfile == null && accessToken != null) {
			this.getProfile();
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
		LOG.warn("WARNING: Not implemented for Yammer");
		throw new SocialAuthException(
				"Upload Image is not implemented for Yammer");
	}

	private String getScope() {
		String scopeStr = null;
		if (Permission.CUSTOM.equals(scope)) {
			scopeStr = config.getCustomPermissions();
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
		return null;
	}

}
