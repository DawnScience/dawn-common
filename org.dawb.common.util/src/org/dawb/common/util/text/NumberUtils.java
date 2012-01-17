/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.text;

public class NumberUtils {

	/**
	 * Will return Object which is Integer, Double, or string depending on what parses
	 * 
	 * NOTE This method is relatively slow because of the parsing!
	 * 
	 * @param stringValue
	 * @return
	 */
	public static Object getNumberIfParses(String stringValue) {
		try {
			final int ival = Integer.parseInt(stringValue);
			return ival;
			
		} catch (Throwable t) {
			try {
				final double dval = Double.parseDouble(stringValue);
				return dval;
				
			} catch (Throwable t2) {
				return stringValue;
			}
		}
	}

}
