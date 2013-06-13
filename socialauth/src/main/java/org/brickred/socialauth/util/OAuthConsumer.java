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
package org.brickred.socialauth.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.exception.SignatureException;
import org.brickred.socialauth.exception.SocialAuthConfigurationException;
import org.brickred.socialauth.exception.SocialAuthException;

/**
 * It contains various method those are required for OAUTH
 * 
 * @author tarunn@brickred.com
 * 
 */
public class OAuthConsumer implements Serializable, Constants {

	private static final long serialVersionUID = -4560115102581632124L;
	private static final Pattern AMPERSAND = Pattern.compile("&");
	private final Log LOG = LogFactory.getLog(OAuthConsumer.class);
	private OAuthConfig config;

	/**
	 * 
	 * @param config
	 *            Configuration object which contains information of application
	 *            configuration
	 */
	public OAuthConsumer(final OAuthConfig config) {
		this.config = config;
	}

	/**
	 * It returns a signature for signing OAuth request. The Signature Base
	 * String is a consistent reproducible concatenation of the request elements
	 * into a single string. The string is used as an input in hashing or
	 * signing algorithms.The request parameters are collected, sorted and
	 * concatenated into a normalized string.
	 * 
	 * @param signatureType
	 *            Type of signature. It can be HMAC-SHA1.
	 * @param method
	 *            Method type can be GET, POST or PUT
	 * @param url
	 *            Requested URL for which generating signature
	 * @param args
	 *            Required arguments to generate signature
	 * @param token
	 *            Token Object
	 * @return The computed signature
	 * @throws Exception
	 */
	public String generateSignature(final String signatureType,
			final String method, final String url,
			final Map<String, String> args, final AccessGrant token)
			throws Exception {
		LOG.debug("Generating OAUTH Signature");
		LOG.debug("Given Signature Type : " + signatureType);
		LOG.debug("Given Method Type : " + method);
		LOG.debug("Given URL : " + url);
		LOG.debug("Given Parameters : " + args);
		if (HMACSHA1_SIGNATURE.equals(signatureType)) {
			return getHMACSHA1(method, url, args, token);
		} else {
			throw new SignatureException("Signature type not implemented :"
					+ signatureType);
		}
	}

	private String getHMACSHA1(final String method, final String url,
			final Map<String, String> args, final AccessGrant token)
			throws Exception {

		if (config.get_consumerSecret().length() == 0) {
			throw new SignatureException("Please check consumer secret");
		}
		boolean valid = MethodType.GET.toString().equals(method)
				|| MethodType.PUT.toString().equals(method)
				|| MethodType.POST.toString().equals(method);
		if (!valid) {
			throw new SignatureException("Invalid method type :" + method);
		}
		if (url.length() == 0) {
			throw new SignatureException("Please check URL");
		}
		String key = HttpUtil.encodeURIComponent(config.get_consumerSecret())
				+ "&";
		if (token != null && token.getSecret() != null) {
			key += HttpUtil.encodeURIComponent(token.getSecret());
		}
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"),
					"HMAC-SHA1");

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			String data = HttpUtil.encodeURIComponent(method) + "&"
					+ HttpUtil.encodeURIComponent(url) + "&"
					+ HttpUtil.encodeURIComponent(HttpUtil.buildParams(args));
			LOG.debug("Signature data : " + data);
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

			// base64-encode the hmac
			LOG.debug("Encoding raw HMAC to Base64");
			String sig = Base64.encodeBytes(rawHmac);

			return sig;
		} catch (Exception e) {
			throw new SignatureException("Unable to generate HMAC-SHA1", e);
		}
	}

	/**
	 * It obtains the request token. The Request Token is a temporary token used
	 * to initiate User authorization.
	 * 
	 * @param reqTokenURL
	 *            Request Token URL
	 * @param callbackURL
	 *            Callback URL
	 * @return The request token
	 * @throws Exception
	 */
	public AccessGrant getRequestToken(final String reqTokenURL,
			final String callbackURL) throws Exception {
		LOG.debug("Preparing to get Request Token");
		LOG.debug("Given Request Token URL : " + reqTokenURL);
		LOG.debug("Given CallBack URL : " + callbackURL);
		AccessGrant token = null;

		// Changes for LinkedIn. We have to pass scope while fetching
		// RequestToken from LinkedIn
		String reqURL = reqTokenURL;
		Map<String, String> params = new HashMap<String, String>();
		if (reqTokenURL.indexOf('?') > 0) {
			reqURL = reqTokenURL.substring(0, reqTokenURL.indexOf('?'));
			String query = reqTokenURL.substring(reqTokenURL.indexOf('?') + 1,
					reqTokenURL.length());
			String[] pairs = query.split("&");

			for (String pair : pairs) {
				String[] kv = pair.split("=");
				if (kv.length == 2) {
					params.put(kv[0], kv[1]);
				}
			}
		}

		params.put(OAUTH_CALLBACK, callbackURL);
		putOauthParams(params);

		String sig = generateSignature(config.get_signatureMethod(),
				config.get_transportName(), reqURL, params, null);
		LOG.debug(config.get_signatureMethod()
				+ " Signature for request token : " + sig);
		params.put(OAUTH_SIGNATURE, sig);
		reqURL += "?" + HttpUtil.buildParams(params);
		LOG.debug("URL to get Request Token : " + reqURL);

		Response response = HttpUtil.doHttpRequest(reqURL,
				config.get_transportName(), null, null);

		if (response.getStatus() == 200) {
			token = new AccessGrant();
			parse(response.getInputStream(), token);
		} else {
			LOG.debug("Error while fetching Request Token");
			throw new SocialAuthConfigurationException(
					"Application keys are not correct. "
							+ "The server running the application should be same that was registered to get the keys.");
		}
		return token;
	}

	/**
	 * It obtains the access token. The Consumer exchanges the Request Token for
	 * an Access Token capable of accessing the Protected Resources.
	 * 
	 * @param accessTokenURL
	 *            Access Token URL
	 * @param reqToken
	 *            Request Token
	 * @return The Access Token
	 * @throws Exception
	 */
	public AccessGrant getAccessToken(final String accessTokenURL,
			final AccessGrant reqToken) throws Exception {
		LOG.debug("Preparing to get Access Token");
		LOG.debug("Given Access Token URL : " + accessTokenURL);
		LOG.debug("Given Request Token : " + reqToken.toString());

		if (reqToken.getKey() == null || reqToken.getKey().length() == 0) {
			throw new SocialAuthException(
					"Key in Request Token is null or blank");
		}
		Map<String, String> params = new HashMap<String, String>();
		AccessGrant accessToken = null;
		if (reqToken.getAttribute(OAUTH_VERIFIER) != null) {
			params.put(OAUTH_VERIFIER, reqToken.getAttribute(OAUTH_VERIFIER)
					.toString());
		}
		params.put(OAUTH_TOKEN, reqToken.getKey());
		putOauthParams(params);

		String reqURL = accessTokenURL;
		String sig = generateSignature(config.get_signatureMethod(),
				config.get_transportName(), reqURL, params, reqToken);
		LOG.debug(config.get_signatureMethod()
				+ " Signature for access token : " + sig);
		params.put(OAUTH_SIGNATURE, sig);
		String body = null;
		if (MethodType.GET.toString().equals(config.get_transportName())) {
			reqURL += "?" + HttpUtil.buildParams(params);
		} else {
			body = HttpUtil.buildParams(params);
		}
		LOG.debug("Access Token URL : " + reqURL);
		Response response = null;
		try {
			response = HttpUtil.doHttpRequest(reqURL,
					config.get_transportName(), body, null);
		} catch (Exception e) {
			LOG.debug("Error while getting Access Token");
			throw new SocialAuthException("Error while getting Access Token", e);
		}

		if (response.getStatus() == 200) {
			accessToken = new AccessGrant();
			parse(response.getInputStream(), accessToken);
		} else {
			throw new SocialAuthException(
					"Unable to retrieve the access token. Status: "
							+ response.getStatus());
		}
		return accessToken;
	}

	private void putOauthParams(final Map<String, String> params) {
		params.put(OAUTH_CONSUMER_KEY, config.get_consumerKey());
		params.put(OAUTH_SIGNATURE_METHOD, config.get_signatureMethod());
		params.put(OAUTH_VERSION, CURRENT_VERSION);
		putNonceAndTimestamp(params);
	}

	/**
	 * Does an HTTP GET request.
	 * 
	 * @param reqURL
	 *            URL to send request to.
	 * @param headerParams
	 *            Header Parameters
	 * @param token
	 *            Token to pass in GET request
	 * @return Response object
	 * @throws Exception
	 */
	public Response httpGet(final String reqURL,
			final Map<String, String> headerParams, final AccessGrant token)
			throws Exception {
		return send(reqURL, null, headerParams, null,
				MethodType.GET.toString(), token, true);
	}

	/**
	 * Does an HTTP POST request.
	 * 
	 * @param reqURL
	 *            URL to send request to.
	 * @param params
	 *            Any additional parameters whose signature we want to compute.
	 * @param headerParams
	 *            Header Parameters
	 * @param body
	 *            Request Body
	 * @param token
	 *            Token to pass in POST request
	 * @return Response object
	 * @throws Exception
	 */
	public Response httpPost(final String reqURL,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body,
			final AccessGrant token) throws Exception {
		return send(reqURL, params, headerParams, body,
				MethodType.POST.toString(), token, true);
	}

	/**
	 * Does an HTTP PUT request.
	 * 
	 * @param reqURL
	 *            URL to send request to.
	 * @param params
	 *            Any additional parameters whose signature we want to compute
	 * @param headerParams
	 *            Header Parameters
	 * @param body
	 *            Request Body
	 * @param token
	 *            Token to pass in PUT request
	 * @return Response object
	 * @throws Exception
	 */
	public Response httpPut(final String reqURL,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body,
			final AccessGrant token) throws Exception {
		return send(reqURL, params, headerParams, body,
				MethodType.PUT.toString(), token, true);
	}

	/**
	 * Does an HTTP PUT request.
	 * 
	 * @param reqURL
	 *            URL to send request to.
	 * @param params
	 *            Any additional parameters whose signature we want to compute
	 * @param headerParams
	 *            Header Parameters
	 * @param body
	 *            Request Body
	 * @param token
	 *            Token to pass in PUT request
	 * @param isHeaderRequired
	 *            True if header is required
	 * @return Response object
	 * @throws Exception
	 */
	public Response httpPut(final String reqURL,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body,
			final AccessGrant token, final boolean isHeaderRequired)
			throws Exception {
		return send(reqURL, params, headerParams, body,
				MethodType.PUT.toString(), token, isHeaderRequired);
	}

	private Response send(final String reqURL,
			final Map<String, String> paramsMap,
			final Map<String, String> headerParams, final String body,
			final String methodName, final AccessGrant token,
			final boolean isHeaderRequired) throws Exception {
		Map<String, String> params;
		if (paramsMap != null) {
			params = paramsMap;
		} else {
			params = new HashMap<String, String>();
		}
		params.put(OAUTH_TOKEN, token.getKey());
		putOauthParams(params);
		String url;
		int idx = reqURL.indexOf('?');
		if (idx != -1) {
			String[] pairs = AMPERSAND.split(reqURL.substring(idx + 1));
			for (String pair : pairs) {
				int eq = pair.indexOf('=');
				if (eq == -1) {
					params.put(pair, "");
				} else {
					params.put(pair.substring(0, eq),
							HttpUtil.decodeURIComponent(pair.substring(eq + 1)));
				}
			}
			url = reqURL.substring(0, idx);
		} else {
			url = reqURL;
		}
		String sig = generateSignature(config.get_signatureMethod(),
				methodName, url, params, token);
		params.put(OAUTH_SIGNATURE, sig);
		Map<String, String> headerMap = null;
		if (isHeaderRequired) {
			String headerVal = getAuthHeaderValue(params);
			headerMap = new HashMap<String, String>();
			headerMap.put("Authorization", headerVal);
			if (headerParams != null) {
				for (String key : headerParams.keySet()) {
					headerMap.put(key, headerParams.get(key));
				}
			}
			url = reqURL;
		} else {
			url += "?" + HttpUtil.buildParams(params);
		}
		return HttpUtil.doHttpRequest(url, methodName, body, headerMap);
	}

	private void parse(final InputStream in, final AccessGrant token)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, ENCODING));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception exc) {
			throw new SocialAuthException("Failed to parse response");
		}

		String[] pairs = sb.toString().split("&");
		String key = null, secret = null;
		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			if (idx == -1) {
				continue;
			}
			String k = pair.substring(0, idx);
			String v = HttpUtil.decodeURIComponent(pair.substring(idx + 1));
			if (key == null && OAUTH_TOKEN.equals(k)) {
				key = v;
				LOG.debug("KEY : " + key);
			} else if (secret == null && OAUTH_TOKEN_SECRET.equals(k)) {
				secret = v;
				LOG.debug("SECRET : " + secret);
			} else {
				token.setAttribute(k, v);
			}
		}
		if (key != null && secret != null) {
			token.setKey(key);
			token.setSecret(secret);
		}
	}

	/**
	 * Adds nonce and timestamp in given parameter map.
	 * 
	 * @param params
	 */
	public void putNonceAndTimestamp(final Map<String, String> params) {
		long ts = System.currentTimeMillis();
		params.put(OAUTH_TIMESTAMP, String.valueOf(ts / 1000));
		params.put(OAUTH_NONCE, String.valueOf(ts));
	}

	/**
	 * Builds the auth url to redirect the user to, based from the given token
	 * and callback url.
	 * 
	 * @param authUrl
	 * @param token
	 * @param callbackUrl
	 *            Callback URL
	 * @return url with query string of parameters
	 * @throws Exception
	 */
	public StringBuilder buildAuthUrl(final String authUrl,
			final AccessGrant token, final String callbackUrl) throws Exception {
		char separator = authUrl.indexOf('?') == -1 ? '?' : '&';
		return new StringBuilder()
				.append(authUrl)
				.append(separator)
				.append(OAUTH_TOKEN)
				.append('=')
				.append(HttpUtil.encodeURIComponent(token.getKey()))
				.append('&')
				.append(OAUTH_CALLBACK)
				.append('=')
				.append(callbackUrl == null ? OOB : HttpUtil
						.encodeURIComponent(callbackUrl));
	}

	/**
	 * Generates Authorization header. The OAuth Protocol Parameters are sent in
	 * the Authorization header. Parameter names and values are encoded. For
	 * each parameter, the name is immediately followed by an '=' character
	 * (ASCII code 61), a '"' character (ASCII code 34), the parameter value
	 * (MAY be empty), and another '"' character (ASCII code 34).Parameters are
	 * separated by a comma character (ASCII code 44).
	 * 
	 * @param params
	 *            Parameters to generate header value
	 * @return Authorize header value
	 * @throws Exception
	 */
	public String getAuthHeaderValue(final Map<String, String> params)
			throws Exception {
		LOG.debug("Genrating Authorization header for given parameters : "
				+ params);
		StringBuilder headerStr = new StringBuilder();
		String[] REQUIRED_OAUTH_HEADERS_TO_SIGN = new String[] {
				OAUTH_CONSUMER_KEY, OAUTH_NONCE, OAUTH_TIMESTAMP,
				OAUTH_SIGNATURE_METHOD };
		for (String key : REQUIRED_OAUTH_HEADERS_TO_SIGN) {
			String value = HttpUtil.encodeURIComponent(params.get(key));
			headerStr.append(',').append(key).append('=').append('"')
					.append(value).append('"');
		}
		if (params.get(OAUTH_VERSION) != null) {
			headerStr
					.append(',')
					.append(OAUTH_VERSION)
					.append('=')
					.append('"')
					.append(HttpUtil.encodeURIComponent(params
							.get(OAUTH_VERSION))).append('"');
		}
		if (params.get(OAUTH_TOKEN) != null) {
			headerStr
					.append(',')
					.append(OAUTH_TOKEN)
					.append('=')
					.append('"')
					.append(HttpUtil.encodeURIComponent(params.get(OAUTH_TOKEN)))
					.append('"');
		}
		if (params.get(OAUTH_SIGNATURE) != null) {
			headerStr
					.append(',')
					.append(OAUTH_SIGNATURE)
					.append('=')
					.append('"')
					.append(HttpUtil.encodeURIComponent(params
							.get(OAUTH_SIGNATURE))).append('"');
		}
		headerStr.setCharAt(0, ' ');
		headerStr.insert(0, "OAuth");
		LOG.debug("Authorize Header : " + headerStr.toString());
		return headerStr.toString();
	}

	public OAuthConfig getConfig() {
		return config;
	}

	/**
	 * 
	 * @param reqURL
	 *            URL to send request to.
	 * @param paramsMap
	 *            Any additional parameters whose signature we want to compute.
	 * @param headerParams
	 *            Header Parameters
	 * @param inputStream
	 *            Input Stream of image
	 * @param fileParamName
	 *            Image Filename parameter. It requires in some provider.
	 * @param fileName
	 *            Image file name
	 * @param methodName
	 *            Method type
	 * @param token
	 *            Token to pass in PUT request
	 * @param isHeaderRequired
	 *            True if header is required
	 * @return Response Object
	 * @throws Exception
	 */
	public Response uploadImage(final String reqURL,
			final Map<String, String> paramsMap,
			final Map<String, String> headerParams,
			final InputStream inputStream, final String fileParamName,
			final String fileName, final String methodName,
			final AccessGrant token, final boolean isHeaderRequired)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put(OAUTH_TOKEN, token.getKey());
		putOauthParams(params);
		String url;
		int idx = reqURL.indexOf('?');
		if (idx != -1) {
			String[] pairs = AMPERSAND.split(reqURL.substring(idx + 1));
			for (String pair : pairs) {
				int eq = pair.indexOf('=');
				if (eq == -1) {
					params.put(pair, "");
				} else {
					params.put(pair.substring(0, eq),
							HttpUtil.decodeURIComponent(pair.substring(eq + 1)));
				}
			}
			url = reqURL.substring(0, idx);
		} else {
			url = reqURL;
		}
		String sig = generateSignature(config.get_signatureMethod(),
				methodName, url, params, token);
		params.put(OAUTH_SIGNATURE, sig);
		Map<String, String> headerMap = null;
		if (isHeaderRequired) {
			String headerVal = getAuthHeaderValue(params);
			headerMap = new HashMap<String, String>();
			headerMap.put("Authorization", headerVal);
			if (headerParams != null) {
				for (String key : headerParams.keySet()) {
					headerMap.put(key, headerParams.get(key));
				}
			}
			url = reqURL;
		} else {
			url += "?" + HttpUtil.buildParams(params);
		}
		return HttpUtil.doHttpRequest(reqURL, methodName, paramsMap, headerMap,
				inputStream, fileName, fileParamName);
	}
}
