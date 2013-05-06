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
package org.brickred.socialauth.plugin.facebook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Album;
import org.brickred.socialauth.Photo;
import org.brickred.socialauth.plugin.AlbumsPlugin;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.MethodType;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Album Plugin implementation for Facebook
 * 
 * @author tarun.nagpal
 * 
 */
public class AlbumsPluginImpl implements AlbumsPlugin, Serializable {

	private static final long serialVersionUID = 5350785649768508189L;
	private static final String ALBUMS_URL = "https://graph.facebook.com/me/albums";
	private static final String ALBUM_PHOTOS_URL = "https://graph.facebook.com/%1$s/photos";
	private static final String ALBUM_COVER_URL = "https://graph.facebook.com/%1$s/picture?access_token=%2$s";
	private final Log LOG = LogFactory.getLog(this.getClass());

	private ProviderSupport providerSupport;

	public AlbumsPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public List<Album> getAlbums() throws Exception {
		Response response = providerSupport.api(ALBUMS_URL,
				MethodType.GET.toString(), null, null, null);
		String respStr = response.getResponseBodyAsString(Constants.ENCODING);
		LOG.debug("Albums JSON :: " + respStr);
		List<Album> albums = new ArrayList<Album>();
		JSONObject resp = new JSONObject(respStr);
		JSONArray data = resp.getJSONArray("data");
		LOG.debug("Albums count : " + data.length());
		for (int i = 0; i < data.length(); i++) {
			Album album = new Album();
			JSONObject obj = data.getJSONObject(i);
			String albumId = obj.getString("id");
			album.setId(albumId);
			if (obj.has("name")) {
				album.setName(obj.getString("name"));
			}

			if (obj.has("link")) {
				album.setLink(obj.getString("link"));
			}
			if (obj.has("cover_photo")) {
				album.setCoverPhoto(obj.getString("cover_photo"));
			}
			if (obj.has("count")) {
				album.setPhotosCount(obj.getInt("count"));
			}
			album.setCoverPhoto(String.format(ALBUM_COVER_URL, albumId,
					providerSupport.getAccessGrant().getKey()));
			List<Photo> photos = getAlbumPhotos(albumId);
			album.setPhotos(photos);
			albums.add(album);
		}
		return albums;
	}

	private List<Photo> getAlbumPhotos(final String id) throws Exception {
		Response response = providerSupport.api(
				String.format(ALBUM_PHOTOS_URL, id), MethodType.GET.toString(),
				null, null, null);
		String respStr = response.getResponseBodyAsString(Constants.ENCODING);
		LOG.info("Getting Photos of Album :: " + id);
		JSONObject resp = new JSONObject(respStr);
		JSONArray data = resp.getJSONArray("data");
		LOG.debug("Photos count : " + data.length());
		List<Photo> photos = new ArrayList<Photo>();
		for (int i = 0; i < data.length(); i++) {
			Photo photo = new Photo();
			JSONObject obj = data.getJSONObject(i);
			photo.setId(obj.getString("id"));
			if (obj.has("name")) {
				photo.setTitle(obj.getString("name"));
			}
			if (obj.has("link")) {
				photo.setLink(obj.getString("link"));
			}
			if (obj.has("picture")) {
				photo.setThumbImage(obj.getString("picture"));
			}
			JSONArray images = obj.getJSONArray("images");
			for (int k = 0; k < images.length(); k++) {
				JSONObject img = images.getJSONObject(k);
				int ht = 0;
				int wt = 0;
				if (img.has("height")) {
					ht = img.getInt("height");
				}
				if (img.has("width")) {
					wt = img.getInt("width");
				}
				if (ht == 600 || wt == 600) {
					photo.setLargeImage(img.getString("source"));
				} else if (ht == 480 || wt == 480) {
					photo.setMediumImage(img.getString("source"));
				} else if (ht == 320 || wt == 320) {
					photo.setSmallImage(img.getString("source"));
				}
			}
			photos.add(photo);
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
