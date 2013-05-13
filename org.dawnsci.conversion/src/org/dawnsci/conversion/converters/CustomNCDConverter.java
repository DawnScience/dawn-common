package org.dawnsci.conversion.converters;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class CustomNCDConverter extends AbstractConversion  {

	private final static Logger logger = LoggerFactory.getLogger(CustomNCDConverter.class);
	private final static String DEFAULT_AXIS_NAME = "x";
	private final static String DEFAULT_COLUMN_NAME = "Column_";

	public CustomNCDConverter(IConversionContext context) {
		super(context);
		;
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
		
		// Remove 1 dimensions
		lz.squeeze();
		
		//get the x axis if required
		AbstractDataset axis = null;
		if (context.getAxisDatasetName() != null) {
			axis = getAxis(context.getAxisDatasetName(), context.getSelectedConversionFile());
		}
		
		//Set up position iterator (final 2 dimensions saved in a single file
		int[] stop = lz.getShape();
		int[] cutAxes;
		if (stop.length == 1) {
			cutAxes = new int[] {0};
		} else {
			cutAxes = new int[] {lz.getRank()-2, lz.getRank()-1};
		}
		
		PositionIterator iterator = new PositionIterator(stop, cutAxes);
		
		for (int i = 0 ; i < stop.length-2 ; i++) {
			stop[i] = 0;
		}
		
		int[] step = new int[stop.length];
		for (int i = 0 ; i < step.length; i++) {
			step[i] = 1;
		}
		
		//Make file header and column names
		final String separator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("#Diamond Light Source Ltd.");
		sb.append(separator);
		sb.append("#Non Crystalline Diffraction Results Export File");
		sb.append(separator);
		sb.append("#Data extracted from file: " + context.getSelectedConversionFile().getAbsolutePath());
		sb.append(separator);
		sb.append("#Dataset name: " + nameFrag);
		
		List<String> headings = new ArrayList<String>();
		
		if (axis != null) {
			String axisName = getAxisName(context.getAxisDatasetName());
			headings.add(axisName);
		}
		
		if (stop.length == 1) {
			headings.add(DEFAULT_COLUMN_NAME +"0");
		} else {
		for (int i = 0; i< stop[lz.getRank()-2]; i++) headings.add(DEFAULT_COLUMN_NAME +i);
		}
		
		//Iterate over lazy dataset and save
		DataHolder dh = new  DataHolder();
		for (int i = 0; iterator.hasNext();) {
			
			if (i == 0) {
				dh = new  DataHolder();
			}
			
			int[] start = iterator.getPos();
			
			for (int j = 0 ; j < start.length-2 ; j++) {
				stop[j] = start[j]+1;
			}
			
			Slice[] slices = Slice.convertToSlice(start, stop, step);
			AbstractDataset data = (AbstractDataset)lz.getSlice(slices);
			data = data.squeeze();
			String nameSuffix = "";
			
			if (!(Arrays.equals(lz.getShape(), data.getShape()))) {
				nameSuffix = nameStringFromSliceArray(slices);
			}
			
			//Check data suitable then concatenate axis with data
			if (data.getRank() == 1) {
				data.setShape(1,data.getShape()[0]);
			}
			
			if (axis != null) {
				data = DatasetUtils.concatenate(new IDataset[]{axis,data.transpose(null)}, 1);
			} else {
				data =data.transpose(null);
			}
			
			String pathToFolder = context.getOutputPath();
			String fileName = buildFileName(context.getSelectedConversionFile().getAbsolutePath(),nameFrag);
			String fullName = pathToFolder + File.separator + fileName + nameSuffix +".dat";
			
			ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(fullName);
			data.setName(nameFrag + nameStringFromSliceArray(slices));
			dh.addDataset(data.getName(), data);
			saver.setHeader(sb.toString());
			saver.setHeadings(headings);
			saver.saveFile(dh);
			
			if (context.getMonitor() != null) {
				IMonitor mon = context.getMonitor();
				if (mon.isCancelled()) return;
				context.getMonitor().subTask(fileName + nameSuffix);
			}
		}
		
		if (context.getMonitor() != null) {
			IMonitor mon = context.getMonitor();
			mon.worked(1);
		}
	}
	
	private String getAxisName(String axisDatasetName) {
		
		if (!(axisDatasetName.contains("/"))) {
			return DEFAULT_AXIS_NAME;
		} else {
			int pos = axisDatasetName.lastIndexOf("/");
			return axisDatasetName.substring(pos+1, axisDatasetName.length());
		}
	}

	private String nameStringFromSliceArray(Slice[] slices) {

		StringBuilder t = new StringBuilder();
		t.append('_');
		for (int idx = 0; idx < slices.length -2; idx++) {
			Slice s = slices[idx];
			t.append(s != null ? s.toString() : "");
			t.append('_');
		}
		t.deleteCharAt(t.length()-1);
		return t.toString();
	}
	
	private AbstractDataset getAxis(String datasetName, File path) {
		
		AbstractDataset data = null;
		try {
			data = LoaderFactory.getDataSet(path.getAbsolutePath(), datasetName, null);
			//expand so the concatenation works later
			data.setShape(data.getShape()[0],1);
		} catch (Exception e) {
			logger.warn("Couldn't get dataset: " + datasetName);
		}
		return data;
	}
	
	protected String buildFileName(String pathToOriginal, String datasetName) {
		
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
