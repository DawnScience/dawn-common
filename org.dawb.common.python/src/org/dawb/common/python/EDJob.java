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

import java.io.Serializable;

import org.dawnsci.python.rpc.PythonService;

/**
 * Class encapsulating EDNA python plugins.
 * 
 * Uses EDJob to run jobs asynchronously and avoid the blocking nature
 * of the jepp system.
 * 
 * Usage:
           final EDJob job = new EDJob(pluginName);
           job.setDataInput(... some xml);
           job.execute() // Blocks
           
           final String output = job.getDataOutput();
           
 * NOTE: It works like this:
 * 
 * 1. Call a python command to call the plugin
 * 
 * 2. Status is checked until either:
 *    A) success
 *    B) failure
 *    C) A python exception is detected.
 * Otherwise it loops FOR EVER because one cannot say how long the underlying software will take - 
 * the timeout is defined in the EDNA 
 * 
 * 3. The output XML is read which also causes the plugin to synchronize. If an error happens on
 *    synchronize then the plugin fails. If null is returned, the plugin fails.
 * 
 * @author gerring
 *
 */
public class EDJob implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2109724544355850369L;
	
	private final PythonService ednaService;
	private final String  pluginName;
	private final String  execDir;

	/**
	 * EDNA Plugin name, required to be set for all EdnaPythonPlugin
	 * 
	 */
	// These vars are only needed for Jep
	private String        edJobVarName  = null;
	private String        outputVarName = null;
	private String        statusVarName = null;
	private String        dataOutput    = null;
	private String        commandName   = null;
	private String        xmlInputString= null;
	// END
		
	private boolean       ednaDebugMode = false;


	/**
	 * Just uses static int incremented as unlikely 
	 * that many jobs will be run in one vm.
	 */
	private static int JOB_COUNT_ID=0;
	
	private static final Object SYNC_LOCK = new Object();

	/**
	 * NOTE Assumes that PYTHONPATH and EDNA_HOME are already set properly.
	 * In the workbench session we do this automatically for the user.

	 * @param ednaService - set to null if you require jep to be used
	 * @param pluginName
	 * @param execDir
	 */
	public EDJob(final PythonService ednaService,
			     final String        pluginName, 
			     final String        execDir) {
		
		this.ednaService= ednaService;
		this.pluginName = pluginName;
		this.execDir    = execDir;
		
		if (ednaService==null) synchronized(SYNC_LOCK) {
			// We need some unique variables to make Jep work.
			JOB_COUNT_ID++;
			this.edJobVarName  = "edjob"+JOB_COUNT_ID;
			this.outputVarName = "output"+JOB_COUNT_ID;
			this.statusVarName = "status"+JOB_COUNT_ID;
			this.commandName   = pluginName+"_run_"+JOB_COUNT_ID;
		}
				
	}

	public void setDataInput(String xmlString) {
		this.xmlInputString = xmlString;
	}
	
	public String getDataOutput() {
		return dataOutput;
	}
	
	/** 
	 * Called by python not Java
	 * @param key
	 */
	public void setDataOutput(String out) {
		dataOutput = out;
	}
	
	/**
	 * Runs plugin and blocks until complete.
	 * @throws Exception
	 */
	public void execute() throws Exception {
		if (ednaService!=null) {
			executeRpc();
		} else {
			executeJep();
		}
	}
	
	/**
	 * Run edna in rpc mode.
	 * @throws Exception 
	 */
	private void executeRpc() throws Exception {
		
		// We want all the edna plugins to run in the same python
		// This means that the first actor actor to use EDJob 
		// sets the properties of the PythonService, for instance if
		// it is debug. This potentially confuses the user because
		// downstream settings from the first edna node might not work.

		this.dataOutput = ednaService.runEdnaPlugin(execDir, pluginName, ednaDebugMode, xmlInputString);
		if (dataOutput==null || "".equals(dataOutput)) {
			throw new Exception(pluginName+" does not have an XML output value!");
		}
	}

	/**
	 * Runs the command in this thread. 
	 * 
	 * It blocks until there is some dataOutput
	 * 
	 * @throws Exception
	 */
	private void executeJep() throws Exception {
		
		// Imports
		final PythonCommand exec = new PythonCommand();
		exec.setCommandName(commandName);
		exec.addCommand("import time");
		exec.addCommand("import os, sys");
		exec.addCommand("import jep");
		exec.addCommand("from jep import *");
		exec.addCommand("if os.environ.has_key('EDNA_HOME'): \n\tsys.path.append(os.path.join(os.environ['EDNA_HOME'],'kernel','src'))");

		// Important! Remove LD_PRELOAD in order to avoid problems in Python programs started by EDNA
		exec.addCommand("if os.environ.has_key('LD_PRELOAD'): \n\tdel os.environ['LD_PRELOAD']");

		// Edna stuff
		exec.addCommand("os.chdir('"+execDir+"')");
		exec.addCommand("from EDVerbose import EDVerbose");
		if (ednaDebugMode) {
			exec.addCommand("EDVerbose.setVerboseDebugOn()");
		} else {
			exec.addCommand("EDVerbose.setVerboseOn()");			
			exec.addCommand("EDVerbose.setVerboseDebugOff()");			
		}
		exec.addCommand("from EDJob import EDJob");
		exec.addCommand("EDVerbose.setLogFileName('"+execDir+"/"+pluginName+".log')");
		
		exec.addCommand(edJobVarName+" = EDJob('"+pluginName+"')");
		// We use the three quotes multi-line definition for the python command
		// this allows the xml string to be multiple line.
		
		// We parse out \\n and \\r and then replace any
		xmlInputString = xmlInputString.replace("\\\\", "\\");
		xmlInputString = xmlInputString.replace("\\", "\\\\");
		
		exec.addCommand(edJobVarName+".setDataInput(\"\"\""+xmlInputString+"\"\"\")");
		exec.addCommand(edJobVarName+".execute()");		
		exec.addCommand(edJobVarName+".synchronize()");		
		
		Python.syncExec(exec); // Waits to queue has run command not until command done
		if (exec.getException()!=null) throw new Exception(exec.getException());
		
		final PythonCommand status = new PythonCommand();
		status.addCommand(statusVarName+" = "+edJobVarName+".getStatus()");
		status.addOutput(statusVarName);
		
		// We run the status to get the status until we get a final status
		while(true) {
			
			Python.syncExec(status); // Waits to queue has run command not until command done
			if (status.getException()!=null) throw new Exception(status.getException());
			
			Object sat = (String)status.getOutputs().get(statusVarName);
			if (sat!=null && "failure".equalsIgnoreCase(String.valueOf(sat))) {
				throw new Exception(pluginName+" failed to run! Its status value is "+String.valueOf(sat));
			}
			if (sat!=null && "success".equalsIgnoreCase(String.valueOf(sat))) {
				break;
			}
		
			final String pollTime = System.getProperty("org.dawb.edna.edjob.poll.freq");
			if (pollTime!=null) {
				Thread.sleep(Integer.parseInt(pollTime));
			} else {
				Thread.sleep(200);
			}
			
		}
		
		final PythonCommand output = new PythonCommand();
		output.addCommand(outputVarName+" = "+edJobVarName+".getDataOutput()");
		output.addCommand(outputVarName+" = "+outputVarName);
		output.addOutput(outputVarName);
        if (System.getProperty("org.dawb.common.python.edjob.printXML")!=null) {
        	output.addCommand("print "+outputVarName);
        }

		Python.syncExec(output); // Waits to queue has run command not until command done
		if (output.getException()!=null) throw new Exception(output.getException());
		
		this.dataOutput = (String)output.getOutputs().get(outputVarName);
		if (dataOutput==null || "".equals(dataOutput)) {
			throw new Exception(pluginName+" does not have an XML output value!");
		}
		
		// Run a command to clear the variables in use
		final PythonCommand clear = new PythonCommand();
		clear.addCommand(edJobVarName+" = None");
		clear.addCommand(outputVarName+" = None");
		clear.addCommand(statusVarName+" = None");
		Python.syncExec(clear);
		if (clear.getException()!=null) throw new Exception(clear.getException());
		
	}
	
	/**
	 * returns true when EDJob has finished.
	 * @return
	 */
	public boolean isFinished() {
		return dataOutput!=null;
	}

	public boolean isEdnaDebugMode() {
		return ednaDebugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.ednaDebugMode = debugMode;
	}

}
