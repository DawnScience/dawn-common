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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntersectionUtils {

	
	/**
	 * Checks if a list of two doubles (start,end) arrays intersect. Throws 
	 * Exception with information about what hit what if do.
	 * 
	 * @param values a list of the array {startValue, endValue, name}
	 * where startValue and endValue must be Numbers, and name will have toString() called on it
	 * when assigning the exception.
	 */
	public static void checkIntersection(final List<Object[]> values) throws IntersectionException {
		
		if (values.size()<2) return;
		
		// First order entries by start value
		Collections.sort(values, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				return Double.compare(((Number)o1[0]).doubleValue(), ((Number)o2[0]).doubleValue());
			}
		});
		
		for (int i = 1; i < values.size(); i++) {
			final Object[] prev = values.get(i-1);
			final Object[] val  = values.get(i);
			
			final Number start1    = (Number)prev[0];
			final Number end1      = (Number)prev[1];
			final Number start2    = (Number)val[0];
			final Number end2      = (Number)val[1];
			
			if (start1.doubleValue()>start2.doubleValue() || start1.doubleValue()>end2.doubleValue() || end1.doubleValue()>start2.doubleValue() || end1.doubleValue()>end2.doubleValue()) {
				final IntersectionException e = new IntersectionException("The indices "+(i-1)+" and "+i+" intersect!");
				e.setFirstName(prev[2].toString());
				e.setSecondName(val[2].toString());
				throw e;
			}
		}
	}
}
