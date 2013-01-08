package org.dawb.common.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.dawb.common.util.ExpressionFactory;
import org.dawb.common.util.IExpressionEvaluator;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

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
			if (!isCalib)  return AbstractDataset.arange(0, set.getSize(), 1, AbstractDataset.INT32);
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
		
		DoubleDataset ret = new DoubleDataset(calib, calib.length);
		ret.setName(name);
		return ret;	
	}

}
