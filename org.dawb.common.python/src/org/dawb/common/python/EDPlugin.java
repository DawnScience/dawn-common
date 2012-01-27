/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.python;

/**
 * Class encapsulating EDNA python plugins.
 * @author svensson
 *
 */
public class EDPlugin {

	/**
	 * EDNA Plugin name, required to be set for all EdnaPythonPlugin
	 */
	private PythonCommand pythonCommand = null;
	
	public EDPlugin(final String pluginName) {
		this.pythonCommand = new PythonCommand();
		this.pythonCommand.addCommand("import time");
		this.pythonCommand.addCommand("import os, sys");
		this.pythonCommand.addCommand("sys.path.append(os.path.join('"+System.getenv("EDNA_HOME")+"', 'kernel', 'src'))");
		this.pythonCommand.addCommand("from EDVerbose import EDVerbose");
		this.pythonCommand.addCommand("EDVerbose.setVerboseDebugOn()");
		this.pythonCommand.addCommand("from EDFactoryPlugin import EDFactoryPlugin");
		this.pythonCommand.addCommand("edFactoryPlugin = EDFactoryPlugin()");
		this.pythonCommand.addCommand("EDVerbose.setLogFileName('/tmp/edna_"+pluginName+".log')");
		this.pythonCommand.addCommand("edPlugin = edFactoryPlugin.loadPlugin('"+pluginName+"')");
	}
	
	public void setDataInput(String key, String xmlString) {
		this.pythonCommand.setInput(key, xmlString);
		this.pythonCommand.addCommand("print \"\"\"edPlugin.setDataInput("+key+", '"+key+"')\"\"\"");
		this.pythonCommand.addCommand("print value1");
		this.pythonCommand.addCommand("edPlugin.setDataInput("+key+", '"+key+"')");
	}
	
	public String getDataOutput(String key) {
		return this.pythonCommand.getOutputs().get(key).toString();
	}
	
	public void executePlugin() throws Exception {
		this.pythonCommand.addCommand("edPlugin.executeSynchronous()");
		Python.syncExec(this.pythonCommand);
	}

}
