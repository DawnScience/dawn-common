package uk.ac.diamond.scisoft.analysis.processing.python;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;

public abstract class AbstractPythonScriptModel extends AbstractOperationModel {
	public abstract String getFilePath();
}
