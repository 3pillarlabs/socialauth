package de.deltatree.social.web.filter.api.security;

import org.brickred.socialauth.SocialAuthManager;

public interface SASFSocialAuthManager {

	SocialAuthManager getSocialAuthManager() throws SASFSecurityException;

}
