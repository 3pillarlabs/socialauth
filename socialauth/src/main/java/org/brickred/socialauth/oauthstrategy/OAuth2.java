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

package org.brickred.socialauth.oauthstrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.exception.ProviderStateException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.HttpUtil;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.OAuthConsumer;
import org.brickred.socialauth.util.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class OAuth2 implements OAuthStrategyBase {

	private static final long serialVersionUID = -8431902665718727947L;
	private final Log LOG = LogFactory.getLog(OAuth2.class);
	private AccessGrant accessGrant;
	private OAuthConsumer oauth;
	private boolean providerState;
	private Map<String, String> endpoints;
	private String scope;
	private Permission permission;
	private String providerId;
	private String successUrl;
	private String accessTokenParameterName;

	public OAuth2(final OAuthConfig config, final Map<String, String> endpoints) {
		oauth = new OAuthConsumer(config);
		this.endpoints = endpoints;
		permission = Permission.DEFAULT;
		providerId = config.getId();
		accessTokenParameterName = Constants.ACCESS_TOKEN_PARAMETER_NAME;
	}

	@Override
	public String getLoginRedirectURL(final String successUrl) throws Exception {
		LOG.info("Determining URL for redirection");
		providerState = true;
		try {
			this.successUrl = URLEncoder.encode(successUrl, Constants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			this.successUrl = successUrl;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(endpoints.get(Constants.OAUTH_AUTHORIZATION_URL));
		char separator = endpoints.get(Constants.OAUTH_AUTHORIZATION_URL)
				.indexOf('?') == -1 ? '?' : '&';
		sb.append(separator);
		sb.append("client_id=").append(oauth.getConfig().get_consumerKey());
		sb.append("&response_type=code");
		sb.append("&redirect_uri=").append(this.successUrl);
		if (scope != null) {
			sb.append("&scope=").append(scope);
		}
		String url = sb.toString();

		LOG.info("Redirection to following URL should happen : " + url);
		return url;
	}

	@Override
	public AccessGrant verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		return verifyResponse(requestParams, MethodType.GET.toString());
	}

	@Override
	public AccessGrant verifyResponse(final Map<String, String> requestParams,
			final String methodType) throws Exception {
		LOG.info("Verifying the authentication response from provider");

		if (requestParams.get("access_token") != null) {
			LOG.debug("Creating Access Grant");
			String accessToken = requestParams.get("access_token");
			Integer expires = null;
			if (requestParams.get(Constants.EXPIRES) != null) {
				expires = new Integer(requestParams.get(Constants.EXPIRES));
			}
			accessGrant = new AccessGrant();
			accessGrant.setKey(accessToken);
			accessGrant.setAttribute(Constants.EXPIRES, expires);
			if (permission != null) {
				accessGrant.setPermission(permission);
			} else {
				accessGrant.setPermission(Permission.ALL);
			}
			accessGrant.setProviderId(providerId);
			LOG.debug(accessGrant);
			return accessGrant;
		}

		if (!providerState) {
			throw new ProviderStateException();
		}

		String code = requestParams.get("code");
		if (code == null || code.length() == 0) {
			throw new SocialAuthException("Verification code is null");
		}
		LOG.debug("Verification Code : " + code);
		String acode;
		String accessToken = null;
		try {
			acode = URLEncoder.encode(code, "UTF-8");
		} catch (Exception e) {
			acode = code;
		}
		StringBuffer sb = new StringBuffer();
		if (MethodType.GET.toString().equals(methodType)) {
			sb.append(endpoints.get(Constants.OAUTH_ACCESS_TOKEN_URL));
			char separator = endpoints.get(Constants.OAUTH_ACCESS_TOKEN_URL)
					.indexOf('?') == -1 ? '?' : '&';
			sb.append(separator);
		}
		sb.append("client_id=").append(oauth.getConfig().get_consumerKey());
		sb.append("&redirect_uri=").append(this.successUrl);
		sb.append("&client_secret=").append(
				oauth.getConfig().get_consumerSecret());
		sb.append("&code=").append(acode);
		sb.append("&grant_type=authorization_code");

		Response response;
		String authURL = null;
		try {
			if (MethodType.GET.toString().equals(methodType)) {
				authURL = sb.toString();
				LOG.debug("URL for Access Token request : " + authURL);
				response = HttpUtil.doHttpRequest(authURL, methodType, null,
						null);
			} else {
				authURL = endpoints.get(Constants.OAUTH_ACCESS_TOKEN_URL);
				LOG.debug("URL for Access Token request : " + authURL);
				response = HttpUtil.doHttpRequest(authURL, methodType,
						sb.toString(), null);
			}
		} catch (Exception e) {
			throw new SocialAuthException("Error in url : " + authURL, e);
		}
		String result;
		try {
			result = response.getResponseBodyAsString(Constants.ENCODING);
		} catch (IOException io) {
			throw new SocialAuthException(io);
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		Integer expires = null;
		if (result.indexOf("{") < 0) {
			String[] pairs = result.split("&");
			for (String pair : pairs) {
				String[] kv = pair.split("=");
				if (kv.length != 2) {
					throw new SocialAuthException(
							"Unexpected auth response from " + authURL);
				} else {
					if (kv[0].equals("access_token")) {
						accessToken = kv[1];
					} else if (kv[0].equals("expires")) {
						expires = Integer.valueOf(kv[1]);
					} else if (kv[0].equals("expires_in")) {
						expires = Integer.valueOf(kv[1]);
					} else {
						attributes.put(kv[0], kv[1]);
					}
				}
			}
		} else {
			try {
				JSONObject jObj = new JSONObject(result);
				if (jObj.has("access_token")) {
					accessToken = jObj.getString("access_token");
				}
				if (jObj.has("expires_in")) {
					String str = jObj.getString("expires_in");
					if (str != null && str.length() > 0) {
						expires = Integer.valueOf(str);
					}
				}
				if (accessToken != null) {
					Iterator<String> keyItr = jObj.keys();
					while (keyItr.hasNext()) {
						String key = keyItr.next();
						if (!"access_token".equals(key)
								&& !"expires_in".equals(key)) {
							attributes.put(key, jObj.optString(key));
						}
					}
				}
			} catch (JSONException je) {
				throw new SocialAuthException("Unexpected auth response from "
						+ authURL);
			}
		}
		LOG.debug("Access Token : " + accessToken);
		LOG.debug("Expires : " + expires);
		if (accessToken != null) {
			accessGrant = new AccessGrant();
			accessGrant.setKey(accessToken);
			accessGrant.setAttribute(Constants.EXPIRES, expires);
			if (attributes.size() > 0) {
				accessGrant.setAttributes(attributes);
			}
			if (permission != null) {
				accessGrant.setPermission(permission);
			} else {
				accessGrant.setPermission(Permission.ALL);
			}
			accessGrant.setProviderId(providerId);
		} else {
			throw new SocialAuthException(
					"Access token and expires not found from " + authURL);
		}
		return accessGrant;
	}

	@Override
	public void setScope(final String scope) {
		this.scope = scope;
	}

	@Override
	public void setPermission(final Permission permission) {
		this.permission = permission;
	}

	@Override
	public Response executeFeed(final String url) throws Exception {
		if (accessGrant == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		char separator = url.indexOf('?') == -1 ? '?' : '&';
		String urlStr = url + separator + accessTokenParameterName + "="
				+ accessGrant.getKey();
		LOG.debug("Calling URL : " + urlStr);
		return HttpUtil.doHttpRequest(urlStr, MethodType.GET.toString(), null,
				null);
	}

	@Override
	public Response executeFeed(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception {
		if (accessGrant == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token");
		}
		String reqURL = url;
		String bodyStr = body;
		StringBuffer sb = new StringBuffer();
		sb.append(accessTokenParameterName).append("=")
				.append(accessGrant.getKey());
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(key).append("=").append(params.get(key));
			}
		}
		if (MethodType.GET.toString().equals(methodType)) {
			if (sb.length() > 0) {
				int idx = url.indexOf('?');
				if (idx == -1) {
					reqURL += "?";
				} else {
					reqURL += "&";
				}
				reqURL += sb.toString();
			}
		} else if (MethodType.POST.toString().equals(methodType)
				|| MethodType.PUT.toString().equals(methodType)) {
			if (sb.length() > 0) {
				if (bodyStr != null) {
					if (headerParams != null
							&& headerParams.containsKey("Content-Type")) {
						String val = headerParams.get("Content-Type");
						if (!"application/json".equals(val)) {
							bodyStr += "&";
							bodyStr += sb.toString();
						}
					} else {
						bodyStr += "&";
						bodyStr += sb.toString();
					}
				} else {
					bodyStr = sb.toString();
				}

			}
		}
		LOG.debug("Calling URL	:	" + reqURL);
		LOG.debug("Body		:	" + bodyStr);
		LOG.debug("Header Params	:	" + headerParams);
		return HttpUtil
				.doHttpRequest(reqURL, methodType, bodyStr, headerParams);
	}

	@Override
	public void setAccessGrant(final AccessGrant accessGrant) {
		this.accessGrant = accessGrant;
	}

	@Override
	public void setAccessTokenParameterName(
			final String accessTokenParameterName) {
		this.accessTokenParameterName = accessTokenParameterName;
	}

	@Override
	public void logout() {
		accessGrant = null;
		providerState = false;
	}

	@Override
	public Response uploadImage(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String fileName,
			final InputStream inputStream, final String fileParamName)
			throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put(accessTokenParameterName, accessGrant.getKey());
		if (params != null && params.size() > 0) {
			map.putAll(params);
		}
		return HttpUtil.doHttpRequest(url, methodType, map, headerParams,
				inputStream, fileName, null);
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessGrant;
	}
}
