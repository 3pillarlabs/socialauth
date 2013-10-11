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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.SessionAware;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;

/**
 * Verifies the user when the external provider redirects back to our
 * application. It gets the instance of the requested provider from session and
 * calls verifyResponse() method which verifies the user and returns profile
 * information. After verification we call the getContactList() method to get
 * the contacts.
 * 
 * @author tarun.nagpal
 * 
 */
@Results({ @Result(name = "success", location = "/jsp/authSuccess.jsp"),
		@Result(name = "failure", location = "/jsp/error.jsp"), })
public class SocialAuthSuccessAction implements SessionAware,
		ServletRequestAware {

	final Log LOG = LogFactory.getLog(this.getClass());

	private Map<String, Object> userSession;
	public HttpServletRequest request;

	/**
	 * Displays the user profile and contacts for the given provider.
	 * 
	 * @return String where the action should flow
	 * @throws Exception
	 *             if an error occurs
	 */
	@Action(value = "/socialAuthSuccessAction")
	public String execute() throws Exception {

		SocialAuthManager manager = null;
		if (userSession.get("socialAuthManager") != null) {
			manager = (SocialAuthManager) userSession.get("socialAuthManager");
		}

		if (manager != null) {
			List<Contact> contactsList = new ArrayList<Contact>();
			Profile profile = null;
			try {
				Map<String, String> paramsMap = SocialAuthUtil
						.getRequestParametersMap(request);
				AuthProvider provider = manager.connect(paramsMap);
				profile = provider.getUserProfile();
				contactsList = provider.getContactList();
				if (contactsList != null && contactsList.size() > 0) {
					for (Contact p : contactsList) {
						if (StringUtils.isEmpty(p.getFirstName())
								&& StringUtils.isEmpty(p.getLastName())) {
							p.setFirstName(p.getDisplayName());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			request.setAttribute("profile", profile);
			request.setAttribute("contacts", contactsList);
			return "success";
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

}
