package org.dawnsci.conversion.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

public class Convert1DtoND extends AbstractConversion {
	
	private final static Logger logger = LoggerFactory.getLogger(Convert1DtoND.class);
	
	Map<String, List<ILazyDataset>> dataMap = new HashMap<String, List<ILazyDataset>>();

	public Convert1DtoND(IConversionContext context) {
		super(context);
	}

	@Override
	protected void convert(AbstractDataset slice) throws Exception {
		
		if (!dataMap.containsKey(slice.getName()))dataMap.put(slice.getName(), new ArrayList<ILazyDataset>());
		dataMap.get(slice.getName()).add(slice);
		
		slice.getName();

	}
	
	@Override
	public void close(IConversionContext context) {

		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getWriter(context.getOutputPath());

			for (String key : dataMap.keySet()) {
				List<ILazyDataset> out = dataMap.get(key);
				String[] paths = getNexusPathAndNameFromKey(key);

				Group entry = file.group(paths[0]);
				file.setNexusAttribute(entry, Nexus.ENTRY);

				if (paths.length>2) {
					for (int i = 1; i < paths.length-1; i++) {
						final String path = paths[i];
						entry = file.group(path, entry);
						if (i<(paths.length-1)) file.setNexusAttribute(entry, Nexus.ENTRY);
					}
				}
				String name = paths[paths.length-1];


				Datatype dt = getDatatype(out.get(0).getSlice());

				Dataset d = file.appendDataset(name, dt, getLong(out.get(0).getShape()), ((AbstractDataset)out.get(0).getSlice()).getBuffer(), entry);
				
				file.setNexusAttribute(d, Nexus.SDS);
				file.setAttribute(d, "original_name", key);
				
				for (int i = 1; i < out.size(); i++) {
					d = file.appendDataset(name, dt, ImagesToHDFConverter.getLong(out.get(i).getShape()), ((AbstractDataset)out.get(i).getSlice()).getBuffer(), entry);
				}

			}
			file.close();
		} catch (Exception e) {
			if (file != null)
				try {
					file.close();
				} catch (Exception e1) {
					logger.error("Problem writing to h5 file :" + e1.getMessage() +" and inner: " + e.getMessage());
				}
		}
	}
	
	private String[] getNexusPathAndNameFromKey(String key) {
		String[]  paths = key.split("/");
		if ("".equals(paths[0])) paths = Arrays.copyOfRange(paths, 1, paths.length);
		return paths;
	}

}
