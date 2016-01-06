package org.dawnsci.macro.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;


/**
 * Class to provide automatic generation of macro content.
 * 
 * @author Matthew Gerring
 *
 */
public class MacroFactory {

	/**
	 * Cannot seem to get this in an automated way, have asked PC.
	 * The supported types are seen in pyroi.py
	 */
	static final Map<ClassKey, AbstractMacroGenerator<? extends Object>> generators;
	static {
		Map<ClassKey, AbstractMacroGenerator<? extends Object>> tmp = new HashMap<ClassKey, AbstractMacroGenerator<? extends Object>>(7);
		tmp.put(new ClassKey(IRegion.class),  new RegionGenerator());
		tmp.put(new ClassKey(IROI.class),     new RegionGenerator());
		tmp.put(new ClassKey(IDataset.class), new DatasetGenerator());
		tmp.put(new ClassKey(Map.class),      new MapGenerator());
		tmp.put(new ClassKey(List.class),     new ListGenerator());
		tmp.put(new ClassKey(IPlottingSystem.class),  new PlottingSystemGenerator());
	    generators = Collections.unmodifiableMap(tmp);
	}
	
	/**
	 * 
	 * If there is no generator for MacroEventObject, the original 
	 * event is returned intact.
	 * 
	 * @param evt
	 * @return
	 */
	public static MacroEventObject generate(MacroEventObject evt) {
		
		if (!evt.isGeneratable()) return evt;
		
		ClassKey key = new ClassKey(evt.getSource().getClass());
		if (generators.containsKey(key)) {
			return generators.get(key).generate(evt);
		}
		return evt;
	}

	public static AbstractMacroGenerator<? extends Object> getGenerator(Class<? extends Object> clazz) {
		return generators.get(new ClassKey(clazz));
	}
	

}
