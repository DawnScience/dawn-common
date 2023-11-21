/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.eclipse;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   BundleUtils
 *   
 *   Assumes that this class can be used before the Logger is loaded, therefore do not put Logging in here.
 *
 *   @author gerring
 *   @date Aug 2, 2010
 *   @project org.dawb.common.util
 **/
public class BundleUtils {
	private static final Logger logger = LoggerFactory.getLogger(BundleUtils.class);

	/**
	 * @param bundleName
	 * @return file this can return null if bundle is not found
	 * @throws IOException
	 */
	public static File getBundleLocation(final String bundleName) throws IOException {
		final Bundle bundle = Platform.getBundle(bundleName);
		if (bundle == null) {
			return null;
		}

		return FileLocator.getBundleFileLocation(bundle).orElseThrow(IOException::new);
	}

	/**
	 * Looks at installed features, gets newest org.dawnsci.base.product.feature
	 * and returns that version.
	 * 
	 * @return null if cannot find a dawn feature
	 */
	public static String getDawnVersion() {
		// only works for product plugin with version
		try {
			return Platform.getProduct().getDefiningBundle().getVersion().toString();
		} catch (NullPointerException e) {
			logger.error("Could not get Dawn version as no current product or product bundle");
			return null;
		}
	}
}
