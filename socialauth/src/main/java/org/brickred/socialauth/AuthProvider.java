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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.brickred.socialauth.exception.AccessTokenExpireException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.plugin.Plugin;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Response;

/**
 * This is the main interface representing an authentication provider. First we
 * call the getLoginRedirectURL method to get the URL where the user needs to be
 * redirected. It is the responsibility of the caller to redirect the user to
 * that URL.
 * 
 * Once the external provider like Facebook redirects the user back to our
 * application, we call the verifyResponse method and pass along the HttpRequest
 * object that is called upon redirection.
 * 
 * If the verifyResponse method returns a non null profile object, we can start
 * calling the other methods to obtain user information, update status or import
 * contacts
 * 
 * @author Abhinav Maheshwari
 * 
 */

public interface AuthProvider {

	String EXT_NAMESPACE = "http://specs.openid.net/extensions/oauth/1.0";
	String EMAIL = "email";
	String COUNTRY = "country";
	String LANGUAGE = "language";
	String FULL_NAME = "fullname";
	String NICK_NAME = "nickname";
	String DOB = "dob";
	String GENDER = "gender";
	String POSTCODE = "postcode";
	String FIRST_NAME = "firstname";
	String LAST_NAME = "lastname";

	/**
	 * This is the most important action. It redirects the browser to an
	 * appropriate URL which will be used for authentication with the provider
	 * that has been set using setId()
	 * 
	 * @throws Exception
	 */
	public String getLoginRedirectURL(String successUrl) throws Exception;

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * @param requestParams
	 *            Request parameters received from the provider
	 * @return AuthProvider object
	 * @throws Exception
	 */
	public Profile verifyResponse(Map<String, String> requestParams)
			throws Exception;

	/**
	 * Updates the status on the chosen provider if available. This may not be
	 * implemented for all providers.
	 * 
	 * @param msg
	 *            Message to be shown as user's status
	 */
	public Response updateStatus(String msg) throws Exception;

	/**
	 * Gets the list of contacts of the user and their email. this may not be
	 * available for all providers.
	 * 
	 * @return List of profile objects representing Contacts. Only name and
	 *         email will be available
	 */
	public List<Contact> getContactList() throws Exception;

	/**
	 * Retrieves the user profile.
	 * 
	 * @return Profile object containing the profile information.
	 * @throws Exception
	 */
	public Profile getUserProfile() throws Exception;

	/**
	 * Logout
	 */
	public void logout();

	/**
	 * 
	 * @param p
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	public void setPermission(final Permission p);

	/**
	 * Makes OAuth signed HTTP request to a given URL for making any provider
	 * specific calls. For more information, read the comments of this function
	 * in different provider.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Any additional parameters whose signature need to compute.
	 *            Only used in case of "POST" and "PUT" method type.
	 * @param headerParams
	 *            Any additional parameters need to pass as Header Parameters
	 * @param body
	 *            Request Body
	 * @return Response object
	 * @throws Exception
	 */
	public Response api(String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception;

	/**
	 * Retrieves the AccessGrant object.
	 * 
	 * @return AccessGrant object.
	 */
	public AccessGrant getAccessGrant();

	/**
	 * Retrieves the provider id
	 * 
	 * @return provider id.
	 */
	public String getProviderId();

	/**
	 * Stores access grant for the provider *
	 * 
	 * @param accessGrant
	 *            It contains the access token and other information
	 * @throws AccessTokenExpireException
	 */
	public void setAccessGrant(AccessGrant accessGrant)
			throws AccessTokenExpireException, SocialAuthException;

	/**
	 * Updates the image and message on the chosen provider if available. This
	 * is implemented only for Facebook and Twitter.
	 * 
	 * @param message
	 *            Status Message
	 * @param fileName
	 *            Image file name
	 * @param inputStream
	 *            Input Stream of image
	 * @return Response object
	 * @throws Exception
	 */
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception;

	/**
	 * Returns True if provider support given plugin otherwise returns False
	 * 
	 * @param clazz
	 *            Fully qualified plugin class name e.g
	 *            <b>org.brickred.socialauth.plugin.FeedPlugin.class</b>
	 * @return true if provider supports plugin otherwise false
	 */
	public boolean isSupportedPlugin(final Class<? extends Plugin> clazz);

	/**
	 * Returns the required plugin if provider support that.
	 * 
	 * @param clazz
	 *            Fully qualified plugin class name e.g
	 *            <b>org.brickred.socialauth.plugin.FeedPlugin.class</b>
	 * 
	 * @return the required plugin object
	 * @throws Exception
	 */
	public <T> T getPlugin(final Class<T> clazz) throws Exception;

	/**
	 * Registers plugin for a provider those are configured in configuration
	 * properties or mentioned in provider implementation.
	 * 
	 * @throws Exception
	 */
	public void registerPlugins() throws Exception;

	/**
	 * Makes a call for a provider to get RefreshToken and returns object of
	 * that provider
	 * 
	 * @param accessGrant
	 *            AccessGrant which contains AccessToken
	 * @return object of provider
	 * @throws SocialAuthException
	 */
	public void refreshToken(AccessGrant accessGrant)
			throws SocialAuthException;
}
