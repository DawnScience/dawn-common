package uk.ac.diamond.scisoft.analysis.processing.python;

import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;

public class PythonScriptModel extends AbstractPythonScriptModel {

	@OperationModelField(hint="Enter the path to the script", file = FileType.EXISTING_FILE, label = "File")
	private String filePath = "";
	

	@Override
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		firePropertyChange("filePath", this.filePath, this.filePath = filePath);
	}
}
