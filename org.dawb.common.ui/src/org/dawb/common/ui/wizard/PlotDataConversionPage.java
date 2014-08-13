package org.dawb.common.ui.wizard;

public class PlotDataConversionPage extends ResourceChoosePage {

	public PlotDataConversionPage() {
		super("wizardPage", "Page exporting plotted data", null);
		setTitle("Export plotted data to file");
		setDirectory(true);
		
		this.setDirectory(false);
		this.setNewFile(true);
		this.setPathEditable(true);
	}
	
}
