package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.dawnsci.python.rpc.AnalysisRpcPythonPyDevService;
import org.dawnsci.python.rpc.PythonRunScriptService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

public abstract class AbstractPythonScriptOperation<T extends PythonScriptModel> extends AbstractOperation<PythonScriptModel,OperationData> {

	AnalysisRpcPythonPyDevService s = null;
	PythonRunScriptService pythonRunScriptService;
	
	@Override
	public void init() {
		
		try {
			s = new AnalysisRpcPythonPyDevService(false);
			pythonRunScriptService = new PythonRunScriptService(s);
		} catch (Exception e) {
			throw new OperationException(this, "Could not create script service!");
		}
		
	}
	
	@Override
	public void dispose() {
		if (s != null) s.stop();
	}
	
	protected abstract Map<String, Object> packInput(IDataset input);
	
	protected abstract OperationData packAndValidateMap(Map<String, Object> output);
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		if (s == null || pythonRunScriptService == null) throw new OperationException(this, "Could not create python interpreter");
		if (model.getFilePath() == null || model.getFilePath().isEmpty()) throw new OperationException(this, "Path to script not set");
		
		Map<String,Object> inputs = packInput(input);
		
		try {
			Map<String, Object> out = pythonRunScriptService.runScript(model.getFilePath(), inputs);
			return packAndValidateMap(out);
		} catch (Exception e) {
			throw new OperationException(this, "Could not run " + model.getFilePath());
		}
	}

}
