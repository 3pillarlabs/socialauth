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

package org.brickred.socialauth.seam;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.seam.Component;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;

/**
 * Modified version of org.jboss.seam.security.openid.OpenIdPhaseListener. This
 * version is a drop in replacement for the OpenIdPhaseListener that comes
 * bundled with the JBoss Seam framework. This listener is called when the
 * external provider returns the control back to our application
 */
@SuppressWarnings("serial")
public class SocialAuthPhaseListener implements PhaseListener {
	private transient LogProvider log = Logging
			.getLogProvider(SocialAuthPhaseListener.class);

	/**
	 * If the view starts with the view URL provided to the SocialAuth Seam
	 * component, this listener assumes that it has been redirected here by the
	 * external provider and verifies if the user is authenticated
	 */
	public void beforePhase(final PhaseEvent event) {

		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ec = ctx.getExternalContext();
		/*
		 * Parameter 'successUrl' is configured in web.xml and it must be same
		 * there.
		 */
		String successUrl = ec.getInitParameter("successUrl");

		String viewId = Pages.getCurrentViewId();
		String view = successUrl.split("\\.")[0];

		if (viewId == null || !viewId.startsWith(view)) {
			return;
		}

		SocialAuth social = (SocialAuth) Component
				.getInstance(SocialAuth.class);
		try {
			social.connect();
		} catch (Exception e) {
			log.warn(e);
		}
		Pages.handleOutcome(event.getFacesContext(), null, successUrl);

	}

	/**
	 * No implementation is required because we have already redirected to the
	 * view provided in the SocialAuth component
	 */
	public void afterPhase(final PhaseEvent event) {
	}

	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}
}
