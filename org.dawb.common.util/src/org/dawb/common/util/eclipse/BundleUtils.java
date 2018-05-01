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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
	
	/**
	 * Get the java.io.File location of a bundle.
	 * @param bundleName
	 * @return
	 * @throws Exception 
	 */
	public static File getBundleLocation(final Bundle bundle) throws IOException {
		return FileLocator.getBundleFile(bundle);
	}

	/**
	 * Get the bundle path using eclipse.home.location not loading the bundle.
	 * @param bundleName
	 * @return
	 */
	public static File getBundlePathNoLoading(String bundleName) {
		return new File(new File(getEclipseHome(), "plugins"), bundleName);
	}
	
	/**
	 * Gets eclipse home in debug and in deployed application mode.
	 * @return
	 */
	public static String getEclipseHome() {
		File hDirectory;
		try {
			URI u = new URI(System.getProperty("eclipse.home.location"));
			hDirectory = new File(u);
		} catch (URISyntaxException e) {
			return null;
		}

		String path = hDirectory.getName();
		if (System.getProperty("os.name").equals("Mac OS X")) {
			path = hDirectory.getParentFile().getAbsolutePath(); // eclipse.home.location returns /Applications/Dawn.app/Contents/Eclipse
		} else if (path.equals("plugins") || path.equals("bundles")) {
			path = hDirectory.getParentFile().getParentFile().getAbsolutePath();
		} else {
			path = hDirectory.getAbsolutePath();
		}
		return path;
	}

	private final static String FEATURE_PLUGIN = "org.dawnsci.base.product.feature";

	private static Pattern FEATURE_MATCH = Pattern.compile(FEATURE_PLUGIN + "_(.+)");

	/**
	 * Looks at installed features, gets newest org.dawnsci.base.product.feature
	 * and returns that version.
	 * 
	 * @return null if cannot find a dawn feature
	 */
	public static String getDawnVersion() {
		
		File dir = new File(getEclipseHome(), "features");
		if (!dir.exists()) {
			return null;
		}

		long date = -1;
		String version = null;
		for (File sd : dir.listFiles()) {
			if (!sd.isDirectory()) continue;
			Matcher matcher = FEATURE_MATCH.matcher(sd.getName());
			if (matcher.matches()) {
				if (date < sd.lastModified()) {
					date    = sd.lastModified();
					version = matcher.group(1); 
				}
			}
		}

		if (version == null) { // running in Eclipse so dir=workspace/tp/features
			try {
				File gp = dir.getParentFile().getParentFile().getParentFile();
				Optional<Path> fp = Files.find(gp.toPath(), 3, (p, a) -> p.getFileName().toString().equals(FEATURE_PLUGIN)).findFirst();
				if (fp.isPresent()) {
					version = parseProductVersionFromFeature(fp.get().resolve("feature.xml"));
				}
			} catch (IOException e) {
				// do nothing
			}
		}
		return version;
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
