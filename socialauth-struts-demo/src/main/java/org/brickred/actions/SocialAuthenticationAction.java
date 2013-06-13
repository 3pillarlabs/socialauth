/*
 ===========================================================================
 Copyright (c) 2013 3PillarGlobal

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

package org.brickred.actions;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.RequestUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.views.util.UrlHelper;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;

/**
 * 
 * It redirects the browser to an appropriate URL which will be used for
 * authentication with the provider that has been set by clicking the icon. It
 * creates an instance of the requested provider from AuthProviderFactory and
 * calls the getLoginRedirectURL() method to find the URL which the user should
 * be redirect to.
 * 
 * @author tarun.nagpal
 * 
 */
@Results({ @Result(name = "home", location = "/index.jsp"),
		@Result(name = "failure", location = "/jsp/error.jsp"),
		@Result(name = "redirect", location = "${url}", type = "redirect") })
public class SocialAuthenticationAction implements SessionAware,
		ServletRequestAware, ServletResponseAware {

	final Log LOG = LogFactory.getLog(SocialAuthenticationAction.class);

	private Map<String, Object> userSession;
	private String id;
	private String mode;
	private String url;
	private HttpServletRequest request;
	private HttpServletResponse response;

	/**
	 * creates a instance of the requested provider from AuthProviderFactory and
	 * calls the getLoginRedirectURL() method to find the URL which the user
	 * should be redirect to.
	 * 
	 * @return String where the action should flow
	 * @throws Exception
	 *             if an error occurs
	 */

	@Action(value = "/socialAuth")
	public String execute() throws Exception {
		LOG.info("Given provider id :: " + id);
		SocialAuthManager manager;
		if (userSession.get("socialAuthManager") != null) {
			manager = (SocialAuthManager) userSession.get("socialAuthManager");
			if ("signout".equals(mode)) {
				manager.disconnectProvider(id);
				return "home";
			}
		} else {
			InputStream in = SocialAuthenticationAction.class.getClassLoader()
					.getResourceAsStream("oauth_consumer.properties");
			SocialAuthConfig conf = SocialAuthConfig.getDefault();
			conf.load(in);
			manager = new SocialAuthManager();
			manager.setSocialAuthConfig(conf);
			userSession.put("socialAuthManager", manager);
		}

		String returnToUrl = RequestUtils.getServletPath(request);
		System.out.println(returnToUrl);
		// returnToUrl =
		// "http://opensource.brickred.com/socialauth-struts-demo/socialAuthSuccessAction.do";
		returnToUrl = UrlHelper.buildUrl("socialAuthSuccessAction.do", request,
				response, null, null, true, true, true);
		url = manager.getAuthenticationUrl(id, returnToUrl);
		LOG.info("Redirecting to: " + url);
		if (url != null) {
			return "redirect";
		}
		return "failure";
	}

	@Override
	public void setSession(final Map<String, Object> session) {
		userSession = session;
	}

	@Override
	public void setServletRequest(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public void setServletResponse(final HttpServletResponse response) {
		this.response = response;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(final String mode) {
		this.mode = mode;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

}
