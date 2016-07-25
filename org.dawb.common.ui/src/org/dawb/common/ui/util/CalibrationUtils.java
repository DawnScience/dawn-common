/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.jexl.ExpressionFactory;
import org.dawnsci.jexl.IExpressionEvaluator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class CalibrationUtils {

	/**
	 * Returns the calibrated abstract data set if required, otherwise returns set
	 * unchanged.
	 * 
	 * @param set
	 * @param scalar optionally specify some extra scalar values for the calibration expression.
	 * @return
	 * @throws Exception
	 */
	public static IDataset getCalibrated(final IDataset set, final Map<String,String> scalar, final boolean checkEnabled) throws Exception {
		
		
		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.tango.extensions");

		if (checkEnabled) {
			final boolean        isCalib = store.getBoolean(CalibrationConstants.USE);
			if (!isCalib)  return DatasetFactory.createRange(0, set.getSize(), 1, Dataset.INT32);
		}
		
     	final String expr = store.getString(CalibrationConstants.EXPR);
		final String name = store.getString(CalibrationConstants.LABEL);
		final double a    = store.getDouble(CalibrationConstants.A);
		final double b    = store.getDouble(CalibrationConstants.B);
		final double c    = store.getDouble(CalibrationConstants.C);
		final double d    = store.getDouble(CalibrationConstants.D);
		
		
		final double[] calib = new double[set.getSize()];
		
		// TODO FIXME - What is the definition of p and p0??
		final double p0 = 0;
	
		final IExpressionEvaluator eval = ExpressionFactory.createExpressionEvaluator();
		eval.setExpression(expr);
		
		final Map<String,Object> vals = new HashMap<String,Object>(7);
		for (int i = 0; i < calib.length; i++) {
			
			vals.clear();
			if (scalar!=null) vals.putAll(scalar);
			vals.put("p",  i);
			vals.put("p0", p0);
			vals.put("a",  a);
			vals.put("b",  b);
			vals.put("c",  c);
			vals.put("d",  d);
			
			calib[i] = eval.evaluate(vals);
		}
		
		Dataset ret = DatasetFactory.createFromObject(calib);
		ret.setName(name);
		return ret;	
	}

}
