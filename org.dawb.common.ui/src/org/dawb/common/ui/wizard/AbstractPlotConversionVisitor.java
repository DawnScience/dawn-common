package org.dawb.common.ui.wizard;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionVisitor;
import org.dawnsci.plotting.api.IPlottingSystem;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public abstract class AbstractPlotConversionVisitor implements IConversionVisitor {

	protected IPlottingSystem system;
	
	public AbstractPlotConversionVisitor(IPlottingSystem system) {
		this.system = system;
	}

	@Override
	public String getConversionSchemeName() {
		return "Conversion of " + system.getPlotName();
	}

	@Override
	public void init(IConversionContext context) throws Exception {
		//Do nothing
	}
	
	@Override
	public abstract void visit(IConversionContext context, IDataset slice)
			throws Exception ;

	@Override
	public void close(IConversionContext context) throws Exception {
		//Do nothing
	}

	@Override
	public boolean isRankSupported(int length) {
		return true;
	}
	
	public abstract String getExtension();

}
