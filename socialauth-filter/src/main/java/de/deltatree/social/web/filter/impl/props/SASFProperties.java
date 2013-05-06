package de.deltatree.social.web.filter.impl.props;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SASFProperties {

	private static final String PROPERTY_OAUTH_CONSUMERS = "oauth_consumers";
	private static final String PROPERTY_WEBAPP_SUCCESS_ACTION = "webapp.success.action";
	private static final String PROPERTY_FILTER_URL = "filter.url";
	private static final String PROPERTY_ERROR_URL = "error.page.url";
	private final Properties props;
	private final String oauthPropertiesFileString;
	private final String openidReturnUrl;
	private final String servlet_main;
	private final String servlet_main_success;
	private final String servlet_main_logoff;
	private final String webapp_success_action;
	private final String error_page;
	private final String filterURL;

	public SASFProperties(String configFile) throws SASFPropertiesException {
		this.props = initProperties(configFile);

		this.filterURL = getProperty(PROPERTY_FILTER_URL);

		this.servlet_main = filterURL + "/SocialAuth";
		this.servlet_main_success = this.servlet_main + "Success";

		this.servlet_main_logoff = this.servlet_main + "Logoff";

		this.webapp_success_action = getProperty(PROPERTY_WEBAPP_SUCCESS_ACTION);

		this.oauthPropertiesFileString = getProperty(PROPERTY_OAUTH_CONSUMERS);

		this.openidReturnUrl = servlet_main_success;
		this.error_page=getProperty(PROPERTY_ERROR_URL);
	}

	public String getFilterURL() {
		return filterURL;
	}

	public String getProperty(String propertyName) {
		String property = props.getProperty(propertyName);
		if (property == null)
			throw new NullPointerException("property is null [" + propertyName
					+ "]");
		return property;
	}

	public String getProperty(String key, String defaultValue) {
		return this.props.getProperty(key, defaultValue);
	}

	private Properties initProperties(String fileString)
			throws SASFPropertiesException {

		InputStream in = SASFProperties.class.getClassLoader()
				.getResourceAsStream(fileString);

		if (in == null)
			throw new SASFPropertiesException("file does not exist: ["
					+ fileString + "]");

		Properties properties = new Properties();
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(in);
			properties.load(stream);
		} catch (IOException e) {
			throw new SASFPropertiesException(
					"load of property file not possible: [" + fileString
							+ "]: " + e.getMessage());
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				throw new SASFPropertiesException(
						"closing property file not possible: [" + fileString
								+ "]: " + e.getMessage());
			}
		}
		return properties;
	}

	public String getOauthPropertiesFileString() {
		return this.oauthPropertiesFileString;
	}

	public String getOpenidReturnUrl() {
		return openidReturnUrl;
	}

	public String getServletMain() {
		return this.servlet_main;
	}

	public String getServletMainSuccess() {
		return this.servlet_main_success;
	}

	public String getServletMainLogoff() {
		return this.servlet_main_logoff;
	}

	public String webappSuccessAction() {
		return this.webapp_success_action;
	}
	
	public String getErrorPage() {
		return this.error_page;
	}

}
