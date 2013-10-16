package org.brickred

import org.brickred.socialauth.SocialAuthManager
import org.codehaus.groovy.grails.web.pages.ext.jsp.GroovyPagesPageContext

import de.deltatree.social.web.filter.api.SASFHelper
import de.deltatree.social.web.filter.api.SASFStaticHelper

class SocialAuthController {

    def index = {
		println "id:::"+params.id;
		redirect(uri: "/SAF/SocialAuth?id=${params.id}")
	}
	
	def signout = {
		SASFHelper helper = SASFStaticHelper.getHelper(request)
		SocialAuthManager socialAuthManager
		if(helper!=null){
			socialAuthManager = helper.getAuthManager()
			if(socialAuthManager != null){
				socialAuthManager.disconnectProvider(params.id)
			}else{
				flash.message = "Unable to logout from "+params.id
			}
		}else{
			flash.message = "Unable to logout from "+params.id
		}
		redirect(uri: "/index.gsp")
	}
}
