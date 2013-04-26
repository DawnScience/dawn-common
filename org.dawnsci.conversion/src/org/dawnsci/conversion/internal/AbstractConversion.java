package org.dawnsci.conversion.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ncsa.hdf.object.Dataset;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * AbstractConversion details converting from hdf/nexus to other
 * things only at the moment.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractConversion {
	
	protected IConversionContext context;

	public AbstractConversion(IConversionContext context) {
		this.context = context;
	}

	public void process(IConversionContext context) throws Exception {
		// Process regular expression
		final List<File> paths = expand(context.getFilePath());
		for (File path : paths) {
			
			final List<String> sets  = getDataNames(path);
			final List<String> names = context.getDatasetNames();
			for (String nameRegExp : names) {
				final List<String> data = getData(sets, nameRegExp);
				for (String dsPath : data) {
					processSlice(path, dsPath, context.getSliceDimensions(), context);
				}
			}
		}
	}
	
	/**
	 * Override this method to provide things which should happen after the processing.
	 * @param context
	 */
	public void close(IConversionContext context) throws Exception{
		
	}
	
	protected IConversionContext getContext() {
		return context;
	}

	/**
	 * Please implement this method to process a single conversion. The files will have been 
	 * expanded, the datasets expanded, the slice done, you need to implement the writing of the
	 * appropriate file for this slice.
	 * 
	 * @param slice
	 * @param context, used to provide the output location mainly.
	 */
	protected abstract void convert(AbstractDataset slice) throws Exception;
	
	public void processSlice(final File                 path, 
				            final String               dsPath,
				            final Map<Integer, String> sliceDimensions,
				            final IConversionContext   context) throws Exception {
		
		// TODO Should have used ILazyDataset here, but it is very slow
		// in comparison to direct. -- seems to return the same data each time!?
		//processSliceDirect(path, dsPath, sliceDimensions, context);
		processSliceLazy(path, dsPath, sliceDimensions, context);  // Slow!
	}

	/**
	 * 
	 * @param path
	 * @param dsPath
	 * @param sliceDimensions
	 * @param context, might be null for testing
	 * @return
	 * @throws Exception
	 */
	private void processSliceDirect(final File                 path, 
			                  final String               dsPath,
			                  final Map<Integer, String> sliceDimensions,
			                  final IConversionContext   context) throws Exception {
		
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(path.getAbsolutePath());
			final Dataset dataset = (Dataset)file.getData(dsPath);
			
			long[] fullDims = dataset.getDims(); // the selected size of the dataset
			long[] selected = dataset.getSelectedDims(); // the selected size of the dataset
			for (int i = 0; i < fullDims.length; i++) selected[i] = fullDims[i];
			
			if (sliceDimensions==null) {
				AbstractDataset data = getSet(dataset.getData(),selected,dataset);
				data.setName(dsPath);
				convert(data);
				return;
			}
			
			if (dataset.getStartDims()==null) dataset.getMetadata();
  		    long[] start    = dataset.getStartDims(); // the off set of the selection
			long[] stride   = dataset.getStride(); // the stride of the dataset
			
			List<Long> dims = new ArrayList<Long>(stride.length);
			String sliceRange=null;
			int    sliceIndex=-1;
			for (int i = 0; i < stride.length; i++) {
				stride[i] = 1;
				
				// Any that parse statically to a single int are not ranges.
				if (sliceDimensions.containsKey(i)) {
					try {
						start[i]    = Long.parseLong(sliceDimensions.get(i));
						selected[i] = 1;
						sliceIndex  = i;
					} catch (Throwable ne) {
						sliceRange = sliceDimensions.get(i);
						sliceIndex = i;
						continue;
					}
				} else {
					dims.add(selected[i]);
				}
			}
			
			long[] dim = new long[dims.size()];
			for (int i = 0; i < dim.length; i++) dim[i] = dims.get(i);
			
			if (sliceRange!=null) { // We compute the range to slice.
				long s = 0;
				long e = fullDims[sliceIndex];
				if (sliceRange.indexOf(":")>0) {
					final String[] sa = sliceRange.split(":");
					s = Long.parseLong(sa[0]);
					e = Long.parseLong(sa[1]);
				}

				for (long index = s; index < e; index++) {
					start[sliceIndex]    = index;
					selected[sliceIndex] = 1;
					
					AbstractDataset data = getSet(dataset.getData(),dim,dataset);
					data.setName(dsPath+" (Dim "+sliceIndex+"; index="+index+")");
					convert(data);
				}
				
			} else {
				AbstractDataset data = getSet(dataset.getData(),dim,dataset);
				data.setName(dsPath+" (Dim "+sliceIndex+"; index="+start[sliceIndex] +")");
				convert(data);
			}
			
		} finally {
			file.close();
		}
	}
	
	/**
	 * 
	 * @param path
	 * @param dsPath
	 * @param sliceDimensions
	 * @param context, might be null for testing
	 * @return
	 * @throws Exception
	 */
	private void processSliceLazy(final File                 path, 
				                  final String               dsPath,
				                  final Map<Integer, String> sliceDimensions,
				                  final IConversionContext   context) throws Exception {
		
		
		final DataHolder   dh = LoaderFactory.getData(path.getAbsolutePath());
		if (sliceDimensions==null) {
			AbstractDataset data = LoaderFactory.getDataSet(path.getAbsolutePath(), dsPath, null);
			data.setName(dsPath);
			convert(data);
			return;
		}
		
		final ILazyDataset lz = dh.getLazyDataset(dsPath);
		final int[] fullDims = lz.getShape();

		int[] start  = new int[fullDims.length];
		for (int i = 0; i < start.length; i++) start[i] = 0;
		
		int[] stop  = new int[fullDims.length];
		for (int i = 0; i < stop.length; i++) stop[i] = fullDims[i];
		
		int[] step  = new int[fullDims.length];

		List<Integer> dims = new ArrayList<Integer>(fullDims.length);
		String sliceRange=null;
		int    sliceIndex=-1;
		for (int i = 0; i < fullDims.length; i++) {
			step[i] = 1;

			// Any that parse statically to a single int are not ranges.
			if (sliceDimensions.containsKey(i)) {
				try {
					start[i]    = Integer.parseInt(sliceDimensions.get(i));
					stop[i]     = start[i]+1;
					sliceIndex  = i;
				} catch (Throwable ne) {
					sliceRange = sliceDimensions.get(i);
					sliceIndex = i;
					continue;
				}
			} else {
				dims.add(fullDims[i]);
			}
		}

		long[] dim = new long[dims.size()];
		for (int i = 0; i < dim.length; i++) dim[i] = dims.get(i);

		if (sliceRange!=null) { // We compute the range to slice.
			int s = 0;
			int e = fullDims[sliceIndex];
			if (sliceRange.indexOf(":")>0) {
				final String[] sa = sliceRange.split(":");
				s = Integer.parseInt(sa[0]);
				e = Integer.parseInt(sa[1]);
			}

			for (int index = s; index < e; index++) {
				start[sliceIndex]   = index;
				stop[sliceIndex]    = index+1;

				AbstractDataset data = (AbstractDataset)lz.getSlice(start, stop, step);
				data = data.squeeze();
				data.setName(dsPath+" (Dim "+sliceIndex+"; index="+index+")");
				convert(data);
			}

		} else {
			AbstractDataset data = (AbstractDataset)lz.getSlice(start, stop, step);
			data = data.squeeze();
			data.setName(dsPath+" (Dim "+sliceIndex+"; index="+start[sliceIndex] +")");
			convert(data);
		}
	}
	
	/**
	 * Used when dims are not the same as the entire set, for instance when doing a slice.
	 * @param val
	 * @param longShape
	 * @param set
	 * @return
	 * @throws Exception
	 */
	public static AbstractDataset getSet(final Object  val, final long[] longShape, final Dataset set) throws Exception {

		final int[] intShape  = getInt(longShape);
         
		AbstractDataset ret = null;
        if (val instanceof byte[]) {
        	ret = new ByteDataset((byte[])val, intShape);
        } else if (val instanceof short[]) {
        	ret = new ShortDataset((short[])val, intShape);
        } else if (val instanceof int[]) {
        	ret = new IntegerDataset((int[])val, intShape);
        } else if (val instanceof long[]) {
        	ret = new LongDataset((long[])val, intShape);
        } else if (val instanceof float[]) {
        	ret = new FloatDataset((float[])val, intShape);
        } else if (val instanceof double[]) {
        	ret = new DoubleDataset((double[])val, intShape);
        } else {
        	throw new Exception("Cannot deal with data type "+set.getDatatype().getDatatypeDescription());
        }
        
		if (set.getDatatype().isUnsigned()) {
			switch (ret.getDtype()) {
			case AbstractDataset.INT32:
				ret = new LongDataset(ret);
				DatasetUtils.unwrapUnsigned(ret, 32);
				break;
			case AbstractDataset.INT16:
				ret = new IntegerDataset(ret);
				DatasetUtils.unwrapUnsigned(ret, 16);
				break;
			case AbstractDataset.INT8:
				ret = new ShortDataset(ret);
				DatasetUtils.unwrapUnsigned(ret, 8);
				break;
			}
		}

        return ret;
       
	}

	/**
	 * Get a int[] from a long[]
	 * @param longShape
	 * @return
	 */
	public static int[] getInt(long[] longShape) {
		final int[] intShape  = new int[longShape.length];
		for (int i = 0; i < intShape.length; i++) intShape[i] = (int)longShape[i];
		return intShape;
	}

	public List<String> getData(File path, String datasetName) throws Exception {
        return getData(getDataNames(path), datasetName);
	}
	
	private List<String> getData(List<String> sets, String datasetName) {

		final List<String> ds = new ArrayList<String>(7);
		for (String hdfPath : sets) {
			if (hdfPath.matches(datasetName)) {
				ds.add(hdfPath);
			}
		}

		return ds.isEmpty() ? null : ds;
	}
	
	/**
	 * Can be used to get a list of Dataset which should be converted. Processes the
	 * regexp for the dataset path and returns the Dataset which can be sliced to get
	 * the array of numbers for the export.
	 * 
	 * @param ioFile
	 * @param context
	 * @return null if none match, the datasets otherwise
	 * @throws Exception
	 */
	public List<String> getDataNames(File ioFile) throws Exception {
		
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(ioFile.getAbsolutePath());
			final List<String> sets = file.getDatasetNames(IHierarchicalDataFile.NUMBER_ARRAY);
			return sets;
			
		} finally {
			if (file!=null) file.close();
		}
	}

	/**
	 * expand the regex according to the javadoc for getFilePath().
	 * @param context
	 * @return
	 */
	public List<File> expand(String path) {
		final List<File> files = new ArrayList<File>(7);
		final String dir    = path.substring(0, path.lastIndexOf("/"));
		final String regexp = path.substring(path.lastIndexOf("/")+1);
		
		final File[] fa = new File(dir).listFiles();
		for (File file : fa) {
			if (regexp==null || "".equals(regexp)) {
				files.add(file);
				continue;
			}
			if (file.getName().matches(regexp)) files.add(file);
		}
		
		return files.isEmpty() ? null : files;
	}

}
