/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.eclipse;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

public class Environment {

	/**
	 * Attempts to set environment variables automatically so that the user
	 * does not have to set them before starting the application.
	 * 
	 * This is in addition to the Bundle-NativeCode: statement in the plugin
	 * as depending on the native code bundled, it will not work.
	 * 
	 * This should be called before the bundle is loaded. It will be necessary
	 * to have the bundle in a non-compressed folder for this to work normally.
	 * 
	 * @param context
	 */
	public static void createEnvironmentVariables(final String        bundleName,
			                                      final String        linuxName,
			                                      final String        windowsName) throws Exception {
		
		final File    path   = BundleUtils.getBundlePathNoLoading(bundleName);
		final boolean is64   = System.getProperty("os.arch").indexOf("64")>-1;
		
		if (System.getProperty("os.name").toLowerCase().indexOf("linux")>-1) {
			final String arch = is64 ? "x86_64" : "i386";
			Environment.append(Environment.getenv(), linuxName, path+"/lib/Linux-"+arch);
		} else {
			final String arch = is64 ? "64" : "32";
			Environment.append(Environment.getenv(),    windowsName, path+"/lib/win"+arch);
			Environment.append(Environment.getwinenv(), windowsName, path+"/lib/win"+arch);
		}
	}
		
	@SuppressWarnings("unchecked")
    public static Map<String, String> getwinenv() throws Exception {

    	Class<?> sc = Class.forName("java.lang.ProcessEnvironment");
    	Field caseinsensitive = sc.getDeclaredField("theCaseInsensitiveEnvironment");
    	caseinsensitive.setAccessible(true);
    	return (Map<String, String>)caseinsensitive.get(null);

    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getenv() throws Exception {

    	Map<String, String> theUnmodifiableEnvironment = System.getenv();
    	Class<?> cu = theUnmodifiableEnvironment.getClass();
    	Field m = cu.getDeclaredField("m");
    	m.setAccessible(true);
    	return (Map<String, String>)m.get(theUnmodifiableEnvironment);
    }

	public static void append(final Map<String, String> env, final String name, final String value) {
		final String curValue = env.get(name);
		if (curValue==null||"".equals(curValue)) {
			env.put(name, value);
		} else {
			if (System.getProperty("os.name").toLowerCase().indexOf("linux")>-1) {
				env.put(name, value+":"+curValue);
			} else {
				env.put(name, value+";"+curValue);
			}
		}
	}

}
