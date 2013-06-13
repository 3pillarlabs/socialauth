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

package org.brickred.actions;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.exception.SocialAuthException;

import de.deltatree.social.web.filter.api.SASFHelper;
import de.deltatree.social.web.filter.api.SASFStaticHelper;

/**
 * This is for updating status with image.
 * 
 * @author tarun.nagpal
 * 
 */
@Results({ @Result(name = "success", location = "/jsp/statusSuccess.jsp"),
		@Result(name = "failure", location = "/jsp/statusSuccess.jsp"), })
@InterceptorRefs({
		@InterceptorRef(value = "fileUpload", params = {
				"AllowedTypes",
				" image/PNG,image/png,image/GIF, image/gif, image/JPEG, image/JPG, image/jpg,image/jpeg" }),
		@InterceptorRef("basicStack") })
public class SocialAuthUploadPhotoAction implements ServletRequestAware {

	final Log LOG = LogFactory.getLog(this.getClass());

	private HttpServletRequest request;
	private File imageFile;
	private String imageFileContentType;
	private String imageFileFileName;
	private String statusMessage;

	/**
	 * Update status for the given provider.
	 * 
	 * @return String where the action should flow
	 * @throws Exception
	 *             if an error occurs
	 */
	@Action(value = "/socialAuthUploadPhotoAction")
	public String execute() throws Exception {

		SASFHelper helper = SASFStaticHelper.getHelper(request);
		SocialAuthManager manager = helper.getAuthManager();

		AuthProvider provider = null;
		if (manager != null) {
			provider = manager.getCurrentAuthProvider();
		}
		if (provider != null) {
			try {
				provider.uploadImage(statusMessage, imageFileFileName,
						new FileInputStream(imageFile));
				request.setAttribute("Message", "Status Updated successfully");
				return "success";
			} catch (SocialAuthException e) {
				request.setAttribute("Message", e.getMessage());
				e.printStackTrace();
			}
		}
		return "failure";

	}

	@Override
	public void setServletRequest(final HttpServletRequest request) {
		this.request = request;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(final File imageFile) {
		this.imageFile = imageFile;
	}

	public String getImageFileContentType() {
		return imageFileContentType;
	}

	public void setImageFileContentType(final String imageFileContentType) {
		this.imageFileContentType = imageFileContentType;
	}

	public String getImageFileFileName() {
		return imageFileFileName;
	}

	public void setImageFileFileName(final String imageFileFileName) {
		this.imageFileFileName = imageFileFileName;
	}

}
