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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawb.common.python.PythonUtils;
import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.python.copiedfromeclipsesrc.JavaVmLocationFinder;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.REF;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.interpreters.JythonInterpreterManager;
import org.python.pydev.ui.interpreters.PythonInterpreterManager;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   InterpreterUtils
 *
 *   @author gerring
 *   @date Jul 29, 2010
 *   @project org.edna.pydev.extensions
 **/
public class InterpreterUtils {


	private static Logger logger = LoggerFactory.getLogger(InterpreterUtils.class);

	
    /**
     * Creates a Python interpreter by attempting to read where the python command being used originates.
     * @param name
     * @param monitor
     * @throws Exception
     */
	public static void createPythonInterpreter(String name, IProgressMonitor monitor) throws Exception{
		
		final String path = PythonUtils.getProbablePythonPath();

		if (path !=null) {
			createPythonInterpreter(name, path, monitor);
		}
	}

	public static void createPythonInterpreter(final String           name, 
									           final String           interpreterExePath,
									           final IProgressMonitor mon) throws Exception {

		if (!isPythonInterpreter(name, mon)) {

			final PythonInterpreterManager man = (PythonInterpreterManager)PydevPlugin.getPythonInterpreterManager();		
			final IInterpreterInfo        info = man.createInterpreterInfo(interpreterExePath, mon, false);
			info.setName(name);

			final Set<String> names = new HashSet<String>(1);
			names.add(name);
			
			man.setInfos(new IInterpreterInfo[]{info}, names, mon);

			PydevPlugin.getWorkspace().save(true, mon);

			logger.info("Workspace saved with interpreter: " + name);
		}
	}
	
	private static boolean isPythonInterpreter(String name, IProgressMonitor mon) {
		try {
		    return PydevPlugin.getPythonInterpreterManager().getInterpreterInfo(name, mon)!=null;
		} catch (Exception ne) {
			return false;
		}
	}
	private static boolean isJythonInterpreter(String name, IProgressMonitor mon) {
		try {
		    return PydevPlugin.getJythonInterpreterManager().getInterpreterInfo(name, mon)!=null;
		} catch (Exception ne) {
			return false;
		}
	}

	/**
	 * We programmatically create a Jython Interpreter so that the user does not have to.
	 * 
	 * @throws Exception
	 */
	public static void createJythonInterpreter(final String           name, 
			                                   final IProgressMonitor mon) throws Exception {


		// Horrible Hack warning: This code is copied from parts of Pydev to set up the interpreter and save it.
		if (!isJythonInterpreter(name, mon)) {

			// Code copies from Pydev when the user chooses a Jython interpreter - these are the defaults.
			final File   libs       = BundleUtils.getBundleLocation("uk.ac.gda.libs");
			final File   jydir      = new File(libs, "jython2.5.1"); // TODO Make more robust?
			final File   exeFile    = new File(jydir, "jython.jar");

			final File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
			if (!script.exists()) {
				throw new Exception("The file specified does not exist: " + script);
			}

			final JythonInterpreterManager man = (JythonInterpreterManager) PydevPlugin.getJythonInterpreterManager();		
			InterpreterInfo info = (InterpreterInfo)man.createInterpreterInfo(exeFile.getAbsolutePath(), mon, false);

			if (info == null) {
				// cancelled
				return;
			}

			// we have to find the jars before we restore the compiled libs
			List<File> jars = JavaVmLocationFinder.findDefaultJavaJars();
			for (File jar : jars) {
				info.libs.add(REF.getFileAbsolutePath(jar));
			}

			// Defines all third party libs that can be used in scripts.
			final List<String> gdaJars = LibsLocationFinder.findWorkspaceLibs();
			info.libs.addAll(gdaJars);

			// java, java.lang, etc should be found now
			info.restoreCompiledLibs(mon);
			info.setName(name);

			final Set<String> names = new HashSet<String>(1);
			names.add(name);
			
			man.setInfos(new IInterpreterInfo[]{info}, names, mon);

			logger.info("Jython interpreter registered: " + name);

			PydevPlugin.getWorkspace().save(true, mon);


			logger.info("Workspace saved with interpreter: " + name);
		}
	}

}
