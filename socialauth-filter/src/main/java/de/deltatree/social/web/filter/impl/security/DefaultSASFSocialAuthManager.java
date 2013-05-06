package de.deltatree.social.web.filter.impl.security;

import java.io.InputStream;

import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;

import de.deltatree.social.web.filter.api.security.SASFSecurityException;
import de.deltatree.social.web.filter.api.security.SASFSocialAuthManager;
import de.deltatree.social.web.filter.impl.props.SASFProperties;

public class DefaultSASFSocialAuthManager implements SASFSocialAuthManager {

	private SocialAuthConfig socialAuthConfig;

	public DefaultSASFSocialAuthManager(final SASFProperties props)
			throws SASFSecurityException {
		SocialAuthConfig config = SocialAuthConfig.getDefault();
		InputStream in = null;
		try {
			in = DefaultSASFSocialAuthManager.class.getClassLoader()
					.getResourceAsStream(props.getOauthPropertiesFileString());
			config.load(in);
			this.socialAuthConfig = config;
		} catch (Exception e) {
			throw new SASFSecurityException(e);
		}
	}

	/**
	 * gets thread safe social auth manager - current solution: each call
	 * produces a new instance
	 */
	@Override
	public SocialAuthManager getSocialAuthManager()
			throws SASFSecurityException {
		try {
			SocialAuthManager socialAuthManager = new SocialAuthManager();
			socialAuthManager.setSocialAuthConfig(this.socialAuthConfig);
			return socialAuthManager;
		} catch (Exception e) {
			throw new SASFSecurityException(e);
		}
	}

}
