package com.threepillar.labs.socialauthsample.bean;

public class User {
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
	private String name;

	/**
	 * Display Name
	 */
	private String displayName;

	/**
	 * Date of Birth
	 */
	private String dob;

	/**
	 * Gender
	 */
	private String gender;

	/**
	 * Location
	 */
	private String location;

	/**
	 * profile image URL
	 */
	private String profileImageURL;

	/**
	 * provider id with this profile associates
	 */
	private String providerId;

	/**
	 * Unique id
	 */
	private String uniqueId;

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
	public String getName() {
		return name;
	}

	/**
	 * Updates the name
	 * 
	 * @param name
	 *            the full name of user
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Retrieves the date of birth
	 * 
	 * @return the date of birth different providers may use different formats
	 */
	public String getDob() {
		return dob;
	}

	/**
	 * Updates the date of birth
	 * 
	 * @param dob
	 *            the date of birth of user
	 */
	public void setDob(final String dob) {
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
		result.append(" name: " + name + NEW_LINE);
		result.append(" displayName: " + displayName + NEW_LINE);
		result.append(" dob: " + dob + NEW_LINE);
		result.append(" gender: " + gender + NEW_LINE);
		result.append(" location: " + location + NEW_LINE);
		result.append(" uniqueId: " + uniqueId + NEW_LINE);
		result.append(" profileImageURL: " + profileImageURL + NEW_LINE);
		result.append(" providerId: " + providerId + NEW_LINE);
		result.append("}");

		return result.toString();

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(final String uniqueId) {
		this.uniqueId = uniqueId;
	}
}
