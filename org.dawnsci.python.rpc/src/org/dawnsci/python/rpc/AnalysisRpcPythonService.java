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
import java.util.Arrays;
import java.util.Map;

import org.dawb.common.util.net.NetUtils;
import org.dawnsci.python.rpc.commandline.CommandLineException;
import org.dawnsci.python.rpc.commandline.ManagedCommandline;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcException;
import org.eclipse.dawnsci.analysis.api.rpc.IAnalysisRpcClient;
import org.eclipse.dawnsci.analysis.api.rpc.IAnalysisRpcPythonRemote;
import org.eclipse.dawnsci.analysis.api.rpc.IAnalysisRpcPythonService;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;

/**
 * Launch a python process running the Python Analysis RPC Server
 * (-mscisoftpy.rpc). The launched analysis server must contain the additional
 * {@link IAnalysisRpcPythonRemote} interface.
 * 
 * Note this class is abstract because it needs extra information to construct
 * properly.
 * 
 * TODO This class does not belong in org.dawb.common.python.rpc, ideally it
 * would be with the rest of the AnalysisRpc code in
 * uk.ac.diamond.scisoft.analysis.rpc. However there are some common utility
 * dependencies that mean for now it is here.
 */
public class AnalysisRpcPythonService implements IAnalysisRpcPythonService {
	private static final Logger logger = LoggerFactory
			.getLogger(AnalysisRpcPythonService.class);

	public static final String SCISOFT_PY_RPC_MAIN_MODULE = "scisoftpy.rpc";
	public static final String PROPERTY_FREE_PORT_KEY = Activator.PLUGIN_ID
			+ "rpc.python.server.free.port";

	private final String jobUserDescription;
	private final File workingDir;
	private final Map<String, String> env;
	private final int port;
	private final IAnalysisRpcClient client;
	private final IAnalysisRpcPythonRemote proxy;
	private final String[] parameters;

	private ManagedCommandline command;

	/**
	 * Simplified
	 * {@link #AnalysisRpcPythonService(String, File, String[], int, String[])}
	 * for use when only minimal settings are required.
	 * 
	 * @param jobUserDescription
	 *            Description for the Job that can be used to terminate. This
	 *            job will appear in the standard Eclipse location of jobs.
	 * @param pythonExe
	 *            The Python Executable to launch
	 * @param workingDir
	 *            The working directory
	 * @param envp
	 *            Environment to launch Python in. Note this should include a
	 *            PYTHONPATH which contains {@value #SCISOFT_PY_RPC_MAIN_MODULE}
	 *            on it.
	 * @throws AnalysisRpcException
	 */
	protected AnalysisRpcPythonService(String jobUserDescription,
			File pythonExe, File workingDir, Map<String, String> env)
			throws AnalysisRpcException {
		this(jobUserDescription, pythonExe, workingDir, env,
				assignPort(PROPERTY_FREE_PORT_KEY));
	}

	/* Only exists so that assignPort can end up in two places. */
	private AnalysisRpcPythonService(String jobUserDescription, File pythonExe,
			File workingDir, Map<String, String> env, int port)
			throws AnalysisRpcException {
		this(jobUserDescription, workingDir, env, port, createParameters(
				pythonExe, port));
	}

	/**
	 * Create a new AnalysisRpcPythonService
	 * 
	 * @param jobUserDescription
	 *            Description for the Job that can be used to terminate. This
	 *            job will appear in the standard Eclipse location of jobs. Can
	 *            be null if custom launch is used.
	 * @param workingDir
	 *            The working directory. Can be null if custom launch is used or
	 *            the default working directory is desired.
	 * @param env
	 *            Environment to launch Python in. Note this should include a
	 *            PYTHONPATH which contains {@value #SCISOFT_PY_RPC_MAIN_MODULE}
	 *            on it. Can be null if custom launch is used.
	 * @param port
	 *            Port that the Analysis RPC server will use. Optionally use
	 *            {@link #assignPort(String)} to assign a port
	 * @param parameters
	 *            The command line parameters, includig the Python executable.
	 *            Can be null if custom launch is used.
	 * @throws AnalysisRpcException
	 */
	protected AnalysisRpcPythonService(String jobUserDescription,
			File workingDir, Map<String, String> env, int port,
			String[] parameters) throws AnalysisRpcException {
		this.jobUserDescription = jobUserDescription;
		this.parameters = parameters;
		this.workingDir = workingDir;
		this.env = env;
		this.port = port;
		this.client = new AnalysisRpcClient(this.port);
		this.proxy = client.newProxyInstance(IAnalysisRpcPythonRemote.class);
		launch();

		// Initialise the plotting port
		setPlottingPort(getSciSoftPlottingPort());
	}

	@Override
	public void addHandlers(String pycode, String[] handler_names)
			throws AnalysisRpcException {
		proxy.addHandlers(pycode, handler_names);
	}

	@Override
	public void addHandler(String pycode, String single_handler_name)
			throws AnalysisRpcException {
		proxy.addHandlers(pycode, new String[] { single_handler_name });
	}

	@Override
	public void setPlottingPort(int port) throws AnalysisRpcException {
		proxy.setPlottingPort(port);
	}

	@Override
	public IAnalysisRpcClient getClient() {
		return client;
	}

	protected void launch() throws AnalysisRpcException {
		if (command != null) {
			throw new AnalysisRpcException("Server has already been launched");
		}

		command = new ManagedCommandline();
		command.addArguments(parameters);
		command.setEnv(env);
		if (workingDir != null) {
			try {
				command.setWorkingDir(workingDir);
			} catch (CommandLineException e) {
				throw new AnalysisRpcException("Working directory is invalid "
						+ workingDir.toString(), e);
			}
		}

		// Currently log back Python output directly to the log file.
		command.setStreamLogsToLogging(true);
		try {
			command.execute();
		} catch (IOException e) {
			throw new AnalysisRpcException("Failed to launch python command "
					+ Arrays.toString(parameters), e);
		}

		Job job = new Job(jobUserDescription) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				while (command != null) {
					if (monitor.isCanceled()) {
						break;
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
					}
				}
				AnalysisRpcPythonService.this.stop();
				return Status.OK_STATUS;
			}
		};
		job.schedule();

		client.waitUntilAlive();
	}

	@Override
	public void stop() {
		if (command == null)
			return;
		if (command.getProcess() == null)
			return;
		command.getProcess().destroy();
		command = null;
	}

	protected static int assignPort(String key) {
		return NetUtils.getFreePort(getStartingPort(key));
	}

	protected static int getStartingPort(String key) {
		String portstr = System.getProperty(key, "8713");
		int startingPort = Integer.parseInt(portstr);
		return startingPort;
	}

	protected static String[] createParameters(File pythonExe, int port) {
		String[] parameters = new String[4];
		parameters[0] = pythonExe.toString();
		parameters[1] = "-u"; // Fully unbuffered
		parameters[2] = "-m" + SCISOFT_PY_RPC_MAIN_MODULE;
		parameters[3] = "" + port;
		return parameters;

	}

	/**
	 * Call to get the scisoft plotting port, may be "", null or 0. Will check
	 * temp variable set to dynamic port.
	 * 
	 * @return
	 */
	private static int getSciSoftPlottingPort() {

		final ScopedPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		int scisoftRpcPort = 0;
		try {
			scisoftRpcPort = store.getInt("analysisrpc.server.port");
			if (scisoftRpcPort > 0) {
				logger.info("Found RPC plotting port set to value of "
						+ scisoftRpcPort);
				return scisoftRpcPort;
			}
		} catch (Exception ne) {
			scisoftRpcPort = 0;
		}

		if (scisoftRpcPort <= 0) {
			try {
				scisoftRpcPort = store.getInt("analysisrpc.server.port.auto");
				if (scisoftRpcPort > 0) {
					logger.info("Found RPC plotting port set to temporary value of "
							+ scisoftRpcPort);
					return scisoftRpcPort;
				}
			} catch (Exception ne) {
				scisoftRpcPort = 0;
			}
		}

		// TODO Ensure plotting is started programmatically in the GUI.
		return scisoftRpcPort;
	}

}
