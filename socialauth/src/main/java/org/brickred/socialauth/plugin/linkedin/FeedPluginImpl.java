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
package org.brickred.socialauth.plugin.linkedin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Feed;
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.plugin.FeedPlugin;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Feed Plugin implementation for Linkedin
 * 
 * @author tarun.nagpal
 * 
 */
public class FeedPluginImpl implements FeedPlugin, Serializable {

	private static final long serialVersionUID = 497690659988355485L;
	private static final String FEED_URL = "http://api.linkedin.com/v1/people/~/network/updates";
	private static final Map<String, String> UPDATE_TYPES;
	private final Log LOG = LogFactory.getLog(this.getClass());

	private ProviderSupport providerSupport;

	String fnameExpression = "//update[%s]/update-content/person/first-name";
	String lnameExpression = "//update[%s]/update-content/person/last-name";
	String idExpression = "//update[%s]/update-content/person/id";
	String cfnameExpression = "//update[%s]/update-content/person/connections/person/first-name";
	String clnameExpression = "//update[%s]/update-content/person/connections/person/last-name";
	String dateExpression = "//update[%s]/timestamp";
	String shareCommentExpression = "//update[%s]/update-content/person/current-share/comment";
	String statusExpression = "//update[%s]/update-content/person/current-status";
	String jobPosterFnameExpression = "//update[%s]/update-content/job-poster/first-name";
	String jobPosterLnameExpression = "//update[%s]/update-content/job-poster/last-name";
	String jobPosterIdExpression = "//update[%s]/update-content/job-poster/id";
	String jobPositionExpression = "//update[%s]/update-content/job/position";
	String jobCompanyExpression = "//update[%s]/update-content/job/company";
	String groupNameExpression = "//update[%s]/update-content/person/member-groups/member-group/name";
	String recommendationExpression = "//update[%s]/update-content/person/recommendations-given/recommendation/recommendation-snippet";
	String recommendeeFnameExpression = "//update[%s]/update-content/person/recommendations-given/recommendation/recommendee/first-name";
	String recommendeeLnameExpression = "//update[%s]/update-content/person/recommendations-given/recommendation/recommendee/last-name";
	String recommenderFnameExpression = "//update[%s]/update-content/person/recommendations-received/recommendation/recommender/first-name";
	String recommenderLnameExpression = "//update[%s]/update-content/person/recommendations-received/recommendation/recommender/last-name";
	String recommendationGivenCountExpression = "count(//update[%s]/update-content/person/recommendations-given)";
	String appUpdateExpression = "//update[%s]/update-content/person/person-activities/activity/body";
	String companyExpression = "//update[%s]/update-content/company/name";
	String companyPersonFnameExpression = "//update[%s]/update-content/company-person-update/person/first-name";
	String companyPersonLnameExpression = "//update[%s]/update-content/company-person-update/person/last-name";
	String companyPersonIdExpression = "//update[%s]/update-content/company-person-update/person/id";

	static final XPath xPath = XPathFactory.newInstance().newXPath();
	static {

		UPDATE_TYPES = new HashMap<String, String>();
		// CONN - connection of the current member has made a new connection
		// Example string
		// equivalent:"John Irving is now connected to Paul Auster."
		UPDATE_TYPES.put("CONN", " is now connected to ");
		// NCON - member has made a new connection
		// Example string equivalent: "John Irving is now a connection."
		UPDATE_TYPES.put("NCON", " is now a connection ");
		// CCEM - someone in their uploaded address book who has just recently
		// became a member of LinkedIn.
		// Example string equivalent: "Gertrude Stein has joined LinkedIn."
		UPDATE_TYPES.put("CCEM", " has joined LinkedIn ");
		// SHAR - shared something
		UPDATE_TYPES.put("SHAR", " has shared - ");
		// STAT - Status Updates
		// Example string equivalent:
		// "Taylor Singletary helping developers http://developers.linkedin.com"
		UPDATE_TYPES.put("STAT", " - ");
		// JOBP - Posted a job
		// Example string
		// equivalent:"John Irving posted a job: Editor at Irving Books."
		UPDATE_TYPES.put("JOBP", "posted a job: ");
		// JGRP - Joined a group
		// Example string equivalent:
		// "Richard Brautigan joined the group Friends of LinkedIn."
		UPDATE_TYPES.put("JGRP", " joined the group ");
		// PREC - first degree connection recommends another LinkedIn member
		// Example string equivalent:
		// "John Irving recommends Richard Brautigan: 'Richard is my favorite author...'"
		UPDATE_TYPES.put("PREC_GIVEN", " recommends ");
		// Example string equivalent: "John Irving was recommended by Richard
		// Brautigan
		UPDATE_TYPES.put("PREC_RECEIVED", " was recommended by ");
		UPDATE_TYPES.put("PROF", " updates his/her profile.");

		// APPM - application update
		UPDATE_TYPES.put("APPM", " - ");
		// MSFC - Member Starts Following Company
		UPDATE_TYPES.put("MSFC", " is now following ");
		// new profile picture
		UPDATE_TYPES.put("PICU", " updated profile picture ");

	}

	public FeedPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	/**
	 * Returns the list of feed. It returns maximum 250 feeds.
	 * 
	 * @return List of feed
	 * @throws Exception
	 */
	@Override
	public List<Feed> getFeeds() throws Exception {
		LOG.info("Getting feeds from URL : " + FEED_URL);
		Response serviceResponse = null;
		List<Feed> list;
		try {
			serviceResponse = providerSupport.api(FEED_URL);
		} catch (Exception ie) {
			throw new SocialAuthException("Failed to retrieve the feeds from "
					+ FEED_URL, ie);
		}

		if (serviceResponse.getStatus() != 200) {
			throw new SocialAuthException("Failed to retrieve the feeds from  "
					+ FEED_URL + ". Staus :" + serviceResponse.getStatus());
		}
		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(serviceResponse
					.getInputStream());
			list = getStatusFeed(root);

		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the feeds from response." + FEED_URL, e);
		}
		return list;
	}

	private List<Feed> getStatusFeed(final Element root) throws Exception {
		NodeList nodes = root.getElementsByTagName("update");
		List<Feed> list = new ArrayList<Feed>();
		if (nodes != null && nodes.getLength() > 0) {
			LOG.debug("Feeds count :: " + nodes.getLength());
			for (int i = 1; i <= nodes.getLength(); i++) {
				String type = xPath.evaluate("//update[" + i + "]/update-type",
						root);
				String fname = xPath.evaluate(
						String.format(fnameExpression, i), root);
				String lname = xPath.evaluate(
						String.format(lnameExpression, i), root);
				String id = xPath
						.evaluate(String.format(idExpression, i), root);
				String name = fname + " " + lname;
				String time = xPath.evaluate(String.format(dateExpression, i),
						root);
				Date date = new Date(Long.valueOf(time));

				boolean isSet = true;
				Feed feed = new Feed();
				feed.setId(id);
				feed.setCreatedAt(date);
				feed.setFrom(name);

				if ("CONN".equals(type)) {
					String cfname = xPath.evaluate(
							String.format(cfnameExpression, i), root);
					String clname = xPath.evaluate(
							String.format(clnameExpression, i), root);
					feed.setMessage(name + UPDATE_TYPES.get("CONN") + cfname
							+ " " + clname);
				} else if ("NCON".equals(type)) {
					feed.setMessage(name + UPDATE_TYPES.get("NCON"));
				} else if ("CCEM".equals(type)) {
					feed.setMessage(name + UPDATE_TYPES.get("CCEM"));
				} else if ("SHAR".equals(type)) {
					String comment = xPath.evaluate(
							String.format(shareCommentExpression, i), root);
					feed.setMessage(name + UPDATE_TYPES.get("SHAR") + comment);
				} else if ("STAT".equals(type)) {
					String status = xPath.evaluate(
							String.format(statusExpression, i), root);
					feed.setMessage(name + UPDATE_TYPES.get("STAT") + status);
				} else if ("JOBP".equals(type)) {
					String jpFname = xPath.evaluate(
							String.format(jobPosterFnameExpression, i), root);
					String jpLname = xPath.evaluate(
							String.format(jobPosterLnameExpression, i), root);
					String jpId = xPath.evaluate(
							String.format(jobPosterIdExpression, i), root);
					String position = xPath.evaluate(
							String.format(jobPositionExpression, i), root);
					String company = xPath.evaluate(
							String.format(jobCompanyExpression, i), root);
					feed.setFrom(jpFname + " " + jpLname);
					feed.setId(jpId);
					feed.setMessage(jpFname + " " + jpLname
							+ UPDATE_TYPES.get("JOBP") + position + " at "
							+ company);
				} else if ("JGRP".equals(type)) {
					String groupName = xPath.evaluate(
							String.format(groupNameExpression, i), root);
					feed.setMessage(name + UPDATE_TYPES.get("JGRP") + groupName);
				} else if ("PREC".equals(type)) {
					double count = (Double) xPath.evaluate(String.format(
							recommendationGivenCountExpression, i), root,
							XPathConstants.NUMBER);
					String message = "";
					if (count > 0) {
						String recommendation = xPath.evaluate(
								String.format(recommendationExpression, i),
								root);
						String recommendeeFname = xPath.evaluate(
								String.format(recommendeeFnameExpression, i),
								root);
						String recommendeeLname = xPath.evaluate(
								String.format(recommendeeLnameExpression, i),
								root);
						message = name + UPDATE_TYPES.get("PREC_GIVEN")
								+ recommendeeFname + " " + recommendeeLname;
						if (recommendation != null
								&& recommendation.length() > 0) {
							message += " :'" + recommendation + "'";
						}
					} else {
						String recommendeeFname = xPath.evaluate(
								String.format(recommenderFnameExpression, i),
								root);
						String recommendeeLname = xPath.evaluate(
								String.format(recommenderLnameExpression, i),
								root);
						message = name + UPDATE_TYPES.get("PREC_RECEIVED")
								+ recommendeeFname + " " + recommendeeLname;
					}
					feed.setMessage(message);
				} else if ("PROF".equals(type)) {
					feed.setMessage(name + UPDATE_TYPES.get("PROF"));
				} else if ("APPM".equals(type)) {
					String update = xPath.evaluate(
							String.format(appUpdateExpression, i), root);
					feed.setMessage(name + UPDATE_TYPES.get("APPM") + update);
				} else if ("MSFC".equals(type)) {
					String company = xPath.evaluate(
							String.format(companyExpression, i), root);
					String pfname = xPath.evaluate(
							String.format(companyPersonFnameExpression, i),
							root);
					String plname = xPath.evaluate(
							String.format(companyPersonLnameExpression, i),
							root);
					String pid = xPath.evaluate(
							String.format(companyPersonIdExpression, i), root);
					feed.setMessage(pfname + " " + plname
							+ UPDATE_TYPES.get("MSFC") + company);
					feed.setFrom(pfname + " " + pfname);
					feed.setId(pid);
					list.add(feed);
				} else if ("PICU".equals(type)) {
					feed.setMessage(name + UPDATE_TYPES.get("PICU"));
				} else {
					isSet = false;
				}

				if (isSet) {
					list.add(feed);
				}
			}
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
