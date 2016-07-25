package org.dawb.common.ui.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.ui.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext.ConversionScheme;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.metadata.IMetadata;

public class DatasetNameUtils {

	
	public static Map<String, int[]> getDatasetInfo(String path, ConversionScheme scheme) {
		IMetadata meta;
		final Map<String, int[]>     names  = new HashMap<String, int[]>();
		try {
			meta = ServiceLoader.getLoaderService().getMetadata(path, null);
		} catch (Exception e) {
			return names;
		}
        
        if (meta!=null && !meta.getDataNames().isEmpty()){
        	for (String name : meta.getDataShapes().keySet()) {
        		int[] shape = meta.getDataShapes().get(name);
        		if (shape != null) {
        			//squeeze to get usable rank
        			int[] ss = ShapeUtils.squeezeShape(shape, false);
        			if (scheme==null || scheme.isRankSupported(ss.length)) {
        				names.put(name, shape);
        			} 
        		} else {
        			//null shape is a bad sign
        			names.clear();
        			break;
        		}
        	}
        }
        
        if (names.isEmpty()) {
        	IDataHolder dataHolder;
			try {
				dataHolder = ServiceLoader.getLoaderService().getData(path, null);
			} catch (Exception e) {
				return names;
			}
        	if (dataHolder!=null) for (String name : dataHolder.getNames()) {
        		if (name.contains("Image Stack")) continue;
        		if (!names.containsKey(name)) {

        			int[] shape = dataHolder.getLazyDataset(name).getShape();
        			int[] ss = ShapeUtils.squeezeShape(shape, false);
        			if (scheme==null || scheme.isRankSupported(ss.length)) {
        				names.put(name, shape);
        			} 

        		}
        	}
        }
	    return sortedByRankThenLength(names);
	}
	
	private static Map<String, int[]> sortedByRankThenLength(Map<String, int[]> map) {
		
		List<Entry<String, int[]>> ll = new LinkedList<Entry<String, int[]>>(map.entrySet());
		
		Collections.sort(ll, new Comparator<Entry<String, int[]>>() {

			@Override
			public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
				int val = Integer.compare(o2.getValue().length, o1.getValue().length);
				
				if (val == 0) val = Integer.compare(o1.getKey().length(), o2.getKey().length());
				
				return val;
			}
		});
		
		Map<String, int[]> lhm = new LinkedHashMap<String, int[]>();
		
		for (Entry<String, int[]> e : ll) lhm.put(e.getKey(), e.getValue());
		
		return lhm;
		
	}
	
}
