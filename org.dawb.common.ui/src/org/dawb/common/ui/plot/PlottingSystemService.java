package org.dawb.common.ui.plot;

import java.util.Hashtable;
import java.util.Map;

import org.dawb.common.services.ISystemService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * We rely on OSGI to only make one of these. Too many would not work!
 * @author fcp94556
 *
 */
public class PlottingSystemService extends AbstractServiceFactory implements ISystemService<IPlottingSystem> {

	
	private Map<String,IPlottingSystem> systems;
	
	public PlottingSystemService() {
		systems = new Hashtable<String,IPlottingSystem>(11);
	}
	
	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==ISystemService.class) {
			return this;
		} 
		return null;
	}

	@Override
	public IPlottingSystem getSystem(String partName) {
		return systems.get(partName);
	}

	@Override
	public IPlottingSystem putSystem(String partName, IPlottingSystem plottingSystem) {
		return systems.put(partName, plottingSystem);
	}

	@Override
	public IPlottingSystem removeSystem(String partName) {
		return systems.remove(partName);
	}

	@Override
	public void clear() {
		systems.clear();
	}

}
