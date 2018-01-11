package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.dawnsci.python.rpc.AnalysisRpcPythonPyDevService;
import org.dawnsci.python.rpc.PythonRunSavuService;
import org.eclipse.dawnsci.analysis.api.processing.Atomic;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Atomic
public class PythonSavuOperation extends AbstractOperation<PythonSavuModel,OperationData> {

	AnalysisRpcPythonPyDevService s = null;
	PythonRunSavuService pythonRunSavuService;
	private static final Logger logger = LoggerFactory.getLogger(PythonSavuOperation.class);
	protected Integer outputRank = null;

	@Override
	public String getId() {
		return"uk.ac.diamond.scisoft.analysis.processing.python.PythonSavuOperation";
	}

	@Override
	public OperationRank getInputRank() {
		if (model != null && model.getPluginRank() > -1) {
			return OperationRank.get(model.getPluginRank());
		}
		return OperationRank.ANY;
	}

	@Override
	public OperationRank getOutputRank() {
		if (outputRank != null)
			return OperationRank.get(outputRank);
		/*if (model.isMetaDataOnly()) {
			return getInputRank();
		}*/
		return OperationRank.SAME;
	}

	private Map<String, Object> packInput(IDataset input) {
		if (model.getPluginRank() == 2) {
			return OperationToPythonUtils.packImage(input);
		} else if (model.getPluginRank() == 1) {
			return OperationToPythonUtils.packXY(input);
		} else {
			return null;			
		}
	}


	private OperationData packAndValidateMap(Map<String, Object> output) {
		try {
			if (model.getPluginRank() == 2) {
				return OperationToPythonUtils.unpackImage(output);
			} else if (model.getPluginRank() == 1) {
				return OperationToPythonUtils.unpackXY(output);
			} else {
				return null;
			}
		} catch (MetadataException e) {
			throw new OperationException(this, e);
		}
	}

	@Override
	public void init() {
		long t = System.currentTimeMillis();
		try {
			s = AnalysisRpcPythonPyDevService.create();
			pythonRunSavuService = new PythonRunSavuService(s);
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
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		if (s == null || pythonRunSavuService == null) throw new OperationException(this, "Could not create python interpreter");
		logger.debug("pluginname and path are"+model.getPluginName()+" "+model.getPluginPath());
		if (model.getPluginName() == null || model.getPluginPath()== null) throw new OperationException(this, "Path to script not set");
		
		Map<String,Object> inputs = packInput(input);
		
		try {
			// need to pass a dictionary of inputs into here.
//			generate a hashmap in model, with a single getter to pass it here
			String pluginPath = model.getPluginPath();
			Map<String, Map<String, Object>> parameters = model.getParameters();
			Boolean metaDataOnly = model.isMetaDataOnly();
			logger.debug(parameters.toString());
			if (outputRank == null) {
				outputRank = pythonRunSavuService.get_output_rank(pluginPath, inputs, parameters.get(model.getPluginName()));
			}
			Map<String, Object> out = pythonRunSavuService.runSavu(pluginPath,parameters.get(model.getPluginName()),metaDataOnly,inputs);
			return packAndValidateMap(out);
		} catch (Exception e) {
			throw new OperationException(this, "Could not run " + model.getPluginName() + " due to " + e.getMessage());
		}
	}
}
