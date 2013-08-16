package org.dawnsci.conversion.converters;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ncsa.hdf.object.Dataset;

import org.apache.commons.lang.ArrayUtils;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IErrorDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class CustomNCDConverter extends AbstractConversion  {

	private static final Logger logger = LoggerFactory.getLogger(CustomNCDConverter.class);
	private static final String DEFAULT_AXIS_NAME = "x";
	private static final String DEFAULT_COLUMN_NAME = "Column_";
	private static final String DEFAULT_ERRORS_COLUMN_NAME = "Error_";
	private static final String DEFAULT_TITLE_NODE = "/entry1/title";
	private static final String DEFAULT_SCAN_COMMAND_NODE = "/entry1/scan_command";

	public CustomNCDConverter(IConversionContext context) {
		super(context);
		final File dir = new File(context.getOutputPath());
		dir.mkdirs();
	}

	@Override
	protected void convert(AbstractDataset slice) {
		//we do our convert elsewhere
	}
	
	@Override
	protected void iterate(final ILazyDataset         lz, 
            final String               nameFrag,
            final IConversionContext   context) throws Exception {
		
		String selFilePath = context.getSelectedConversionFile().getAbsolutePath();
		IHierarchicalDataFile hdf5Reader = HierarchicalDataFactory.getReader(selFilePath);
		
		//get the x axis if required
		IDataset axis = null;
		if (context.getAxisDatasetName() != null) {
			axis = getAxis(context.getAxisDatasetName(), context.getSelectedConversionFile());
		}
		
		//Set up position iterator (final 2 dimensions saved in a single file
		int[] stop = lz.getShape();
		boolean hasErrors = (lz.getLazyErrors() != null ? true : false);
		int iterDim;
		int[] cutAxes;
		if (stop.length == 1 || hasErrors) {
			iterDim = lz.getRank() - 1;
			cutAxes = new int[] {lz.getRank() - 1};
		} else {
			iterDim = lz.getRank() - 2;
			cutAxes = new int[] {lz.getRank() - 2, lz.getRank() - 1};
		}
		
		PositionIterator iterator = new PositionIterator(stop, cutAxes);
		
		for (int i = 0 ; i < iterDim ; i++) {
			stop[i] = 0;
		}
		
		int[] step = new int[stop.length];
		for (int i = 0 ; i < step.length; i++) {
			step[i] = 1;
		}
		
		//Make file header and column names
		final String separator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("# Diamond Light Source Ltd.");
		sb.append(separator);
		sb.append("# Non Crystalline Diffraction Results Export File");
		sb.append(separator);
		sb.append("# Data extracted from file: " + selFilePath);
		sb.append(separator);
		sb.append("# Dataset name: " + nameFrag);
		
		try {
			Dataset titleData = (Dataset) hdf5Reader.getData(DEFAULT_TITLE_NODE);
			String[] str = (String[]) titleData.getData();
			if (str.length > 0) {
				String title = str[0];
				sb.append(separator);
				sb.append("# Title: " + title);
			}
		} catch (Exception e) {
			logger.info("Default title node {} was not found", DEFAULT_TITLE_NODE);
		}
		try {
			Dataset scanCommandData = (Dataset)hdf5Reader.getData(DEFAULT_SCAN_COMMAND_NODE);
			String[] str = (String[])scanCommandData.getData();
			if (str.length > 0) {
				String scanCommand = str[0];
				sb.append(separator);
				sb.append("# Scan command: " + scanCommand);
			}
		} catch (Exception e) {
			logger.info("Default scan command node {} was not found", DEFAULT_SCAN_COMMAND_NODE);
		}
		
		List<String> headings = new ArrayList<String>();
		
		if (axis != null) {
			String axisName = axis.getName();
			headings.add(" ".concat(axisName));
		}
		
		if (stop.length == 1 || hasErrors) {
			headings.add(DEFAULT_COLUMN_NAME + "0");
			if (hasErrors) {
				headings.add(DEFAULT_ERRORS_COLUMN_NAME + "0");
			}
		} else {
			for (int i = 0; i< stop[iterDim]; i++) {
				headings.add(DEFAULT_COLUMN_NAME + i);
			}
		}
		
		//Iterate over lazy dataset and save
		DataHolder dh = new  DataHolder();
		for (int i = 0; iterator.hasNext();) {
			
			if (i == 0) {
				dh = new  DataHolder();
			}
			
			int[] start = iterator.getPos();
			
			for (int j = 0 ; j < iterDim ; j++) {
				stop[j] = start[j]+1;
			}
			
			Slice[] slices = Slice.convertToSlice(start, stop, step);
			IDataset data = lz.getSlice(slices);
			data = (IDataset)data.squeeze();
			
			AbstractDataset errors = null;
			if (hasErrors) {
				errors = DatasetUtils.cast((AbstractDataset) ((IErrorDataset) data).getError(),
						((AbstractDataset)data).getDtype());
			}
			
			String nameSuffix = "";
			
			if (!(Arrays.equals(lz.getShape(), data.getShape()))) {
				nameSuffix = nameStringFromSliceArray(iterDim, slices);
			}
			
			//Check data suitable then concatenate axis with data
			if (data.getRank() == 1) {
				data.setShape(1,data.getShape()[0]);
				if (hasErrors) {
					errors.setShape(1,errors.getShape()[0]);
				}
			}
			
			IDataset[] columns = new IDataset[] {DatasetUtils.transpose(data, null)};
			if (axis != null) {
				columns = (IDataset[]) ArrayUtils.addAll(new IDataset[]{axis}, columns);
			}
			if (hasErrors) {
				columns = (IDataset[]) ArrayUtils.addAll(columns, new IDataset[]{DatasetUtils.transpose(errors, null)});
			}
			data = DatasetUtils.concatenate(columns, 1);
			
			String pathToFolder = context.getOutputPath();
			String fileName = buildFileName(context.getSelectedConversionFile().getAbsolutePath(),nameFrag);
			String fullName = pathToFolder + File.separator + fileName + nameSuffix +".dat";
			
			ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(fullName);
			data.setName(nameFrag + nameStringFromSliceArray(iterDim, slices));
			dh.addDataset(data.getName(), data);
			saver.setHeader(sb.toString());
			saver.setHeadings(headings);
			saver.saveFile(dh);
			
			if (context.getMonitor() != null) {
				IMonitor mon = context.getMonitor();
				if (mon.isCancelled()) {
					return;
				}
				context.getMonitor().subTask(fileName + nameSuffix);
			}
		}
		
		if (context.getMonitor() != null) {
			IMonitor mon = context.getMonitor();
			mon.worked(1);
		}
	}
	
	private String getAxisDatasetName(String axisDatasetName) {
		
		if (!(axisDatasetName.contains("/"))) {
			return DEFAULT_AXIS_NAME;
		} else {
			int pos = axisDatasetName.lastIndexOf("/");
			return axisDatasetName.substring(pos+1, axisDatasetName.length());
		}
	}

	private String nameStringFromSliceArray(int iterDim, Slice[] slices) {

		StringBuilder t = new StringBuilder();
		t.append('_');
		for (int idx = 0; idx < iterDim; idx++) {
			Slice s = slices[idx];
			t.append(s != null ? s.toString() : "");
			t.append('_');
		}
		t.deleteCharAt(t.length()-1);
		return t.toString();
	}
	
	private IDataset getAxis(String datasetName, File path) {
		
		IDataset data = null;
		try {
			HDF5File tree = new HDF5Loader(path.getAbsolutePath()).loadTree();
			HDF5Node node = tree.findNodeLink(datasetName).getDestination();
			data = ((HDF5Dataset) node).getDataset().getSlice();
			//expand so the concatenation works later
			data.setShape(data.getShape()[0],1);
			
			if (node.containsAttribute("unit")) {
				String qaxisUnit = node.getAttribute("unit").getFirstElement();
				data.setName(getAxisDatasetName(datasetName).concat(" / ").concat(qaxisUnit));
			} else {
				data.setName(getAxisDatasetName(datasetName));
			}
		} catch (Exception e) {
			logger.warn("Couldn't get dataset: " + datasetName);
		}
		return data;
	}
	
	private String buildFileName(String pathToOriginal, String datasetName) {
		
		String name = new File(pathToOriginal).getName();
		int index = name.lastIndexOf('.');
		name = name.substring(0, index);
		
		if (datasetName.contains("processing")) {
			String trimmed = datasetName.replaceAll("(.*_processing/)", "");
			trimmed = trimmed.replaceAll("/data", "");
			name = name + "_" + trimmed;
		}
		return name;
	}
}
