package org.dawnsci.jexl.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlEngine;
import org.dawnsci.jexl.internal.DatasetArithmetic;
import org.dawnsci.jexl.internal.JexlGeneralFunctions;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FFT;
import uk.ac.diamond.scisoft.analysis.dataset.Image;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.Random;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;

/**
 * Utilities class for using the Jexl expression engine with AbstractDatasets
 */
public class JexlUtils {
	
	/**
	 * Get a JexlEngine configured to perform mathematical calculations on AbstractDatasets
	 * Function namespaces dnp (Maths), rd (Random) and dd (DoubleDataset) have been added,
	 * giving access to the static methods on these classes
	 * 
	 * @return AbstractDataset aware Jexl engine
	 */
	public static JexlEngine getDawnJexlEngine() {
		
		//Create the Jexl engine with the DatasetArthmetic object to allows basic
		//mathematical calculations to be performed on AbstractDatasets
		JexlEngine jexl = new JexlEngine(null, new DatasetArithmetic(false),null,null);
		
		//Add some useful functions to the engine
		Map<String,Object> funcs = new HashMap<String,Object>();
		funcs.put("dnp", Maths.class);
		funcs.put("dat", JexlGeneralFunctions.class);
		funcs.put("im", Image.class);
		funcs.put("fft", FFT.class);
		jexl.setFunctions(funcs);
		
		return jexl;
	}
}
