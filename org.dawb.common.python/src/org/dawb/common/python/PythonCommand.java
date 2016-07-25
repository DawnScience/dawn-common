/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class encapsulating python commands, including edna specific options.
 * @author gerring
 *
 */
public class PythonCommand {
	private static Logger logger = LoggerFactory.getLogger(PythonCommand.class);

	protected boolean      complete   = false;
	protected Throwable    exception;
	protected List<String> commands;
	protected List<String> status;
	protected Map<String,Object> inputs;
	protected Map<String,Object> outputs;
	protected long         timeout=5000;
	protected boolean      timedOut=false;
	protected String       commandName;


	public PythonCommand() {
		
	}
	
	public PythonCommand(final String command) {
		addCommand(command);
	}
	
	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((commands == null) ? 0 : commands.hashCode());
		result = prime * result + (complete ? 1231 : 1237);
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + (timedOut ? 1231 : 1237);
		result = prime * result + (int) (timeout ^ (timeout >>> 32));
		return result;
	}

	/**
	 * Each command only equal if same object
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	public List<String> getStatus() {
		return status;
	}

	public void setStatus(List<String> status) {
		this.status = status;
	}
	
	public void addCommand(final String command) {
		if (logger.isTraceEnabled()) {
			String debugText = "Python command: "+ command;
			if (debugText.length() > 160) {
				debugText = debugText.substring(0, 150)+".....";
			}
			logger.trace(debugText);
		}
		if (commands==null) commands = new ArrayList<String>(7);
		commands.add(command);
	}
	public void addStatus(final String stat) {
		if (status==null) status = new ArrayList<String>(7);
		status.add(stat);
	}
	
	public void clear() {
		if (commands!=null) commands.clear();
		if (status!=null)   status.clear();
	}

	@Override
	public String toString() {
		return "PythonCommand [commands=" + commands + ", complete=" + complete + "]";
	}

	public long getTimeout() {
		return timeout;
	}

	/**
	 * Time in ms, do not set to less than 1000 usually.
	 * @param timeout
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}

	public Map<String, Object> getInputs() {
		return inputs;
	}
	
	public void setInput(final String key, final Object value) {
		if (inputs==null) inputs = new HashMap<String,Object>(7);
		inputs.put(key, value);
	}

	public void setInputs(Map<String, Object> input) {
		this.inputs = input;
	}

	public Map<String, Object> getOutputs() {
		return outputs;
	}

	public void setOutputs(Map<String, Object> output) {
		this.outputs = output;
	}
	
	/**
	 * Set the name of a required output to read from the python interpreter
	 * @param outputRequired
	 */
	public void addOutput(final String outputRequired) {
		if (outputs==null) outputs = new HashMap<String,Object>(7);
		outputs.put(outputRequired, null);
	}
	
	void setOutput(final String key, final Object value) {
		if (outputs==null) outputs = new HashMap<String,Object>(7);
		outputs.put(key, value);
	}
	
	public String getCommandName() {
		if (commandName==null) commandName = toString();
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

}
