package org.dawb.common.python.rpc;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
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
