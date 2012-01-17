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

import jep.Jep;

import org.dawb.common.python.NumpyUtils;
import org.dawb.common.util.test.TestUtils;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class NumpyThreadTest extends AbstractThreadTest {

	@Test
	public void test2() throws Throwable {
		super.testWithNThreads(2);
	}
	// @Test - Memory leak means this will not run
	public void test10() throws Throwable {
		super.testWithNThreads(10);
	}
	// @Test - Memory leak means this will not run
	public void test100() throws Throwable {
		super.testWithNThreads(100);
	}
	
	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		Jep jep =null;
		try {
		    jep = new Jep();
			System.out.println("Processing loop of medium sized files into numpy, count "+index);
			String path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
                                                    "test/org/dawb/common/python/test/billeA_4201_EF_XRD_5998.edf");				
			final AbstractDataset set = LoaderFactory.getData(path, null).getDataset(0);
			set.setName("i");
			NumpyUtils.setNumpy(jep, set);
			NumpyUtils.getNumpy(jep, set.getName());
        } finally { 
			if (jep!=null) {
				jep.close();
			}
			jep = null;
			System.gc();
		}
		
	}

}
