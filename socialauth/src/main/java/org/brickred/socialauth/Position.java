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
 * Data bean for position information.
 * 
 * @author tarun.nagpal
 * 
 */
public class Position implements Serializable {

	private static final long serialVersionUID = -446348881145861791L;

	private String positionId;
	private String title;
	private DateComponents startDate;
	private DateComponents endDate;
	private boolean currentcompany;
	private String companyId;
	private String companyName;
	private String companyType;
	private String industry;

	/**
	 * Retrieves the position id
	 * 
	 * @return String the position id
	 */
	public String getPositionId() {
		return positionId;
	}

	/**
	 * Updates the position id
	 * 
	 * @param positionId
	 *            the position id
	 */
	public void setPositionId(final String positionId) {
		this.positionId = positionId;
	}

	/**
	 * Retrieves the position title
	 * 
	 * @return String, the position title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Updates the position title
	 * 
	 * @param title
	 *            the position title
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Retrieves the start date of this position
	 * 
	 * @return {@link DateComponents} the start date of this position
	 */
	public DateComponents getStartDate() {
		return startDate;
	}

	/**
	 * Updates the start date of this position
	 * 
	 * @param startDate
	 *            the start date of this position
	 */
	public void setStartDate(final DateComponents startDate) {
		this.startDate = startDate;
	}

	/**
	 * Retrieves the end date of this position. It may be null if this position
	 * is the current position
	 * 
	 * @return {@link DateComponents} the end date of this position
	 */
	public DateComponents getEndDate() {
		return endDate;
	}

	/**
	 * Updates the end date of this position
	 * 
	 * @param endDate
	 *            the end date of this position
	 */
	public void setEndDate(final DateComponents endDate) {
		this.endDate = endDate;
	}

	/**
	 * Returns true if this company is the current company otherwise returns
	 * false
	 * 
	 * @return boolean true|false
	 */
	public boolean isCurrentCompany() {
		return currentcompany;
	}

	/**
	 * Updates the current company status
	 * 
	 * @param currentcompany
	 *            the current company status
	 */
	public void setCurrentCompany(final boolean currentcompany) {
		this.currentcompany = currentcompany;
	}

	/**
	 * Retrieves the company id
	 * 
	 * @return String the company id
	 */
	public String getCompanyId() {
		return companyId;
	}

	/**
	 * Updates the company id
	 * 
	 * @param companyId
	 *            the company id
	 */
	public void setCompanyId(final String companyId) {
		this.companyId = companyId;
	}

	/**
	 * Retrieves the company name
	 * 
	 * @return String the company name
	 */
	public String getCompanyName() {
		return companyName;
	}

	/**
	 * Updates the company name
	 * 
	 * @param companyName
	 *            the company name
	 */
	public void setCompanyName(final String companyName) {
		this.companyName = companyName;
	}

	/**
	 * Retrieves the company type
	 * 
	 * @return String the company type
	 */
	public String getCompanyType() {
		return companyType;
	}

	/**
	 * Updates the company type
	 * 
	 * @param companyType
	 *            the company type
	 */
	public void setCompanyType(final String companyType) {
		this.companyType = companyType;
	}

	/**
	 * Retrieves the industry
	 * 
	 * @return String the industry
	 */
	public String getIndustry() {
		return industry;
	}

	/**
	 * Updates the industry
	 * 
	 * @param industry
	 */
	public void setIndustry(final String industry) {
		this.industry = industry;
	}

	/**
	 * Retrieves the position as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" id: " + positionId + NEW_LINE);
		result.append(" title: " + title + NEW_LINE);
		result.append(" startDate: " + startDate + NEW_LINE);
		result.append(" endDate: " + endDate + NEW_LINE);
		result.append(" currentcompany: " + currentcompany + NEW_LINE);
		result.append(" companyId: " + companyId + NEW_LINE);
		result.append(" companyName: " + companyName + NEW_LINE);
		result.append(" companyType: " + companyType + NEW_LINE);
		result.append(" industry: " + industry + NEW_LINE);
		result.append("}");
		return result.toString();
	}
}
