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
package org.brickred.socialauthcdi.session;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.brickred.socialauth.cdi.SocialAuth;

/**
 * This class used to update the user status on various public sites.
 * 
 * @author lakhdeeps@brickred.com
 * 
 */
@RequestScoped
@Named("socialAuthUpdateStatus")
public class UpdateStatus implements Serializable {
	private static final Logger log = Logger.getLogger(Authenticator.class);

	@Inject
	SocialAuth socialauth;

	String statusText;

	/**
	 * Method which updates the status on profile.
	 * 
	 * @param ActionEvent
	 * @throws Exception
	 */

	public void updateStatus() throws Exception {
		final HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		String statusText = request.getParameter("statusMessage");
		if (statusText != null && !statusText.equals("")) {
			socialauth.setStatus(statusText);
			socialauth.updateStatus();
			setStatus("Status Updated Successfully");
			System.out.println("status text:" + statusText);
		}
	}

	public String getStatus() {
		return statusText;
	}

	public void setStatus(String statusText) {
		this.statusText = statusText;
	}
}
