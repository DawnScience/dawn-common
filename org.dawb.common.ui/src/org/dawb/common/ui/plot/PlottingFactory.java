/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * PlottingFactory is the preferred way to get an AbstractPlottingSystem. Also
 * consider just using the IPlottingSystem interface as this is more likely to 
 * be persistent when declaring the type in the calling code.
 * 
 * 
 * 
 * @author fcp94556
 *
 */
public class PlottingFactory {

	/**
	 * This class has a public constructor so that the squish tests can get a references using
	 * the class loader. Really it should be private. 
	 * 
	 * In the squish tests there is a script called 'use_case_utils.py' with a def called getPlottingSystem(...)
	 * which requires this to be there.
	 * 
	 */
	public PlottingFactory() {
		
	}
	
	/**
	 * Reads the extension points for the plotting systems registered and returns
	 * a plotting system based on the users current preferences.
	 * 
	 * @return
	 */
	public static AbstractPlottingSystem createPlottingSystem() throws Exception {
				
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,"org.dawb.workbench.ui");
		String plotType = store.getString("org.dawb.plotting.system.choice");
		if (plotType==null) plotType = System.getProperty("org.dawb.plotting.system.choice");// For Geoff et. al. can override.
		if (plotType==null) plotType = "org.dawb.workbench.editors.plotting.lightWeightPlottingSystem"; // That is usually around
		
        AbstractPlottingSystem system = createPlottingSystem(plotType);
        if (system!=null) return system;
		
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.plottingClass");
        AbstractPlottingSystem ifnotfound = (AbstractPlottingSystem)systems[0].createExecutableExtension("class");
		store.setValue("org.dawb.plotting.system.choice", systems[0].getAttribute("id"));
		return ifnotfound;
		
	}
	
	/**
	 * Always returns the light weight plotter if one is available, otherwise null.
	 * 
	 * @return
	 */
	public static AbstractPlottingSystem getLightWeightPlottingSystem() throws Exception {
				
		return  createPlottingSystem("org.dawb.workbench.editors.plotting.lightWeightPlottingSystem");		
	}
	
	private static final AbstractPlottingSystem createPlottingSystem(final String plottingSystemId) throws CoreException {
		
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.plottingClass");
        for (IConfigurationElement ia : systems) {
			if (ia.getAttribute("id").equals(plottingSystemId)) return (AbstractPlottingSystem)ia.createExecutableExtension("class");
		}
		
        return null;
	}

	
	public static String[][] getPlottingPreferenceChoices() {
		
		final List<String[]> choices = new ArrayList<String[]>(7);
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.plottingClass");
        for (IConfigurationElement ia : systems) {
        	choices.add(new String[]{ia.getAttribute("visible_type"), ia.getAttribute("id")});
		}
        
        final String [][] ret = new String[choices.size()][];
        for (int i = 0; i < choices.size(); i++) {
        	ret[i] = choices.get(i);
		}
        return ret;
	}

	private static Map<String, AbstractPlottingSystem> plottingSystems;
	
	public static void clear() {
		if (plottingSystems!=null) plottingSystems.clear();
	}
	
	/**
	 * Removes a plot system from the registered names.
	 * @param plotName
	 * @return the removed system
	 */
	public static AbstractPlottingSystem removePlottingSystem(String plotName) {
		if (plottingSystems==null) return null;
		return plottingSystems.remove(plotName);
	}

	/**
	 * Registers a plotting system by name. NOTE if the name is already used this
	 * will overwrite the old one!
	 * 
	 * @param plotName
	 * @param abstractPlottingSystem
	 * @return the replaced system if any or null otherwise.
	 */
	public static AbstractPlottingSystem registerPlottingSystem(final String                 plotName,
			                                                   final AbstractPlottingSystem abstractPlottingSystem) {
		
		if (plottingSystems==null) plottingSystems = new HashMap<String, AbstractPlottingSystem>(7);
		return plottingSystems.put(plotName, abstractPlottingSystem);
	}
	
	/**
	 * Get a plotting system by name. NOTE if more than one plotting system has the same name the
	 * last one registered with this name is returned.
	 * 
	 * NOTE an AbstractPlottingSystem is also a IToolPageSystem, you can get tool pages here.
	 * 
	 * @param plotName
	 * @return AbstractPlottingSystem or null
	 */
	public static AbstractPlottingSystem getPlottingSystem(String plotName) {
		if (plottingSystems==null) return null;
		return plottingSystems.get(plotName);
	}
}
