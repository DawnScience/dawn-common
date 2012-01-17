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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jep.Jep;
import jep.JepException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class deals with calling python jobs on any thread to the same interpreter.
 * 
 * This class contains a single thread which is running and the class can be used
 * inside or outside the eclipse framework.
 * 
 * @author gerring
 *
 */
public class Python {

	
	/**
	 * The logger we use to report errors which should not kill the python thread.
	 */
	private static final Logger logger = LoggerFactory.getLogger(Python.class);
	
	/**
	 * Process queue
	 */
	private static BlockingQueue<PythonCommand> QUEUE;
	
	/**
	 * The engine for jep
	 */
	private static Jep pythonEngine;
	
	/**
	 * This is set by the processing thread to non-null only while the command
	 * is processed.
	 */
	private static PythonCommand currentCommand;
	
	/**
	 * Run a python command in the current interpreter.
	 * 
	 * This method is synchronized to ensure that more than one Jep cannot be created.
	 * 
	 * NOTE: It returns after the command returns but if the command starts a new
	 * thread in Python, the method will obviously return. If you are starting a 
	 * new thread from your python script see the class EDJob which manages doing this.
	 * 
	 * @param command
	 * @throws IllegalStateException
	 */
	public static void syncExec(final PythonCommand command) throws IllegalStateException, InterruptedException  {
		Python.exec(command, true);
	}
	
	/**
	 * Run a python command in the current interpreter.
	 * 
	 * This method is synchronized to ensure that more than one Jep cannot be created.
	 * 
	 * @param command
	 * @throws IllegalStateException
	 */
	public static void asyncExec(final PythonCommand command) throws IllegalStateException, InterruptedException  {
		Python.exec(command, false);
	}

	private static synchronized void exec(final PythonCommand command, final boolean isSync) throws IllegalStateException, InterruptedException {

		/**
		 * NOTE This means the queue cannot be larger than 89 
		 */
		if (QUEUE==null) QUEUE = new LinkedBlockingQueue<PythonCommand>(89);		
		if (pythonEngine==null) { // Create the engine and the thread.
			/**
			 * Do not normally deal with Thread class as too low level.
			 * We make an exception here as we want this utility to run in 
			 * any framework.
			 */
			Thread pythonThread = new Thread(new Runnable() {
				public void run() {

					try {
						createInterpreter(command);
					} catch (Throwable ne) {
						logger.error("Cannot create python interpreter, last command was: "+command, ne);
						return;
					}

					try {
						interpreterLoop();
					} catch (Exception ne) {
						logger.error("Stopping python interpreter, last command was: "+command, ne);
					}
					stop();
				}
			}, "Python Interpreter Connection");
			pythonThread.setDaemon(true); // Means it dies if we exit.
			pythonThread.setPriority(Thread.NORM_PRIORITY);
			pythonThread.start();
		}

		QUEUE.put(command); // Put blocks if queue larger than 89

		// There is a better way of doing this using join etc. 
		// This is a stinky part of the design - needs improving.
		if (isSync) {
			long timeWaited = 0;
			while((currentCommand!=null && currentCommand.equals(command)) ||
					QUEUE.contains(command)) {

				try {
					Thread.sleep(100);
					timeWaited+=100;
					if (timeWaited>command.getTimeout()) {
						command.setTimedOut(true);
						return;
					}
				} catch (Exception ne) {
					logger.error("Thread running Python.synExec(...) interupted, last command was: "+command, ne);
				}
			}
		}
	}


	/**
	 * Does not currently use the command argument, extend later if more work required for 
	 * running EDNA plugin.
	 * 
	 * @param command
	 * @throws JepException
	 */
	protected static void createInterpreter(final PythonCommand command) throws JepException {
		
		if (pythonEngine!=null) return;
		// TODO - Why using FableJep at all then? It has some useful methods, maybe
		// pythonEngine should be a FableJep?
		//pythonEngine = (new FableJep()).getJep();
		pythonEngine = new Jep(true, null, String.class.getClassLoader());
	}

	protected static void stop() {
		if (pythonEngine!=null) pythonEngine.close();
		pythonEngine = null;	
	}

	protected static void interpreterLoop() throws InterruptedException {
		
		while(pythonEngine!=null) { 
			currentCommand = QUEUE.take(); // NOTE This blocks until something comes in the queue.
			try {
	            Python.runCommand(currentCommand, pythonEngine);
		        
	        } catch (Throwable ne) {
	        	currentCommand.addStatus("Major failior with running command "+currentCommand);
	        	currentCommand.setException(ne);
	        	
	        } finally {
	        	if (currentCommand!=null) currentCommand.setComplete(true);
		        currentCommand = null;
	        }
		}
	}
	
	/**
	 * Run using a new Jepp in the current thead.
	 * @param cmd
	 * @throws JepException
	 */
	public static void runCommand(final PythonCommand cmd) throws JepException {
		Python.runCommand(cmd, new Jep(true, null, String.class.getClassLoader()));
	}
		 
	/**
	 * Can be used to run a command with a specific jep
	 * 
	 * 
	 * 
	 * @param cmd
	 * @param jep
	 * @throws JepException
	 */
	private static void runCommand(final PythonCommand cmd, final Jep jep) throws JepException {
    	
		// Set any variables
    	if (cmd.getInputs()!=null) for (String key : cmd.getInputs().keySet()) {
    		jep.set(key, cmd.getInputs().get(key));
		}
    	
    	// Process commands
    	for (String com : cmd.getCommands()) {
			final boolean ok = jep.eval(com);
			if (!ok) cmd.addStatus("Command '"+com+"' failed.");
		}
        
    	// Set any variables
    	if (cmd.getOutputs()!=null) for (String key : cmd.getOutputs().keySet()) {
    		cmd.setOutput(key, jep.getValue(key));
		}
		
	}

	public static synchronized boolean isActive() {
		if (QUEUE==null)          return false;
		if (!QUEUE.isEmpty())     return false;
		if (currentCommand!=null) return true;
		return false;
	}

	/**
	 * Starts a specific thread for the jepp instance to use
	 * @param pythonCommand
	 * @throws InterruptedException 
	 */
	public static void execInThread(final PythonCommand cmd) throws InterruptedException {
		
		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				
				Jep jep = null;
		        try {
		        	jep = new Jep(true, null, String.class.getClassLoader());
		            Python.runCommand(cmd, jep);
			        
		        } catch (JepException jepError) {
		        	cmd.addStatus("Major failior with running command "+currentCommand);
		        	cmd.setException(jepError);
		        	
		        } finally {
		        	if (jep!=null) jep.close();
		        	cmd.setComplete(true);
		        }
			}
			
		}, cmd.getCommandName());
		
		thread.start();
		
		// TODO Add timeout here? EDNA already has one.
		thread.join();
		
	}

}
