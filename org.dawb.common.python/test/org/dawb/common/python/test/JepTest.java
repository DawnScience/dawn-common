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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import jep.Jep;

import org.junit.Test;

public class JepTest {

	/**
	 * At least this one works...
	 * @throws Throwable
	 */
	@Test
	public void testJepBasic() throws Throwable {
		
		Jep jep1=null,jep2=null;
		
		try {
			jep1 = new Jep();
			jep1.eval("x = 10");
			final Object o = jep1.getValue("x");
			jep1.set("m", new float[]{0,1,2,3,4});
			final Object m = jep1.getValue("m");
			
			jep2 = new Jep();
			jep2.eval("y = 10");
			jep2.eval("z = 10*y");
			
			System.out.println("Jepped!");
		} finally {
			if (jep1!=null) jep1.close();
			if (jep2!=null) jep2.close();
		}
	}


	private static int[] staticData;
	public static int[] getData() {
		return staticData;
	}
	
	/**
	 * We get python to call back on Java, if Java calls python
	 * directly, the variable sent to python is never deallocated
	 * causing a memory leak.
	 * 
	 * @throws Throwable
	 */
	@Test 
	public void testSendingMemoryWithPythonCallingData() throws Throwable {
		
		final long memStart = Runtime.getRuntime().totalMemory();

		for (int i = 0; i < 100; i++) {
			Jep jep = new Jep(true, null, String.class.getClassLoader());
			try {
				System.out.println("Processing loop of medium sized array into jep, count "+i);
				
				int[] data = new int[2048*2048];
				for (int j = 0; j < data.length; j++) data[j] = -1*j+i;
			
				staticData = data;
				jep.eval("import jep");
				jep.eval("from jep import *");
				jep.eval("ntest = jep.forName(\""+getClass().getName()+"\")");
				jep.eval("fred  = ntest.getData()");
				jep.eval("len(fred)");
				jep.eval("del fred");
				jep.eval("import gc");
				jep.eval("gc.collect()");
				
                data       = null;
                staticData = null;
                jep.close();
                				
				
			} finally {
				if (jep!=null) {
					jep.close();
				}
				jep = null;
				System.gc();
				
			}

		}

		System.gc();
		Thread.sleep(100);
		System.out.println("Total data in memory after closing = "+(Runtime.getRuntime().totalMemory()-memStart));
	}

	/**
	 * Memory leak occurs here - THIS DIES FAIRLY HORRIBLY
	 * @throws Throwable
	 */
	@Test
	public void testMemoryLeakJep() throws Throwable {
		
		
		final long memStart = Runtime.getRuntime().totalMemory();

		for (int i = 0; i < 100; i++) {
			Jep jep = new Jep();
			try {
				System.out.println("Processing loop of medium sized array into jep, count "+i);
				
				int[] data = new int[2048*2048];
				for (int j = 0; j < data.length; j++) {
					data[j] = -1*j;
				}
			
				jep.set("fred", data);

				data = null;
                				
				
			} finally {
				jep.close();
				jep = null;
				System.gc();
			}

		}

		System.gc();
		Thread.sleep(100);
		System.out.println("Total data in memory after closing = "+(Runtime.getRuntime().totalMemory()-memStart));
	}
	
	/**
	 * THIS DIES HORRIBLY
	 * @throws Throwable
	 */
	@Test
	public void testMemoryLeakUsingScriptFile() throws Throwable {
		
		
		final long memStart = Runtime.getRuntime().totalMemory();

		for (int i = 0; i < 100; i++) {
			Jep jep = new Jep();
			try {
				System.out.println("Processing loop of medium sized array into jep, count "+i);
				
				int[] data = new int[2048*2048];
				for (int j = 0; j < data.length; j++) {
					data[j] = -1*j;
				}
			
				final File tmp = File.createTempFile("jep"+System.currentTimeMillis(), "py");
				final BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
				try {
					out.write("fred = ");
					out.write("[");
					for (int j = 0; j < data.length; j++) {
						out.write(String.valueOf(data[i]));
						if (i < data.length-1) out.write(", ");
						if (j%2048 == 0) out.flush(); 
					}
					out.write("]");
					out.newLine();
				} finally {
					out.close();
				}
				tmp.deleteOnExit();
				
				jep.runScript(tmp.getAbsolutePath());

				jep.eval("len(fred)");
				
                data = null;
                jep.close();
                				
				
			} finally {
				if (jep!=null) {
					jep.close();
				}
				jep = null;
				System.gc();
			}

		}

		System.gc();
		Thread.sleep(100);
		System.out.println("Total data in memory after closing = "+(Runtime.getRuntime().totalMemory()-memStart));
	}

	
	private String getBuffer(int[] data, int start, int chunk) {
		final StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i = start; i < start+chunk; i++) {
			buf.append(data[i]);
			if (i < start+chunk-1) buf.append(", ");
		}
		buf.append("]");
		return buf.toString();
	}
}
