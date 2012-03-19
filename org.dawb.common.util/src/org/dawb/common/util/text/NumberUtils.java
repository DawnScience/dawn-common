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
	
	
	/**
	 * Test if two numbers are equal using their double value.
	 * @param compare
	 * @param with
	 * @param tolerance
	 * @return true if foo equals bar within tolerance
	 */
	public static boolean equalsTolerance(Number compare, Number with, Number tolerance) {
		final double c = compare.doubleValue();
		final double w = with.doubleValue();
		final double t = tolerance.doubleValue();	
		return t>=Math.abs(c-w);
	}
	
	/**
	 * Test if two numbers are equal using their double value.
	 * Percentage is a percentage of the compare number which the 
	 * with number must fall within as a fraction of 1.
	 * 
	 * @param compare
	 * @param with
	 * @param percentage
	 * @return
	 */
	public static boolean equalsPercent(Number compare, Number with, Number percentage) {

		final double c = compare.doubleValue();
		final double w = with.doubleValue();
		final double p = percentage.doubleValue();
		double r = (p * Math.abs(c)) / 100.; // relative tolerance
		
		return r >= Math.abs(c - w);
	}
	
	/**
	 * Test if two numbers are equal using their double value.
	 * Both the absolute tolerance and a percentage of the compare value can be used.
	 * The relative tolerance is given by a percentage and calculated from the absolute maximum of the input numbers.
	 * Equality is found using whichever tolerance  is larger.
	 * 
	 * @param compare
	 * @param with
	 * @param tolerance
	 * @param percentage
	 * @return true if foo equals bar within tolerance
	 */
	public static boolean equalsTolerances(Number compare, Number with, Number tolerance, Number percentage) {
		final double c = compare.doubleValue();
		final double w = with.doubleValue();
		final double t = tolerance.doubleValue();
		final double p = percentage.doubleValue();

		double r = p * Math.max(Math.abs(c), Math.abs(w)) / 100.; // relative tolerance
		if (r > t) return r >= Math.abs(c - w);
		return t >= Math.abs(c - w);
	}

}
