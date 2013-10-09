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
package com.threepillar.labs.socialauthwalldemo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Feed;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.plugin.FeedPlugin;
import org.brickred.socialauth.util.AccessGrant;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author tarun.nagpal
 * 
 */
@Controller
public class ShowWallController {

	private SocialAuthManager socialAuthManager;

	SocialAuthConfig config;

	@Autowired
	public void setSocialAuthManager(final SocialAuthManager socialAuthManager) {
		this.socialAuthManager = socialAuthManager;
	}

	@Autowired
	public void setConfig(final SocialAuthConfig config) {
		this.config = config;
	}

	@RequestMapping(value = "/showwall")
	public ModelAndView showWall(
			@RequestParam(value = "providerId", required = true) final String providerId,
			final HttpServletRequest request) throws Exception {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				providerId + "_accessGrant_file.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		ObjectMapper mapper = new ObjectMapper();
		AccessGrant accessGrant = mapper.readValue(sb.toString(),
				AccessGrant.class);
		AuthProvider provider = socialAuthManager.connect(accessGrant);
		List<Feed> feeds = new ArrayList<Feed>();
		if (provider
				.isSupportedPlugin(org.brickred.socialauth.plugin.FeedPlugin.class)) {
			FeedPlugin p = provider
					.getPlugin(org.brickred.socialauth.plugin.FeedPlugin.class);
			feeds = p.getFeeds();
		}
		ModelAndView view = new ModelAndView("showwall", "feeds", feeds);
		return view;
	}
}
