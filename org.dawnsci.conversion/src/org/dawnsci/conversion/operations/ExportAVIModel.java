package org.dawnsci.conversion.operations;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;

public class ExportAVIModel extends AbstractOperationModel {
	
	@OperationModelField(label = "Frame rate", hint="Enter the frame rate, in frames per second")
	private int frameRate = 30;
	
	@OperationModelField(label = "Output File Path", hint="Enter the path to save output file to, leave blank to autogenerate", file = FileType.NEW_FILE)
	private String filePath = "";

	@OperationModelField(label = "Colourmap Min,Max", hint="Enter comma separated values for the minimum and maximum of the colourmap")
	private double[] colormapMinMax = null;
	
	
	public double[] getColormapMinMax() {
		return colormapMinMax;
	}

	public void setColormapMinMax(double[] colormapMinMax) {
		firePropertyChange("colormapMinMax", this.colormapMinMax, this.colormapMinMax = colormapMinMax);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		firePropertyChange("filePath", this.filePath, this.filePath = filePath);
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int frameRate) {
		firePropertyChange("frameRate", this.frameRate, this.frameRate = frameRate);
	}

}
