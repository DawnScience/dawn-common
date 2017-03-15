package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.dawnsci.python.rpc.AnalysisRpcPythonPyDevService;
import org.dawnsci.python.rpc.PythonRunSavuService;
import org.dawnsci.python.rpc.PythonRunScriptService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

public abstract class AbstractPythonSavuOperation<T extends PythonSavuModel> extends AbstractOperation<PythonSavuModel,OperationData> {

	AnalysisRpcPythonPyDevService s = null;
	PythonRunSavuService pythonRunSavuService;
	
	@Override
	public void init() {
		
		try {
			s = AnalysisRpcPythonPyDevService.create();
			pythonRunSavuService = new PythonRunSavuService(s);
			// can I add a plugin populator here? drop down+params list test it with a fake param. No needs to be model...
			
			
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
		
		if (s == null || pythonRunSavuService == null) throw new OperationException(this, "Could not create python interpreter");
		System.out.println("pluginname and path are"+model.getPluginName()+" "+model.getPluginPath());
		if (model.getPluginName() == null || model.getPluginPath()== null) throw new OperationException(this, "Path to script not set");
		
		Map<String,Object> inputs = packInput(input);
		
		try {
			// need to pass a dictionary of inputs into here.
//			generate a hashmap in model, with a single getter to pass it here
			String pluginPath = (String) model.getPluginPath();
			Map <String, Object> parameters = (Map <String, Object>) model.getParameters();
			Boolean metaDataOnly = (Boolean) model.isMetaDataOnly();
			System.out.println(parameters);
			Map<String, Object> out = pythonRunSavuService.runSavu(pluginPath,parameters,metaDataOnly,inputs);
			return packAndValidateMap(out);
		} catch (Exception e) {
			throw new OperationException(this, "Could not run " + model.getPluginName() + " due to " + e.getMessage());
		}
	}

}
