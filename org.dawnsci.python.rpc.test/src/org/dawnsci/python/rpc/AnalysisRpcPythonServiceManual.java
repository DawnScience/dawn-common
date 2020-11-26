/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcException;
import org.python.pydev.core.CorePlugin;

class AnalysisRpcPythonServiceManual extends AnalysisRpcPythonService {
	public AnalysisRpcPythonServiceManual() throws AnalysisRpcException,
			IOException, CoreException {
		super("AnalysisRpcPythonServiceTest", new File("python"), (File) null,
				createEnv());
	}

	public static Map<String, String> createEnv() throws IOException,
			CoreException {
		Map<String, String> env = new HashMap<String, String>();
		String scisoftpath = BundleUtils.getBundleLocation(
				PythonService.SCISOFTPY).getAbsolutePath();
		env.put("PYTHONPATH", scisoftpath + File.pathSeparator + scisoftpath
				+ "/src" + File.pathSeparator + CorePlugin.getPySrcPath());
		return env;
	}
}
