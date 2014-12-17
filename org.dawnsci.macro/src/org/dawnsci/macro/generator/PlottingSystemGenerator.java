package org.dawnsci.macro.generator;

import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

class PlottingSystemGenerator extends AbstractMacroGenerator<IPlottingSystem> {

	@Override
	public String getPythonCommand(IPlottingSystem source) {
		return createCommand(source);
	}

	@Override
	public String getJythonCommand(IPlottingSystem source) {
		return createCommand(source);
	}

	private String createCommand(IPlottingSystem source) {
		return "ps = dnp.plot.getPlottingSystem(\""+source.getPlotName()+"\")";
	}

}
