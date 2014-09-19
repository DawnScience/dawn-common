/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.test;

import java.io.File;
import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class TestUtils {

	public static String getAbsolutePath(final Bundle bundle) {
		return cleanPath(bundle.getLocation());
	}
	
	/**
	 * 
	 * @param relPath = to pluging
	 * @return
	 */
	public static String getAbsolutePath(final String bundle, final String relPath) {
		return TestUtils.getAbsolutePath(Platform.getBundle(bundle), relPath);
	}

	/**
	 * 
	 * @param relPath = to pluging
	 * @return
	 */
	public static String getAbsolutePath(final Bundle bundle, final String relPath) {
		String dir = cleanPath(bundle.getLocation());
		return dir+relPath;
	}

	
	public static String cleanPath(String loc) {
		
		// Remove reference:file: from the start. TODO find a better way,
	    // and test that this works on windows (it might have ///)
        if (loc.startsWith("reference:file:")){
        	loc = loc.substring(15);
        } else if (loc.startsWith("file:")) {
        	loc = loc.substring(5);
        } else {
        	return loc;
        }
        
        loc = loc.replace("//", "/");
        loc = loc.replace("\\\\", "\\");
        
        boolean isWindows = System.getProperty("os.name").toUpperCase(Locale.ENGLISH).contains("WINDOWS") &&
				            (File.separatorChar == '\\');
        
        if (isWindows&&loc.startsWith("/")) {
        	loc = loc.substring(1);
        }

        return loc;
	}


}
