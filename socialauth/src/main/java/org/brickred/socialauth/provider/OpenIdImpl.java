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

package org.brickred.socialauth.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.exception.AccessTokenExpireException;
import org.brickred.socialauth.exception.ProviderStateException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * Implementation of Open ID provider. Currently only name and email has been
 * implemented as part of profile. Other functionality like updating status and
 * importing contacts is not available for generic Open ID providers
 */
public class OpenIdImpl extends AbstractProvider implements AuthProvider,
		Serializable {

	private static final long serialVersionUID = 7694191649303094756L;
	private final Log LOG = LogFactory.getLog(OpenIdImpl.class);

	private ConsumerManager manager;
	private DiscoveryInformation discovered;
	private String id;
	private AccessGrant accessGrant;
	private String successUrl;
	private Profile userProfile;
	private boolean providerState = false;

	public OpenIdImpl(final Properties props) throws ConsumerException,
			Exception {
		manager = new ConsumerManager();
		discovered = null;
		this.id = props.getProperty("id");
	}

	public OpenIdImpl(final OAuthConfig config) throws ConsumerException,
			Exception {
		manager = new ConsumerManager();
		this.id = config.getId();
		discovered = null;
	}

	@Override
	public void setAccessGrant(final AccessGrant accessGrant)
			throws AccessTokenExpireException {
		try {
			manager = new ConsumerManager();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		discovered = null;
		this.accessGrant = accessGrant;
	}

	/**
	 * This is the most important action. It redirects the browser to an
	 * appropriate URL which will be used for authentication with the provider
	 * that has been set using setId()
	 * 
	 * @throws Exception
	 */
	@Override
	public String getLoginRedirectURL(final String successUrl)
			throws IOException {
		providerState = true;
		String url = authRequest(id, successUrl);
		this.successUrl = successUrl;
		LOG.info("Redirection to following URL should happen : " + url);
		return url;
	}

	private String authRequest(final String userSuppliedString,
			final String returnToUrl) throws IOException {
		try {
			// perform discovery on the user-supplied identifier
			List discoveries = manager.discover(userSuppliedString);

			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			discovered = manager.associate(discoveries);

			// // store the discovery information in the user's session
			// httpReq.getSession().setAttribute("openid-disc", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

			// Attribute Exchange example: fetching the 'email' attribute
			FetchRequest fetch = FetchRequest.createFetchRequest();

			// Using axschema
			fetch.addAttribute("emailax", "http://axschema.org/contact/email",
					true);

			fetch.addAttribute("firstnameax",
					"http://axschema.org/namePerson/first", true);

			fetch.addAttribute("lastnameax",
					"http://axschema.org/namePerson/last", true);

			fetch.addAttribute("fullnameax", "http://axschema.org/namePerson",
					true);

			fetch.addAttribute("email",
					"http://schema.openid.net/contact/email", true);

			// Using schema.openid.net (for compatibility)
			fetch.addAttribute("firstname",
					"http://schema.openid.net/namePerson/first", true);

			fetch.addAttribute("lastname",
					"http://schema.openid.net/namePerson/last", true);

			fetch.addAttribute("fullname",
					"http://schema.openid.net/namePerson", true);

			// attach the extension to the authentication request
			authReq.addExtension(fetch);

			return authReq.getDestinationUrl(true);
		} catch (OpenIDException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * 
	 * @param requestParams
	 *            request parameters, received from the provider
	 * @return Profile object containing the profile information
	 * @throws Exception
	 */

	@Override
	public Profile verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		if (!providerState) {
			throw new ProviderStateException();
		}
		try {
			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList response = new ParameterList(requestParams);

			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = new StringBuffer();
			receivingURL.append(successUrl);
			StringBuffer sb = new StringBuffer();
			for (Map.Entry<String, String> entry : requestParams.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(key).append("=").append(value);
			}
			receivingURL.append("?").append(sb.toString());

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = manager.verify(
					receivingURL.toString(), response, discovered);

			// examine the verification result and extract the verified
			// identifier
			Identifier verified = verification.getVerifiedId();
			if (verified != null) {
				LOG.debug("Verified Id : " + verified.getIdentifier());
				Profile p = new Profile();
				p.setValidatedId(verified.getIdentifier());
				AuthSuccess authSuccess = (AuthSuccess) verification
						.getAuthResponse();

				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					FetchResponse fetchResp = (FetchResponse) authSuccess
							.getExtension(AxMessage.OPENID_NS_AX);

					p.setEmail(fetchResp.getAttributeValue("email"));
					p.setFirstName(fetchResp.getAttributeValue("firstname"));
					p.setLastName(fetchResp.getAttributeValue("lastname"));
					p.setFullName(fetchResp.getAttributeValue("fullname"));

					// also use the ax namespace for compatibility
					if (p.getEmail() == null) {
						p.setEmail(fetchResp.getAttributeValue("emailax"));
					}
					if (p.getFirstName() == null) {
						p.setFirstName(fetchResp
								.getAttributeValue("firstnameax"));
					}
					if (p.getLastName() == null) {
						p.setLastName(fetchResp.getAttributeValue("lastnameax"));
					}
					if (p.getFullName() == null) {
						p.setFullName(fetchResp.getAttributeValue("fullnameax"));
					}

				}
				userProfile = p;
				return p;
			}
		} catch (OpenIDException e) {
			throw e;
		}

		return null;
	}

	/**
	 * Updating status is not available for generic Open ID providers.
	 */
	@Override
	public Response updateStatus(final String msg) throws Exception {
		LOG.warn("WARNING: Not implemented for OpenId");
		throw new SocialAuthException(
				"Update Status is not implemented for OpenId");
	}

	/**
	 * Contact list is not available for generic Open ID providers.
	 * 
	 * @return null
	 */
	@Override
	public List<Contact> getContactList() {
		LOG.info("Contacts are not available in OpenId");
		return null;
	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		discovered = null;
	}

	/**
	 * 
	 * @param p
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	@Override
	public void setPermission(final Permission p) {
		LOG.warn("Setting Permission for openid is not valid.");
	}

	/**
	 * Not implemented for OpenId provider.
	 * 
	 * @param url
	 * @param methodType
	 * @param params
	 * @param headerParams
	 * @param body
	 * @return Response object
	 * @throws Exception
	 */
	@Override
	public Response api(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception {
		LOG.warn("WARNING: API method is not implemented for OpenId");
		throw new SocialAuthException(
				"API method is not implemented for OpenId");
	}

	/**
	 * Retrieves the user profile. Null in case of OpenId provider.
	 * 
	 * @return Profile object containing the profile information.
	 */
	@Override
	public Profile getUserProfile() {
		return userProfile;
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessGrant;
	}

	@Override
	public String getProviderId() {
		return id;
	}

	@Override
	public Response uploadImage(final String message, final String fileName,
			final InputStream inputStream) throws Exception {
		LOG.warn("WARNING: Not implemented for OpenId");
		throw new SocialAuthException(
				"Upload Image is not implemented for OpenId");
	}

	@Override
	protected OAuthStrategyBase getOauthStrategy() {
		return null;
	}

	@Override
	protected List<String> getPluginsList() {
		// TODO Auto-generated method stub
		return null;
	}
}
