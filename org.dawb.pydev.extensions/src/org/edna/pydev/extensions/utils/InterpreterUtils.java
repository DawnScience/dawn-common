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

import java.util.HashSet;
import java.util.Set;

import org.dawb.common.python.PythonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.interpreters.PythonInterpreterManager;
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

}
