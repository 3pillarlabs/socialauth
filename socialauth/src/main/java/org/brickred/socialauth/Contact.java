/*
 ===========================================================================
 Copyright (c) 2012 3Pillar Global

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
 * Data bean for contact information.
 * 
 * @author tarunn@brickred.com
 * 
 */
public class Contact implements Serializable {
	private static final long serialVersionUID = 7983770896851139223L;

	/**
	 * Email
	 */
	String email;

	/**
	 * First Name
	 */
	String firstName;

	/**
	 * Last Name
	 */
	String lastName;

	/**
	 * Display Name
	 */
	String displayName;

	/**
	 * Other emails array.
	 */
	String otherEmails[];

	/**
	 * Profile URL
	 */
	String profileUrl;

	/**
	 * Id of person
	 */
	String id;

	/**
	 * Email hash
	 */
	String emailHash;

	/**
	 * profile image URL
	 */
	private String profileImageURL;

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
	 * Retrieves the contact person emails
	 * 
	 * @return String Array of emails
	 */
	public String[] getOtherEmails() {
		return otherEmails;
	}

	/**
	 * 
	 * @param otherEmails
	 *            array of emails, if contact person has more than one email
	 *            then it contains rest of the emails except first one
	 */
	public void setOtherEmails(final String[] otherEmails) {
		this.otherEmails = otherEmails;
	}

	/**
	 * Retrieves the contact person Public profile URL
	 * 
	 * @return String contact person Public profile URL
	 */
	public String getProfileUrl() {
		return profileUrl;
	}

	/**
	 * Updates the contact person Public profile URL
	 * 
	 * @param profileUrl
	 *            contact person Public profile URL
	 */
	public void setProfileUrl(final String profileUrl) {
		this.profileUrl = profileUrl;
	}

	/**
	 * Retrieves the contact person id
	 * 
	 * @return String contact person id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Updates the contact person id
	 * 
	 * @param id
	 *            contact person id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Retrieves the email hash
	 * 
	 * @return String contact person email hash
	 */
	public String getEmailHash() {
		return emailHash;
	}

	/**
	 * Updates the contact person email hash
	 * 
	 * @param emailHash
	 *            contact person email hash
	 */
	public void setEmailHash(final String emailHash) {
		this.emailHash = emailHash;
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
		result.append(" displayName: " + displayName + NEW_LINE);
		result.append(" id: " + id + NEW_LINE);
		result.append(" profileUrl: " + profileUrl + NEW_LINE);
		result.append(" profileImageURL: " + profileImageURL + NEW_LINE);
		result.append("emailHash: " + emailHash + NEW_LINE);
		result.append(" otherEmails: ");
		if (otherEmails != null) {
			StringBuilder estr = new StringBuilder();
			for (String str : otherEmails) {
				if (estr.length() > 0) {
					estr.append(" , ");
				}
				estr.append(str);
			}
			result.append(estr.toString());
		}
		result.append(NEW_LINE);
		result.append("}");
		return result.toString();
	}

}
