package org.dawnsci.jexl.internal;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;

/**
 * Class to wrap the methods on the AbstractDataset object (and a few select others)
 * allowing them to be called in a manner consistent with the other Jexl functions
 * i.e. namespace:methodName()
 * <p>
 * Methods only for use in the DawnJexlEngine which can be obtained from JexlUtils
 */
public class JexlGeneralFunctions {
	
	public static AbstractDataset mean(final AbstractDataset data,final int axis) {
		return data.mean(axis);
	}
	
	public static AbstractDataset sum(final AbstractDataset data,final int axis) {
		return data.sum(axis);
	}
	
	public static AbstractDataset slice(final AbstractDataset data,final int[] start,
																   final int[] stop,
																   final int[] step) {
		return data.getSlice(start, stop, step);
	}
	
	public static AbstractDataset stdDev(final AbstractDataset data, final int axis) {
		return data.stdDeviation(axis);
	}
	
	public static AbstractDataset max (final AbstractDataset data, final int axis) {
		return data.max(axis);
	}
	
	public static AbstractDataset min (final AbstractDataset data, final int axis) {
		return data.min(axis);
	}
	
	public static AbstractDataset peakToPeak(final AbstractDataset data, final int axis) {
		return data.peakToPeak(axis);
	}
	
	public static AbstractDataset product(final AbstractDataset data, final int axis) {
		return data.product(axis);
	}
	
	public static AbstractDataset rootMeanSquare(AbstractDataset data, int axis) {
		return data.rootMeanSquare(axis);
	}
	
	public static AbstractDataset median(AbstractDataset data, int axis) {
		return Stats.median(data,axis);
	}
	
	public static AbstractDataset slice(AbstractDataset data, String sliceString) {
		Slice[] slices = getSliceFromString(sliceString);
		
		if (slices.length != data.getRank()) throw new IllegalArgumentException("Invalid string");
		
		return data.getSlice(slices).squeeze();
	}
	
	private static Slice[] getSliceFromString(String sliceString) {
		
		String clean = sliceString.replace("[", "");
		clean = clean.replace("]", "");
		
		String[] sub = clean.split(",");
		
		Slice[] slices= new Slice[sub.length];
		
		for (int i = 0; i < sub.length; i++) {
			String s = sub[i];
			
			slices[i]= new Slice();
			
			int idx0 = s.indexOf(":");
			
			if (idx0 == -1) {
				slices[i].setStart(Integer.parseInt(s));
				slices[i].setStop(Integer.parseInt(s)+1);
				continue;
			} else if (idx0 == 0) {
				slices[i].setStart(0);
			} else {
				slices[i].setStart(Integer.parseInt(s.substring(0,idx0)));
			}
			
			int idx1 = s.indexOf(":", idx0+1);
			if (idx1 == -1) {
				
				if (s.substring(idx0+1).length() == 0) continue;
				
				slices[i].setStop(Integer.parseInt(s.substring(idx0+1)));
				continue;
			} else if (idx1-idx0 == 1) {
				slices[i].setStep(Integer.parseInt(s.substring(idx1+1)));
				continue;
			} else {
				slices[i].setStop(Integer.parseInt(s.substring(idx0+1,idx1)));
				slices[i].setStep(Integer.parseInt(s.substring(idx1+1)));
			}
			
		}
		
		return slices;
	}
	
}
