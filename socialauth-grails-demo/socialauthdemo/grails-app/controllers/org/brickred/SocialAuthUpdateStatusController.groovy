package org.brickred

import org.brickred.socialauth.AuthProvider
import org.brickred.socialauth.SocialAuthManager
import org.brickred.socialauth.exception.SocialAuthException

import de.deltatree.social.web.filter.api.SASFHelper
import de.deltatree.social.web.filter.api.SASFStaticHelper

class SocialAuthUpdateStatusController {

    def index = {
		def statusMsg = params.statusMessage
		def callbackStatus
		def callbackMesg
		if (statusMsg == null || statusMsg.trim().length() == 0) {
			callbackMesg =  "Status can't be left blank.";
		}else{

			SASFHelper helper = SASFStaticHelper.getHelper(request);
			SocialAuthManager manager = helper.getAuthManager();
	
			AuthProvider provider = null;
			if (manager != null) {
				provider = manager.getCurrentAuthProvider();
			}
			if (provider != null) {
				try {
					provider.updateStatus(statusMsg);
					callbackMesg =  "Status Updated successfully";
				} catch (SocialAuthException e) {
					callbackMesg =  e.getMessage();
					e.printStackTrace();
				}
			}else{
				callbackMesg =  "Unable to upload status"
			}
		}
		[callbackMesg:callbackMesg]
	}
}
