package org.dawb.common.python.test;
/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 


import java.util.LinkedHashMap;

import jep.Jep;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;

public class NumpyUtils {

		
	/**
	 * Currently only works for 1D arrays.
	 * 
	 * The name of the data set is used as the python variable.
	 * @param set
	 * @param jep
	 * @throws Throwable
	 */
	public static void setNumpy(final Jep jep, final AbstractDataset set) throws Throwable {
		
		// numpy!
		jep.eval("from numpy import *");
		
		// Send array, this has huge memory leak at the moment!
		switch(set.getDtype()) {
		case AbstractDataset.INT16:
			jep.set(set.getName(), (short[])set.getBuffer());
			break;
		case AbstractDataset.INT32:
			jep.set(set.getName(), (int[])set.getBuffer());
			break;
		case AbstractDataset.INT64:
			jep.set(set.getName(), (long[])set.getBuffer());
			break;
		case AbstractDataset.FLOAT32:
			jep.set(set.getName(), (float[])set.getBuffer());
			break;
		case AbstractDataset.FLOAT64:
			jep.set(set.getName(), (double[])set.getBuffer());
			break;
		default:
			throw new Exception("Cannot deal with dtype "+set.getDtype());
		}

		// Make numpy array, this is 1D
		final String cmd = set.getName()+" = array("+set.getName()+", dtype="+getDType(set.getDtype())+")";
		jep.eval(cmd);
		
		// Set the shape
		jep.eval(set.getName()+".shape = "+getShape(set));
		//jep.eval(set.getName()+" = "+set.getName()+".transpose()");
	}
	
	private static String getShape(AbstractDataset set) {
		final int[] shape = set.getShape();
		final StringBuilder buf = new StringBuilder();
		buf.append("(");
		for (int i = 0; i < shape.length; i++) {
			buf.append(shape[i]);
			if (i<(shape.length-1)) buf.append(", ");
		}
		buf.append(")");
		return buf.toString();
	}

	/**
	 * Currently only works for 1D arrays.
	 * After the get the name is set to None in python.
	 * @param name
	 * @param dtype
	 * @param jep
	 * @return
	 * @throws Exception
	 */
	public static AbstractDataset getNumpy(final Jep     jep,
			                               final String  name) throws Throwable {
		
		// Get the dtype
		final int   dtype = getDType(jep.getValue(name+".dtype").toString());
		
		// Get the shape
		final int[] shape = getShape(jep.getValue(name+".shape").toString());

		// The variable 'name' must be a numpy dataset
		jep.eval(name+" = "+name+".ravel().tolist()");
		jep.eval("from jep import *");
		
		
		AbstractDataset ret = null;
		switch(dtype) {
		case AbstractDataset.INT16:
			jep.eval("jepArray"+name+" = jarray(len("+name+"), JSHORT_ID)");
			jep.eval("for index in range(len("+name+")):\n\tjepArray"+name+"[index] = int("+name+"[index])");
			ret = new ShortDataset((short[])jep.getValue("jepArray"+name), shape);
			break;
		case AbstractDataset.INT32:
			jep.eval("jepArray"+name+" = jarray(len("+name+"), JINT_ID)");
			jep.eval("for index in range(len("+name+")):\n\tjepArray"+name+"[index] = int("+name+"[index])");
			ret = new IntegerDataset((int[])jep.getValue("jepArray"+name), shape);
			break;
		case AbstractDataset.INT64:
			jep.eval("jepArray"+name+" = jarray(len("+name+"), JLONG_ID)");
			jep.eval("for index in range(len("+name+")):\n\tjepArray"+name+"[index] = long("+name+"[index])");
			ret = new LongDataset((long[])jep.getValue("jepArray"+name), shape);
			break;
		case AbstractDataset.FLOAT32:
			jep.eval("jepArray"+name+" = jarray(len("+name+"), JFLOAT_ID)");
			jep.eval("for index in range(len("+name+")):\n\tjepArray"+name+"[index] = float("+name+"[index])");
			ret = new FloatDataset((float[])jep.getValue("jepArray"+name), shape);
			break;
		case AbstractDataset.FLOAT64:
			jep.eval("jepArray"+name+" = jarray(len("+name+"), JDOUBLE_ID)");
			jep.eval("for index in range(len("+name+")):\n\tjepArray"+name+"[index] = float("+name+"[index])");
			ret = new DoubleDataset((double[])jep.getValue("jepArray"+name), shape);
			break;
		default:
			throw new Exception("Cannot deal with dtype "+dtype);
		}

		jep.eval(name+" = None");
		jep.eval("jepArray"+name+" = None");
		ret.setName(name);
		return ret;
	}

	private static int[] getShape(String value) {
		
		value = value.substring(1,value.length()-1);
		if (value.endsWith(",")) value = value.substring(0,value.length()-1);
		
		final String[] tokens = value.split(",");
		final int[]    shape  = new int[tokens.length];
		for (int i = 0; i < shape.length; i++) {
			shape[i]  = Integer.parseInt(tokens[i].trim());
		}
		return shape;
	}

	private static LinkedHashMap<Object,Object> DTYPES;
	private static int getDType(String value) {
		createMap();
		return (Integer)DTYPES.get(value);
	}
	private static String getDType(int value) {
		createMap();
		return (String)DTYPES.get(value);
	}

	private static void createMap() {
		if (DTYPES==null) {
			DTYPES = new LinkedHashMap<Object, Object>(7);
			
			DTYPES.put("int16",   AbstractDataset.INT16);
			DTYPES.put("int32",   AbstractDataset.INT32);
			DTYPES.put("int64",   AbstractDataset.INT64);
			DTYPES.put("float32", AbstractDataset.FLOAT32);
			DTYPES.put("float64", AbstractDataset.FLOAT64);
			DTYPES.put(AbstractDataset.INT16, "int16");
			DTYPES.put(AbstractDataset.INT32, "int32");
			DTYPES.put(AbstractDataset.INT64, "int64");
			DTYPES.put(AbstractDataset.FLOAT32, "float32");
			DTYPES.put(AbstractDataset.FLOAT64, "float64");
		}
	}

}
