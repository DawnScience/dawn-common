package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;

public class PythonImageToXYOperation extends AbstractPythonScriptOperation<PythonScriptModel> {

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.python.PythonImageToXYOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.ONE;
	}

	@Override
	protected Map<String, Object> packInput(IDataset input) {
		return OperationToPythonUtils.packImage(input);
	}

	@Override
	protected OperationData packAndValidateMap(Map<String, Object> output) {
		return OperationToPythonUtils.unpackXY(output);
	}

}
