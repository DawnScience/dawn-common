package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.january.dataset.IDataset;

public class PythonImageToImageOperation extends AbstractPythonScriptOperation<PythonScriptModel> {

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.python.PythonImageToImageOperation";
	}

	
	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}
	
	@Override
	protected Map<String, Object> packInput(IDataset input) {
		return OperationToPythonUtils.packImage(input);
	}
	
	@Override
	protected OperationData packAndValidateMap(Map<String, Object> output){ 
		return OperationToPythonUtils.unpackImage(output);
	}

}
