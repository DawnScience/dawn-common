/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.edna.pydev.extensions.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawb.common.util.eclipse.BundleUtils;

/**
 *   LibsLocationFinder
 *
 *   @author gerring
 *   @date Jul 29, 2010
 *   @project org.edna.pydev.extensions
 **/
public class LibsLocationFinder {
	
	
	/**
	 * Method returns libs that should be used in the jython path so that the
	 * jython editor autocompletes.
	 * 
	 * @return list of jar paths
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static final List<String> findWorkspaceLibs() throws URISyntaxException, IOException {
		
		final List<String> libs = new ArrayList<String>(31);
		
		// Use bundle as works even in debug mode.
		final File libsFolder     = BundleUtils.getBundleLocation("uk.ac.gda.libs");
		if (libsFolder.exists()&&libsFolder.isDirectory()) {
			final File[] fa = libsFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".jar");
				}
			});
			for (int i = 0; i < fa.length; i++) {
				libs.add(fa[i].getAbsolutePath());
			}
		}
		
		final Set<String> frags = new HashSet<String>();
		frags.add("uk.ac.diamond.");
		frags.add("uk.ac.gda.");
		frags.add("uk.ac.sda.");
		frags.add("org.edna.");
		frags.add("commons-math-");
		
		// We loop over all the plugins and add jars and
		// directories for various matches. We add folders
		// and jars here as some plugins may be expanded.
		final File plugins = libsFolder.getParentFile();
		
		if (plugins.exists() && plugins.isDirectory()) {
			final File[] fa = plugins.listFiles();
			for (int i = 0; i < fa.length; i++) {
				for (String frag : frags) {
					if (fa[i].getName().startsWith(frag)) {
						if (fa[i].isDirectory() && System.getProperty("eclipse.debug.session")!=null) {
							if (fa[i].getName().equals("org.edna.workbench.target")) {
								libs.add(fa[i].getAbsolutePath()+"/bundles");
								libs.add(fa[i].getAbsolutePath()+"/bundles/commons-math-2.0.jar");
								libs.add(fa[i].getAbsolutePath()+"/bundles/uk.ac.diamond.jama-1.0.1.jar");
								libs.add(fa[i].getAbsolutePath()+"/eclipse/plugins");
								libs.add(fa[i].getAbsolutePath()+"/other/plugins");
								libs.add(fa[i].getAbsolutePath()+"/pydev/plugins");
							} else {
								libs.add(fa[i].getAbsolutePath()+"/src");
								libs.add(fa[i].getAbsolutePath()+"/bin");
							}
							continue;
						}
						libs.add(fa[i].getAbsolutePath());
					}
				}
			}
		}
	
		
		return libs;
	}

}
