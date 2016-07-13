/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc.action;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dawnsci.python.rpc.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.jface.action.Action;

public class InjectPyDevConsoleAction extends Action {

	private Map<String, String> params;
	private InjectPyDevConsole injector;
	private Map<String, IDataset> data;
	private boolean isDataInjected = true;

	public InjectPyDevConsoleAction(String label) {
		super(label, Activator.getImageDescriptor("icons/application_osx_terminal.png"));
		this.params = new HashMap<String,String>(7);
	}

	public void run() {
		try {
			// Console may have been closed, see if we can get the active
			// one that they are using.
			if (injector != null && !injector.isConsoleAvailable()) {
				injector = null;
			}
			// Otherwise open one.
			if (injector==null) {
				injector = new InjectPyDevConsole(params);
				// Opens the console if required, including if it was closed.
				if (isDataInjected)
					injector.inject(data);
				injector.open(true);
			} else {
				if (isDataInjected)
					injector.inject(data);
				if (!isDataInjected)
					// if we don't inject data (simple cmd injection, then we
					// run the open method)
					injector.open(true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Call this method to manually set the dataset which we should use to 
	 * send to the console. This data is currently sent using flattening.
	 * 
	 * This will clear other datasets already sent. To set more than one
	 * dataset at once, call setData(Map).
	 * 
	 * @param name
	 * @param data
	 */
	public void setData(String name, IDataset value) {
		if (this.data==null) data = new LinkedHashMap<String, IDataset>();
		data.clear();
		data.put(name, value);
	}
	
	/**
	 * Set the map of datsets which should be avialable once the console is started.
	 * @param data
	 */
	public void setData(Map<String, IDataset> data) {
		this.data = data;
	}

	public boolean isDataInjected() {
		return isDataInjected;
	}

	/**
	 * is set by default to True. Set to False if no need for data and only a
	 * basic command needs to be injected into the console
	 * 
	 * @param isDataInjected
	 */
	public void setDataInjected(boolean isDataInjected) {
		this.isDataInjected = isDataInjected;
	}

	/**
	 * Set a connection parameter
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value) {
		params.put(name, value);
	}

}
