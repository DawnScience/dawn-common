package org.dawnsci.conversion.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B18AverageConverter extends AbstractConversion {

	private static final Logger logger = LoggerFactory.getLogger(B18AverageConverter.class);

	private static class B18AverageAsciiData {
		public Dataset qexafs_energy;
		public Dataset time;
		public Dataset I0;
		public Dataset It;
		public Dataset Iref;
		public Dataset lnI0It;
		public Dataset lnItIref;
		public Dataset QexafsFFI0; // QexafsFFI0 or FF...
		
		public static String[] getNames() {
			return new String[]{"qexafs_energy", "time", "I0", "It", "Iref", "lnI0It", "lnItIref", "QexafsFFI0"};
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
			B18AverageAsciiData data = new B18AverageAsciiData();
			
			//in case all files do not have the same number of rows, use the minimal number of rows
			int nrows_min = Integer.MAX_VALUE;
			int nrows_max = Integer.MIN_VALUE;
			for (File file : files) {
				//get the number of elements in the energy array
				int irows = LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "qexafs_energy", null).getSize();
				nrows_min = Math.min(nrows_min, irows);
				nrows_max = Math.max(nrows_max, irows);
			}
			
			if (nrows_min != nrows_max) {
				logger.warn("files in group do not have the same number of rows: calculating averages on minimum number of rows");
			}
			
			data.qexafs_energy = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(files.get(0).getAbsolutePath(), "qexafs_energy", null).getSlice(null, new int[]{nrows_min}, null));
			
			for (File file : files) {
				Dataset timeTemp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "time", null).getSlice(null, new int[]{nrows_min}, null));
				Dataset I0Temp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "I0", null).getSlice(null, new int[]{nrows_min}, null));
				Dataset ItTemp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "It", null).getSlice(null, new int[]{nrows_min}, null));
				Dataset IrefTemp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "Iref", null).getSlice(null, new int[]{nrows_min}, null));
				Dataset lnI0ItTemp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "lnI0It", null).getSlice(null, new int[]{nrows_min}, null));
				Dataset lnItIrefTemp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "lnItIref", null).getSlice(null, new int[]{nrows_min}, null));
				Dataset QexafsFFI0Temp = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(file.getAbsolutePath(), "QexafsFFI0", null).getSlice(null, new int[]{nrows_min}, null));
				if (file == files.get(0)) {
					// first file
					data.time = timeTemp;
					data.I0 = I0Temp;
					data.It = ItTemp;
					data.Iref = IrefTemp;
					data.lnI0It = lnI0ItTemp;
					data.lnItIref = lnItIrefTemp;
					data.QexafsFFI0 = QexafsFFI0Temp;
				} else {
					data.time.iadd(timeTemp);
					data.I0.iadd(I0Temp);
					data.It.iadd(ItTemp);
					data.Iref.iadd(IrefTemp);
					data.lnI0It.iadd(lnI0ItTemp);
					data.lnItIref.iadd(lnItIrefTemp);
					data.QexafsFFI0.iadd(QexafsFFI0Temp);
				}
			}
			data.time.idivide(files.size());
			data.I0.idivide(files.size());
			data.It.idivide(files.size());
			data.Iref.idivide(files.size());
			data.lnI0It.idivide(files.size());
			data.lnItIref.idivide(files.size());
			data.QexafsFFI0.idivide(files.size());
			
			//ok time to write these datasets to file
			writeFile(files.get(0), context, data);
		}


		
		logger.debug("Our datasets: " + context.getDatasetNames());
		
	}

	private void writeFile(File groupFirstFile, IConversionContext context, B18AverageAsciiData data) throws Exception {
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

	private static void writeData(StringBuilder contents, B18AverageAsciiData data, IConversionContext context) {
		contents.append("# ");
		contents.append(String.join("\t", B18AverageAsciiData.getNames()));
		contents.append("\r\n"); // Intentionally windows.

		for (int i = 0 ; i < data.qexafs_energy.getSize() ; i++) {
			contents.append(String.join("\t",
					Double.toString(data.qexafs_energy.getDouble(i)),
					Double.toString(data.time.getDouble(i)),
					Double.toString(data.I0.getDouble(i)),
					Double.toString(data.It.getDouble(i)),
					Double.toString(data.Iref.getDouble(i)),
					Double.toString(data.lnI0It.getDouble(i)),
					Double.toString(data.lnItIref.getDouble(i)),
					Double.toString(data.QexafsFFI0.getDouble(i))
				));
			contents.append("\r\n"); // Intentionally windows.
		}
		
	}
		
}
