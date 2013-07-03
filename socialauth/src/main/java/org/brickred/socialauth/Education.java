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

import org.brickred.socialauth.util.DateComponents;

/**
 * Provides education detail
 * 
 * @author tarun.nagpal
 * 
 */
public class Education implements Serializable {

	private static final long serialVersionUID = 1790175712918723267L;

	private String schoolName;
	private String fieldOfStudy;
	private String degree;
	private DateComponents startDate;
	private DateComponents endDate;

	/**
	 * Returns school/college name
	 * 
	 * @return school name
	 */
	public String getSchoolName() {
		return schoolName;
	}

	/**
	 * Updates School Name
	 * 
	 * @param schoolName
	 *            the school name
	 */
	public void setSchoolName(final String schoolName) {
		this.schoolName = schoolName;
	}

	/**
	 * Returns Field of Study
	 * 
	 * @return field of study
	 */
	public String getFieldOfStudy() {
		return fieldOfStudy;
	}

	/**
	 * Updates Field of Study
	 * 
	 * @param fieldOfStudy
	 *            the field of study
	 */
	public void setFieldOfStudy(final String fieldOfStudy) {
		this.fieldOfStudy = fieldOfStudy;
	}

	/**
	 * Returns the degree name
	 * 
	 * @return degree name
	 */
	public String getDegree() {
		return degree;
	}

	/**
	 * Updates the degree name
	 * 
	 * @param degree
	 *            the degree name
	 */
	public void setDegree(final String degree) {
		this.degree = degree;
	}

	/**
	 * Returns the start date of education
	 * 
	 * @return start date of education
	 */
	public DateComponents getStartDate() {
		return startDate;
	}

	/**
	 * Updates the start date of education
	 * 
	 * @param startDate
	 *            the start date of education
	 */
	public void setStartDate(final DateComponents startDate) {
		this.startDate = startDate;
	}

	/**
	 * Returns the end date of education
	 * 
	 * @return the end date of education
	 */
	public DateComponents getEndDate() {
		return endDate;
	}

	/**
	 * Updates the end date of education
	 * 
	 * @param endDate
	 *            the end date of education
	 */
	public void setEndDate(final DateComponents endDate) {
		this.endDate = endDate;
	}

	/**
	 * Retrieves the education as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" schoolName: " + schoolName + NEW_LINE);
		result.append(" fieldOfStudy: " + fieldOfStudy + NEW_LINE);
		result.append(" degree: " + degree + NEW_LINE);
		result.append(" startDate: " + startDate + NEW_LINE);
		result.append(" endDate: " + endDate + NEW_LINE);
		result.append("}");
		return result.toString();
	}
}
