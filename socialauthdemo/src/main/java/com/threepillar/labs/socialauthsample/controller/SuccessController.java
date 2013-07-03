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

package com.threepillar.labs.socialauthsample.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.spring.bean.SocialAuthTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.threepillar.labs.socialauthsample.util.Constants;

@Controller
public class SuccessController {

	@Autowired
	private SocialAuthTemplate socialAuthTemplate;

	@RequestMapping(value = "/authSuccess")
	public ModelAndView getRedirectURL(final HttpServletRequest request)
			throws Exception {
		SocialAuthManager manager = socialAuthTemplate.getSocialAuthManager();
		AuthProvider provider = manager.getCurrentAuthProvider();
		HttpSession session = request.getSession();
		String type = null;
		if (session.getAttribute(Constants.REQUEST_TYPE) != null) {
			type = (String) session.getAttribute(Constants.REQUEST_TYPE);
		}
		if (type != null) {
			if (Constants.REGISTRATION.equals(type)) {
				return registration(provider);
			} else if (Constants.IMPORT_CONTACTS.equals(type)) {
				return importContacts(provider);
			} else if (Constants.SHARE.equals(type)) {
				return new ModelAndView("shareForm", "connectedProvidersIds",
						manager.getConnectedProvidersIds());
			}
		}

		return null;
	}

	private ModelAndView registration(final AuthProvider provider)
			throws Exception {
		Profile profile = provider.getUserProfile();
		if (profile.getFullName() == null) {
			String name = null;
			if (profile.getFirstName() != null) {
				name = profile.getFirstName();
			}
			if (profile.getLastName() != null) {
				if (profile.getFirstName() != null) {
					name += " " + profile.getLastName();
				} else {
					name = profile.getLastName();
				}
			}
			if (name == null && profile.getDisplayName() != null) {
				name = profile.getDisplayName();
			}
			if (name != null) {
				profile.setFullName(name);
			}
		}
		ModelAndView view = new ModelAndView("registrationForm", "profile",
				profile);
		return view;
	}

	private ModelAndView importContacts(final AuthProvider provider)
			throws Exception {
		List<Contact> contactsList = new ArrayList<Contact>();
		contactsList = provider.getContactList();
		if (contactsList != null && contactsList.size() > 0) {
			for (Contact p : contactsList) {
				if (!StringUtils.hasLength(p.getFirstName())
						&& !StringUtils.hasLength(p.getLastName())) {
					p.setFirstName(p.getDisplayName());
				}
			}
		}
		ModelAndView view = new ModelAndView("showImportContacts", "contacts",
				contactsList);
		return view;
	}

}
