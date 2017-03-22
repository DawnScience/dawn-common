package uk.ac.diamond.scisoft.analysis.processing.python;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.processing.Atomic;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;

@Atomic
public class PythonSavuOperation extends AbstractPythonSavuOperation<PythonSavuModel> {

	@Override
	public String getId() {
		return"uk.ac.diamond.scisoft.analysis.processing.python.PythonSavuOperation";
	}

	@Override
	public OperationRank getInputRank() {
		if (model != null && model.getPluginRank()!=null) {
			return OperationRank.get(model.getPluginRank());
		}
		return OperationRank.ANY;
	}

	@Override
	public OperationRank getOutputRank() {
		if (model.isMetaDataOnly()) {
			return getInputRank();
		}
		return OperationRank.TWO;
	}

	@Override
	protected Map<String, Object> packInput(IDataset input) {
		if (model.getPluginRank().equals(2)) {
		return OperationToPythonUtils.packImage(input);
		}
		else if (model.getPluginRank().equals(1)) {
		return OperationToPythonUtils.packXY(input);
		}
		else {
		return null;			
		}

		}


	@Override
	protected OperationData packAndValidateMap(Map<String, Object> output) {
		try {
			return OperationToPythonUtils.unpackImage(output);
//			return OperationToPythonUtils.unpackXY(output);
		} catch (MetadataException e) {
			throw new OperationException(this, e);
		}
	}

}
