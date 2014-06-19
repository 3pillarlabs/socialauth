package org.brickred.socialauth.provider;

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
import org.brickred.socialauth.util.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

/**
 * @author artemgapchenko
 * Created on 16.06.14.
 */
public class VKImpl extends AbstractProvider {
    private static final Log LOG = LogFactory.getLog(VKImpl.class);

    private static final String[] FULL_PERMISSIONS = { "photos", "wall" };

    private static final Map<String, String> ENDPOINTS;

    private static final String API_VERSION = "5.21";

    static {
        ENDPOINTS = new HashMap<String, String>();
        ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL, "https://oauth.vk.com/authorize");
        ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL, "https://api.vkontakte.ru/oauth/access_token");
    }

    private static final String PROFILE_URL = "https://api.vk.com/method/account.getProfileInfo?v=5.21";
    private static final String WALL_UPLOAD_SERVER_URL = "https://api.vk.com/method/photos.getWallUploadServer?v=5.21";
    private static final String PHOTO_SAVE_URL = "https://api.vk.com/method/photos.saveWallPhoto?v=5.21";
    private static final String WALL_POST_URL = "https://api.vk.com/method/wall.post?v=5.21";

    private static final long serialVersionUID = -2223447470772943798L;

    private final OAuthConfig oAuthConfig;
    private final OAuthStrategyBase authenticationStrategy;

    private AccessGrant accessGrant;
    private Permission scope;

    public VKImpl(final OAuthConfig config) throws Exception {
        oAuthConfig = config;

        if (config.getCustomPermissions() != null) scope = Permission.CUSTOM;

        if (config.getAuthenticationUrl() != null) {
            ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL, config.getAuthenticationUrl());
        } else {
            oAuthConfig.setAuthenticationUrl(ENDPOINTS.get(Constants.OAUTH_AUTHORIZATION_URL));
        }

        if (config.getAccessTokenUrl() != null) {
            ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL, config.getAccessTokenUrl());
        } else {
            oAuthConfig.setAccessTokenUrl(ENDPOINTS.get(Constants.OAUTH_ACCESS_TOKEN_URL));
        }

        authenticationStrategy = new OAuth2(config, ENDPOINTS);
        authenticationStrategy.setPermission(Permission.CUSTOM);
        authenticationStrategy.setScope(getScope());
    }

    private String getScope() {
        return null;
    }

    @Override
    protected List<String> getPluginsList() {
        final List<String> list = new ArrayList<String>();

        if (oAuthConfig.getRegisteredPlugins() != null && oAuthConfig.getRegisteredPlugins().length > 0) {
            list.addAll(Arrays.asList(oAuthConfig.getRegisteredPlugins()));
        }

        return list;
    }

    @Override
    protected OAuthStrategyBase getOauthStrategy() {
        return authenticationStrategy;
    }

    @Override
    public String getLoginRedirectURL(final String successUrl) throws Exception {
        String url = authenticationStrategy.getLoginRedirectURL(successUrl);

        // tweak the url because VK has some additional parameters
        url = url.replaceFirst("(?<=[?&;])response_type=.*?($|[&;])", "response_type=token&");
        url = url.replaceFirst("(?<=[?&;])type=.*?($|[&;])", "");
        url = url.concat("&v=" + API_VERSION);

        final StringBuilder sb = new StringBuilder();

        for (String l : FULL_PERMISSIONS) sb.append(",").append(l);

        url = url.concat("&scope=" + sb.toString().substring(1));

        return url;
    }

    @Override
    public Profile verifyResponse(Map<String, String> requestParams) throws Exception {
        if (requestParams.containsKey("error") && requestParams.get("error").equalsIgnoreCase("access_denied")) {
            throw new UserDeniedPermissionException();
        }

        accessGrant = authenticationStrategy.verifyResponse(requestParams, MethodType.POST.toString());

        if (accessGrant != null) {
            LOG.debug("Obtaining user profile");
            return obtainVKProfile(requestParams);
        } else {
            throw new SocialAuthException("Access token not found");
        }
    }

    private Profile obtainVKProfile(final Map<String, String> requestParams) throws SocialAuthException, ServerDataException {
        final Profile profile = new Profile();

        JSONObject json;
        String response;

        try {
            response = authenticationStrategy.executeFeed(PROFILE_URL).getResponseBodyAsString(Constants.ENCODING);
        } catch (Exception e) {
            throw new SocialAuthException("Error while getting profile from " + PROFILE_URL, e);
        }

        try {
            json = new JSONObject(response).getJSONObject("response");

            profile.setValidatedId(requestParams.get("user_id"));

            if (json.has("first_name")) profile.setFirstName(json.getString("first_name"));
            if (json.has("last_name")) profile.setLastName(json.getString("last_name"));
            if (json.has("sex")) profile.setGender(json.getString("sex"));
            if (json.has("city")) profile.setLocation(json.getString("city"));

            if (json.has("bdate")) {
                final BirthDate birthDate = new BirthDate();
                final String[] tokens = json.getString("bdate").split("\\.");

                birthDate.setDay(Integer.valueOf(tokens[0]));
                birthDate.setMonth(Integer.valueOf(tokens[1]));
                birthDate.setYear(Integer.valueOf(tokens[2]));

                profile.setDob(birthDate);
            }

            profile.setProviderId(getProviderId());
        } catch (JSONException e) {
            throw new ServerDataException("Failed to parse the user profile json : " + response, e);
        } catch (Exception e) {
            throw new ServerDataException("Failed to parse the user profile json : " + response, e);
        }

        return profile;
    }

    @Override
    public Response updateStatus(String msg) throws Exception {
        throw new UnsupportedOperationException("updateStatus(String) is not supported yet");
    }

    @Override
    public List<Contact> getContactList() throws Exception {
        throw new UnsupportedOperationException("getContactList() is not supported yet");
    }

    @Override
    public Profile getUserProfile() throws Exception {
        throw new UnsupportedOperationException("getUserProfile() is not supported yet");
    }

    @Override
    public void logout() {
        accessGrant = null;
        authenticationStrategy.logout();
    }

    @Override
    public void setPermission(final Permission p) {
        this.scope = p;
        authenticationStrategy.setPermission(this.scope);
        authenticationStrategy.setScope(getScope());
    }

    @Override
    public Response api(String url, String methodType, Map<String, String> params, Map<String, String> headerParams, String body) throws Exception {
        return null;
    }

    @Override
    public AccessGrant getAccessGrant() {
        return accessGrant;
    }

    @Override
    public String getProviderId() {
        return oAuthConfig.getId();
    }

    @Override
    public void setAccessGrant(AccessGrant accessGrant) throws AccessTokenExpireException, SocialAuthException {

    }

    @Override
    public Response uploadImage(String message, String fileName, InputStream inputStream) throws Exception {
        // Get server upload url
        Response response = authenticationStrategy.executeFeed(WALL_UPLOAD_SERVER_URL);

        if (response.getStatus() != 200) {
            throw new SocialAuthException("couldn't get image: " + response.getErrorStreamAsString(Constants.ENCODING));
        }

        // Upload image
        String albumUrl = new JSONObject(response.getResponseBodyAsString(Constants.ENCODING))
                .getJSONObject("response")
                .getString("upload_url");

        Map<String, String> params = new HashMap<String, String>();
        response = authenticationStrategy.uploadImage(
                albumUrl, MethodType.POST.toString(), params, null, fileName, inputStream, "photo", false
        );

        if (response.getStatus() != 200) {
            throw new SocialAuthException("couldn't upload an image: " + response.getErrorStreamAsString(Constants.ENCODING));
        }

        // Save it
        JSONObject result = new JSONObject(response.getResponseBodyAsString(Constants.ENCODING));

        params.clear();
        params.put("server", result.getString("server"));
        params.put("photo", result.getString("photo"));
        params.put("hash", result.getString("hash"));

        response = authenticationStrategy.executeFeed(PHOTO_SAVE_URL, MethodType.POST.toString(), params, null, null);

        if (response.getStatus() != 200) {
            throw new SocialAuthException("couldn't upload an image: " + response.getErrorStreamAsString(Constants.ENCODING));
        }

        // And post on the wall
        result = new JSONObject(response.getResponseBodyAsString(Constants.ENCODING)).getJSONArray("response").getJSONObject(0);

        params = new HashMap<String, String>();
        params.put("attachments", "photo" + result.get("owner_id") + "_" + result.get("id"));
        params.put("message", message);

        return authenticationStrategy.executeFeed(WALL_POST_URL, MethodType.POST.toString(), params, null, null);
    }
}