package org.dawnsci.conversion.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.util.list.SortNatural;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

/**
 * This converter converts a directory of images to a stack in HDF5
 * which allows slicing tools to be run in a faster way.
 * 
 * @author fcp94556
 *
 */
public class ImagesToHDFConverter extends AbstractConversion{
	

	private IHierarchicalDataFile file;
	private Group                 group;
	private String                name;

	public ImagesToHDFConverter(IConversionContext context) throws Exception {
		
		super(context);
		
		// We open the file here, and create the group.
		file   = HierarchicalDataFactory.getWriter(context.getOutputPath());

		// We make the group
		final String datasetNameStr = context.getDatasetNames().get(0);
		String[]  paths = datasetNameStr.split("/");
		if ("".equals(paths[0])) paths = Arrays.copyOfRange(paths, 1, paths.length);
 		final Group entry = file.group(paths[0]);
		file.setNexusAttribute(entry, Nexus.ENTRY);

		group = entry;
		if (paths.length>2) {
			for (int i = 1; i < paths.length-1; i++) {
				final String path = paths[i];
				group = file.group(path, group);
				if (i<(paths.length-2)) file.setNexusAttribute(group, Nexus.ENTRY);
			}
			file.setNexusAttribute(group, Nexus.DATA);
		}
		name = paths[paths.length-1];

		// We put the many files in one ILazyDataset and set that in the context as an override.
		if (context.getFilePaths().size()>1) throw new Exception(getClass().getSimpleName()+" can only be used with one path regex at the moment!");
		ILazyDataset set = getLazyDataset(context.getFilePaths().get(0));
		context.setLazyDataset(set);
		
		context.addSliceDimension(0, "all");
		
	}

	private ILazyDataset getLazyDataset(final String dir) throws Exception {

		final List<File>   files = expand(dir);
		final List<String> paths = new ArrayList<String>(files.size());
		for (File file : files) paths.add(file.getAbsolutePath());
		
		Collections.sort(paths, new SortNatural<String>(true));
		ImageStackLoader loader = new ImageStackLoader(paths, context.getMonitor());
		LazyDataset lazyDataset = new LazyDataset("Folder Stack", loader.getDtype(), loader.getShape(), loader);
		return lazyDataset;

	}

    private boolean first = true;
	@Override
	protected void convert(AbstractDataset slice) throws Exception {
		
        final String datasetPath = context.getDatasetNames().get(0);
		final Datatype        dt = getDatatype(slice);
		
		Dataset d = file.appendDataset(name, dt, getLong(slice.getShape()), slice.getBuffer(), group);
		if (first) {
			file.setNexusAttribute(d, Nexus.SDS);
			file.setAttribute(d, "original_name", datasetPath);
			first = false;
		}
	}
	
	/**
	 * Determines the HDF5 Datatype for an abstract dataset.
	 * @param a
	 * @return data type
	 */
	public static Datatype getDatatype(IDataset a) throws Exception {
		
		// There is a smarter way of doing this, but am in a hurry...
		if (a instanceof ByteDataset || a instanceof BooleanDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 8/8, Datatype.NATIVE, Datatype.SIGN_NONE);
         	
        } else if (a instanceof ShortDataset) {
        	return new H5Datatype(Datatype.CLASS_INTEGER, 16/8, Datatype.NATIVE, Datatype.NATIVE); 
        	
        } else if (a instanceof IntegerDataset) {
        	return new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE); 
       	
        } else if (a instanceof LongDataset) {
        	return new H5Datatype(Datatype.CLASS_INTEGER, 64/8, Datatype.NATIVE, Datatype.NATIVE); 
        	
        } else if (a instanceof FloatDataset) {
        	return new H5Datatype(Datatype.CLASS_FLOAT, 32/8, Datatype.NATIVE, Datatype.NATIVE); 
        	
        } else if (a instanceof DoubleDataset) {
        	return new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE); 
      	    
        }
        
        throw new Exception("Cannot deal with data type "+a.getClass().getName());
	}
	
	public static long[] getLong(int[] intShape) {
		final long[] longShape  = new long[intShape.length];
		for (int i = 0; i < intShape.length; i++) longShape[i] = intShape[i];
		return longShape;
	}
	
	public void close(IConversionContext context) throws Exception{
		file.close();
	}

}
