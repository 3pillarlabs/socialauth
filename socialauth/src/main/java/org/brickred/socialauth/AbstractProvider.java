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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.plugin.Plugin;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.ProviderSupport;

/**
 * It implements AuthProvider interface and provides some methods for
 * registering and getting plugins.
 * 
 * @author tarunn@brickred.com
 * 
 */
public abstract class AbstractProvider implements AuthProvider, Serializable {

	private static final long serialVersionUID = -7827145708317886744L;

	private Map<Class<? extends Plugin>, Class<? extends Plugin>> pluginsMap;

	private final Log LOG = LogFactory.getLog(this.getClass());

	public AbstractProvider() throws Exception {
		pluginsMap = new HashMap<Class<? extends Plugin>, Class<? extends Plugin>>();
	}

	@Override
	public <T> T getPlugin(final Class<T> clazz) throws Exception {
		Class<? extends Plugin> plugin = pluginsMap.get(clazz);
		Constructor<? extends Plugin> cons = plugin
				.getConstructor(ProviderSupport.class);
		ProviderSupport support = new ProviderSupport(getOauthStrategy());
		Plugin obj = cons.newInstance(support);
		return (T) obj;
	}

	@Override
	public boolean isSupportedPlugin(final Class<? extends Plugin> clazz) {
		if (pluginsMap.containsKey(clazz)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public final void registerPlugins() throws Exception {
		LOG.info("Loading plugins");
		List<String> pluginsList = getPluginsList();
		if (pluginsList != null && !pluginsList.isEmpty()) {
			for (String s : pluginsList) {
				LOG.info("Loading plugin :: " + s);
				Class<? extends Plugin> clazz = Class.forName(s).asSubclass(
						Plugin.class);
				// getting constructor only for checking
				Constructor<? extends Plugin> cons = clazz
						.getConstructor(ProviderSupport.class);
				Class<?> interfaces[] = clazz.getInterfaces();
				for (Class<?> c : interfaces) {
					if (Plugin.class.isAssignableFrom(c)) {
						pluginsMap.put(c.asSubclass(Plugin.class), clazz);
					}
				}
			}
		}
	}

	@Override
	public void refreshToken(AccessGrant accessGrant)
			throws SocialAuthException {
		throw new SocialAuthException("Not implemented for given provider");

	}

	/**
	 * Returns the scopes of custom plugins of a provider those are configured
	 * in properties file
	 * 
	 * @param oauthConfig
	 *            OAuthConfig object of that provider
	 * @return String of comma separated scopes of all register plugins of a
	 *         provider those are configured in properties file
	 */
	public String getPluginsScope(final OAuthConfig oauthConfig) {
		List<String> scopes = oauthConfig.getPluginsScopes();
		if (scopes != null && !scopes.isEmpty()) {
			String scopesStr = scopes.get(0);
			for (int i = 1; i < scopes.size(); i++) {
				scopesStr += "," + scopes.get(i);
			}
			return scopesStr;
		}
		return null;
	}

	/**
	 * Returns the list of plugins of a provider.
	 * 
	 * @return List of plugins of a provider
	 */
	protected abstract List<String> getPluginsList();

	/**
	 * Returns the OAuthStrategyBase of a provider.
	 * 
	 * @return OAuthStrategyBase of a provider.
	 */
	protected abstract OAuthStrategyBase getOauthStrategy();
}
