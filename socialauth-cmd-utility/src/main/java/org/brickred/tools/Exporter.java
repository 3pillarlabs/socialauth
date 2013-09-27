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
package org.brickred.tools;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.util.AccessGrant;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Saves {@link AccessGrant} in a file
 * 
 * @author tarun.nagpal
 * 
 */
public class Exporter {

	private static final Log LOG = LogFactory.getLog(Exporter.class);

	public static void exportAccessGrant(final AccessGrant accessGrant,
			final String filePath) throws Exception {
		LOG.info("Exporting AccessGrant");
		ObjectMapper mapper = new ObjectMapper();
		String str = mapper.writeValueAsString(accessGrant);

		File file = null;
		String fpath = filePath;
		if (fpath != null) {
			fpath = fpath.replaceAll("/", String.valueOf(File.separatorChar));
			file = new File(filePath + File.separatorChar
					+ accessGrant.getProviderId() + "_accessGrant_file.txt");
		} else {
			throw new SocialAuthException(
					"File location is not given to store access grant file.");
		}
		LOG.debug("Persisting access grant on :: " + file.getAbsolutePath());
		file.createNewFile();
		PrintWriter out = new PrintWriter(file);
		out.println(str);
		out.flush();
		out.close();

	}
}
