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
package org.brickred.socialauth.plugin.linkedin;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Career;
import org.brickred.socialauth.Education;
import org.brickred.socialauth.Position;
import org.brickred.socialauth.Recommendation;
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.plugin.CareerPlugin;
import org.brickred.socialauth.util.DateComponents;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Career plugin implementation for linkedin
 * 
 * @author tarun.nagpal
 * 
 */
public class CareerPluginImpl implements CareerPlugin, Serializable {

	private static final long serialVersionUID = -1733773634998485298L;

	private ProviderSupport providerSupport;
	private final Log LOG = LogFactory.getLog(this.getClass());

	private static final String PROFILE_URL = "http://api.linkedin.com/v1/people/~:(id,phone-numbers,headline,educations,positions,recommendations-received)";

	public CareerPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public Career getCareerDetails() throws Exception {
		LOG.info("Fetching career details from " + PROFILE_URL);
		Response serviceResponse = null;
		try {
			serviceResponse = providerSupport.api(PROFILE_URL);
		} catch (Exception ie) {
			throw new SocialAuthException(
					"Failed to retrieve the career details from " + PROFILE_URL,
					ie);
		}
		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(serviceResponse
					.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the career details from response."
							+ PROFILE_URL, e);
		}
		Career career = null;
		if (root != null) {
			career = new Career();
			Education[] educationsArr = null;
			Position[] positionsArr = null;
			Recommendation[] recommendationsArr = null;
			String headline = XMLParseUtil.getElementData(root, "headline");
			career.setHeadline(headline);
			String id = XMLParseUtil.getElementData(root, "id");
			career.setId(id);
			// get educations
			NodeList educations = root.getElementsByTagName("education");
			if (educations != null && educations.getLength() > 0) {
				LOG.debug("Educations count " + educations.getLength());
				educationsArr = new Education[educations.getLength()];
				for (int i = 0; i < educations.getLength(); i++) {
					Education educationObj = new Education();
					Element educationEl = (Element) educations.item(i);
					String schoolName = XMLParseUtil.getElementData(
							educationEl, "school-name");
					if (schoolName != null) {
						educationObj.setSchoolName(schoolName);
					}
					String degree = XMLParseUtil.getElementData(educationEl,
							"degree");
					if (degree != null) {
						educationObj.setDegree(degree);
					}
					String fieldOfStudy = XMLParseUtil.getElementData(
							educationEl, "field-of-study");
					if (fieldOfStudy != null) {
						educationObj.setFieldOfStudy(fieldOfStudy);
					}
					NodeList sd = educationEl
							.getElementsByTagName("start-date");
					if (sd != null && sd.getLength() > 0) {
						String year = XMLParseUtil.getElementData(
								(Element) sd.item(0), "year");
						if (year != null) {
							DateComponents comp = new DateComponents();
							comp.setYear(Integer.parseInt(year));
							educationObj.setStartDate(comp);
						}

					}
					NodeList ed = educationEl.getElementsByTagName("end-date");
					if (ed != null && ed.getLength() > 0) {
						String year = XMLParseUtil.getElementData(
								(Element) ed.item(0), "year");
						if (year != null) {
							DateComponents comp = new DateComponents();
							comp.setYear(Integer.parseInt(year));
							educationObj.setEndDate(comp);
						}

					}
					educationsArr[i] = educationObj;
				}
			}

			// get positions
			NodeList positions = root.getElementsByTagName("position");
			if (positions != null && positions.getLength() > 0) {
				LOG.debug("Positions count " + positions.getLength());
				positionsArr = new Position[positions.getLength()];
				for (int i = 0; i < positions.getLength(); i++) {
					Position positionnObj = new Position();
					Element positionEl = (Element) positions.item(i);
					String pid = XMLParseUtil.getElementData(positionEl, "id");
					if (pid != null) {
						positionnObj.setPositionId(pid);
					}
					String title = XMLParseUtil.getElementData(positionEl,
							"title");
					if (title != null) {
						positionnObj.setTitle(title);
					}
					String isCurrent = XMLParseUtil.getElementData(positionEl,
							"is-current");
					if (isCurrent != null) {
						positionnObj.setCurrentCompany(Boolean
								.valueOf(isCurrent));
					}
					NodeList sd = positionEl.getElementsByTagName("start-date");
					if (sd != null && sd.getLength() > 0) {
						String year = XMLParseUtil.getElementData(
								(Element) sd.item(0), "year");
						if (year != null) {
							DateComponents comp = new DateComponents();
							comp.setYear(Integer.parseInt(year));
							positionnObj.setStartDate(comp);
						}

					}
					NodeList ed = positionEl.getElementsByTagName("end-date");
					if (ed != null && ed.getLength() > 0) {
						String year = XMLParseUtil.getElementData(
								(Element) ed.item(0), "year");
						if (year != null) {
							DateComponents comp = new DateComponents();
							comp.setYear(Integer.parseInt(year));
							positionnObj.setEndDate(comp);
						}

					}

					NodeList companyNodes = positionEl
							.getElementsByTagName("company");
					if (companyNodes != null && companyNodes.getLength() > 0) {
						Element company = (Element) companyNodes.item(0);
						String compid = XMLParseUtil.getElementData(company,
								"id");
						if (compid != null) {
							positionnObj.setCompanyId(compid);
						}
						String compName = XMLParseUtil.getElementData(company,
								"name");
						if (compName != null) {
							positionnObj.setCompanyName(compName);
						}
						String industry = XMLParseUtil.getElementData(company,
								"industry");
						if (industry != null) {
							positionnObj.setIndustry(industry);
						}
						String type = XMLParseUtil.getElementData(company,
								"type");
						if (type != null) {
							positionnObj.setCompanyType(type);
						}
					}
					positionsArr[i] = positionnObj;
				}
			}

			// getRecommendation
			NodeList recommendations = root
					.getElementsByTagName("recommendation");
			if (recommendations != null && recommendations.getLength() > 0) {
				LOG.debug("Recommendations count "
						+ recommendations.getLength());
				recommendationsArr = new Recommendation[recommendations
						.getLength()];
				for (int i = 0; i < recommendations.getLength(); i++) {
					Recommendation recommendationObj = new Recommendation();
					Element recommendationEl = (Element) recommendations
							.item(i);
					String rid = XMLParseUtil.getElementData(recommendationEl,
							"id");
					if (rid != null) {
						recommendationObj.setRecommendationId(rid);
					}
					String text = XMLParseUtil.getElementData(recommendationEl,
							"recommendation-text");
					if (text != null) {
						recommendationObj.setRecommendationText(text);
					}

					String code = XMLParseUtil.getElementData(recommendationEl,
							"code");
					if (code != null) {
						recommendationObj.setRecommendationType(code);
					}

					NodeList recommenderNodes = recommendationEl
							.getElementsByTagName("recommender");
					if (recommenderNodes != null
							&& recommenderNodes.getLength() > 0) {
						Element recommenderEl = (Element) recommenderNodes
								.item(0);
						String recommenderId = XMLParseUtil.getElementData(
								recommenderEl, "id");
						if (recommenderId != null) {
							recommendationObj.setRecommenderId(recommenderId);
						}
						String fname = XMLParseUtil.getElementData(
								recommenderEl, "first-name");
						if (fname != null) {
							recommendationObj.setRecommenderFirstName(fname);
						}
						String lname = XMLParseUtil.getElementData(
								recommenderEl, "last-name");
						if (lname != null) {
							recommendationObj.setRecommenderLastName(lname);
						}

					}
					recommendationsArr[i] = recommendationObj;
				}
			}

			if (educationsArr != null) {
				career.setEducations(educationsArr);
			}

			if (positionsArr != null) {
				career.setPositions(positionsArr);
			}

			if (recommendationsArr != null) {
				career.setRecommendations(recommendationsArr);
			}
		}
		return career;
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
