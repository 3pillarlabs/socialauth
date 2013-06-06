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
import java.util.List;

/**
 * Contains the Album info
 * 
 * @author tarun.nagpal
 * 
 */
public class Album implements Serializable {

	private static final long serialVersionUID = 4987858003739130638L;

	private String id;
	private String name;
	private String link;
	private String coverPhoto;
	private int photosCount;
	List<Photo> photos;

	/**
	 * Retrieves the album id
	 * 
	 * @return album id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Updates the album id
	 * 
	 * @param id
	 *            album id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Retrieves the album name
	 * 
	 * @return album name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Updates the album nane
	 * 
	 * @param name
	 *            album name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Retrieves the album view link
	 * 
	 * @return
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Updates the album view link
	 * 
	 * @param link
	 *            album link
	 */
	public void setLink(final String link) {
		this.link = link;
	}

	/**
	 * Retrieves the URL of album cover photo
	 * 
	 * @return album cover photo URL string
	 */
	public String getCoverPhoto() {
		return coverPhoto;
	}

	/**
	 * Updates the album cover photo URL string
	 * 
	 * @param coverPhoto
	 *            album cover photo URL string
	 */
	public void setCoverPhoto(final String coverPhoto) {
		this.coverPhoto = coverPhoto;
	}

	/**
	 * Retrieves the album photos count
	 * 
	 * @return album photos count
	 */
	public int getPhotosCount() {
		return photosCount;
	}

	/**
	 * Updates the album photo count
	 * 
	 * @param photosCount
	 *            album photos count
	 */
	public void setPhotosCount(final int photosCount) {
		this.photosCount = photosCount;
	}

	/**
	 * Retrieves the list of albmun photos
	 * 
	 * @return
	 */
	public List<Photo> getPhotos() {
		return photos;
	}

	/**
	 * Updates the list of album photos
	 * 
	 * @param photos
	 */
	public void setPhotos(final List<Photo> photos) {
		this.photos = photos;
	}

	/**
	 * Retrieves the Album as a string
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" id: " + id + NEW_LINE);
		result.append(" name: " + name + NEW_LINE);
		result.append(" coverPhoto: " + coverPhoto + NEW_LINE);
		result.append(" link: " + link + NEW_LINE);
		result.append(" photosCount: " + photosCount + NEW_LINE);
		result.append(" photos: " + photos + NEW_LINE);
		result.append("}");
		return result.toString();
	}
}
