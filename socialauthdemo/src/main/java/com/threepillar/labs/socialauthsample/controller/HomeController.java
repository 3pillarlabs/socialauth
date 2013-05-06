package com.threepillar.labs.socialauthsample.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.spring.bean.SocialAuthTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.threepillar.labs.socialauthsample.bean.User;
import com.threepillar.labs.socialauthsample.util.Constants;

@Controller
public class HomeController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SocialAuthTemplate socialAuthTemplate;

	@RequestMapping(value = "/registration")
	public ModelAndView showRegistration(final HttpServletRequest request) {
		logger.info("Showing registration page");
		HttpSession session = request.getSession();
		session.setAttribute(Constants.REQUEST_TYPE, Constants.REGISTRATION);
		ModelAndView modelAndView = new ModelAndView("registration");
		return modelAndView;

	}

	@RequestMapping(value = "/submitRegistration")
	public ModelAndView showRegistrationPage(
			@ModelAttribute("user") final User user,
			final HttpServletRequest request) {
		logger.info("Submiting registration");
		ModelAndView modelAndView = new ModelAndView("registrationSuccess",
				"user", user);
		return modelAndView;

	}

	@RequestMapping(value = "/importContacts")
	public ModelAndView importContacts(final HttpServletRequest request) {
		logger.info("Showing import contacts page");
		HttpSession session = request.getSession();
		session.setAttribute(Constants.REQUEST_TYPE, Constants.IMPORT_CONTACTS);
		ModelAndView modelAndView = new ModelAndView("importContacts");
		return modelAndView;
	}

	@RequestMapping(value = "/shareForm")
	public ModelAndView shareForm(final HttpServletRequest request) {
		logger.info("Showing share form");
		HttpSession session = request.getSession();
		session.setAttribute(Constants.REQUEST_TYPE, Constants.SHARE);
		SocialAuthManager manager = socialAuthTemplate.getSocialAuthManager();
		List<String> connectedProvidersIds = new ArrayList<String>();
		if (manager != null) {
			connectedProvidersIds = manager.getConnectedProvidersIds();
		}

		ModelAndView modelAndView = new ModelAndView("shareForm",
				"connectedProvidersIds", connectedProvidersIds);
		return modelAndView;

	}

	@RequestMapping(value = "/share", method = RequestMethod.POST)
	public ModelAndView share(
			@RequestParam(value = "message", required = true) final String message,
			final HttpServletRequest request) {
		logger.info("Showing share form");
		HttpSession session = request.getSession();
		session.setAttribute(Constants.REQUEST_TYPE, Constants.SHARE);
		SocialAuthManager manager = socialAuthTemplate.getSocialAuthManager();
		List<String> connectedProvidersIds = new ArrayList<String>();
		if (manager != null) {
			connectedProvidersIds = manager.getConnectedProvidersIds();
		}
		String providerIds = null;
		for (String id : connectedProvidersIds) {
			try {
				AuthProvider provider = manager.getProvider(id);
				provider.updateStatus(message);
				if (providerIds == null) {
					providerIds = provider.getProviderId();
				} else {
					providerIds += ", " + provider.getProviderId();
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		ModelAndView modelAndView = new ModelAndView("shareForm");
		modelAndView.addObject("connectedProvidersIds", connectedProvidersIds);
		if (providerIds != null) {
			String str = "Status is updated on " + providerIds;
			if (providerIds.indexOf(',') != -1) {
				str += " providers.";
			} else {
				str += " provider.";
			}
			modelAndView.addObject("message", str);
		}
		return modelAndView;

	}
}
