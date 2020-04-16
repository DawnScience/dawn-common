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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.python.pydev.ast.interpreter_managers.InterpreterManagersAPI;
import org.python.pydev.ast.runners.SimpleRunner;
import org.python.pydev.core.CorePlugin;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.NotConfiguredInterpreterException;
import org.python.pydev.core.PythonNatureWithoutProjectException;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclass of {@link AnalysisRpcPythonService} that uses PyDev's
 * InterpreterInfos to generate PYTHONPATHs and path to Python executable.
 *
 * TODO Instead of having concrete constructors of AnalysisRpcPythonPyDevService
 * around, this service should be contributed using OSGI and have an
 * associated interface.
 */
public class AnalysisRpcPythonPyDevService extends AnalysisRpcPythonService {
	private static final String PYTHON_ON_PATH = "python";

	private static final Logger logger = LoggerFactory.getLogger(AnalysisRpcPythonPyDevService.class);

	// TODO should we add bundle dependency on uk.ac.diamond.scisoft.python?
	private static final String UK_AC_DIAMOND_SCISOFT_PYTHON = "uk.ac.diamond.scisoft.python";

	static {
		System.out.println("Starting Analysis RPC Pydev service.");
	}

	/**
	 * Create new service using the default (first listed) Python
	 * InterpreterInfo if any are available, or else use python from the PATH.
	 * 
	 * This is a convenience method that ensures that MisconfigurationException and
	 * NotConfiguredInterpreterException will not be thrown and be handled
	 * instead by picking python from the PATH.
	 * 
	 * @throws AnalysisRpcException
	 *             if an error occurs setting up the AnalysisRpc remote Python
	 *             server or the Java client
	 */
	public static AnalysisRpcPythonPyDevService create() throws AnalysisRpcException {
		try {
			return new AnalysisRpcPythonPyDevService(false);
		} catch (MisconfigurationException e) {
			String jobUserDescription = getJobUserDescription(PYTHON_ON_PATH);
			File pythonExe = new File(PYTHON_ON_PATH);
			Map<String, String> env = getEnvWithoutPyDev();
			return new AnalysisRpcPythonPyDevService(jobUserDescription, pythonExe, env);
		}
	}

	public AnalysisRpcPythonPyDevService() throws MisconfigurationException, AnalysisRpcException {
		this(true);
	}

	/**
	 * Create new service using the default (first listed) Python
	 * InterpreterInfo.
	 *
	 * @param autoConfig
	 *            if true, prompt user to configure a new Python Interpreter
	 * @throws NotConfiguredInterpreterException
	 *             if no interpreters are configured. As autoConfig is a long
	 *             running process, PyDev runs this asynchronously so this
	 *             exception is still thrown at this point. This is a
	 *             recoverable error that should generally be handled in the
	 *             code, with a user prompt.
	 * @throws MisconfigurationException
	 *             if any error occurs in the configuration of the python
	 *             interpreter. NOTE NotConfiguredInterpreterException is a
	 *             subclass of MisconfigurationException. This is a recoverable
	 *             error that should generally be handled in the code, with a
	 *             user prompt.
	 * @throws AnalysisRpcException
	 *             if an error occurs setting up the AnalysisRpc remote Python
	 *             server or the Java client
	 */
	public AnalysisRpcPythonPyDevService(boolean autoConfig)
			throws MisconfigurationException, AnalysisRpcException {
		this(getDefaultInfo(autoConfig), null);
	}

	/**
	 * Create new service using the named Python InterpreterInfo.
	 *
	 * @param interpreterName
	 *            name of the interpreter to use (as listed in Python
	 *            Interpreters)
	 * @throws MisconfigurationException
	 *             if any error occurs in the configuration of the Python
	 *             interpreter. This is raised if the interpreterName is not
	 *             found. This is a recoverable error that should generally be
	 *             handled in the code, with a user prompt.
	 * @throws AnalysisRpcException
	 *             if an error occurs setting up the AnalysisRpc remote Python
	 *             server or the Java client
	 */
	public AnalysisRpcPythonPyDevService(String interpreterName)
			throws MisconfigurationException, AnalysisRpcException {
		this(getInfoFromName(interpreterName), null);
	}

	/**
	 * Create new service using the Python InterpreterInfo as configured for the
	 * given project.
	 *
	 * @param project
	 *            project to use for InterpreterInfo. This means that the
	 *            PYTHONPATH used for the launched Python will match that of the
	 *            project.
	 * @throws MisconfigurationException
	 *             if any error occurs in the configuration of the Python
	 *             interpreter. This is raised if the project does not have a
	 *             PythonNature. This is a recoverable error that should
	 *             generally be handled in the code, with a user prompt.
	 * @throws AnalysisRpcException
	 *             if an error occurs setting up the AnalysisRpc remote Python
	 *             server or the Java client
	 */
	public AnalysisRpcPythonPyDevService(IProject project)
			throws MisconfigurationException, AnalysisRpcException {
		this(getInfoFromProject(project), project);
	}

	/**
	 * Create new service using the Python InterpreterInfo as configured for the
	 * given project.
	 *
	 * @param project
	 *            project to use for InterpreterInfo. This means that the
	 *            PYTHONPATH used for the launched Python will match that of the
	 *            project.
	 * @throws MisconfigurationException
	 *             if any error occurs in the configuration of the Python
	 *             interpreter. This is raised if the project does not have a
	 *             PythonNature. This is a recoverable error that should
	 *             generally be handled in the code, with a user prompt.
	 * @throws AnalysisRpcException
	 *             if an error occurs setting up the AnalysisRpc remote Python
	 *             server or the Java client
	 */
	public AnalysisRpcPythonPyDevService(IInterpreterInfo interpreter,
			IProject project) throws AnalysisRpcException {
		this(getJobUserDescription(interpreter), getPythonExe(interpreter), getEnv(interpreter, project));
	}

	/**
	 * The private constructor, accessed via the create* methods or public
	 * constructors. The key thing about this constructor is all the PyDev
	 * settings have been flattened so no PyDev types are needed by this point.
	 * The callers are responsible for resolving to actual exe name, etc.
	 * 
	 * @param jobUserDescription
	 *            Description of the job, use
	 *            {@link #getJobUserDescription(String)} helper.
	 * @param pythonExe
	 *            Python executable.
	 * @param env
	 *            Environment, containing SciSoftPy on the PYTHONPATH
	 * @throws AnalysisRpcException
	 *             if an error occurs setting up the AnalysisRpc remote Python
	 *             server or the Java client
	 */
	private AnalysisRpcPythonPyDevService(String jobUserDescription, File pythonExe, Map<String, String> env)
			throws AnalysisRpcException {
		super(jobUserDescription, pythonExe, null, env);

		// Default the port in the launched PyDev to this server
		getClient().setPyDevSetTracePort(getPyDevDebugServerPort());
	}

	private static IInterpreterInfo getDefaultInfo(boolean autoConfig)
			throws MisconfigurationException {
		IInterpreterManager pythonInterpreterManager = InterpreterManagersAPI.getPythonInterpreterManager();
		return pythonInterpreterManager.getDefaultInterpreterInfo(autoConfig);
	}

	private static IInterpreterInfo getInfoFromName(String interpreterName)
			throws MisconfigurationException {
		IInterpreterManager pythonInterpreterManager = InterpreterManagersAPI.getPythonInterpreterManager();
		return pythonInterpreterManager.getInterpreterInfo(interpreterName,
				new NullProgressMonitor());
	}

	private static IInterpreterInfo getInfoFromProject(IProject project)
			throws MisconfigurationException {
		PythonNature nature = PythonNature.getPythonNature(project);
		if (nature == null) {
			throw new MisconfigurationException(
					"The project does not appear to have a "
							+ "valid Python Nature, it needs to "
							+ "be set as a PyDev Project");
		}
		IInterpreterInfo info;
		try {
			info = nature.getProjectInterpreter();
		} catch (PythonNatureWithoutProjectException e) {
			// Simplify the interface of the users of getInfoFromProject.
			// PythonNatureWithoutProjectException is only thrown from one place
			// and it probably should be a subclass of MisconfigurationException
			throw new MisconfigurationException(e.getMessage(), e);
		}
		return info;
	}

	private static String getJobUserDescription(IInterpreterInfo interpreter) {
		return getJobUserDescription(interpreter.getExecutableOrJar());
	}

	private static String getJobUserDescription(String pythonExe) {
		return "Python Service (" + pythonExe + ")";
	}

	private static File getPythonExe(IInterpreterInfo interpreter) {
		return new File(interpreter.getExecutableOrJar());
	}

	private static Map<String, String> getEnv(IInterpreterInfo interpreter,
			IProject project) {
		IPythonNature pythonNature = null;
		if (project != null) {
			pythonNature = PythonNature.getPythonNature(project);
		}

		IInterpreterManager manager = InterpreterManagersAPI.getPythonInterpreterManager();

		String[] envp = null;
		try {
			waitForPythonNaturesToLoad();
			envp = SimpleRunner.getEnvironment(pythonNature, interpreter,
					manager);
		} catch (CoreException e) {
			// Should be unreachable
			logger.error("exception occurred while setting environemt", e);
		}

		final Map<String, String> env = new HashMap<String, String>(
				System.getenv());
		for (String s : envp) {
			String kv[] = s.split("=", 2);
			env.put(kv[0], kv[1]);
		}
		return updatePythonPathForSciSoftPy(env);
	}

	private static Map<String, String> getEnvWithoutPyDev() {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        Map<String, String> env = launchManager.getNativeEnvironment();
		return updatePythonPathForSciSoftPy(env);
	}

	private static Map<String, String> updatePythonPathForSciSoftPy(Map<String, String> env) {

		// To support this flow, we need both Diamond and PyDev's python
		// paths in the PYTHONPATH. We add the expected ones here.
		// NOTE: This can be problematic in cases where the user really
		// wanted a different Diamond or PyDev python path. Therefore we
		// force the paths in here.
		// TODO consider if scisoftpath should be added
		// in AnalysisRpcPythonService instead
		String path = env.get("PYTHONPATH");
		if (path == null) {
			path = "";
		}
		StringBuilder pythonpath = new StringBuilder(path);
		if (pythonpath.length() > 0) {
			pythonpath.append(File.pathSeparator);
		}

		String pyDevPySrc = getPyDevPySrc();
		if (pyDevPySrc != null) {
			pythonpath.append(pyDevPySrc).append(File.pathSeparator);
		}

		String scisoftpath = getScisoftPath();
		if (scisoftpath != null) {
			pythonpath.append(scisoftpath).append(File.pathSeparator);
			pythonpath.append(scisoftpath + "/src").append(File.pathSeparator);
		}

		env.put("PYTHONPATH", pythonpath.toString());

		return env;
	}

	/**
	 * PyDev defers the full loading of PythonNature to a background job. The
	 * result is if you try to access the nature before it is ready you can't
	 * get all the information. In our case we want to get the information about
	 * how to construct the PYTHONPATH, therefore we need the natures to be
	 * loaded. So this function blocks until all projects with a PythonNature
	 * have that nature loaded.
	 */
	private static void waitForPythonNaturesToLoad() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for (int i = 100; i > 0; i--) {
			List<String> list = new ArrayList<String>();
			IProject[] projects = workspace.getRoot().getProjects();
			for (IProject project : projects) {
				if (project.isAccessible()) {
					try {
						if (project
								.isNatureEnabled(PythonNature.PYTHON_NATURE_ID)) {
							PythonNature nature = PythonNature
									.getPythonNature(project);
							if (!nature.isOkToUse()) {
								list.add(project.getName());
							}
						}
					} catch (CoreException e) {
						// No PyDev Nature or otherwise unusable, so no
						// workaround to fix
					}
				}
			}

			if (list.isEmpty()) {
				logger.info("Python Natures for all PyDev projects loaded");
				break;
			}

			String waiting = "Waiting (up to " + i / 10.0 + " seconds)"
					+ " for Python Natures to load for these projects: ";
			String join = StringUtils.join(list.toArray(), ", ");
			logger.info(waiting + join);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	public static String getScisoftPath() {
		String scisoftpath = null;
		try {
			scisoftpath = BundleUtils.getBundleLocation(
					UK_AC_DIAMOND_SCISOFT_PYTHON).getAbsolutePath();
		} catch (IOException e) {
			logger.error(UK_AC_DIAMOND_SCISOFT_PYTHON
					+ " not available, import of scisoftpy.rpc may fail", e);
		} catch (NullPointerException e) {
			logger.error(UK_AC_DIAMOND_SCISOFT_PYTHON
					+ " not available, import of scisoftpy.rpc may fail", e);
		}
		return scisoftpath;
	}

	private static String getPyDevPySrc() {
		String pyDevPySrc = null;
		try {
			pyDevPySrc = CorePlugin.getPySrcPath().getAbsolutePath();
		} catch (CoreException e) {
			logger.error(
					"PydevPlugin's Src Path not available, debugging launched Python may not work",
					e);
		}
		return pyDevPySrc;
	}

	/**
	 * Get the PyDev Debug Server Listening Port.
	 * XXX This method is a reimplementation of:
	 * DebugPluginPrefsInitializer.getRemoteDebuggerPort
	 * which suffers from two problems:
	 * 1) com.python.pydev.debug is not an exported package
	 * 2) The DebugPluginPrefsInitializer is a preference initializer, but it
	 *    violates this rule for preferences:
	 *       Note: Clients should only set default preference values for their own bundle.
	 * Therefore this method attempts to get the current value set for the port,
	 * but in the case that the default-default value of 0 is returned (meaning
	 * preference has not been initialised) we return the default value.
	 */
	public static int getPyDevDebugServerPort() {
		IPreferenceStore store = PydevPlugin.getDefault().getPreferenceStore();
		// XXX should use DebugPluginPrefsInitializer.PYDEV_REMOTE_DEBUGGER_PORT
		int port = store.getInt("PYDEV_REMOTE_DEBUGGER_PORT");
		if (port == 0) {
			// XXX should use DebugPluginPrefsInitializer.DEFAULT_REMOTE_DEBUGGER_PORT
			port = 5678;
		}
		return port;
	}
}
