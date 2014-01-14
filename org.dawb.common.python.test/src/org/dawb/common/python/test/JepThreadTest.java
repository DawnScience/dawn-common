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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jep.Jep;

public class JepThreadTest extends AbstractThreadTest {
	
	@Test
	public void test8() throws Throwable {
		super.testWithNThreads(8);
	}

	@Before
	public void create() {
		staticData = new int[8][];
	}
	
	@After
	public void clean() {
		staticData = null;
	}
	
	private static int[][] staticData;
	
	public static int[] getData(int index) {
		final int[] data  = staticData[index];
		staticData[index] = null;
		return data;
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		
		
		Jep jep = new Jep(true, null, JepThreadTest.class.getClassLoader());
		try {
			System.out.println("Processing loop of medium sized array into jep, count "+index);
			
			int[] data = new int[2048*2048];
			for (int j = 0; j < data.length; j++) data[j] = -1*j+index;
		
			staticData[index] = data;
			jep.eval("import jep");
			jep.eval("from jep import *");
			jep.eval("ntest = jep.forName(\""+JepThreadTest.class.getName()+"\")");
			jep.eval("fred  = ntest.getData("+index+")");
			jep.eval("print \"\\nLength of fred in python, thread "+index+"  = \"+str(len(fred))");
			jep.eval("del fred");
			jep.eval("import gc");
			jep.eval("print \"\\nObjects in garbage, thread "+index+" = \"+str(gc.collect())");
			
            data       = null;
            jep.close();			
			
		} finally {
			if (jep!=null) {
				jep.close();
			}
			jep = null;
			System.gc();
			
		}
		
	}

}
