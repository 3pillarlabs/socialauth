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

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.AccessGrant;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * Generates access token through command line utility. Generated access token
 * will be saved in a file on a configures location OR user home directory.
 * Later you can use the {@link Importer} class to create an AccessGrant object
 * from this file.
 * 
 * @author tarun.nagpal
 * 
 */
public class GenerateToken {

	int port;
	String host;
	Server server;
	String callbackPath;

	/** Lock on the code and error. */
	final Lock lock = new ReentrantLock();

	/** Condition for receiving an authorization response. */
	final Condition gotAuthorizationResponse = lock.newCondition();
	String code;
	String error;
	Map<String, String> paramsMap;

	String providerId;
	String successURL;
	String tokenFilePath;

	private Map<String, String> argumentsMap;
	private final Log LOG = LogFactory.getLog(GenerateToken.class);

	final static String MESSAGE = "Usage: java -jar GenerateToken.jar "
			+ " --providerId=<providerId> --host=<hostname> --port=<port> --returnURL=<returnURL> --tokenFileLocation=<tokenFileLocation>\n"
			+ "For example, to generate token use these options:\n"
			+ "--host=opensource.brickred.com --port=80 --returnURL=http://opensource.brickred.com/socialauthdemo/socialAuthSuccessAction.do";

	/**
	 * Main entry point. Parses arguments and generates access token.<br>
	 * <br>
	 * 
	 * Usage: java GenerateToken --providerId=[providerId] --host=[hostname]
	 * --port=[port] --returnURL=[returnURL]
	 * --tokenFileLocation=[tokenFileLocation]<br>
	 * 
	 */

	public static void main(final String[] args) throws Exception {
		GenerateToken generateToken = new GenerateToken();
		generateToken.argumentParser(args);

		if (generateToken.host == null || generateToken.providerId == null
				|| generateToken.successURL == null) {
			System.out
					.println("=================================================================");
			System.out.println(MESSAGE);
			System.out
					.println("=================================================================");
			return;
		}
		generateToken.getAccessToken();
	}

	private void getAccessToken() throws Exception {
		SocialAuthConfig config = SocialAuthConfig.getDefault();
		config.load();
		SocialAuthManager manager = new SocialAuthManager();
		manager.setSocialAuthConfig(config);

		URL aURL = new URL(successURL);
		host = aURL.getHost();
		port = aURL.getPort();
		port = port == -1 ? 80 : port;
		callbackPath = aURL.getPath();

		if (tokenFilePath == null) {
			tokenFilePath = System.getProperty("user.home");
		}
		String url = manager.getAuthenticationUrl(providerId, successURL);
		startServer();

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Action.BROWSE)) {
				try {
					desktop.browse(URI.create(url));
					// return;
				} catch (IOException e) {
					// handled below
				}
			}
		}

		lock.lock();
		try {
			while (paramsMap == null && error == null) {
				gotAuthorizationResponse.awaitUninterruptibly();
			}
			if (error != null) {
				throw new IOException("User authorization failed (" + error
						+ ")");
			}
		} finally {
			lock.unlock();
		}
		stop();

		AccessGrant accessGrant = manager.createAccessGrant(providerId,
				paramsMap, successURL);

		Exporter.exportAccessGrant(accessGrant, tokenFilePath);

		LOG.info("Access Grant Object saved in a file  :: " + tokenFilePath
				+ File.separatorChar + accessGrant.getProviderId()
				+ "_accessGrant_file.txt");
	}

	private void startServer() throws Exception {
		server = new Server(port);
		for (Connector c : server.getConnectors()) {
			c.setHost(host);
		}

		server.addHandler(new CallbackHandler());
		try {
			server.start();
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	public void stop() throws IOException {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				throw new IOException(e);
			}
			server = null;
		}
	}

	class CallbackHandler extends AbstractHandler {

		@Override
		public void handle(final String target,
				final HttpServletRequest request,
				final HttpServletResponse response, final int dispatch)
				throws IOException {
			if (!callbackPath.equals(target)) {
				return;
			}
			writeLandingHtml(response);
			response.flushBuffer();
			((Request) request).setHandled(true);
			lock.lock();
			try {
				error = request.getParameter("error");
				if (request.getParameter("code") != null) {
					code = request.getParameter("code");
				} else if (request.getParameter("oauth_token") != null) {
					code = request.getParameter("oauth_token");
				}
				Map<String, String[]> map = request.getParameterMap();
				paramsMap = new HashMap<String, String>();
				for (Map.Entry<String, String[]> entry : map.entrySet()) {
					String key = entry.getKey();
					String values[] = entry.getValue();
					paramsMap.put(key, values[0].toString()); // Only 1 value is
				}
				gotAuthorizationResponse.signal();
				LOG.debug("Auth code :: " + code);
			} finally {
				lock.unlock();
			}
		}

		private void writeLandingHtml(final HttpServletResponse response)
				throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");

			PrintWriter doc = response.getWriter();
			doc.println("<html>");
			doc.println("<head><title>OAuth 2.0 Authentication Token Recieved</title></head>");
			doc.println("<body>");
			doc.println("Generating access token. It'll be save in a file :: "
					+ tokenFilePath + File.separatorChar + providerId
					+ "_accessGrant_file.txt");
			doc.println("<script type='text/javascript'>");
			doc.println("window.setTimeout(function() {");
			doc.println("    window.open('', '_self', ''); window.close(); }, 1000);");
			doc.println("if (window.opener) { window.opener.checkToken(); }");
			doc.println("</script>");
			doc.println("</body>");
			doc.println("</HTML>");
			doc.flush();
		}
	}

	public void argumentParser(final String[] arg) {
		argumentsMap = new HashMap<String, String>();
		for (int i = 0; i < arg.length; i++) {
			String key;
			if (arg[i].startsWith("--")) {
				key = arg[i].substring(2);
			} else if (arg[i].startsWith("-")) {
				key = arg[i].substring(1);
			} else {
				argumentsMap.put(arg[i], null);
				continue;
			}
			String value;
			int index = key.indexOf('=');
			if (index == -1) {
				if (((i + 1) < arg.length) && (arg[i + 1].charAt(0) != '-')) {
					argumentsMap.put(key, arg[i + 1]);
					i++;
				} else {
					argumentsMap.put(key, null);
				}
			} else {
				value = key.substring(index + 1);
				key = key.substring(0, index);
				argumentsMap.put(key, value);
			}
		}

		LOG.debug("Given Arguments :: " + argumentsMap);

		for (Map.Entry<String, String> entry : argumentsMap.entrySet()) {
			if ("providerId".equals(entry.getKey())) {
				this.providerId = entry.getValue();
			} else if ("host".equals(entry.getKey())) {
				this.host = entry.getValue();
			} else if ("port".equals(entry.getKey())) {
				this.port = Integer.parseInt(entry.getValue());
			} else if ("returnURL".equals(entry.getKey())) {
				this.successURL = entry.getValue();
			} else if ("tokenFileLocation".equals(entry.getKey())) {
				this.tokenFilePath = entry.getValue();
			}
		}
	}

}
