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
package org.brickred.socialauth.plugin;

import org.brickred.socialauth.util.ProviderSupport;

/**
 * This interface represents the Plugin. All plugins should be inherited from
 * this interface.
 * 
 * @author tarun.nagpal
 * 
 */
public interface Plugin {

	/**
	 * Retrieves the provider support object. provider support object provides
	 * the functionality to make OAuth HTTP call for a given provider.
	 * 
	 * @return
	 */
	public ProviderSupport getProviderSupport();

	/**
	 * Updates the {@link ProviderSupport}
	 * 
	 * @param providerSupport
	 *            provider support object
	 */
	public void setProviderSupport(ProviderSupport providerSupport);

}
