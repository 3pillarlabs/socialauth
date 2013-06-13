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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.brickred.socialauth.SocialAuthManager;

import de.deltatree.social.web.filter.api.SASFHelper;
import de.deltatree.social.web.filter.api.SASFStaticHelper;

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
		@Result(name = "forward", location = "${url}", type = "redirect") })
public class SocialAuthenticationAction implements ServletRequestAware {

	final Log LOG = LogFactory.getLog(SocialAuthenticationAction.class);

	private String id;
	private String mode;
	private String url;
	private HttpServletRequest request;

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

		SASFHelper helper = SASFStaticHelper.getHelper(request);
		if (mode == null) {
			url = "http://opensource.brickred.com/socialauth-struts-filter-demo/SAF/SocialAuth?id="
					+ id;
			return "forward";

		} else if ("signout".equals(mode)) {
			SocialAuthManager manager = null;
			if (helper != null) {
				manager = helper.getAuthManager();
				if (manager != null) {
					manager.disconnectProvider(id);
				}
			}
			return "home";
		}
		return "home";

	}

	@Override
	public void setServletRequest(final HttpServletRequest request) {
		this.request = request;
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
