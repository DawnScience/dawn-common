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
import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;

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
				try {
					hFile.setNexusAttribute(group, Nexus.DATA);
				} catch (Exception ignored) {
					continue;
				}
			}
			groups.put(datasetNameStr, group);
			written.put(datasetNameStr, false);
		}
	}


	private Map<String, int[]> requiredShapes;
	
 	@Override
	protected void convert(IDataset slice) throws Exception {
		
 		final String datasetPath = slice.getName(); // Slice must be named the same as the path it will write to
 		final Datatype        dt = getDatatype(slice);
		
 		final String name = datasetPath.substring(datasetPath.lastIndexOf('/')+1);
 		final Group group = groups.get(datasetPath);
 		
 		
 		AbstractDataset abs = DatasetUtils.convertToAbstractDataset(slice).squeeze();
 		
 		// Each dataset must come through as the same shape, even if it is not.
 		// We keep the requiredShapes and force the current dataset to be the same
 		// as previous shapes.
 		int[] requiredShape = getRequiredShape(datasetPath, abs.getShape());
 		abs = resize(abs, requiredShape);

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
 	
	public static AbstractDataset resize(final AbstractDataset a, final int... shape) {
		
		if (a.getDtype()==AbstractDataset.STRING) return a;
		int size = a.getSize();
		AbstractDataset rdata = AbstractDataset.zeros(a.getElementsPerItem(), shape, a.getDtype());
		IndexIterator it = rdata.getIterator();
		while (it.hasNext()) {
			rdata.setObjectAbs(it.index, it.index<size ? a.getObjectAbs(it.index) : Double.NaN);
		}

		return rdata;
	}

	
 	private int[] getRequiredShape(String path, int[] defaultShape) {
 		if (requiredShapes==null) requiredShapes = new HashMap<String, int[]>();
 		int[] shape = context.getUserObject()!=null ? ((ConversionInfoBean)context.getUserObject()).getRequiredShape(path) : null;
 		if (shape==null) shape = requiredShapes.get(path);
 		if (shape==null) {
 			shape = defaultShape;
 			requiredShapes.put(path, defaultShape);
 		}
 		return shape;
 	}
	
	public void close(IConversionContext context) throws Exception{
		hFile.close();
	}

	
	/**
	 * To be used as the user object to convey data about the conversion.
	 * @author fcp94556
	 *
	 */
	public static final class ConversionInfoBean {

		private Map<String, int[]> requiredShapes = new HashMap<String, int[]>();

	 	private int[] getRequiredShape(String path) {
	 		if (requiredShapes==null) return null;
			return requiredShapes.get(path);
		}
	 	private void addRequiredShape(String path, int[] shape) {
			requiredShapes.put(path, shape);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((requiredShapes == null) ? 0 : requiredShapes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConversionInfoBean other = (ConversionInfoBean) obj;
			if (requiredShapes == null) {
				if (other.requiredShapes != null)
					return false;
			} else if (!requiredShapes.equals(other.requiredShapes))
				return false;
			return true;
		}
		
	}
}
