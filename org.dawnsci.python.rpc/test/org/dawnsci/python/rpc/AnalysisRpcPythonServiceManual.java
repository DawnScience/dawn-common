package org.dawnsci.python.rpc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.util.eclipse.BundleUtils;
import org.dawnsci.python.rpc.AnalysisRpcPythonService;
import org.eclipse.core.runtime.CoreException;
import org.python.pydev.plugin.PydevPlugin;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

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
				"uk.ac.diamond.scisoft.python").getAbsolutePath();
		env.put("PYTHONPATH", scisoftpath + File.pathSeparator + scisoftpath
				+ "/src" + File.pathSeparator + PydevPlugin.getPySrcPath());
		return env;
	}
}
