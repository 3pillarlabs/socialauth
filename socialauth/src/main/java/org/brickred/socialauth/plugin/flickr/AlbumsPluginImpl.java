/*
 ===========================================================================
 Copyright (c) 2012 3PillarGlobal

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
package org.brickred.socialauth.plugin.flickr;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Album;
import org.brickred.socialauth.Photo;
import org.brickred.socialauth.exception.ServerDataException;
import org.brickred.socialauth.plugin.AlbumsPlugin;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.XMLParseUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Album Plugin implementation for Flickr
 * 
 * @author tarun.nagpal
 * 
 */
public class AlbumsPluginImpl implements AlbumsPlugin, Serializable {

	private static final long serialVersionUID = 5350785649768508189L;
	private static final String ALBUMS_URL = "https://api.flickr.com/services/rest/?method=flickr.photosets.getList&api_key=%1$s&primary_photo_extras=url_m";
	private static final String ALBUM_PHOTOS_URL = "https://api.flickr.com/services/rest/?method=flickr.photosets.getPhotos&photoset_id=%1$s&extras=url_t,url_s,url_m,url_o&api_key=%2$s";
	private static final String PROFILE_URL = "https://api.flickr.com/services/rest/?method=flickr.urls.getUserProfile&api_key=%1$s";
	private static final String PHOTO_LINK = "https://www.flickr.com/photos/%1$s/%2$s";
	private static final String SET_LINK = "https://www.flickr.com/photos/%1$s/sets/%2$s";	
	
	private final Log LOG = LogFactory.getLog(this.getClass());

	private ProviderSupport providerSupport;
	
	private String userId = null;

	public AlbumsPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public List<Album> getAlbums() throws Exception {
		String albumUrl = String.format(ALBUMS_URL, providerSupport.getAccessGrant().getKey());
		Response response = providerSupport.api(albumUrl,
				MethodType.GET.toString(), null, null, null);

		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(response.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the albums from response." + albumUrl, e);
		}		

		List<Album> albums = new ArrayList<Album>();
		if (root != null) {
			NodeList cList = root.getElementsByTagName("photosets");
			if (cList != null && cList.getLength() > 0) {
				Element photosets  = (Element) cList.item(0);
				NodeList photosetNodes = photosets
						.getElementsByTagName("photoset");
				if (photosetNodes != null && photosetNodes.getLength() > 0) {
					LOG.debug("Found photo sets : " + photosetNodes.getLength());
					
					for (int i = 0; i < photosetNodes.getLength(); i++) {
						Element photoset = (Element) photosetNodes.item(i);
						String id = photoset.getAttribute("id");
						if (id != null) {
							Album albumObj = new Album();
							albumObj.setId(id);
							
							String photoCount = photoset.getAttribute("photos");
							if (photoCount != null) {
								albumObj.setPhotosCount(Integer.parseInt(photoCount));
							}
							
							NodeList titleList = photoset.getElementsByTagName("title");
							if (titleList != null && titleList.getLength() > 0){
								String title = titleList.item(0).getTextContent();
								if (title != null) {
									albumObj.setName(title);
								}
							}
							
							NodeList coverList = photoset.getElementsByTagName("primary_photo_extras");	
							if (coverList != null && coverList.getLength() > 0){
								Element cE = (Element)coverList.item(0);
								String coverPhotoUrl = cE.getAttribute("url_m");;
								if (coverPhotoUrl != null) {
									albumObj.setCoverPhoto(coverPhotoUrl);
								}
							}
							String uId = this.getUserId();
							if(uId != ""){
								albumObj.setLink(String.format(SET_LINK, this.getUserId() , id));
							}
							List<Photo> photos = getAlbumPhotos(id);
							albumObj.setPhotos(photos);
	
							albums.add(albumObj);
							//System.out.println(albumObj);
						}
					}
				}
			} else {
				LOG.debug("No Albums were obtained from : " + albumUrl);
			}
		}
		return albums;
	}
	
	private String getUserId() throws Exception {
		if (this.userId != null){
			return this.userId;
		} else {
			String profileUrl = String.format(PROFILE_URL, providerSupport.getAccessGrant().getKey());
			Response response = providerSupport.api(profileUrl,
					MethodType.GET.toString(), null, null, null);
			//System.out.println(response.getResponseBodyAsString(Constants.ENCODING));
			Element root;
			try {
				root = XMLParseUtil.loadXmlResource(response.getInputStream());
			} catch (Exception e) {
				throw new ServerDataException(
						"Failed to parse the User from response." + profileUrl, e);
			}
			String id = "";
			if (root != null) {
				NodeList uList = root.getElementsByTagName("user");
				if (uList != null && uList.getLength() > 0) {
					Element user  = (Element) uList.item(0);
					id = user.getAttribute("nsid");
				}
				
			}
			return id;
		}
	}

	private List<Photo> getAlbumPhotos(final String id) throws Exception {
		String url = String.format(ALBUM_PHOTOS_URL, id, providerSupport.getAccessGrant().getKey());
		Response response = providerSupport.api(url,
				MethodType.GET.toString(), null, null, null);
		LOG.info("Getting Photos of Album :: " + id);

		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(response.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the photos from response." + url
							+ id, e);
		}

		List<Photo> photos = new ArrayList<Photo>();

		if (root != null) {
			NodeList photoList = root.getElementsByTagName("photo");
			if (photoList != null && photoList.getLength() > 0) {
				LOG.info("Found photos : " + photoList.getLength());
				for (int i = 0; i < photoList.getLength(); i++) {
					Photo photo = new Photo();
					Element pl = (Element) photoList.item(i);
					
					String photoId  = pl.getAttribute("id");
					photo.setId(photoId);

					photo.setTitle(pl.getAttribute("title"));
					String uId = this.getUserId();
					if (uId != ""){
						String link = String.format(PHOTO_LINK, uId, photoId);
						photo.setLink(link);
					}
				
					photo.setLargeImage(pl.getAttribute("url_o"));
					photo.setMediumImage(pl.getAttribute("url_m"));
					photo.setSmallImage(pl.getAttribute("url_s"));
					photo.setThumbImage(pl.getAttribute("url_t"));

					photos.add(photo);
				}
			} else {
				LOG.info("No photos were obtained from : " + url);
			}
		}
		return photos;
	}	

	@Override
	public ProviderSupport getProviderSupport() {
		return providerSupport;
	}

	@Override
	public void setProviderSupport(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}
}
