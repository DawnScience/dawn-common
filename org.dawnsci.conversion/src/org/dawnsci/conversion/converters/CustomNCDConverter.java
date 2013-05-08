package org.dawnsci.conversion.converters;


import java.io.File;
import java.util.ArrayList;
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

	public CustomNCDConverter(IConversionContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void convert(AbstractDataset slice) {
		//we do our convert elsewhere
	}
	
	@Override
	protected void iterate(final ILazyDataset         lz, 
            final String               nameFrag,
            final IConversionContext   context) throws Exception {
		
		AbstractDataset axis = null;
		
		if (context.getAxisDatasetName() != null) {
			axis = getAxis(context.getAxisDatasetName(), context.getSelectedConversionFile());
		}
		
		int[] stop = lz.getShape();
		
		int[] cutAxes = new int[] {lz.getRank()-2, lz.getRank()-1};
		
		PositionIterator iterator = new PositionIterator(stop, cutAxes);
		
		for (int i = 0 ; i < stop.length-2 ; i++) {
			stop[i] = 0;
		}
		
		int[] step = new int[stop.length];
		for (int i = 0 ; i < step.length; i++) {
			step[i] = 1;
		}
		
		DataHolder dh = new  DataHolder();
		
		String header = "Data extracted from file: " + nameFrag;
		
		List<String> headings = new ArrayList<String>(stop[lz.getRank()-2]);
		
		if (axis != null) headings.add("x");
		
		for (int i = 0; i< stop[lz.getRank()-2]; i++) {
			headings.add("Data" +i);
		}
		
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
			
			if (data.getRank() == 1) {
				data.setShape(1,data.getShape()[0]);
			}
			
			if (axis != null) {
				data = DatasetUtils.concatenate(new IDataset[]{axis,data.transpose(null)}, 1);
						
			}
			
			String pathToFolder = context.getOutputPath();
			
			String fileName = buildFileName(context.getSelectedConversionFile().getAbsolutePath(),nameFrag);
			
			data.setName(nameFrag + stringFromSliceArray(slices));
			dh.addDataset(data.getName(), data);
			ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(pathToFolder + "/" + fileName +stringFromSliceArray(slices) +".dat");
			
			saver.setHeader(header);
			saver.setHeadings(headings);
			saver.saveFile(dh);
			
			if (context.getMonitor() != null) {
				IMonitor mon = context.getMonitor();
				if (mon.isCancelled()) return;
			}
		}
	}
	
	private String stringFromSliceArray(Slice[] slices) {
		StringBuilder t = new StringBuilder();
		t.append('[');
		for (int idx = 0; idx < slices.length; idx++) {
			Slice s = slices[idx];
			t.append(s != null ? s.toString() : ':');
			t.append(',');
		}
		t.deleteCharAt(t.length()-1);
		t.append(']');
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
