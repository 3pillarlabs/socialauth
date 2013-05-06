package de.deltatree.social.web.filter.api;

public interface SASFUser {
	public static final String SESSION_KEY = "S_SASFUser";

	public String getId();

	public String getFunctionalName();
}
