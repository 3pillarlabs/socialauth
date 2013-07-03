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
package org.brickred.socialauth;

import java.io.Serializable;

/**
 * Data bean for recommendation info
 * 
 * @author tarun.nagpal
 * 
 */
public class Recommendation implements Serializable {

	private static final long serialVersionUID = -7779809226300050070L;

	private String recommendationId;
	private String recommendationType;
	private String recommendationText;
	private String recommenderId;
	private String recommenderFirstName;
	private String recommenderLastName;

	/**
	 * Retrieves the recommendation id
	 * 
	 * @return String the recommendation id
	 */
	public String getRecommendationId() {
		return recommendationId;
	}

	/**
	 * Updates the recommendation id
	 * 
	 * @param recommendationId
	 *            the recommendation id
	 */
	public void setRecommendationId(final String recommendationId) {
		this.recommendationId = recommendationId;
	}

	/**
	 * Retrieves the recommendation type
	 * 
	 * @return String the recommendation type
	 */
	public String getRecommendationType() {
		return recommendationType;
	}

	/**
	 * Updates the recommendation type
	 * 
	 * @param recommendationType
	 *            the recommendation type
	 */
	public void setRecommendationType(final String recommendationType) {
		this.recommendationType = recommendationType;
	}

	/**
	 * Retrieves the recommendation text
	 * 
	 * @return String the recommendation text
	 */
	public String getRecommendationText() {
		return recommendationText;
	}

	/**
	 * Updates the recommendation text
	 * 
	 * @param recommendationText
	 *            the recommendation text
	 */
	public void setRecommendationText(final String recommendationText) {
		this.recommendationText = recommendationText;
	}

	/**
	 * Retrieves the recommender id
	 * 
	 * @return String the recommender id
	 */
	public String getRecommenderId() {
		return recommenderId;
	}

	/**
	 * Updates the recommender id
	 * 
	 * @param recommenderId
	 *            the recommender id
	 */
	public void setRecommenderId(final String recommenderId) {
		this.recommenderId = recommenderId;
	}

	/**
	 * Retrieves the recommender first name
	 * 
	 * @return String the recommender first name
	 */
	public String getRecommenderFirstName() {
		return recommenderFirstName;
	}

	/**
	 * Updates the recommender first name
	 * 
	 * @param recommenderFirstName
	 *            the recommender first name
	 */
	public void setRecommenderFirstName(final String recommenderFirstName) {
		this.recommenderFirstName = recommenderFirstName;
	}

	/**
	 * Retrieves the recommender last name
	 * 
	 * @return String the recommender last name
	 */
	public String getRecommenderLastName() {
		return recommenderLastName;
	}

	/**
	 * Updates the recommender last name
	 * 
	 * @param recommenderLastName
	 *            the recommender last name
	 */
	public void setRecommenderLastName(final String recommenderLastName) {
		this.recommenderLastName = recommenderLastName;
	}

	/**
	 * Retrieves the recommendation as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" recommendationId: " + recommendationId + NEW_LINE);
		result.append(" recommendationType: " + recommendationType + NEW_LINE);
		result.append(" recommendationText: " + recommendationText + NEW_LINE);
		result.append(" recommenderId: " + recommenderId + NEW_LINE);
		result.append(" recommenderFirstName: " + recommenderFirstName
				+ NEW_LINE);
		result.append(" recommenderLastName: " + recommenderLastName + NEW_LINE);
		result.append("}");
		return result.toString();
	}

}
