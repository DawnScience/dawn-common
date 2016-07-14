package org.dawnsci.conversion.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class B18AverageConverter extends AbstractConversion {

	private static final Logger logger = LoggerFactory.getLogger(B18AverageConverter.class);

	private static class B18AverageData {
		public Dataset qexafs_energy;
		/*public Dataset time;
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
		FLUORESCENCE("Fluorescence", new int[]{1,2,3,4,5,-2,-1});
		
		private final String type;
		private final int[] dataIndices;
		
			
		B18DataType(String type, int[] dataIndices) {
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
		
		public int[] getDataIndices() {
			return dataIndices;
		}
		
	}
	
	public static final class ConversionInfoBean {
		
		B18DataType dataType = B18DataType.TRANSMISSION;
		
		public B18DataType getDataType() {
			return dataType;
		}
		public void setDataType(B18DataType dataType) {
			this.dataType = dataType;
		}
		
		
		
	}
	
	public B18AverageConverter(IConversionContext context) {
		super(context);
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		// this method needs an implementation but it won't actually be used as we are overriding process in which everything is going to happen
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		final ConversionInfoBean bean = (ConversionInfoBean) context.getUserObject();
		
		List<File> file_list_in = new ArrayList<>();
		List<Integer> rep_1st = new ArrayList<>();
		final List<String> filePaths = context.getFilePaths();
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
				if (components[components.length-1].equals("1.dat")) {
					int position = file_list_in.indexOf(path);
					rep_1st.add(position);
				}
			}
		}
		
		// add the very last path to rep_1st
		rep_1st.add(file_list_in.size());
		
		for (int grp_ind = 0 ; grp_ind < rep_1st.size()-1 ; grp_ind++) {
			List<File> files = file_list_in.subList(rep_1st.get(grp_ind), rep_1st.get(grp_ind+1));
			logger.debug("group " + grp_ind + " : " + files.stream().map(File::getAbsolutePath).collect(Collectors.joining(", ")));
		
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
			for (File file : files) {
				//get the number of elements in the energy array
				IDataHolder dh = LoaderFactory.getData(file.getAbsolutePath());
				int irows = dh.getDataset(B18DataType.getEnergyIndex()).getSize();
				nrows_min = Math.min(nrows_min, irows);
				nrows_max = Math.max(nrows_max, irows);
			}
			
			if (nrows_min != nrows_max) {
				logger.warn("files in group do not have the same number of rows: calculating averages on minimum number of rows");
			}
		
			IDataHolder dh = LoaderFactory.getData(files.get(0).getAbsolutePath());
			
			// loading datasets by index could be a bit dangerous...
			data.qexafs_energy = DatasetUtils.convertToDataset(dh.getDataset(B18DataType.getEnergyIndex()).getSlice(null, new int[]{nrows_min}, null));
			logger.debug("qexafs_energy name: " + data.qexafs_energy.getName());
			
			int[] indices = bean.getDataType().getDataIndices();
			
			data.allData = null;
			
			for (File file : files) {
				Dataset[] tempData = new Dataset[indices.length];
				dh = LoaderFactory.getData(file.getAbsolutePath());
				int counter = 0;
				for (int index : indices) {
					if (index < 0)
						index += dh.size();
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
			
			//ok time to write these datasets to file
			writeFile(files.get(0), context, data);
		}


		
		logger.debug("Our datasets: " + context.getDatasetNames());
		
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
		header.add(cleanDatasetName(data.qexafs_energy.getName()));
		for (Dataset tempData : data.allData) {
			header.add(cleanDatasetName(tempData.getName()));
		}
		contents.append(header.toString());
		contents.append("\r\n"); // Intentionally windows.

		for (int i = 0 ; i < data.qexafs_energy.getSize() ; i++) {
			StringJoiner dataLine = new StringJoiner("\t");
			dataLine.add(Double.toString(data.qexafs_energy.getDouble(i)));
			for (Dataset tempData : data.allData) {
				dataLine.add(Double.toString(tempData.getDouble(i)));
			}
			contents.append(dataLine.toString());
			contents.append("\r\n"); // Intentionally windows.
		}
	}
	
	private static String cleanDatasetName(String dirtyName) {
		return dirtyName.substring(0, dirtyName.length() - 3);
	}
}
