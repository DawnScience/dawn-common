package org.dawb.common.python.rpc;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

/**
 * The methods that must be supported by the Python Analysis RPC server that is
 * launched by {@link AnalysisRpcPythonService}.
 */
public interface IAnalysisRpcPythonRemote {
	/**
	 * Add new handlers to the running server. The pycode is 'exec'uted with
	 * custom dictionaries, the names in handler_names must be defined by code
	 * and then are added to the server
	 * 
	 * @param pycode
	 *            The Python code that is executed (with exec) to generate the
	 *            handlers named in handler_names
	 * @param handler_names
	 *            The string names of the handlers to register with the server
	 * @throws AnalysisRpcException
	 */
	public void addHandlers(String pycode, String[] handler_names)
			throws AnalysisRpcException;

	/**
	 * Set the scisoftpy plotting port to direct plots back to.
	 * 
	 * @param port
	 *            Port that the Analysis RPC Plot Server is listening on
	 * @throws AnalysisRpcException
	 */
	public void setPlottingPort(int port) throws AnalysisRpcException;
}