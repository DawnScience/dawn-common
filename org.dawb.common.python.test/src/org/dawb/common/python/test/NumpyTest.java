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

import java.util.Arrays;

import jep.Jep;

import org.dawb.common.util.test.TestUtils;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class NumpyTest {

	
	@Test
	public void testJepDataset() throws Throwable {
  
		Jep jep=null;
		
		try {
			jep = new Jep();
			
			jep.set("p", new float[]{0,1,2,3,4});
			Object pBack = jep.getValue("p");
			
			jep.eval("from numpy import *");
			jep.eval("p = array([1,3])");
			pBack = jep.getValue("p");
			
			final FloatDataset p = (FloatDataset)AbstractDataset.arange(100, AbstractDataset.FLOAT32);
			jep.set("p",  p.getData());
			jep.eval("p = array(p)");
			jep.eval("print p.__class__");
			jep.eval("p = arccos(p)");
			jep.eval("p = p.tolist()");
			jep.eval("from jep import *");
			jep.eval("plen = len(p)");
			jep.eval("jp = jarray(plen, JDOUBLE_ID)");
			jep.eval("for index in range(plen):\n\tjp[index] = float(p[index])");
			jep.eval("p = jp");

			
			pBack = jep.getValue("p");
		    if (!(pBack instanceof double[])) throw new Exception("Should get float back and is "+pBack.getClass());
		    final double[] dataBack = (double[])pBack;
		    if (dataBack.length!=100) throw new Exception("Returned array should be size 100!");
		    if (dataBack[0]!=1.5707963267948966d) throw new Exception("Value should be 1.5707963267948966 not "+dataBack[0]);
		} finally {
			if (jep!=null) jep.close();
		}
		
	}
	
	@Test
	public void testNumpyUtils() throws Throwable {
		
		doSimpleNumpyTest(new ShortDataset(new short[]{0,1,2,3,4,5},   6));
		doSimpleNumpyTest(new IntegerDataset(new int[]{0,1,2,3,4,5},   6));
		doSimpleNumpyTest(new LongDataset(new long[]{0,1,2,3,4,5},     6));
		doSimpleNumpyTest(new FloatDataset(new float[]{0,1,2,3,4,5},   6));
		doSimpleNumpyTest(new DoubleDataset(new double[]{0,1,2,3,4,5}, 6));
		
		doSimpleNumpyTest(new ShortDataset(new short[]{0,1,2,3,4,5},   2,3));
		doSimpleNumpyTest(new IntegerDataset(new int[]{0,1,2,3,4,5},   2,3));
		doSimpleNumpyTest(new LongDataset(new long[]{0,1,2,3,4,5},     2,3));
		doSimpleNumpyTest(new FloatDataset(new float[]{0,1,2,3,4,5},   2,3));
		doSimpleNumpyTest(new DoubleDataset(new double[]{0,1,2,3,4,5}, 2,3));

		
		doExpressionNumpyTest(new ShortDataset(new short[]{0,1,2,3,4,5},   6));
		doExpressionNumpyTest(new IntegerDataset(new int[]{0,1,2,3,4,5},   6));
		doExpressionNumpyTest(new LongDataset(new long[]{0,1,2,3,4,5},     6));
		doExpressionNumpyTest(new FloatDataset(new float[]{0,1,2,3,4,5},   6));
		doExpressionNumpyTest(new DoubleDataset(new double[]{0,1,2,3,4,5}, 6));
		
		doExpressionNumpyTest(new ShortDataset(new short[]{0,1,2,3,4,5},   2,3));
		doExpressionNumpyTest(new IntegerDataset(new int[]{0,1,2,3,4,5},   2,3));
		doExpressionNumpyTest(new LongDataset(new long[]{0,1,2,3,4,5},     2,3));
		doExpressionNumpyTest(new FloatDataset(new float[]{0,1,2,3,4,5},   2,3));
		doExpressionNumpyTest(new DoubleDataset(new double[]{0,1,2,3,4,5}, 2,3));

	}

	private void doSimpleNumpyTest(AbstractDataset set) throws Throwable {
	
		Jep jep=null;
		
		try {
			set.setName("x");
			jep = new Jep();
			NumpyUtils.setNumpy(jep, set);
			
			AbstractDataset outNump = NumpyUtils.getNumpy(jep, "x");
			if (!outNump.equals(set)) throw new Exception("Jep and sda dataset utils do not agree!");
			
		} finally {
			if (jep!=null) jep.close();
		}
		
	}
	
	
	private void doExpressionNumpyTest(AbstractDataset set) throws Throwable {
		
		Jep jep=null;
		
		try {
			set.setName("x");
			jep = new Jep();
			NumpyUtils.setNumpy(jep, set);
			
			jep.eval("x = x*10");
			
			AbstractDataset outNump = NumpyUtils.getNumpy(jep, "x");
			final AbstractDataset mult    = Maths.multiply(set, (short)10);
			
			if (!outNump.equals(mult)) throw new Exception("Jep and sda dataset utils do not agree!");
			
		} finally {
			if (jep!=null) jep.close();
		}
		
	}
	
	@Test
	public void testLargerDataset() throws Throwable {
		
		String path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
                                                "test/org/dawb/common/python/test/billeA_4201_EF_XRD_5998.edf");				

		final IDataHolder dh = LoaderFactory.getData(path, null);
		final IDataset set = dh.getDataset(0);
		
		Jep jep=null;
		
		try {
			set.setName("x");
			jep = new Jep();
			NumpyUtils.setNumpy(jep, set);
			
			AbstractDataset outNump = NumpyUtils.getNumpy(jep, "x");
			if (!outNump.equals(set)) throw new Exception("Jep and sda dataset utils do not agree!");
			
		} finally {
			if (jep!=null) jep.close();
		}
	}


	@Test
	public void testImageManipulation() throws Throwable {
		
		
		final long memStart = Runtime.getRuntime().totalMemory();
		
		String path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
				                                "test/org/dawb/common/python/test/billeA_4201_EF_XRD_5998.edf");				
		final IDataset i = LoaderFactory.getData(path, null).getDataset(0);
		i.setName("i");
		
		System.gc();
		Thread.sleep(100);
		System.out.println("Read one image, memory increase = "+(Runtime.getRuntime().totalMemory()-memStart));
		
		path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
                                                "test/org/dawb/common/python/test/dark_0001.edf");				
		final IDataset d = LoaderFactory.getData(path, null).getDataset(0);
		d.setName("d");
	
		path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
                                                "test/org/dawb/common/python/test/flat_0001.edf");				
		IDataset f = LoaderFactory.getData(path, null).getDataset(0);
		f = DatasetUtils.cast(f, AbstractDataset.FLOAT32);
		f.setName("f");

		System.gc();
		Thread.sleep(100);
		System.out.println("Read two more images, memory increase = "+(Runtime.getRuntime().totalMemory()-memStart));

		Jep jep=null;
		
		try {
			jep = new Jep();
			
			System.gc();
			Thread.sleep(100);
			long mem = Runtime.getRuntime().totalMemory();
			NumpyUtils.setNumpy(jep, i);
			System.out.println("Send image to numpy = "+(Runtime.getRuntime().totalMemory()-mem));

			System.gc();
			Thread.sleep(100);
			mem = Runtime.getRuntime().totalMemory();
			NumpyUtils.setNumpy(jep, d);
			System.out.println("Send dark to numpy = "+(Runtime.getRuntime().totalMemory()-mem));
			
			System.gc();
			Thread.sleep(100);
			mem = Runtime.getRuntime().totalMemory();
			NumpyUtils.setNumpy(jep, f);
			System.out.println("Send flat to numpy = "+(Runtime.getRuntime().totalMemory()-mem));
			
			System.gc();
			Thread.sleep(100);
			mem = Runtime.getRuntime().totalMemory();
			jep.eval("i_dash = (i-d)/f");
			System.out.println("Evaluated expression in numpy = "+(Runtime.getRuntime().totalMemory()-mem));

			System.gc();
			Thread.sleep(100);
			mem = Runtime.getRuntime().totalMemory();
			AbstractDataset i_dash = NumpyUtils.getNumpy(jep, "i_dash");
			System.out.println("Read data set back from numpy = "+(Runtime.getRuntime().totalMemory()-mem));

			if (!Arrays.equals(i.getShape(), i_dash.getShape())) throw new Exception("The image does not have the right shape!");
		    
		} finally {
			if (jep!=null) jep.close();
		}
		
		
		System.gc();
		Thread.sleep(100);
		System.out.println("Total data in memory after closing numpy = "+(Runtime.getRuntime().totalMemory()-memStart));
      
	}


	@Test
	public void testNumpyOperator() throws Throwable {
		
		DoubleDataset x = new DoubleDataset(new double[]{0,1,2,3,4,5}, 2,3);
		x.setName("x");
		DoubleDataset y = new DoubleDataset(new double[]{0,1,2,3,4,5}, 2,3);
		y.setName("y");

		Jep jep=null;
		
		try {
			jep = new Jep();

			NumpyUtils.setNumpy(jep, x);
			jep.eval("print(x)");
			NumpyUtils.setNumpy(jep, y);
			jep.eval("print(y)");
			
			final boolean b = jep.eval("z = x+y");
			if (!b) throw new Exception("z = x+y  - did not work!");
			
			final AbstractDataset z = NumpyUtils.getNumpy(jep, "z");
			if (z.getShape()[1]!=3) throw new Exception("z is not expected size!");
			
		} finally {
			if (jep!=null) jep.close();
		}
		
		
		System.gc();
		Thread.sleep(100);
	}

	// TODO Causes memory leak to show up - Memory leak means this will not run
	// Fix this and put the test back in!! 
	/**@Test**/
	public void testNumpyMemorySingleJep() throws Throwable {
		
		
		final long memStart = Runtime.getRuntime().totalMemory();
		
		
		Jep jep = new Jep(true);
		try {

			for (int i = 0; i < 100; i++) {
				System.out.println("Processing loop of medium sized files into numpy, count "+i);
				
				final String path = TestUtils.getAbsolutePath(org.dawb.common.python.Activator.getDefault().getBundle(), 
						"test/org/dawb/common/python/test/billeA_4201_EF_XRD_5998.edf");				
				
				IDataset set = LoaderFactory.getData(path, null).getDataset(0);
				set.setName("i");
				NumpyUtils.setNumpy(jep, set);
				NumpyUtils.getNumpy(jep, set.getName());
				
				Thread.sleep(100);
				set = null;
				System.gc();

			}

		} finally {
			if (jep!=null) {
				jep.close();
			}
			jep = null;
			System.gc();
		}

		
		System.gc();
		Thread.sleep(100);
		System.out.println("Total data in memory after closing numpy = "+(Runtime.getRuntime().totalMemory()-memStart));
	}
	
}
