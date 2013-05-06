package de.deltatree.social.web.filter.api;

import java.util.List;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;

import de.deltatree.social.web.filter.api.security.SASFSocialAuthManager;
import de.deltatree.social.web.filter.impl.props.SASFProperties;

public interface SASFHelper {
	public static final String SESSION_KEY = "S_SASFHelper";

	public SASFSocialAuthManager getMgr();

	public void setError(String message, Throwable cause);

	public String getError();

	public String getErrorPage();

	public Throwable getErrorCause();

	public String getErrorCauseAsString();

	public void setProvider(AuthProvider provider);

	public AuthProvider getProvider();
	
	public Profile getProfile();
	
	public List<Contact> getContactList();

	public void setAuthManager(SocialAuthManager socialAuthManager);

	public SocialAuthManager getAuthManager();

	public String getServletMain();

	public String getServletSuccess();

	public String getServletLogoff();

	public String getOpenidReturnUrl();

	public String getWebappSuccessAction();

	public SASFProperties getProps();
}
