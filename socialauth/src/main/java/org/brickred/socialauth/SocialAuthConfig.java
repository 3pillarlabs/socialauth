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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.exception.SocialAuthConfigurationException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.HttpUtil;
import org.brickred.socialauth.util.OAuthConfig;

/**
 * This class is used to load the configuration for all providers. Load() method
 * is used to upload the configuration. Configuration can be loaded through
 * InputStream, Properties or from file. An instance of this class is passed to
 * SocialAuthManager for configuring providers.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class SocialAuthConfig implements Serializable {

	private static final long serialVersionUID = 1298666003842985895L;
	private static final String OAUTH_CONSUMER_PROPS = "oauth_consumer.properties";
	private Map<String, Class<?>> providersImplMap;
	private Map<String, OAuthConfig> providersConfig;
	private Properties applicationProperties;
	private Map<String, String> domainMap;
	private boolean configSetup;
	private static final Log LOG = LogFactory.getLog(SocialAuthConfig.class);
	private static SocialAuthConfig DEFAULT = new SocialAuthConfig();
	private boolean isConfigLoaded;

	/**
	 * Returns the instance of SocialAuthConfig
	 * 
	 * @return SocialAuthConfig default object
	 */
	public static SocialAuthConfig getDefault() {
		return DEFAULT;
	}

	public SocialAuthConfig() {
		providersImplMap = new HashMap<String, Class<?>>();
		providersImplMap.put(Constants.FACEBOOK,
				org.brickred.socialauth.provider.FacebookImpl.class);
		providersImplMap.put(Constants.FOURSQUARE,
				org.brickred.socialauth.provider.FourSquareImpl.class);
		providersImplMap.put(Constants.GOOGLE,
				org.brickred.socialauth.provider.GoogleImpl.class);
		providersImplMap.put(Constants.HOTMAIL,
				org.brickred.socialauth.provider.HotmailImpl.class);
		providersImplMap.put(Constants.LINKEDIN,
				org.brickred.socialauth.provider.LinkedInImpl.class);
		providersImplMap.put(Constants.MYSPACE,
				org.brickred.socialauth.provider.MySpaceImpl.class);
		providersImplMap.put(Constants.OPENID,
				org.brickred.socialauth.provider.OpenIdImpl.class);
		providersImplMap.put(Constants.TWITTER,
				org.brickred.socialauth.provider.TwitterImpl.class);
		providersImplMap.put(Constants.YAHOO,
				org.brickred.socialauth.provider.YahooImpl.class);
		providersImplMap.put(Constants.SALESFORCE,
				org.brickred.socialauth.provider.SalesForceImpl.class);
		providersImplMap.put(Constants.YAMMER,
				org.brickred.socialauth.provider.YammerImpl.class);
		providersImplMap.put(Constants.MENDELEY,
				org.brickred.socialauth.provider.MendeleyImpl.class);
		providersImplMap.put(Constants.RUNKEEPER,
				org.brickred.socialauth.provider.RunkeeperImpl.class);
		providersImplMap.put(Constants.GOOGLE_PLUS,
				org.brickred.socialauth.provider.GooglePlusImpl.class);
		providersImplMap.put(Constants.INSTAGRAM,
				org.brickred.socialauth.provider.InstagramImpl.class);
		providersImplMap.put(Constants.GITHUB,
				org.brickred.socialauth.provider.GitHubImpl.class);
		providersImplMap.put(Constants.FLICKR,
				org.brickred.socialauth.provider.FlickerImpl.class);
		providersImplMap.put(Constants.NIMBLE,
				org.brickred.socialauth.provider.NimbleImpl.class);

		domainMap = new HashMap<String, String>();
		domainMap.put(Constants.GOOGLE, "www.google.com");
		domainMap.put(Constants.YAHOO, "api.login.yahoo.com");
		domainMap.put(Constants.TWITTER, "twitter.com");
		domainMap.put(Constants.FACEBOOK, "graph.facebook.com");
		domainMap.put(Constants.HOTMAIL, "consent.live.com");
		domainMap.put(Constants.LINKEDIN, "api.linkedin.com");
		domainMap.put(Constants.FOURSQUARE, "foursquare.com");
		domainMap.put(Constants.MYSPACE, "api.myspace.com");
		domainMap.put(Constants.SALESFORCE, "login.salesforce.com");
		domainMap.put(Constants.YAMMER, "www.yammer.com");
		domainMap.put(Constants.MENDELEY, "api.mendeley.com");
		domainMap.put(Constants.RUNKEEPER, "runkeeper.com");
		domainMap.put(Constants.GOOGLE_PLUS, "googleapis.com");
		domainMap.put(Constants.INSTAGRAM, "api.instagram.com");
		domainMap.put(Constants.GITHUB, "api.github.com");
		domainMap.put(Constants.FLICKR, "www.flickr.com");
		domainMap.put(Constants.NIMBLE, "api.nimble.com");

		providersConfig = new HashMap<String, OAuthConfig>();

		OAuthConfig c = new OAuthConfig("openid", "openid");
		c.setProviderImplClass(org.brickred.socialauth.provider.OpenIdImpl.class);
		providersConfig.put(Constants.OPENID, c);

	}

	private void registerProviders() throws Exception {
		for (Object key : applicationProperties.keySet()) {
			String str = key.toString();
			if (str.startsWith("socialauth.")) {
				String val = str.substring("socialauth.".length());
				providersImplMap.put(val, Class.forName(applicationProperties
						.get(str).toString()));
				domainMap.put(val, val);
			}
		}
	}

	/**
	 * Registers a new provider implementation.
	 * 
	 * @param pname
	 *            provider name or id
	 * @param clazz
	 *            class name of the provider implementation.
	 */
	public void addProvider(final String pname, final Class<?> clazz)
			throws Exception {
		LOG.debug("Registering a provider " + pname);
		providersImplMap.put(pname, clazz);
	}

	/**
	 * Returns the application configuration properties
	 * 
	 * @return the application configuration properties
	 */
	public Properties getApplicationProperties() {
		return applicationProperties;
	}

	/**
	 * Setter for application configuration properties.
	 * 
	 * @param applicationProperties
	 *            the application configuration properties
	 */
	public void setApplicationProperties(final Properties applicationProperties)
			throws Exception {
		LOG.info("Loading application properties");
		this.applicationProperties = applicationProperties;
		load(this.applicationProperties);
	}

	/**
	 * Loads the application configuration from the given input stream Format of
	 * the input stream should be as follows: <br/>
	 * www.google.com.consumer_key = opensource.brickred.com
	 * 
	 * @param inputStream
	 *            property file input stream which contains the configuration.
	 * @throws Exception
	 */
	public void load(final InputStream inputStream) throws Exception {
		if (!isConfigLoaded) {
			LOG.debug("Loading application configuration through input stream.");
			Properties props = new Properties();
			try {
				props.load(inputStream);
				load(props);
			} catch (IOException ie) {
				throw new IOException(
						"Could not load configuration from input stream");
			}
		}
	}

	/**
	 * Loads the application configuration from the given properties
	 * 
	 * @param properties
	 *            application configuration properties
	 * @throws Exception
	 */
	public void load(final Properties properties) throws Exception {
		if (!isConfigLoaded) {
			LOG.info("Loading application configuration");
			LOG.debug("Loading application configuration through properties. Given properties are :"
					+ properties);
			this.applicationProperties = properties;
			registerProviders();
			loadProvidersConfig();
			setProxy();
			String timeout = null;
			if (applicationProperties
					.containsKey(Constants.HTTP_CONNECTION_TIMEOUT)) {
				timeout = applicationProperties.getProperty(
						Constants.HTTP_CONNECTION_TIMEOUT).trim();
			}
			if (timeout != null && !timeout.isEmpty()) {
				int time = 0;
				try {
					time = Integer.parseInt(timeout);
				} catch (NumberFormatException ne) {
					LOG.warn("Http connection timout is not an integer in configuration");
				}
				HttpUtil.setConnectionTimeout(time);
			}
			isConfigLoaded = true;
		}
	}

	/**
	 * Loads the application configuration from the given file
	 * 
	 * @param fileName
	 *            the file name which contains the application configuration
	 *            properties
	 * @throws Exception
	 */
	public void load(final String fileName) throws Exception {
		if (!isConfigLoaded) {
			LOG.debug("Loading application configuration from file " + fileName);
			ClassLoader loader = SocialAuthConfig.class.getClassLoader();
			try {
				InputStream in = loader.getResourceAsStream(fileName);
				load(in);
			} catch (NullPointerException ne) {
				throw new FileNotFoundException(fileName
						+ " file is not found in your class path");
			}
		}
	}

	/**
	 * Loads the application properties from oauth_consumer.properties file.
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
		if (!isConfigLoaded) {
			LOG.debug("Loading application configuration from file "
					+ OAUTH_CONSUMER_PROPS);
			load(OAUTH_CONSUMER_PROPS);
		}
	}

	/**
	 * Updates the provider specific configuration.
	 * 
	 * @param providerId
	 *            the provider id for which configuration need to be add or
	 *            update
	 * @param config
	 *            the OAuthConfig object which contains the configuration.
	 */
	public void addProviderConfig(final String providerId,
			final OAuthConfig config) throws Exception {
		config.setId(providerId);
		LOG.debug("Adding provider configuration :" + config);
		providersConfig.put(providerId, config);
		if (config.getProviderImplClass() != null) {
			providersImplMap.put(providerId, config.getProviderImplClass());
			domainMap.put(providerId, providerId);
		}
		if (!providersImplMap.containsKey(providerId)) {
			throw new SocialAuthException("Provider Impl class not found");
		} else if (config.getProviderImplClass() == null) {
			config.setProviderImplClass(providersImplMap.get(providerId));
		}
		configSetup = true;
	}

	private void loadProvidersConfig() {
		for (Map.Entry<String, String> entry : domainMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String cKey = applicationProperties.getProperty(value
					+ ".consumer_key");
			String cSecret = applicationProperties.getProperty(value
					+ ".consumer_secret");
			if (cKey != null && cSecret != null) {
				cKey = cKey.trim();
				cSecret = cSecret.trim();
				LOG.debug("Loading configuration for provider : " + key);
				OAuthConfig conf = new OAuthConfig(cKey, cSecret);
				conf.setId(key);
				conf.setProviderImplClass(providersImplMap.get(key));
				if (applicationProperties.containsKey(value
						+ ".custom_permissions")) {
					String perms = applicationProperties.getProperty(
							value + ".custom_permissions").trim();
					if (perms.length() > 0) {
						conf.setCustomPermissions(perms);
					}
				}
				if (applicationProperties.containsKey(value
						+ ".request_token_url")) {
					String reqUrl = applicationProperties.getProperty(
							value + ".request_token_url").trim();
					if (reqUrl.length() > 0) {
						conf.setRequestTokenUrl(reqUrl.trim());
					}
				}
				if (applicationProperties.containsKey(value
						+ ".authentication_url")) {
					String authUrl = applicationProperties.getProperty(
							value + ".authentication_url").trim();
					if (authUrl.length() > 0) {
						conf.setAuthenticationUrl(authUrl.trim());
					}
				}
				if (applicationProperties.containsKey(value
						+ ".access_token_url")) {
					String tokenUrl = applicationProperties.getProperty(
							value + ".access_token_url").trim();
					if (tokenUrl.length() > 0) {
						conf.setAccessTokenUrl(tokenUrl.trim());
					}
				}
				if (applicationProperties.containsKey(value + ".plugins")) {
					String pluginsStr = applicationProperties.getProperty(
							value + ".plugins").trim();
					if (pluginsStr.length() > 0) {
						String plugins[] = pluginsStr.split(",");
						if (plugins.length > 0) {
							List<String> pluginScopes = new ArrayList<String>();
							conf.setRegisteredPlugins(plugins);
							for (String plugin : plugins) {
								if (applicationProperties.containsKey(plugin
										+ ".scope")) {
									String pscope = applicationProperties
											.getProperty(plugin + ".scope")
											.trim();
									if (pscope.length() > 0) {
										String sarr[] = pscope.split(",");
										pluginScopes
												.addAll(Arrays.asList(sarr));
									}
								}
							}
							if (!pluginScopes.isEmpty()) {
								conf.setPluginsScopes(pluginScopes);
							}
						}
					}
				}
				providersConfig.put(key, conf);
			} else {
				LOG.debug("Configuration for provider " + key
						+ " is not available");
			}
		}
		configSetup = true;
	}

	/**
	 * Retrieves the configuration of given provider
	 * 
	 * @param id
	 *            the provider id
	 * @return the configuration of given provider
	 * @throws SocialAuthException
	 * @throws SocialAuthConfigurationException
	 */
	public OAuthConfig getProviderConfig(final String id)
			throws SocialAuthException, SocialAuthConfigurationException {
		OAuthConfig config = providersConfig.get(id);
		if (config == null) {
			try {
				new URL(id);
				config = getProviderConfig(Constants.OPENID);
				if (config != null) {
					config.setId(id);
				}
			} catch (MalformedURLException me) {
				throw new SocialAuthException(id
						+ " is not a provider or valid OpenId URL");
			}
		}
		if (config == null) {
			throw new SocialAuthConfigurationException("Configuration of " + id
					+ " provider is not found");
		}

		if (config.get_consumerSecret().length() <= 0) {
			throw new SocialAuthConfigurationException(id
					+ " consumer_secret value is null");
		}
		if (config.get_consumerKey().length() <= 0) {
			throw new SocialAuthConfigurationException(id
					+ " consumer_key value is null");
		}
		return config;
	}

	protected boolean isConfigSetup() {
		return configSetup;
	}

	private void setProxy() {
		String proxyHost = null;
		String proxyPort = null;
		if (applicationProperties.containsKey(Constants.PROXY_HOST)) {
			proxyHost = applicationProperties.getProperty(Constants.PROXY_HOST)
					.trim();
		}
		if (applicationProperties.containsKey(Constants.PROXY_PORT)) {
			proxyPort = applicationProperties.getProperty(Constants.PROXY_PORT)
					.trim();
		}
		if (proxyHost != null && !proxyHost.isEmpty()) {
			int port = 0;
			if (proxyPort != null && !proxyPort.isEmpty()) {
				try {
					port = Integer.parseInt(proxyPort);
				} catch (NumberFormatException ne) {
					LOG.warn("Proxy port is not an integer in configuration");
				}
			}
			HttpUtil.setProxyConfig(proxyHost, port);
		}
	}

}
