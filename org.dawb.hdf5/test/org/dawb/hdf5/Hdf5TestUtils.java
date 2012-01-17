/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.hdf5;

public class Hdf5TestUtils {

	/**
	 * 
	 * @param relPath = to pluging
	 * @return
	 */
	public static String getAbsolutePath(final String relPath) {
		if (Activator.getContext()==null) { // Tests not running in eclipse
			return "org.dawb.hdf5/"+relPath;
		}
		String dir = cleanPath(Activator.getContext().getBundle().getLocation());
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
        
        if (loc.startsWith("/C:/")) {
            loc = loc.substring(1);
        }
        
        loc = loc.replace("//", "/");
        loc = loc.replace("\\\\", "\\");

        return loc;
	}

}
