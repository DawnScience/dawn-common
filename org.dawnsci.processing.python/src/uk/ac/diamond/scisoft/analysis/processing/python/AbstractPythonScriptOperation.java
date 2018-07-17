package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.dawnsci.python.rpc.AnalysisRpcPythonPyDevService;
import org.dawnsci.python.rpc.PythonRunScriptService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPythonScriptOperation<T extends AbstractPythonScriptModel> extends AbstractOperation<T, OperationData> {

	protected AnalysisRpcPythonPyDevService s = null;
	protected PythonRunScriptService pythonRunScriptService;
	
	private final static Logger logger = LoggerFactory.getLogger(AbstractPythonScriptOperation.class);
	
	@Override
	public void init() {
		long t = System.currentTimeMillis();
		try {
			s = AnalysisRpcPythonPyDevService.create();
			pythonRunScriptService = new PythonRunScriptService(s);
			logger.debug("Time to start service: " + (System.currentTimeMillis()-t));
		} catch (Exception e) {
			logger.debug("Failed to start service in: " + (System.currentTimeMillis()-t));
			logger.error("Could not create script service!");
			throw new OperationException(this, "Could not create script service!");
		}
		
	}
	
	@Override
	public void dispose() {
		if (s != null) s.stop();
	}
	
	protected abstract Map<String, Object> packInput(IDataset input);
	
	protected abstract OperationData packAndValidateMap(Map<String, Object> output);

	protected OperationData postValidateProcessing(IDataset input, Map<String, Object> output, OperationData operationData) {
		return operationData;
	}
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		if (s == null || pythonRunScriptService == null) throw new OperationException(this, "Could not create python interpreter");
		if (model.getFilePath() == null || model.getFilePath().isEmpty()) throw new OperationException(this, "Path to script not set");
		
		Map<String,Object> inputs = packInput(input);
		
		try {
			Map<String, Object> out = pythonRunScriptService.runScript(model.getFilePath(), inputs);
			return postValidateProcessing(input, out, packAndValidateMap(out));
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException(this, "Could not run " + model.getFilePath() + " due to " + e.getMessage());
		}
	}

}
