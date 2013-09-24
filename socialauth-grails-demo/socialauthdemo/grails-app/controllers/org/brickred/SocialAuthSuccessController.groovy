package org.brickred

import org.brickred.socialauth.Contact
import org.brickred.socialauth.Profile

import de.deltatree.social.web.filter.api.SASFHelper
import de.deltatree.social.web.filter.api.SASFStaticHelper

class SocialAuthSuccessController {

    def index = {
		SASFHelper helper = SASFStaticHelper.getHelper(request);
		Profile profile = helper.getProfile();
		List<Contact> contactsList = helper.getContactList();
		println profile;
		[profile:profile,contacts:contactsList]
	}
}

