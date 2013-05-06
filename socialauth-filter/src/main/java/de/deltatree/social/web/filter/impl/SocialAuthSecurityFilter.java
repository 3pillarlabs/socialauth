package de.deltatree.social.web.filter.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;

import de.deltatree.social.web.filter.api.SASFHelper;
import de.deltatree.social.web.filter.api.security.SASFSecurityException;
import de.deltatree.social.web.filter.impl.props.SASFProperties;
import de.deltatree.social.web.filter.impl.props.SASFPropertiesException;
import de.deltatree.social.web.filter.impl.security.DefaultSASFSocialAuthManager;
import de.deltatree.social.web.filter.impl.tools.InitParamUtil;

public class SocialAuthSecurityFilter implements Filter {

	private static final String VAR_PROPERTIES = "properties";
	private SASFProperties props;
	private DefaultSASFSocialAuthManager sdbSocialAuthManager;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res,
			final FilterChain fc) throws IOException, ServletException {
		try {
			doFilter((HttpServletRequest) req, (HttpServletResponse) res, fc);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public void doFilter(final HttpServletRequest req,
			final HttpServletResponse res, final FilterChain fc)
			throws Exception {
		SASFHelper h = new DefaultSASFHelper(req, this.props,
				this.sdbSocialAuthManager, req.getSession());
		String path = lookupPath(req);
		if (path != null && path.startsWith(h.getServletMain())) {
			try {
				if (path.equals(h.getServletSuccess())) {
					SocialAuthManager manager = h.getAuthManager();
					AuthProvider provider = manager.connect(SocialAuthUtil
							.getRequestParametersMap(req));
					h.setProvider(provider);
					res.sendRedirect(h.getWebappSuccessAction());
					return;
				} else {
					String id = req.getParameter("id");
					SocialAuthManager socialAuthManager = null;
					synchronized (req.getSession()) {
						if (h.getAuthManager() != null) {
							socialAuthManager = h.getAuthManager();
						} else {
							socialAuthManager = h.getMgr()
									.getSocialAuthManager();
							h.setAuthManager(socialAuthManager);
						}
					}

					res.sendRedirect(socialAuthManager.getAuthenticationUrl(id,
							h.getOpenidReturnUrl()));
					return;

				}
			} catch (Throwable t) {
				h.setError(t.getMessage(), t);
				res.sendRedirect(h.getErrorPage());
				return;
			}
		}
		if (!res.isCommitted()) {
			fc.doFilter(req, res);
		}
	}

	private String lookupPath(final HttpServletRequest req) {
		return req.getRequestURI();
	}

	@Override
	public void init(final FilterConfig config) throws ServletException {
		try {
			this.props = new SASFProperties(InitParamUtil.getInitParam(config,
					VAR_PROPERTIES));
			this.sdbSocialAuthManager = new DefaultSASFSocialAuthManager(props);
		} catch (SASFPropertiesException e) {
			throw new ServletException(e);
		} catch (SASFSecurityException e) {
			throw new ServletException(e);
		}
	}
}
