package org.dawnsci.conversion.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ncsa.hdf.object.Dataset;

import org.dawb.common.services.IConversionContext;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;

public abstract class AbstractConversion {

	public void process(IConversionContext context) throws Exception {
		// Process regular expression
		final List<File> paths = expand(context.getFilePath());
		for (File path : paths) {
			final List<String> data = getData(path, context.getDatasetName());
			for (String dsPath : data) {
				processSlice(path, dsPath, context.getSliceDimensions(), context);
			}
		}
	}

	/**
	 * Please implement this method to process a single conversion. The files will have been 
	 * expanded, the datasets expanded, the slice done, you need to implement the writing of the
	 * apropriate file for this slice.
	 * 
	 * @param slice
	 * @param context, used to provide the output location mainly.
	 */
	protected abstract void convert(AbstractDataset slice, IConversionContext context);
	
	/**
	 * 
	 * @param path
	 * @param dsPath
	 * @param sliceDimensions
	 * @param context, might be null for testing
	 * @return
	 * @throws Exception
	 */
	private void processSlice(final File                 path, 
			                  final String               dsPath,
			                  final Map<Integer, String> sliceDimensions,
			                  final IConversionContext   context) throws Exception {
		
		// TODO Should have used ILazyDataset here, but it works as is for now.
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
				convert(data, context);
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
					convert(data, context);
				}
				
			} else {
				AbstractDataset data = getSet(dataset.getData(),dim,dataset);
				data.setName(dsPath+" (Dim "+sliceIndex+"; index="+start[sliceIndex] +")");
				convert(data, context);
			}
			
		} finally {
			file.close();
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
	protected List<String> getData(File ioFile, String datasetName) throws Exception {
		
		final List<String> ds = new ArrayList<String>(7);
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(ioFile.getAbsolutePath());
			final List<String> sets = file.getDatasetNames(IHierarchicalDataFile.NUMBER_ARRAY);
			for (String hdfPath : sets) {
				if (hdfPath.matches(datasetName)) {
					ds.add(hdfPath);
				}
			}
			
		} finally {
			file.close();
		}
		return ds.isEmpty() ? null : ds;
	}

	/**
	 * expand the regex according to the javadoc for getFilePath().
	 * @param context
	 * @return
	 */
	protected List<File> expand(String path) {
		final List<File> files = new ArrayList<File>(7);
		final String dir    = path.substring(0, path.lastIndexOf("/"));
		final String regexp = path.substring(path.lastIndexOf("/")+1);
		
		final File[] fa = new File(dir).listFiles();
		for (File file : fa) {
			if (file.getName().matches(regexp)) files.add(file);
		}
		
		return files.isEmpty() ? null : files;
	}

	/**
	 * TODO FIXME - move these to junit tests!
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		
		final AbstractConversion conv = new AbstractConversion() {
			@Override
			protected void convert(AbstractDataset slice, IConversionContext context) {
				System.out.println(slice);
			}
		};
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ EXPAND");
		System.out.println(Arrays.toString(conv.expand("C:/Work/results/i03_data/35873/35873_M3S15_1_0001.cbf").toArray()));
		System.out.println(Arrays.toString(conv.expand("C:/Work/results/i03_data/35873/35873_M3S15_1_000(\\d+).cbf").toArray()));
		System.out.println(Arrays.toString(conv.expand("C:/Work/results/i03_data/35873/35873_M3S15_1_00(\\d+).cbf").toArray()));
		System.out.println(conv.expand("C:/Work/results/i03_data/35873/fred.cbf"));
		System.out.println(conv.expand("C:/Work/results/i03_data/35873/(.*).img"));
		System.out.println(conv.expand("C:/Work/results/i03_data/35873/(.*).cbf"));
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ DATA");
		System.out.println(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "naff"));
		System.out.println(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/data"));
		System.out.println(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/data_(.*)"));
		System.out.println(conv.getData(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/(.*)"));
		
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ SLICE");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/white_z", null, null);
		
		final Map<Integer,String> slice = new HashMap<Integer,String>(1);
		slice.put(0, "36");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/dark_data", slice, null);

		slice.put(0, "all");
		conv.processSlice(new File("C:/Work/results/results/large test files/TomographyDataSet.hdf5"), "/entry/exchange/dark_data", slice, null);

	}
}
