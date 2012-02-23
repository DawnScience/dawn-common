package org.dawb.common.ui.plot.tool;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.eclipse.ui.part.Page;

/**
 * Page to extend for adding a tool to the plotting.
 * @author fcp94556
 *
 */
public abstract class AbstractToolPage extends Page implements IToolPage {

	
	private IToolPageSystem toolSystem;
	private IPlottingSystem plotSystem;

	public AbstractToolPage(IToolPageSystem toolSystem) {
		this.toolSystem = toolSystem;
	}
	@Override
	public void setPlottingSystem(IPlottingSystem system) {
		this.plotSystem = system;
	}

	@Override
	public IPlottingSystem getPlottingSystem() {
		return plotSystem;
	}

	@Override
	public IToolPageSystem getToolSystem() {
		return toolSystem;
	}

}
