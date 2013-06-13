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

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.brickred.socialauth.cdi.SocialAuth;


/**
 * This is the main component class, it is referred in application pages and
 * provides the navigation functionality to the user.
 * 
 * @author lakhdeeps@brickred.com
 * 
 */
@SessionScoped
@Named("socialauthenticator")

public class Authenticator implements Serializable{

	private static final Logger log = Logger.getLogger(Authenticator.class);

	@Inject
	SocialAuth socialauth;

	/**
	 * Variable for storing open id from main form
	 */
	private String openID;

	/**
	 * Track the user interaction with main page and set the state of components
	 * accordingly.
	 * 
	 * @param ActionEvent
	 */

	public void updateId(ActionEvent ae) {
		String btnClicked = ae.getComponent().getId();
		log.info("*************login method called ************"
				+ socialauth.getId());

		ExternalContext context = javax.faces.context.FacesContext
				.getCurrentInstance().getExternalContext();

		String viewUrl = context.getInitParameter("successUrl");
		socialauth.setViewUrl(viewUrl);

		if (btnClicked.indexOf("facebook") != -1) {
			socialauth.setId("facebook");
			log.info("***facebook*********" + socialauth.getId());
		} else if (btnClicked.indexOf("twitter") != -1) {
			socialauth.setId("twitter");
			log.info("***twitter*********" + socialauth.getId());
		} else if (btnClicked.indexOf("yahoo") != -1) {
			socialauth.setId("yahoo");
			log.info("***yahoo*********" + socialauth.getId());
		} else if (btnClicked.indexOf("hotmail") != -1) {
			socialauth.setId("hotmail");
			log.info("***hotmail*********" + socialauth.getId());
		} else if (btnClicked.indexOf("google") != -1) {
			socialauth.setId("google");
			log.info("***google*********" + socialauth.getId());
		} else if (btnClicked.indexOf("linkedin") != -1) {
			socialauth.setId("linkedin");
			log.info("***linkedin*********" + socialauth.getId());
		} else if (btnClicked.indexOf("foursquare") != -1) {
			socialauth.setId("foursquare");
			log.info("***foursquare*********" + socialauth.getId());
		} else {
			socialauth.setId(openID);
			log.info("***openID*********" + socialauth.getId());
		}
	}

	/**
	 * Redirect the user back to the main page from success view.
	 * 
	 * @param ActionEvent
	 */
	public String mainPage() {
		return "/home.xhtml";
	}

	public String getOpenID() {
		return openID;
	}

	public void setOpenID(String openID) {
		this.openID = openID;
	}

	public void verify(ComponentSystemEvent cse) {

		boolean ajaxRequest = javax.faces.context.FacesContext
				.getCurrentInstance().getPartialViewContext().isAjaxRequest();
		if (!ajaxRequest) {
						try {
				socialauth.connect();
			} catch (Exception e) {
				log.warn(e);
			}
		}
	}
}
