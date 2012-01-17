/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.python.test;

import org.dawb.common.python.Python;
import org.dawb.common.python.PythonCommand;
import org.junit.Test;


/**
 * Class to test Python class, requires further tests to be written.
 * 
 * The current tests here could also be done smarter and improve coverage.
 * 
 * Please add more tests...
 * 
 * @author gerring
 *
 */
public class PythonCommandTest {

	
	@Test
	public void testSyncPrint() throws Exception {
		
		final PythonCommand cmd = new PythonCommand("print 'Hello World'");
		Python.syncExec(cmd);
		
		if (!cmd.isComplete()) throw new Exception("The command did not complete!");
	}
	
	
	@Test
	public void testInputOutput() throws Exception {
		
		final PythonCommand cmd = new PythonCommand("print 'Input variable = ', inputVariable");
		cmd.addCommand("outputVariable = 'A test output variable'");
		cmd.setInput("inputVariable", "A test value");
		cmd.addOutput("outputVariable");
		Python.syncExec(cmd);
		if (!cmd.getOutputs().get("outputVariable").equals("A test output variable")) throw new Exception("Call to getOutputs failed!");
		
		if (!cmd.isComplete()) throw new Exception("The command did not complete!");
	}

	
	@Test
	public void testTimeout() throws Exception {
		
		final PythonCommand cmd = new PythonCommand();
		cmd.addCommand("import time");
		cmd.addCommand("time.sleep(5)"); // 2s
		cmd.setTimeout(1000);
		
		Python.syncExec(cmd);
		
		if (!cmd.isTimedOut()) throw new Exception("The command should have timed out!");
		
		Thread.sleep(5000); // We wait for the python command to return before doing more tests.
	}

	
	@Test
	public void testAsyncSleep() throws Exception {
		
		final PythonCommand cmd = new PythonCommand();
		cmd.addCommand("import time");
		cmd.addCommand("time.sleep(2)"); // 2s
		
		Python.asyncExec(cmd);
		
		if (cmd.isComplete()) throw new Exception("The command did not work because it says it is complete too soon!");
		
		Thread.sleep(2500);
		
		if (!cmd.isComplete()) throw new Exception("The command did not complete!");
		
	}
	
	
	@Test
	public void test25Threads() throws Exception {

		final Thread[] threads = new Thread[25];
		for (int i = 0; i < threads.length; i++) {
			final int index = i;
			threads[i] =  new Thread(new Runnable() {
				@Override
				public void run() {
					final PythonCommand cmd = new PythonCommand("print 'Hello World "+index+"'");
					try {
						Python.syncExec(cmd);
					} catch (Exception e) {
						e.printStackTrace(); // TODO Better logging of this.
					}
					System.out.println(cmd);
				}
			}, "Test thread "+index);// Always name them!
			
			threads[i].start();
			Thread.sleep(100);
		}
		
		
		while(Python.isActive()) {
		    Thread.sleep(400);
		}
 	}
		
		

}
