package de.deltatree.social.web.filter.impl.tools;

import javax.servlet.FilterConfig;

public class InitParamUtil {
	public static String getInitParam(FilterConfig conf, String param) {
		String value = conf.getInitParameter(param);
		if (value == null) {
			value = conf.getServletContext().getInitParameter(param);
			if (value == null)
				throw new IllegalArgumentException("InitParameter [" + param
						+ "] does not exist in web.xml -> Please add!!!");
		}
		return value;
	}

}
