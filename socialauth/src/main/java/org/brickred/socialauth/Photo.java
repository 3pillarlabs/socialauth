/*
 ===========================================================================
 Copyright (c) 2012 3Pillar Global

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
package org.brickred.socialauth;

import java.io.Serializable;
import java.util.Map;

/**
 * Data bean for photo info
 * 
 * @author tarun.nagpal
 * 
 */
public class Photo implements Serializable {

	private static final long serialVersionUID = 327011969016300722L;

	private String id;
	private String title;
	private String largeImage;
	private String mediumImage;
	private String smallImage;
	private String thumbImage;
	private String link;
	private Map<String, String> metaData;

	/**
	 * Returns 600 X 600 image
	 * 
	 * @return large image URL string
	 */
	public String getLargeImage() {
		return largeImage;
	}

	/**
	 * Updates Image URL of size 600 X 600
	 * 
	 * @param largeImage
	 */
	public void setLargeImage(final String largeImage) {
		this.largeImage = largeImage;
	}

	/**
	 * Returns 480 X 480 image
	 * 
	 * @return medium image URL string
	 */
	public String getMediumImage() {
		return mediumImage;
	}

	/**
	 * Updates image URL of size 480 X 480
	 * 
	 * @param mediumImage
	 */
	public void setMediumImage(final String mediumImage) {
		this.mediumImage = mediumImage;
	}

	/**
	 * Returns 320 X 320
	 * 
	 * @return
	 */
	public String getSmallImage() {
		return smallImage;
	}

	/**
	 * Updates image ULR of size 320 X 320
	 * 
	 * @param smallImage
	 */
	public void setSmallImage(final String smallImage) {
		this.smallImage = smallImage;
	}

	/**
	 * Returns image height less than 100px
	 * 
	 * @return
	 */
	public String getThumbImage() {
		return thumbImage;
	}

	/**
	 * Updates the image URL string which has height less than 100px
	 * 
	 * @param thumbImage
	 */
	public void setThumbImage(final String thumbImage) {
		this.thumbImage = thumbImage;
	}

	/**
	 * Retrieves photo id
	 * 
	 * @return photo id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Updates the photo id
	 * 
	 * @param id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Retrieves the photo caption
	 * 
	 * @return photo caption
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Updates the photo caption
	 * 
	 * @param name
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Retrieves the photo URL string
	 * 
	 * @return photo URL string
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Updates the photo URL string
	 * 
	 * @param link
	 *            photo URL string
	 */
	public void setLink(final String link) {
		this.link = link;
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}

	public void setMetaData(final Map<String, String> metaData) {
		this.metaData = metaData;
	}

	/**
	 * Retrieves the Photo as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" id: " + id + NEW_LINE);
		result.append(" title: " + title + NEW_LINE);
		result.append(" largeImage: " + largeImage + NEW_LINE);
		result.append(" mediumImage: " + mediumImage + NEW_LINE);
		result.append(" smallImage: " + smallImage + NEW_LINE);
		result.append(" thumbImage: " + thumbImage + NEW_LINE);
		result.append(" link: " + link + NEW_LINE);
		result.append(" metadata: " + metaData + NEW_LINE);
		result.append("}");
		return result.toString();
	}

}
