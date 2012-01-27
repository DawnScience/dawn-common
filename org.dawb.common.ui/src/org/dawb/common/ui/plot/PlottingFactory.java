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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PlottingFactory {

	
	/**
	 * Reads the extension points for the plotting systems registered and returns
	 * a plotting system based on the users current preferences.
	 * 
	 * @return
	 */
	public static AbstractPlottingSystem getPlottingSystem() throws Exception {
				
		final ScopedPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(),"org.dawb.workbench.ui");
		String plotType = store.getString("org.dawb.plotting.system.choice");
		if (plotType==null) plotType = System.getProperty("org.dawb.plotting.system.choice");// For Geoff et. al. can override.
		if (plotType==null) plotType = "org.dawb.workbench.editors.plotting.lightWeightPlottingSystem"; // That is usually around
		
        AbstractPlottingSystem system = getPlottingSystem(plotType);
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
				
		return  getPlottingSystem("org.dawb.workbench.editors.plotting.lightWeightPlottingSystem");		
	}
	
	private static final AbstractPlottingSystem getPlottingSystem(final String plotType) throws CoreException {
		
        IConfigurationElement[] systems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.plottingClass");
        for (IConfigurationElement ia : systems) {
			if (ia.getAttribute("id").equals(plotType)) return (AbstractPlottingSystem)ia.createExecutableExtension("class");
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
}
