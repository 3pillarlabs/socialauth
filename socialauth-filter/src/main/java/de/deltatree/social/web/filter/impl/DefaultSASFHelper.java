package de.deltatree.social.web.filter.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;

import de.deltatree.social.web.filter.api.SASFHelper;
import de.deltatree.social.web.filter.api.security.SASFSecurityException;
import de.deltatree.social.web.filter.api.security.SASFSocialAuthManager;
import de.deltatree.social.web.filter.impl.props.SASFProperties;

public class DefaultSASFHelper implements SASFHelper, Serializable {

	private final static long serialVersionUID = 2188168359053036053L;

	private final static String SESSION_KEY = "S_SASFHelper";
	private final static String SESSION_SOCIAL_AUTH_PROVIDER = "SESSION_SOCIAL_AUTH_PROVIDER";
	private final static String SESSION_SOCIAL_AUTH_MANAGER = "SESSION_SOCIAL_AUTH_MANAGER";
	private final static String SESSION_ERROR = "S_SASFError";
	private final static String SESSION_ERROR_CAUSE = "S_SASFErrorCause";
	private final SASFSocialAuthManager sdbSocialAuthManager;
	private final HttpServletRequest request;
	private final HttpSession session;
	private final SASFProperties props;

	public DefaultSASFHelper(final HttpServletRequest req,
			final SASFProperties props,
			final SASFSocialAuthManager sdbSocialAuthManager,
			final HttpSession session) throws SASFSecurityException {
		this.request = req;
		this.props = props;
		this.sdbSocialAuthManager = sdbSocialAuthManager;
		this.session = session;
		setSessionKey();
	}

	private void setSessionKey() {
		this.session.setAttribute(SESSION_KEY, this);
	}

	@Override
	public SASFSocialAuthManager getMgr() {
		return sdbSocialAuthManager;
	}

	@Override
	public void setError(final String message, final Throwable cause) {
		this.session.setAttribute(SESSION_ERROR, message);
		this.session.setAttribute(SESSION_ERROR_CAUSE, cause);
	}

	@Override
	public String getError() {
		return (String) this.getSession().getAttribute(SESSION_ERROR);
	}

	@Override
	public Throwable getErrorCause() {
		return (Throwable) this.getSession().getAttribute(SESSION_ERROR_CAUSE);
	}

	@Override
	public String getErrorCauseAsString() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Throwable errorCause = getErrorCause();
		if (errorCause != null) {
			errorCause.printStackTrace(new PrintStream(bos));
		}
		return bos.toString();
	}

	@Override
	public void setProvider(final AuthProvider provider) {
		this.session.setAttribute(
				DefaultSASFHelper.SESSION_SOCIAL_AUTH_PROVIDER, provider);
	}

	@Override
	public AuthProvider getProvider() {
		return (AuthProvider) this.getSession().getAttribute(
				DefaultSASFHelper.SESSION_SOCIAL_AUTH_PROVIDER);
	}

	@Override
	public void setAuthManager(final SocialAuthManager socialAuthManager) {
		this.session.setAttribute(
				DefaultSASFHelper.SESSION_SOCIAL_AUTH_MANAGER,
				socialAuthManager);
	}

	@Override
	public SocialAuthManager getAuthManager() {
		return (SocialAuthManager) this.getSession().getAttribute(
				DefaultSASFHelper.SESSION_SOCIAL_AUTH_MANAGER);
	}

	@Override
	public String getServletMain() {
		return this.request.getContextPath() + props.getServletMain();
	}

	@Override
	public String getServletSuccess() {
		return this.request.getContextPath() + props.getServletMainSuccess();
	}

	@Override
	public String getOpenidReturnUrl() {
		String returnUrl = getURLWithContextPath()
				+ this.props.getOpenidReturnUrl();
		return returnUrl;
	}

	private String getURLWithContextPath() {
		StringBuffer sb = new StringBuffer();
		String protocol = request.getScheme();
		String host = request.getServerName();
		int port = request.getServerPort();
		String context = request.getContextPath();
		sb.append(protocol);
		sb.append("://");
		sb.append(host);
		if (port > 0) {
			if (!(protocol.equals("http") && port == 80)
					&& !(protocol.equals("https") && port == 443)) {
				sb.append(":");
				sb.append(port);
			}
		}
		sb.append(context);
		return sb.toString();

	}

	@Override
	public String getWebappSuccessAction() {
		return this.request.getContextPath() + props.webappSuccessAction();
	}

	@Override
	public String getServletLogoff() {
		return this.request.getContextPath() + props.getServletMainLogoff();
	}

	@Override
	public String getErrorPage() {
		return this.request.getContextPath() + props.getErrorPage();
	}

	@Override
	public SASFProperties getProps() {
		return props;
	}

	@Override
	public Profile getProfile() {
		Profile profile = null;
		SocialAuthManager manager = getAuthManager();
		if (manager != null) {
			try {
				AuthProvider provider = manager.getCurrentAuthProvider();
				profile = provider.getUserProfile();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return profile;
	}

	@Override
	public List<Contact> getContactList() {
		List<Contact> contactsList = null;
		SocialAuthManager manager = getAuthManager();
		if (manager != null) {
			contactsList = new ArrayList<Contact>();
			try {
				AuthProvider provider = manager.getCurrentAuthProvider();
				contactsList = provider.getContactList();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return contactsList;
	}

	public HttpSession getSession() {
		return session;
	}
}