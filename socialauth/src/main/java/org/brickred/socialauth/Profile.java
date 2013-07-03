/*
 ===========================================================================
 Copyright (c) 2010 BrickRed Technologies Limited

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
import java.util.Map;

import org.brickred.socialauth.util.BirthDate;

/**
 * Data bean for profile information.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class Profile implements Serializable {

	private static final long serialVersionUID = 6082073969740796991L;

	/**
	 * Email
	 */
	private String email;

	/**
	 * First Name
	 */
	private String firstName;

	/**
	 * Last Name
	 */
	private String lastName;

	/**
	 * Country
	 */
	private String country;

	/**
	 * Language
	 */
	private String language;

	/**
	 * Full Name
	 */
	private String fullName;

	/**
	 * Display Name
	 */
	private String displayName;

	/**
	 * Date of Birth
	 */
	private BirthDate dob;

	/**
	 * Gender
	 */
	private String gender;

	/**
	 * Location
	 */
	private String location;

	/**
	 * Validated Id
	 */
	private String validatedId;

	/**
	 * profile image URL
	 */
	private String profileImageURL;

	/**
	 * provider id with this profile associates
	 */
	private String providerId;

	/**
	 * contact info
	 */
	private Map<String, String> contactInfo;

	/**
	 * Retrieves the first name
	 * 
	 * @return String the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Updates the first name
	 * 
	 * @param firstName
	 *            the first name of user
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Retrieves the last name
	 * 
	 * @return String the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Updates the last name
	 * 
	 * @param lastName
	 *            the last name of user
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Returns the email address.
	 * 
	 * @return email address of the user
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Updates the email
	 * 
	 * @param email
	 *            the email of user
	 */
	public void setEmail(final String email) {
		this.email = email;
	}

	/**
	 * Retrieves the validated id
	 * 
	 * @return String the validated id
	 */
	public String getValidatedId() {
		return validatedId;
	}

	/**
	 * Updates the validated id
	 * 
	 * @param validatedId
	 *            the validated id of user
	 */
	public void setValidatedId(final String validatedId) {
		this.validatedId = validatedId;
	}

	/**
	 * Retrieves the display name
	 * 
	 * @return String the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Updates the display name
	 * 
	 * @param displayName
	 *            the display name of user
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Retrieves the country
	 * 
	 * @return String the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Updates the country
	 * 
	 * @param country
	 *            the country of user
	 */
	public void setCountry(final String country) {
		this.country = country;
	}

	/**
	 * Retrieves the language
	 * 
	 * @return String the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Updates the language
	 * 
	 * @param language
	 *            the language of user
	 */
	public void setLanguage(final String language) {
		this.language = language;
	}

	/**
	 * Retrieves the full name
	 * 
	 * @return String the full name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Updates the full name
	 * 
	 * @param fullName
	 *            the full name of user
	 */
	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	/**
	 * Retrieves the date of birth
	 * 
	 * @return the date of birth different providers may use different formats
	 */
	public BirthDate getDob() {
		return dob;
	}

	/**
	 * Updates the date of birth
	 * 
	 * @param dob
	 *            the date of birth of user
	 */
	public void setDob(final BirthDate dob) {
		this.dob = dob;
	}

	/**
	 * Retrieves the gender
	 * 
	 * @return String the gender - could be "Male", "M" or "male"
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Updates the gender
	 * 
	 * @param gender
	 *            the gender of user
	 */
	public void setGender(final String gender) {
		this.gender = gender;
	}

	/**
	 * Retrieves the location
	 * 
	 * @return String the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Updates the location
	 * 
	 * @param location
	 *            the location of user
	 */
	public void setLocation(final String location) {
		this.location = location;
	}

	/**
	 * Retrieves the profile image URL
	 * 
	 * @return String the profileImageURL
	 */
	public String getProfileImageURL() {
		return profileImageURL;
	}

	/**
	 * Updates the profile image URL
	 * 
	 * @param profileImageURL
	 *            profile image URL of user
	 */
	public void setProfileImageURL(final String profileImageURL) {
		this.profileImageURL = profileImageURL;
	}

	/**
	 * Retrieves the provider id with this profile associates
	 * 
	 * @return the provider id
	 */
	public String getProviderId() {
		return providerId;
	}

	/**
	 * Updates the provider id
	 * 
	 * @param providerId
	 *            the provider id
	 */
	public void setProviderId(final String providerId) {
		this.providerId = providerId;
	}

	/**
	 * Retrieves the contact information. It contains address and phone numbers.
	 * Key may contain values like mainAddress, home, work where home and work
	 * value will be phone numbers.
	 * 
	 * @return contact information.
	 */
	public Map<String, String> getContactInfo() {
		return contactInfo;
	}

	/**
	 * Updates the contact info.
	 * 
	 * @param contactInfo
	 *            the map which contains the contact information
	 */
	public void setContactInfo(final Map<String, String> contactInfo) {
		this.contactInfo = contactInfo;
	}

	/**
	 * Retrieves the profile info as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" email: " + email + NEW_LINE);
		result.append(" firstName: " + firstName + NEW_LINE);
		result.append(" lastName: " + lastName + NEW_LINE);
		result.append(" country: " + country + NEW_LINE);
		result.append(" language: " + language + NEW_LINE);
		result.append(" fullName: " + fullName + NEW_LINE);
		result.append(" displayName: " + displayName + NEW_LINE);
		result.append(" dob: " + dob + NEW_LINE);
		result.append(" gender: " + gender + NEW_LINE);
		result.append(" location: " + location + NEW_LINE);
		result.append(" validatedId: " + validatedId + NEW_LINE);
		result.append(" profileImageURL: " + profileImageURL + NEW_LINE);
		result.append(" providerId: " + providerId + NEW_LINE);
		result.append(" contactInfo { " + NEW_LINE);
		if (contactInfo != null && !contactInfo.isEmpty()) {
			for (Map.Entry<String, String> entry : contactInfo.entrySet()) {
				result.append(entry.getKey() + " = " + entry.getValue()
						+ NEW_LINE);
			}
		}
		result.append(" } " + NEW_LINE);
		result.append("}");

		return result.toString();

	}

}
