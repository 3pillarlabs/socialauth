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
 * Data bean for career info
 * 
 * @author tarun.nagpal
 * 
 */
public class Career implements Serializable {

	private static final long serialVersionUID = -3192339680277686552L;

	private String id;
	private String headline;
	private Education[] educations;
	private Position[] positions;
	private Recommendation[] recommendations;

	/**
	 * Retrieves the id
	 * 
	 * @return String the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Updates the id
	 * 
	 * @param id
	 *            the id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Retrieves the headline
	 * 
	 * @return String the headline
	 */
	public String getHeadline() {
		return headline;
	}

	/**
	 * Updates the headline
	 * 
	 * @param headline
	 *            the headline
	 */
	public void setHeadline(final String headline) {
		this.headline = headline;
	}

	/**
	 * Retrieves the educations
	 * 
	 * @return {@link Education} array, the list of educations
	 */
	public Education[] getEducations() {
		return educations;
	}

	/**
	 * Updates the educations
	 * 
	 * @param educations
	 *            the educations as a array of {@link Education} object
	 */
	public void setEducations(final Education[] educations) {
		this.educations = educations;
	}

	/**
	 * Retrieves the positions
	 * 
	 * @return {@link Position} array, the list of positions
	 */
	public Position[] getPositions() {
		return positions;
	}

	/**
	 * Updates the positions
	 * 
	 * @param positions
	 *            the positions as a array of {@link Position} object
	 */
	public void setPositions(final Position[] positions) {
		this.positions = positions;
	}

	/**
	 * Retrieves the recommendations
	 * 
	 * @return {@link Recommendation} array, the list of recommendation
	 */
	public Recommendation[] getRecommendations() {
		return recommendations;
	}

	/**
	 * Updates the recommendations
	 * 
	 * @param recommendations
	 *            the recommendations as a array of {@link Recommendation}
	 *            object
	 */
	public void setRecommendations(final Recommendation[] recommendations) {
		this.recommendations = recommendations;
	}

	/**
	 * Retrieves the career as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" id: " + id + NEW_LINE);
		result.append(" headline: " + headline + NEW_LINE);
		result.append(" educations { " + NEW_LINE);
		if (educations != null) {
			for (Education education : educations) {
				result.append(education + NEW_LINE);
			}
		}
		result.append(" } " + NEW_LINE);
		result.append(" positions { " + NEW_LINE);
		if (positions != null) {
			for (Position position : positions) {
				result.append(position + NEW_LINE);
			}
		}
		result.append(" } " + NEW_LINE);
		result.append(" recommendations { " + NEW_LINE);
		if (recommendations != null) {
			for (Recommendation recommendation : recommendations) {
				result.append(recommendation + NEW_LINE);
			}
		}
		result.append(" } " + NEW_LINE);
		result.append("}");
		return result.toString();
	}

}
