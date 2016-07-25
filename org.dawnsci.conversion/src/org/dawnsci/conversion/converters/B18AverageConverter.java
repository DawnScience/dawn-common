package org.dawnsci.conversion.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.IMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.function.Interpolation1D;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class B18AverageConverter extends AbstractConversion {

	private static final Logger logger = LoggerFactory.getLogger(B18AverageConverter.class);

	private static final double SHORT_RANGE = 10.0;

	private static class B18AverageData {
		/*public Dataset qexafs_energy;
		public Dataset time;
		public Dataset I0;
		public Dataset It;
		public Dataset Iref;
		public Dataset lnI0It;
		public Dataset lnItIref;
		public Dataset QexafsFFI0; // QexafsFFI0 or FF...*/
		public Dataset[] allData;
		
		/*public static String[] getNames() {
			return new String[]{"qexafs_energy", "time", "I0", "It", "Iref", "lnI0It", "lnItIref", "QexafsFFI0"};
		}*/
	}
	
	public enum B18DataType {
		TRANSMISSION("Transmission", new int[]{1,2,3,4,5}),
		FLUORESCENCE("Fluorescence", new int[]{1,2,3,4,5,-2,-1}),
		CUSTOM("Custom", null);
		
		private final String type;
		private final int[] dataIndices;
		
			
		private B18DataType(String type, int[] dataIndices) {
			this.type = type;
			this.dataIndices = dataIndices;
		}
		
		@Override
		public String toString() {
			return type;
		}
		
		public static int getEnergyIndex() {
			return 0;
		}
		
		public int[] getDataIndices(IConversionContext context, File firstFile) throws Exception {
			if (dataIndices == null) {
				// check the context for dataset names and match them with indices
				IDataHolder dh = LoaderFactory.getData(firstFile.getAbsolutePath());
				List<String> namesFromFile = Arrays.asList(dh.getNames());
				List<Integer> dataIndicesList = new ArrayList<>();
				for (String name : context.getDatasetNames()) {
					int index = namesFromFile.indexOf(name);
					if (index == -1) {
						// this should never happen
						throw new Exception("getDataIndices: selected dataset not found in first file");
					}
					dataIndicesList.add(index);
				}
				return dataIndicesList.stream().mapToInt(i -> i).toArray();
			}
			return dataIndices;
		}
		
	}
	
	public enum B18InterpolationType {
		NONE("None"),
		LINEAR("Linear"),
		SPLINE("Spline");
	
		private final String type;
		
		private B18InterpolationType(String type) {
			this.type = type;
		}
		
		@Override
		public String toString() {
			return type;
		}
	}
	
	public static final class ConversionInfoBean {
		
		B18DataType dataType = B18DataType.TRANSMISSION;
		B18InterpolationType interpolationType = B18InterpolationType.LINEAR;
		boolean useMetadataForGrouping = false;
		String metadataForGroupingName = "";
		double metadataForGroupingDelta = 0.0;
		
		public B18DataType getDataType() {
			return dataType;
		}
		public void setDataType(B18DataType dataType) {
			this.dataType = dataType;
		}
		public B18InterpolationType getInterpolationType() {
			return interpolationType;
		}
		public void setInterpolationType(B18InterpolationType interpolationType) {
			this.interpolationType = interpolationType;
		}
		public boolean isUseMetadataForGrouping() {
			return useMetadataForGrouping;
		}
		public void setUseMetadataForGrouping(boolean useMetadataForGrouping) {
			this.useMetadataForGrouping = useMetadataForGrouping;
		}
		public String getMetadataForGroupingName() {
			return metadataForGroupingName;
		}
		public void setMetadataForGroupingName(String metadataForGroupingName) {
			this.metadataForGroupingName = metadataForGroupingName;
		}
		public double getMetadataForGroupingDelta() {
			return metadataForGroupingDelta;
		}
		public void setMetadataForGroupingDelta(double metadataForGroupingDelta) {
			this.metadataForGroupingDelta = metadataForGroupingDelta;
		}
	}
	
	public B18AverageConverter(IConversionContext context) {
		super(context);
		// hope context is not null here...
		this.context = context;
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		// this method needs an implementation but it won't actually be used as we are overriding process in which everything is going to happen
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		final ConversionInfoBean bean = (ConversionInfoBean) context.getUserObject();
		//this.context = context; 
		
		List<File> file_list_in = new ArrayList<>();
		List<Integer> rep_1st = new ArrayList<>();
		final List<String> filePaths = context.getFilePaths();
		double refMetadataValue = Double.NEGATIVE_INFINITY;
		for (String filePathRegEx : filePaths) {
			final List<File> paths = expand(filePathRegEx);
			// add all paths after expansion to the file_list
			file_list_in.addAll(paths);
			for (File path : paths) {
				logger.debug("Processing file: " + path.getAbsolutePath());
				String basename = path.getName();
				//logger.debug("basename: " + basename);
				String[] components = basename.split("_");
				//logger.debug("components: " + Arrays.toString(components));
				// check if this file marks the start of a repetition
				if (!bean.useMetadataForGrouping && components[components.length-1].equals("1.dat")) {
					int position = file_list_in.indexOf(path);
					rep_1st.add(position);
				} else if (bean.useMetadataForGrouping) {
					// if first component -> add index to list
					if (components[components.length-1].equals("1.dat")) {
						int position = file_list_in.indexOf(path);
						rep_1st.add(position);
					}
					//open file and extract the required metadata
					IDataHolder dh = LoaderFactory.getData(path.getAbsolutePath());
					IMetadata md = dh.getMetadata();
					double currentMetadataValue = Double.parseDouble((String) md.getMetaValue(bean.getMetadataForGroupingName()));
					
					if (components[components.length-1].equals("1.dat")) {
						refMetadataValue = currentMetadataValue;
					} else if (currentMetadataValue > refMetadataValue + bean.getMetadataForGroupingDelta()) {
						// start a new group
						int position = file_list_in.indexOf(path);
						rep_1st.add(position);
						refMetadataValue = currentMetadataValue;
					} else {
						// no nothing
					}
				}
			}
		}
		
		// add the very last path to rep_1st
		rep_1st.add(file_list_in.size());
		
		for (int grp_ind = 0 ; grp_ind < rep_1st.size()-1 ; grp_ind++) {
			List<File> files = file_list_in.subList(rep_1st.get(grp_ind), rep_1st.get(grp_ind+1));
			//logger.debug("group " + grp_ind + " : " + files.stream().map(File::getAbsolutePath).collect(Collectors.joining(", ")));
		
			//if group contains just one file: ignore and go to the next one
			if (files.size() == 1) {
				logger.debug("Orphan file " + files.get(0).getAbsolutePath() + " detected. Ignoring");
				continue;
			}
			//we will be needing several datasets now
			//for now we will assume that the energy is constant across the files in the current group
			B18AverageData data = new B18AverageData();
			
			//in case all files do not have the same number of rows, use the minimal number of rows
			int nrows_min = Integer.MAX_VALUE;
			int nrows_max = Integer.MIN_VALUE;
			
			for (int i = 0 ; i < files.size() ; ++i) {
				File file = files.get(i);
				//get the number of elements in the energy array
				IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
				Dataset energy = DatasetUtils.convertToDataset(dh.getDataset(B18DataType.getEnergyIndex()));
				int irows = energy.getSize();
				nrows_min = Math.min(nrows_min, irows);
				nrows_max = Math.max(nrows_max, irows);
			}
			
			
			if (nrows_min != nrows_max) {
				logger.warn("files in group do not have the same number of rows: calculating averages on minimum number of rows");
			}
		
			if (bean.getInterpolationType() == B18InterpolationType.NONE) {
				// NO interpolation
				logger.debug("No interpolation used");
				
				List<Integer> indices = new ArrayList<>();
				indices.add(B18DataType.getEnergyIndex());
				indices.addAll(IntStream.of(bean.getDataType().getDataIndices(context, file_list_in.get(0))).boxed().collect(Collectors.toList()));
				
				data.allData = null;
			
				for (File file : files) {
					Dataset[] tempData = new Dataset[indices.size()];
					IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
					int counter = 0;
					for (int index : indices) {
						if (index < 0)
							index += dh.size();
						// loading datasets by index could be a bit dangerous...
						tempData[counter++] = DatasetUtils.convertToDataset(dh.getDataset(index).getSlice(null, new int[]{nrows_min}, null));
					}
					if (file == files.get(0)) {
						// first file
						data.allData = tempData;
					} else {
						for (int i = 0 ; i < tempData.length ; i++) {
							data.allData[i].iadd(tempData[i]);
						}
					}
				}
				for (Dataset tempData : data.allData) {
					tempData.idivide(files.size());
				}
			} else {
				logger.debug("Interpolation used: " + bean.getInterpolationType().toString());
				// interpolation mode
				double[] enemin_arr = new double[files.size()]; 
				double[] enemax_arr = new double[files.size()]; 
			
				// identify which energy dataset we should be using
				for (int i = 0 ; i < files.size() ; ++i) {
					File file = files.get(i);
					//get the number of elements in the energy array
					IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
					Dataset energy = DatasetUtils.convertToDataset(dh.getDataset(B18DataType.getEnergyIndex()).getSlice(null, new int[]{nrows_min}, null));
					// 
					double enemin = energy.getDouble(0);
					double enemax = energy.getDouble(-1);
					enemin_arr[i] = enemin;
					enemax_arr[i] = enemax;
				}
				
				double enemin_min = DoubleStream.of(enemin_arr).min().getAsDouble();
				double enemax_max = DoubleStream.of(enemax_arr).max().getAsDouble();

				Dataset xarr = null; /* energy to be used for interpolation */
				
				for (int i = 0 ; i < files.size() ; ++i) {
					File file = files.get(i);
					if (enemin_arr[i] < enemin_min + SHORT_RANGE && enemax_arr[i] > enemax_max - SHORT_RANGE) {
						// good energy array found
						IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
						xarr = DatasetUtils.convertToDataset(dh.getDataset(B18DataType.getEnergyIndex()).getSlice(null, new int[]{nrows_min}, null));
						break;
					}
				}
				
				if (xarr == null) 
					throw new Exception("Could not find a suitable energy array for interpolation");
				
				int[] indices = bean.getDataType().getDataIndices(context, file_list_in.get(0));
			
				data.allData = new Dataset[1 + indices.length];
				data.allData[0] = xarr;
				
				for (File file : files) {
					Dataset[] tempData = new Dataset[indices.length];
					IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
					Dataset energy_old = DatasetUtils.convertToDataset(dh.getDataset(B18DataType.getEnergyIndex()).getSlice(null, new int[]{nrows_min}, null));
					int counter = 0;
					for (int index : indices) {
						if (index < 0)
							index += dh.size();
						Dataset tempDataset = DatasetUtils.convertToDataset(dh.getDataset(index).getSlice(null, new int[]{nrows_min}, null));
						if (bean.getInterpolationType() == B18InterpolationType.LINEAR) {
							tempData[counter++] = Interpolation1D.linearInterpolation(energy_old, tempDataset, xarr);
						} else if (bean.getInterpolationType() == B18InterpolationType.SPLINE) {
							tempData[counter++] = Interpolation1D.splineInterpolation(energy_old, tempDataset, xarr);
						}
					}
					if (file == files.get(0)) {
						// first file
						System.arraycopy(tempData, 0, data.allData, 1, indices.length);
					} else {
						for (int i = 0 ; i < tempData.length ; i++) {
							data.allData[i + 1].iadd(tempData[i]);
						}
					}
				}
				for (Dataset tempDataset : Arrays.asList(data.allData).subList(1, data.allData.length)) {
					tempDataset.idivide(files.size());
				}
				
				
				
			}
			//ok time to write these datasets to file
			writeFile(files.get(0), context, data);
		}


		
		
	}

	private void writeFile(File groupFirstFile, IConversionContext context, B18AverageData data) throws Exception {
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		
		final File file;
		final String name = getFileNameNoExtension(groupFirstFile);
		file = new File(context.getOutputPath()+ File.separator +name+"_avg.dat");
		
		final StringBuilder contents = new StringBuilder();
        writeData(contents, data, context);

        if (file.exists()) {
        	file.delete();
        } else {
        	file.getParentFile().mkdirs();   	
        }
    	file.createNewFile();

        write(file, contents.toString(), "US-ASCII");
	}

	private static void write(final File file, final String text, final String encoding) throws Exception {
		try (
			OutputStream out = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
			BufferedWriter b = new BufferedWriter(writer);
		) {
			b.write(text.toCharArray());
		}
	}

	private static void writeData(StringBuilder contents, B18AverageData data, IConversionContext context) {
		contents.append("# ");
		StringJoiner header = new StringJoiner("\t");
		for (Dataset tempData : data.allData) {
			header.add(cleanDatasetName(tempData.getName()));
		}
		contents.append(header.toString());
		contents.append("\r\n"); // Intentionally windows.

		for (int i = 0 ; i < data.allData[0].getSize() ; i++) {
			StringJoiner dataLine = new StringJoiner("\t");
			for (Dataset tempData : data.allData) {
				dataLine.add(Double.toString(tempData.getDouble(i)));
			}
			contents.append(dataLine.toString());
			contents.append("\r\n"); // Intentionally windows.
		}
	}
	
	private static String cleanDatasetName(String dirtyName) {
		int last_bracket = dirtyName.lastIndexOf('[');
		return dirtyName.substring(0, last_bracket);
	}
}
