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

package org.brickred.socialauth.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.exception.AccessTokenExpireException;
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provider implementation for GooglePlus. Now google supports OAuth2.0
 * protocol, so if you are registering your application now on google, use this
 * provider and set the key/secret in configuration accordingly. Please visit <a
 * href="https://github.com/3pillarlabs/socialauth/wiki/Sample-Properties"
 * >Sample Properties</a> to configure GooglePlus key/secret.
 * 
 * @author tarun.nagpal
 * 
 */
public class GooglePlusImpl extends AbstractProvider {

    private static final long serialVersionUID = 8644510564735754296L;
    private static final String PROFILE_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final String CONTACTS_FEED_URL_TEMPLATE = "https://www.google.com/m8/feeds/contacts/default/full/?start-index=%s&max-results=%s";
    private static final String GDATA_NAMESPACE = "http://schemas.google.com/g/2005";
    private static final String CONTACT_NAMESPACE = "http://schemas.google.com/contact/2008";
    private static final Map<String, String> ENDPOINTS;
    private static final String state = "SocialAuth" + System.currentTimeMillis();

    private final Log LOG = LogFactory.getLog(GooglePlusImpl.class);

    private Permission scope;
    private OAuthConfig config;
    private Profile userProfile;
    private AccessGrant accessGrant;
    private OAuthStrategyBase authenticationStrategy;

    // set this to the list of extended permissions you want
    private static final String[] AllPerms = new String[] { "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/plus.login",
            "https://www.google.com/m8/feeds", "https://picasaweb.google.com/data/" };

    private static final String[] AuthPerms = new String[] { "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email" };

    static {
        ENDPOINTS = new HashMap<String, String>();
        ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL, "https://accounts.google.com/o/oauth2/auth" + "?" + Constants.STATE
                + "=" + state);
        ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL, "https://accounts.google.com/o/oauth2/token");
    }

    /**
     * Stores configuration for the provider
     * 
     * @param providerConfig
     *            It contains the configuration of application like consumer key
     *            and consumer secret
     * @throws Exception
     */
    public GooglePlusImpl(final OAuthConfig providerConfig) throws Exception {
        config = providerConfig;

        if (config.getCustomPermissions() != null) {
            scope = Permission.CUSTOM;
        }

        if (config.getAuthenticationUrl() != null) {
            ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL, config.getAuthenticationUrl());
        } else {
            config.setAuthenticationUrl(ENDPOINTS.get(Constants.OAUTH_AUTHORIZATION_URL));
        }

        if (config.getAccessTokenUrl() != null) {
            ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL, config.getAccessTokenUrl());
        } else {
            config.setAccessTokenUrl(ENDPOINTS.get(Constants.OAUTH_ACCESS_TOKEN_URL));
        }

        authenticationStrategy = new OAuth2(config, ENDPOINTS);
        authenticationStrategy.setPermission(scope);
        authenticationStrategy.setScope(getScope());
    }

    /**
     * Stores access grant for the provider
     * 
     * @param accessGrant
     *            It contains the access token and other information
     * @throws AccessTokenExpireException
     */
    @Override
    public void setAccessGrant(final AccessGrant accessGrant) throws AccessTokenExpireException {
        this.accessGrant = accessGrant;
        authenticationStrategy.setAccessGrant(accessGrant);
    }

    /**
     * This is the most important action. It redirects the browser to an
     * appropriate URL which will be used for authentication with the provider
     * that has been set using setId()
     * 
     */
    @Override
    public String getLoginRedirectURL(final String successUrl) throws Exception {
        return authenticationStrategy.getLoginRedirectURL(successUrl);
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
    public Profile verifyResponse(final Map<String, String> requestParams) throws Exception {
        if (requestParams.containsKey(Constants.STATE)) {
            String stateStr = requestParams.get(Constants.STATE);
            if (!state.equals(stateStr)) {
                throw new SocialAuthException("State parameter value does not match with expected value");
            }
        }

        return doVerifyResponse(requestParams);
    }

    private Profile doVerifyResponse(final Map<String, String> requestParams) throws Exception {
        LOG.info("Retrieving Access Token in verify response function");
        if (requestParams.get("error_reason") != null && "user_denied".equals(requestParams.get("error_reason"))) {
            throw new UserDeniedPermissionException();
        }
        accessGrant = authenticationStrategy.verifyResponse(requestParams, MethodType.POST.toString());

        if (accessGrant != null) {
            LOG.debug("Obtaining user profile");
            return getProfile();
        } else {
            throw new SocialAuthException("Access token not found");
        }
    }

    private Profile getProfile() throws Exception {
        String presp;

        try {
            Response response = authenticationStrategy.executeFeed(PROFILE_URL);
            presp = response.getResponseBodyAsString(Constants.ENCODING);
        } catch (Exception e) {
            throw new SocialAuthException("Error while getting profile from " + PROFILE_URL, e);
        }
        try {
            LOG.debug("User Profile : " + presp);
            JSONObject resp = new JSONObject(presp);
            Profile p = new Profile();
            p.setValidatedId(resp.getString("id"));
            if (resp.has("name")) {
                p.setFullName(resp.getString("name"));
            }
            if (resp.has("given_name")) {
                p.setFirstName(resp.getString("given_name"));
            }
            if (resp.has("family_name")) {
                p.setLastName(resp.getString("family_name"));
            }
            if (resp.has("email")) {
                p.setEmail(resp.getString("email"));
            }
            if (resp.has("gender")) {
                p.setGender(resp.getString("gender"));
            }
            if (resp.has("picture")) {
                p.setProfileImageURL(resp.getString("picture"));
            }
            if (resp.has("id")) {
                p.setValidatedId(resp.getString("id"));
            }
            if (config.isSaveRawResponse()) {
                p.setRawResponse(presp);
            }

            p.setProviderId(getProviderId());
            userProfile = p;
            return p;

        } catch (Exception ex) {
            throw new ServerDataException("Failed to parse the user profile json : " + presp, ex);
        }
    }

    /**
     * Updates the status on the chosen provider if available. This may not be
     * implemented for all providers.
     * 
     * @param msg
     *            Message to be shown as user's status
     * @throws Exception
     */

    @Override
    public Response updateStatus(final String msg) throws Exception {
        LOG.warn("WARNING: Not implemented for GooglePlus");
        throw new SocialAuthException("Update Status is not implemented for GooglePlus");
    }

    @Override
    public List<Contact> getContactList() throws Exception {
        return getContactList(1, 1000);
    }

    @Override
    public List<Contact> getContactList(int startIndex, int pageSize) throws Exception {
        final String contactsFeedUrl = String.format(CONTACTS_FEED_URL_TEMPLATE, startIndex, pageSize);

        LOG.info("Fetching contacts from " + contactsFeedUrl);
        if (Permission.AUTHENTICATE_ONLY.equals(this.scope)) {
            throw new SocialAuthException("You have not set Permission to get contacts.");
        }
        Response serviceResponse = null;
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("Authorization", "Bearer " + getAccessGrant().getKey());
            map.put("GData-Version", "3.0");
            serviceResponse = authenticationStrategy.executeFeed(contactsFeedUrl, null, null, map, null);
        } catch (Exception ie) {
            throw new SocialAuthException("Failed to retrieve the contacts from " + contactsFeedUrl, ie);
        }
        List<Contact> plist = new ArrayList<Contact>();
        Element root;

        try {
            root = XMLParseUtil.loadXmlResource(serviceResponse.getInputStream());
        } catch (Exception e) {
            throw new ServerDataException("Failed to parse the contacts from response." + contactsFeedUrl, e);
        }
        NodeList contactsList = root.getElementsByTagName("entry");
        if (contactsList != null && contactsList.getLength() > 0) {
            LOG.debug("Found contacts : " + contactsList.getLength());
            for (int i = 0; i < contactsList.getLength(); i++) {
                Element contact = (Element) contactsList.item(i);
                String fname = "";
                NodeList l = contact.getElementsByTagNameNS(GDATA_NAMESPACE, "email");
                String address = null;
                String emailArr[] = null;
                if (l != null && l.getLength() > 0) {
                    Element el = (Element) l.item(0);
                    if (el != null) {
                        address = el.getAttribute("address");
                    }
                    if (l.getLength() > 1) {
                        emailArr = new String[l.getLength() - 1];
                        for (int k = 1; k < l.getLength(); k++) {
                            Element e = (Element) l.item(k);
                            if (e != null) {
                                emailArr[k - 1] = e.getAttribute("address");
                            }
                        }
                    }
                }
                String lname = "";
                String dispName = XMLParseUtil.getElementData(contact, "title");
                if (dispName != null) {
                    String sarr[] = dispName.split(" ");
                    if (sarr.length > 0) {
                        if (sarr.length >= 1) {
                            fname = sarr[0];
                        }
                        if (sarr.length >= 2) {
                            StringBuilder sb = new StringBuilder();
                            for (int k = 1; k < sarr.length; k++) {
                                sb.append(sarr[k]).append(" ");
                            }
                            lname = sb.toString();
                        }
                    }
                }

                List<String> phoneNumbers = new ArrayList<String>();
                NodeList phoneNumberNodes = contact.getElementsByTagNameNS(GDATA_NAMESPACE, "phoneNumber");
                if (phoneNumberNodes != null && phoneNumberNodes.getLength() > 0) {
                    for (int j = 0; j < phoneNumberNodes.getLength(); j++) {
                        phoneNumbers.add(phoneNumberNodes.item(j).getTextContent());
                    }
                }

                String profileUrl = null;
                String imageUrl = null;
                NodeList profileUrlNodes = contact.getElementsByTagNameNS(CONTACT_NAMESPACE, "website");
                if (profileUrlNodes != null && profileUrlNodes.getLength() > 0) {
                    for (int j = 0; j < profileUrlNodes.getLength(); j++) {
                        Node profileUrlHref = profileUrlNodes.item(j).getAttributes().getNamedItem("href");
                        
                        if(profileUrlHref != null) {
                        
                            // Filter out the G+ profile URL
                            String candidateProfileUrl = profileUrlHref.getTextContent();
                            if(candidateProfileUrl.contains("google.com")) {
                                profileUrl = candidateProfileUrl;
                                break;
                            }
                        }
                    }
                    
                    if(profileUrl != null) {
                        
                        // We can get the profile picture from Picasa with the G+ profile ID (does not count in API rate limits)
                        try {
                            String[] profileUrlParts = profileUrl.split("/");
                            String profileId = profileUrlParts[profileUrlParts.length - 1];
                            String imageJsonUrl = String.format("http://picasaweb.google.com/data/entry/api/user/%s?alt=json", profileId);
                            LOG.debug("Getting profile image JSON from: " + imageJsonUrl);
                            
                            HttpMethod getImageJsonMethod = new GetMethod(imageJsonUrl);
                            HttpClient client = new HttpClient();
                            client.executeMethod(getImageJsonMethod);
                            String imageJson = getImageJsonMethod.getResponseBodyAsString();
                            LOG.debug("Got profile image JSON: " + imageJson);
                            
                            imageUrl = new JSONObject(imageJson).getJSONObject("entry").getJSONObject("gphoto$thumbnail").getString("$t");
                            LOG.debug("Profile image URL: " + imageUrl);
                        } catch(Exception ignore) {
                            LOG.debug("Could not retrieve profile image URL", ignore);
                        }
                    }
                }
                
                String id = XMLParseUtil.getElementData(contact, "id");

                if (address != null && address.length() > 0) {
                    Contact p = new Contact();
                    p.setFirstName(fname);
                    p.setLastName(lname);
                    p.setEmail(address);
                    p.setDisplayName(dispName);
                    p.setOtherEmails(emailArr);
                    p.setPhoneNumbers(phoneNumbers.toArray(new String[phoneNumbers.size()]));
                    p.setProfileUrl(profileUrl);
                    p.setProfileImageURL(imageUrl);
                    p.setId(id);
                    if (config.isSaveRawResponse()) {
                        p.setRawResponse(XMLParseUtil.getStringFromElement(contact));
                    }
                    plist.add(p);
                }
            }
        } else {
            LOG.debug("No contacts were obtained from the feed : " + contactsFeedUrl);
        }
        return plist;
    }

    /**
     * Logout
     */
    @Override
    public void logout() {
        accessGrant = null;
        authenticationStrategy.logout();
    }

    /**
     * 
     * @param p
     *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
     *            Permission.ALL, Permission.DEFAULT
     */
    @Override
    public void setPermission(final Permission p) {
        LOG.debug("Permission requested : " + p.toString());
        this.scope = p;
        authenticationStrategy.setPermission(this.scope);
        authenticationStrategy.setScope(getScope());
    }

    /**
     * Makes HTTP request to a given URL.It attaches access token in URL.
     * 
     * @param url
     *            URL to make HTTP request.
     * @param methodType
     *            Method type can be GET, POST or PUT
     * @param params
     *            Not using this parameter in Google API function
     * @param headerParams
     *            Parameters need to pass as Header Parameters
     * @param body
     *            Request Body
     * @return Response object
     * @throws Exception
     */
    @Override
    public Response api(final String url, final String methodType, final Map<String, String> params,
            final Map<String, String> headerParams, final String body) throws Exception {
        LOG.info("Calling api function for url	:	" + url);
        Response response = null;
        try {
            response = authenticationStrategy.executeFeed(url, methodType, params, headerParams, body);
        } catch (Exception e) {
            throw new SocialAuthException("Error while making request to URL : " + url, e);
        }
        return response;
    }

    /**
     * Retrieves the user profile.
     * 
     * @return Profile object containing the profile information.
     */
    @Override
    public Profile getUserProfile() throws Exception {
        if (userProfile == null && accessGrant != null) {
            getProfile();
        }
        return userProfile;
    }

    @Override
    public AccessGrant getAccessGrant() {
        return accessGrant;
    }

    @Override
    public String getProviderId() {
        return config.getId();
    }

    @Override
    public Response uploadImage(final String message, final String fileName, final InputStream inputStream) throws Exception {
        LOG.warn("WARNING: Not implemented for GooglePlus");
        throw new SocialAuthException("Upload Image is not implemented for GooglePlus");
    }

    private String getScope() {
        StringBuffer result = new StringBuffer();
        String arr[] = null;
        if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
            arr = AuthPerms;
        } else if (Permission.CUSTOM.equals(scope) && config.getCustomPermissions() != null) {
            arr = config.getCustomPermissions().split(",");
        } else {
            arr = AllPerms;
        }
        result.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            result.append("+").append(arr[i]);
        }
        String pluginScopes = getPluginsScope(config);
        if (pluginScopes != null) {
            result.append("+").append(pluginScopes);
        }
        return result.toString();
    }

    @Override
    protected List<String> getPluginsList() {
        List<String> list = new ArrayList<String>();
        list.add("org.brickred.socialauth.plugin.googleplus.FeedPluginImpl");
        list.add("org.brickred.socialauth.plugin.googleplus.AlbumsPluginImpl");
        if (config.getRegisteredPlugins() != null && config.getRegisteredPlugins().length > 0) {
            list.addAll(Arrays.asList(config.getRegisteredPlugins()));
        }
        return list;
    }

    @Override
    protected OAuthStrategyBase getOauthStrategy() {
        return authenticationStrategy;
    }

}