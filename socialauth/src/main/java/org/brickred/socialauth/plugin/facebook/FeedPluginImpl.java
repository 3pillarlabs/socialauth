/*
 ===========================================================================
 Copyright (c) 2012 3PillarGlobal

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
package org.brickred.socialauth.plugin.facebook;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Feed;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.plugin.FeedPlugin;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Feed Plugin implementation for Facebook
 * 
 * @author tarun.nagpal
 * 
 */
public class FeedPluginImpl implements FeedPlugin, Serializable {

	private static final long serialVersionUID = 2108503235436046045L;
	private static final String FEED_URL = "https://graph.facebook.com/me/feed";
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ssz");
	private final Log LOG = LogFactory.getLog(FeedPluginImpl.class);

	private ProviderSupport providerSupport;

	public FeedPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public List<Feed> getFeeds() throws Exception {
		List<Feed> list = new ArrayList<Feed>();
		try {
			Response response = providerSupport.api(FEED_URL);
			String respStr = response
					.getResponseBodyAsString(Constants.ENCODING);
			JSONObject resp = new JSONObject(respStr);
			JSONArray data = resp.getJSONArray("data");
			LOG.debug("Feeds count : " + data.length());
			for (int i = 0; i < data.length(); i++) {
				Feed feed = new Feed();
				JSONObject obj = data.getJSONObject(i);
				if (obj.has("from")) {
					JSONObject fobj = obj.getJSONObject("from");
					if (fobj.has("name")) {
						feed.setFrom(fobj.getString("name"));
					}
					if (fobj.has("id")) {
						feed.setId(fobj.getString("id"));
					}
				}
				if (obj.has("message")) {
					feed.setMessage(obj.getString("message"));
				} else if (obj.has("story")) {
					feed.setMessage(obj.getString("story"));
				} else if (obj.has("name")) {
					feed.setMessage(obj.getString("name"));
				} else if (obj.has("caption")) {
					feed.setMessage(obj.getString("caption"));
				} else if (obj.has("description")) {
					feed.setMessage(obj.getString("description"));
				} else if (obj.has("picture")) {
					feed.setMessage(obj.getString("picture"));
				}

				if (obj.has("created_time")) {
					feed.setCreatedAt(dateFormat.parse(obj
							.getString("created_time")));
				}
				list.add(feed);
			}
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting Feeds from "
					+ FEED_URL, e);
		}
		return list;
	}

	@Override
	public ProviderSupport getProviderSupport() {
		return providerSupport;
	}

	@Override
	public void setProviderSupport(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}
}
