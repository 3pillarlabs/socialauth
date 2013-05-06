package de.deltatree.social.web.filter.api;

import javax.servlet.http.HttpServletRequest;

public class SASFStaticHelper {
	public static SASFHelper getHelper(HttpServletRequest request) {
		return (SASFHelper) request.getSession().getAttribute(
				SASFHelper.SESSION_KEY);
	}
}
