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

package org.brickred.socialauth;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.exception.AccessTokenExpireException;
import org.brickred.socialauth.exception.SocialAuthConfigurationException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.SocialAuthManagerStateException;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.OAuthConfig;

/**
 * This class manages the Map of all the connected providers by using this
 * manager.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class SocialAuthManager implements Serializable {

	private static final long serialVersionUID = 1620459182486095613L;
	private final Log LOG = LogFactory.getLog(SocialAuthManager.class);
	private AuthProvider authProvider;
	private String providerId;
	private String currentProviderId;
	private final Map<String, AuthProvider> providersMap;
	private SocialAuthConfig socialAuthConfig;
	private final Map<String, Permission> permissionsMap;

	public SocialAuthManager() {
		providersMap = new HashMap<String, AuthProvider>();
		permissionsMap = new HashMap<String, Permission>();
	}

	/**
	 * Retrieves the socialauth config
	 * 
	 * @return the socialauth config
	 */
	public SocialAuthConfig getSocialAuthConfig() {
		return socialAuthConfig;
	}

	/**
	 * Updates the socialauth config
	 * 
	 * @param socialAuthConfig
	 *            the SocialAuthConfig object which contains the configuration
	 *            for providers
	 * @throws Exception
	 */
	public void setSocialAuthConfig(final SocialAuthConfig socialAuthConfig)
			throws Exception {
		LOG.debug("Setting socialauth config");
		if (socialAuthConfig == null) {
			throw new SocialAuthConfigurationException(
					"SocialAuthConfig is null");
		} else {
			if (!socialAuthConfig.isConfigSetup()) {
				throw new SocialAuthConfigurationException(
						"Configuration is not provided. Call load() method of SocialAuthConfig class to set up configuration");
			}
		}
		this.socialAuthConfig = socialAuthConfig;
	}

	/**
	 * This is the most important action. It provides the URL which will be used
	 * for authentication with the provider
	 * 
	 * @param id
	 *            the provider id
	 * @param successUrl
	 *            success page URL on which provider will redirect after
	 *            authentication
	 * @return the URL string which will be used for authentication with
	 *         provider
	 * @throws Exception
	 */
	public String getAuthenticationUrl(final String id, final String successUrl)
			throws Exception {
		LOG.debug("Getting Authentication URL for provider " + id
				+ ", with success url : " + successUrl);
		return getAuthURL(id, successUrl, null);
	}

	/**
	 * This is the most important action. It provides the URL which will be used
	 * for authentication with the provider
	 * 
	 * @param id
	 *            the provider id
	 * @param successUrl
	 *            success page URL on which provider will redirect after
	 *            authentication
	 * @param permission
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 * @return the URL string which will be used for authentication with
	 *         provider
	 * @throws Exception
	 */
	public String getAuthenticationUrl(final String id,
			final String successUrl, final Permission permission)
			throws Exception {
		LOG.debug("Getting Authentication URL for provider " + id
				+ ", with success url : " + successUrl);
		return getAuthURL(id, successUrl, permission);
	}

	private String getAuthURL(final String id, final String successUrl,
			final Permission permission) throws Exception {
		String url;
		providerId = id;
		if (socialAuthConfig == null) {
			throw new SocialAuthConfigurationException(
					"SocialAuth configuration is null.");
		}
		if (providersMap.get(id) != null) {
			url = successUrl;
			authProvider = providersMap.get(id);
		} else {
			authProvider = getProviderInstance(id);
			if (permissionsMap.get(id) != null) {
				authProvider.setPermission(permissionsMap.get(id));
			}
			if (permission != null) {
				authProvider.setPermission(permission);
			}
			url = authProvider.getLoginRedirectURL(successUrl);
		}
		return url;
	}

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * @param requestParams
	 *            the request parameters
	 * @return object of the required auth provider. You can call various
	 *         function of this provider to get the information.
	 * @throws Exception
	 */
	public AuthProvider connect(final Map<String, String> requestParams)
			throws Exception {
		if (providerId == null || authProvider == null) {
			throw new SocialAuthManagerStateException();
		}
		LOG.info("Connecting provider : " + providerId);
		if (providersMap.get(providerId) == null) {
			authProvider.verifyResponse(requestParams);
			providersMap.put(providerId, authProvider);
		}
		currentProviderId = providerId;
		providerId = null;
		return authProvider;
	}

	/**
	 * Generates access token and creates a object of AccessGrant
	 * 
	 * @param providerId
	 *            the provider id
	 * @param authCode
	 *            auth code for generating access token
	 * @param redirectURL
	 *            return url which is given while registering app with provider
	 *            to generate client id and secret
	 * @return the AccessGrant object
	 * @throws Exception
	 */
	public AccessGrant createAccessGrant(final String providerId,
			final String authCode, final String redirectURL) throws Exception {
		this.providerId = providerId;
		if (socialAuthConfig == null) {
			throw new SocialAuthConfigurationException(
					"SocialAuth configuration is null.");
		}
		getAuthenticationUrl(providerId, redirectURL);
		if (providersMap.get(providerId) != null) {
			authProvider = providersMap.get(providerId);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("code", authCode);
		connect(map);
		LOG.debug("Access Grant Object :: " + authProvider.getAccessGrant());
		return authProvider.getAccessGrant();
	}

	public AccessGrant createAccessGrant(final String providerId,
			final Map<String, String> params, final String redirectURL)
			throws Exception {
		this.providerId = providerId;
		if (socialAuthConfig == null) {
			throw new SocialAuthConfigurationException(
					"SocialAuth configuration is null.");
		}
		
		if (providersMap.get(providerId) != null) {
			authProvider = providersMap.get(providerId);
		}
		connect(params);
		LOG.debug("Access Grant Object :: " + authProvider.getAccessGrant());
		return authProvider.getAccessGrant();
	}

	/**
	 * It disconnects with provider
	 * 
	 * @param id
	 *            the provider id
	 * @return True if provider is disconnected or false if not.
	 */
	public boolean disconnectProvider(final String id) {
		if (providersMap.get(id) != null) {
			AuthProvider p = providersMap.get(id);
			p.logout();
			providersMap.remove(id);
			return true;
		}
		return false;

	}

	/**
	 * Creates the provider with given access grant
	 * 
	 * @param accessGrant
	 *            the access grant object which contains
	 * @return the AuthProvider
	 * @throws Exception
	 */
	public AuthProvider connect(final AccessGrant accessGrant)
			throws SocialAuthConfigurationException,
			AccessTokenExpireException, SocialAuthException {
		if (accessGrant.getProviderId() == null || accessGrant.getKey() == null) {
			throw new SocialAuthException("access grant is not valid");
		}
		LOG.debug("Connecting provider : " + accessGrant.getProviderId()
				+ ", from given access grant");
		AuthProvider provider = getProviderInstance(accessGrant.getProviderId());
		provider.setAccessGrant(accessGrant);
		authProvider = provider;
		currentProviderId = accessGrant.getProviderId();
		providersMap.put(currentProviderId, authProvider);
		return provider;
	}

	/**
	 * Makes a call for a provider to get RefreshToken and returns object of
	 * that provider
	 * 
	 * @param accessGrant
	 *            AccessGrant object which contains access token
	 * @return the provider object
	 * @throws SocialAuthConfigurationException
	 * @throws SocialAuthException
	 */
	public AuthProvider refreshToken(final AccessGrant accessGrant)
			throws SocialAuthConfigurationException, SocialAuthException {
		if (accessGrant.getProviderId() == null || accessGrant.getKey() == null) {
			throw new SocialAuthException("access grant is not valid");
		}
		LOG.debug("Connecting provider : " + accessGrant.getProviderId()
				+ ", from given access grant");
		AuthProvider provider = getProviderInstance(accessGrant.getProviderId());
		provider.refreshToken(accessGrant);
		authProvider = provider;
		currentProviderId = accessGrant.getProviderId();
		providersMap.put(currentProviderId, authProvider);
		return provider;
	}

	/**
	 * Returns True if given provider is connected otherwise returns False
	 * 
	 * @param providerId
	 *            the provider id
	 * @return provider connected status
	 */
	public boolean isConnected(final String providerId) {
		if (providersMap.containsKey(providerId)) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieves the instance of given provider
	 * 
	 * @param providerId
	 *            the provider id
	 * @return the instance of given provider
	 */
	public AuthProvider getProvider(final String providerId) {
		return providersMap.get(providerId);
	}

	private AuthProvider getProviderInstance(final String id)
			throws SocialAuthConfigurationException, SocialAuthException {
		OAuthConfig config = socialAuthConfig.getProviderConfig(id);
		Class<?> obj = config.getProviderImplClass();
		AuthProvider provider;
		try {
			Constructor<?> cons = obj.getConstructor(OAuthConfig.class);
			provider = (AuthProvider) cons.newInstance(config);
		} catch (NoSuchMethodException me) {
			LOG.warn(obj.getName() + " does not implement a constructor "
					+ obj.getName() + "(Poperties props)");
			try {
				provider = (AuthProvider) obj.newInstance();
			} catch (Exception e) {
				throw new SocialAuthConfigurationException(e);
			}
		} catch (Exception e) {
			throw new SocialAuthConfigurationException(e);
		}
		try {
			provider.registerPlugins();
		} catch (Exception e) {
			throw new SocialAuthConfigurationException(e);
		}

		return provider;
	}

	/**
	 * Returns the array list of connected providers ids.
	 * 
	 * @return List of connected providers ids string.
	 */
	public List<String> getConnectedProvidersIds() {
		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, AuthProvider> entry : providersMap.entrySet()) {
			list.add(entry.getKey());
		}
		return list;
	}

	/**
	 * Retrieves the current auth provider instance which is last connected.
	 * 
	 * @return AuthProvider object
	 */
	public AuthProvider getCurrentAuthProvider() {
		if (currentProviderId != null) {
			return providersMap.get(currentProviderId);
		}
		return null;
	}

	/**
	 * Sets the permission for given provider.
	 * 
	 * @param providerId
	 *            the provider id for which permission need to be set
	 * @param permission
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	public void setPermission(final String providerId,
			final Permission permission) {
		permissionsMap.put(providerId, permission);
	}

}
