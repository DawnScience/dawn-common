/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.util.list;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

	/**
	 * 
	 * @param value
	 * @return v
	 */
	public static String getString(final List<String> value) {
		if (value == null)   return null;
		if (value.isEmpty()) return null;
		final String line = value.toString();
		return line.substring(1,line.length()-1);
	}
	
	/**
	 * 
	 * @param value
	 * @return v
	 */
	public static List<String> getList(final String value) {
		if (value == null)           return null;
		if ("".equals(value.trim())) return null;
		final String[]    vals = value.split(",");
		final List<String> ret = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++) ret.add(vals[i].trim());
		return ret;
	}
	
	public static void main(String[] args) {
		String test = "one,two,three";
		List<String> v = getList(test);
		System.out.println(v);
		
		test = " ";
		v = getList(test);
		System.out.println(v);
		
		test = null;
		v = getList(test);
		System.out.println(v);

		test = ",";
		v = getList(test);
		System.out.println(v);
		
		test = ",,";
		v = getList(test);
		System.out.println(v);
		
		test = ",,)";
		v = getList(test);
		System.out.println(v);

	}
}
