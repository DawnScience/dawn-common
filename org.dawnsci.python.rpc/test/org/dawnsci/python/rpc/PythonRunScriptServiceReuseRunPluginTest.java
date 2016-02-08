/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc;

import java.io.IOException;
import java.util.Map;

import org.dawnsci.python.rpc.PythonRunScriptService;
import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

/**
 * This concrete test runs all the tests in
 * AbstractPythonRunScriptServicePluginTest with the same service.
 */
public class PythonRunScriptServiceReuseRunPluginTest extends
		AbstractPythonRunScriptServicePluginTest {

	private static AnalysisRpcPythonServiceManual service;
	private static PythonRunScriptService runScript;

	@Override
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> data) throws AnalysisRpcException {
		return runScript.runScript(scriptFullPath, data);
	}

	@Override
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> data, String funcName)
			throws AnalysisRpcException {
		return runScript.runScript(scriptFullPath, data, funcName);
	}

	@BeforeClass
	public static void before() throws AnalysisRpcException, IOException,
			CoreException {
		service = new AnalysisRpcPythonServiceManual();
		runScript = new PythonRunScriptService(service);
	}

	@AfterClass
	public static void after() {
		service.stop();
	}
}
