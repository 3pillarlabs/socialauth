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
package org.brickred.socialauth.util;

import java.io.Serializable;

/**
 * Stores the BirthDate
 * 
 * @author tarun.nagpal
 * 
 */
public class DateComponents implements Serializable {

	private static final long serialVersionUID = 219784612977177962L;

	private int day;
	private int month;
	private int year;

	/**
	 * Retrieves the birth day.
	 * 
	 * @return the birth day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * Updates the birth day
	 * 
	 * @param day
	 *            the birth day
	 */
	public void setDay(final int day) {
		this.day = day;
	}

	/**
	 * Retrieves the birth month
	 * 
	 * @return the birth month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Updates the birth month
	 * 
	 * @param month
	 *            the birth month
	 */
	public void setMonth(final int month) {
		this.month = month;
	}

	/**
	 * Retrieves the birth year
	 * 
	 * @return the birth year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Updates the birth year
	 * 
	 * @param year
	 *            the birth year
	 */
	public void setYear(final int year) {
		this.year = year;
	}

	/**
	 * Returns the date in mm/dd/yyyy format
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (month > 0) {
			if (month < 10) {
				sb.append("0");
			}
			sb.append(month);
		} else {
			sb.append("00");
		}
		sb.append("/");
		if (day > 0) {
			if (day < 10) {
				sb.append("0");
			}
			sb.append(day);
		} else {
			sb.append("00");
		}
		sb.append("/");
		if (year > 0) {
			sb.append(year);
		} else {
			sb.append("0000");
		}
		return sb.toString();
	}
}
