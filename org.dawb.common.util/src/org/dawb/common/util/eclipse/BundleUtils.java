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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

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
		return FileLocator.getBundleFile(bundle);
	}

	private final static String FEATURE_PLUGIN = "org.dawnsci.base.product.feature";

	/**
	 * Looks at installed features, gets newest org.dawnsci.base.product.feature
	 * and returns that version.
	 * 
	 * @return null if cannot find a dawn feature
	 */
	public static String getDawnVersion() {
		Bundle feature = Platform.getBundle(FEATURE_PLUGIN);

		if (feature != null) {
			return feature.getVersion().toString();
		}

		// running from Eclipse so assume in same repo as product plugin 
		feature = Platform.getBundle("org.dawnsci.product.plugin");
		try {
			URL u = FileLocator.resolve(feature.getEntry("META-INF"));
			Path p = Paths.get(new File(u.toURI()).getCanonicalFile().getParentFile().getParent(), FEATURE_PLUGIN, "feature.xml");
			return parseProductVersionFromFeature(p);
		} catch (Exception e) {
		}

		return null;
	}

	private static final Pattern VERSION_MATCH = Pattern.compile("\\s*version=\"([^\"]+)\"");

	private static String parseProductVersionFromFeature(Path path) throws IOException {
		List<String> lines = Files.readAllLines(path, Charset.defaultCharset());

		Iterator<String> it = lines.iterator();
		while (it.hasNext()) {
			if (it.next().contains("<feature")) {
				break;
			}
		}
		while (it.hasNext()) {
			Matcher m = VERSION_MATCH.matcher(it.next());
			if (m.matches()) {
				return m.group(1);
			}
		}

		return null;
	}
}
