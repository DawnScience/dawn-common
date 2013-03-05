package org.dawnsci.jexl.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlEngine;
import org.dawnsci.jexl.internal.DatasetArithmetic;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.Random;

public class JexlUtils {
	
	public static JexlEngine getDawnJexlEngine() {
		
		JexlEngine jexl = new JexlEngine(null, new DatasetArithmetic(false),null,null);
		
		//Add some useful functions to the engine
		Map<String,Object> funcs = new HashMap<String,Object>();
		funcs.put("dnp", Maths.class);
		funcs.put("rd", Random.class);
		funcs.put("dd", DoubleDataset.class);
		
		jexl.setFunctions(funcs);
		return jexl;
	}
	
	//public static 

}
