package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;

public class PythonSavuOperation extends AbstractPythonSavuOperation<PythonSavuModel> {

	@Override
	public String getId() {
		return"uk.ac.diamond.scisoft.analysis.processing.python.PythonSavuOperation";
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
		return OperationToPythonUtils.packXY(input);
	}

	@Override
	protected OperationData packAndValidateMap(Map<String, Object> output) {
		try {
			return OperationToPythonUtils.unpackXY(output);
		} catch (MetadataException e) {
			throw new OperationException(this, e);
		}
	}

}
