package org.dawnsci.conversion.converters;

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

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * This converter creates stacks from 
 * 
 * @author fcp94556
 *
 */
public class CompareConverter extends AbstractConversion{
	

	private IHierarchicalDataFile hFile;
	private Map<String,Group>     groups;
	private Map<String,Boolean>   written;

	public CompareConverter(IConversionContext context) throws Exception {
		
		super(context);
		
		// We open the file here, and create the group.
		hFile   = HierarchicalDataFactory.getWriter(context.getOutputPath());

		// For each dataset name we make a branch in the conversion file,
		// to store the data.
		final List<String> names = context.getDatasetNames();
		
		groups  = new HashMap<String, Group>(names.size());
		written = new HashMap<String, Boolean>(names.size());
		Group group = null;
		for (String datasetNameStr : names) {
			
			String[]  paths = datasetNameStr.split("/");
			if ("".equals(paths[0])) paths = Arrays.copyOfRange(paths, 1, paths.length);
			
	 		final Group entry = hFile.group(paths[0]);
	 		hFile.setNexusAttribute(entry, Nexus.ENTRY);

			group = entry;
			if (paths.length>2) {
				for (int i = 1; i < paths.length-1; i++) {
					final String path = paths[i];
					group = hFile.group(path, group);
					if (i<(paths.length-2)) hFile.setNexusAttribute(group, Nexus.ENTRY);
				}
				hFile.setNexusAttribute(group, Nexus.DATA);
			}
			groups.put(datasetNameStr, group);
			written.put(datasetNameStr, false);
		}
	}


 	@Override
	protected void convert(IDataset slice) throws Exception {
		
 		final String datasetPath = slice.getName(); // Slice mus tbe named the same as the path it will write to
 		final Datatype        dt = getDatatype(slice);
		
 		final String name = datasetPath.substring(datasetPath.lastIndexOf('/')+1);
 		final Group group = groups.get(datasetPath);
 		
 		AbstractDataset abs = DatasetUtils.convertToAbstractDataset(slice);
		Dataset d = hFile.appendDataset(name, dt, getLong(slice.getShape()), abs.getBuffer(), group);
		if (!written.get(datasetPath)) {
			hFile.setNexusAttribute(d, Nexus.SDS);
			hFile.setAttribute(d, "original_name", datasetPath);
			written.put(datasetPath, true);
		}
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			hFile.close();
			throw new Exception("Conversion is cancelled!");
		}
	}
	
	
	public void close(IConversionContext context) throws Exception{
		hFile.close();
	}

}
