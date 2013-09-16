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
package org.brickred.socialauth.plugin.googleplus;

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
 * Album Plugin implementation for Picassa
 * 
 * @author tarun.nagpal
 * 
 */
public class AlbumsPluginImpl implements AlbumsPlugin, Serializable {

	private static final long serialVersionUID = 1408925565059390624L;
	private static final String ALBUMS_URL = "https://picasaweb.google.com/data/feed/api/user/default";
	private static final String PHOTOS_URL = "https://picasaweb.google.com/data/feed/api/user/default/albumid/";
	private static final String ALBUM_NAMESPACE = "http://schemas.google.com/photos/2007";
	private static final String MEDIA_NAMESPACE = "http://search.yahoo.com/mrss/";
	private final Log LOG = LogFactory.getLog(this.getClass());

	private ProviderSupport providerSupport;

	public AlbumsPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public List<Album> getAlbums() throws Exception {
		Response response = providerSupport.api(ALBUMS_URL,
				MethodType.GET.toString(), null, null, null);

		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(response.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the albums from response." + ALBUMS_URL, e);
		}

		List<Album> albums = new ArrayList<Album>();

		if (root != null) {
			NodeList albumList = root.getElementsByTagName("entry");
			if (albumList != null && albumList.getLength() > 0) {
				LOG.info("Found albums : " + albumList.getLength());
				for (int i = 0; i < albumList.getLength(); i++) {
					Album album = new Album();
					Element p = (Element) albumList.item(i);

					NodeList id = p.getElementsByTagNameNS(ALBUM_NAMESPACE,
							"id");
					String albumId = XMLParseUtil.getElementData(id.item(0));
					album.setId(albumId);

					album.setName(XMLParseUtil.getElementData(p, "title"));

					NodeList count = p.getElementsByTagNameNS(ALBUM_NAMESPACE,
							"numphotos");
					album.setPhotosCount(Integer.parseInt(XMLParseUtil
							.getElementData(count.item(0))));

					NodeList mediaGroup = p.getElementsByTagNameNS(
							MEDIA_NAMESPACE, "group");
					String url = null;
					if (mediaGroup != null && mediaGroup.getLength() > 0) {
						Element el = (Element) mediaGroup.item(0);
						if (el != null) {
							NodeList thumbnail = el.getElementsByTagNameNS(
									MEDIA_NAMESPACE, "thumbnail");
							if (thumbnail != null) {
								Element tl = (Element) thumbnail.item(0);
								if (tl != null) {
									url = tl.getAttribute("url");
								}
							}
						}
					}
					album.setCoverPhoto(url);

					String rel = null;
					String href = null;

					NodeList link = p.getElementsByTagName("link");
					if (link != null && link.getLength() > 0) {
						for (int j = 0; j < link.getLength(); j++) {
							Element l = (Element) link.item(j);
							if (l != null) {
								rel = l.getAttribute("rel");
								if (rel != null
										&& rel.equalsIgnoreCase("alternate")) {
									href = l.getAttribute("href");
								}
							}
						}
					}

					album.setLink(href);
					List<Photo> photos = getAlbumPhotos(albumId);
					album.setPhotos(photos);

					albums.add(album);
					System.out.println(album);
				}
			} else {
				LOG.info("No albums were obtained from : " + ALBUMS_URL);
			}
		}

		return albums;
	}

	private List<Photo> getAlbumPhotos(final String id) throws Exception {

		Response response = providerSupport.api(PHOTOS_URL + id,
				MethodType.GET.toString(), null, null, null);
		LOG.info("Getting Photos of Album :: " + id);

		Element root;
		try {
			root = XMLParseUtil.loadXmlResource(response.getInputStream());
		} catch (Exception e) {
			throw new ServerDataException(
					"Failed to parse the photos from response." + PHOTOS_URL
							+ id, e);
		}

		List<Photo> photos = new ArrayList<Photo>();

		if (root != null) {
			NodeList photoList = root.getElementsByTagName("entry");
			if (photoList != null && photoList.getLength() > 0) {
				LOG.info("Found photos : " + photoList.getLength());
				for (int i = 0; i < photoList.getLength(); i++) {
					Photo photo = new Photo();
					Element pl = (Element) photoList.item(i);

					NodeList pid = pl.getElementsByTagNameNS(ALBUM_NAMESPACE,
							"id");
					String photoId = XMLParseUtil.getElementData(pid.item(0));
					photo.setId(photoId);

					photo.setTitle(XMLParseUtil.getElementData(pl, "title"));

					NodeList mediaGroup = pl.getElementsByTagNameNS(
							MEDIA_NAMESPACE, "group");
					String urlLarge = null;
					String urlMedium = null;
					String urlSmall = null;
					String urlThumb = null;
					int width = 0;

					if (mediaGroup != null && mediaGroup.getLength() > 0) {
						Element el = (Element) mediaGroup.item(0);
						if (el != null) {
							NodeList content = el.getElementsByTagNameNS(
									MEDIA_NAMESPACE, "content");
							if (content != null) {
								Element cl = (Element) content.item(0);
								if (cl != null) {
									urlLarge = cl.getAttribute("url");
								}
							}

							NodeList thumbnail = el.getElementsByTagNameNS(
									MEDIA_NAMESPACE, "thumbnail");
							if (thumbnail != null && thumbnail.getLength() > 0) {
								for (int k = 0; k < thumbnail.getLength(); k++) {
									Element thumb = (Element) thumbnail.item(k);
									if (thumb != null) {
										width = Integer.parseInt(thumb
												.getAttribute("width"));
										if (width == 288) {
											urlMedium = thumb
													.getAttribute("url");
										} else if (width == 144) {
											urlSmall = thumb
													.getAttribute("url");
										} else if (width == 72) {
											urlThumb = thumb
													.getAttribute("url");
										}
									}
								}
							}
						}
					}
					photo.setLargeImage(urlLarge);
					photo.setMediumImage(urlMedium);
					photo.setSmallImage(urlSmall);
					photo.setThumbImage(urlThumb);

					photos.add(photo);
				}
			} else {
				LOG.info("No photos were obtained from : " + PHOTOS_URL);
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
