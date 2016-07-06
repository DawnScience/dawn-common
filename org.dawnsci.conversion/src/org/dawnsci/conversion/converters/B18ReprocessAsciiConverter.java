package org.dawnsci.conversion.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public class B18ReprocessAsciiConverter extends AbstractConversion {

	public B18ReprocessAsciiConverter(IConversionContext context) {
		super(context);
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		// this method needs an implementation but it won't actually be used as we are overriding process in which everything is going to happen
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		final List<String> filePaths = context.getFilePaths();
		for (String filePathRegEx : filePaths) {
			final List<File> paths = expand(filePathRegEx);
			for (File path : paths) {
				//convert file
				//get new filename
				writeFile(path, context);
			}
		}
		
	}

	private void writeFile(File selectedConversionFile, IConversionContext context) throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		
		final File file;
		if (context.getFilePaths().size()>1) {
			final String name = getFileNameNoExtension(selectedConversionFile);
			file = new File(context.getOutputPath()+ File.separator +name+"_rep.dat");

		} else {
			final String filePath = context.getOutputPath();
			file = new File(filePath);
			if (file.isDirectory()) throw new Exception("B18ReprocessAscii must be written with an output file!");
		}
	
		// read file into a list
		List<String> old_contents = FileUtils.readFileAsList(selectedConversionFile);
		List<String> new_contents = null; 
		
		// go through the file and look for the line with the column headers
		List<String> header = new ArrayList<>();
		
		int index = 0;
		for (String line : old_contents) {
			//check for lines starting with a '#'
			//abort when encountering a line that does not
			if (line.trim().charAt(0) == '#') {
				header.add(line);
			} else {
				break;
			}
			index++;
		}
		
		//there should be at least one line in the header, otherwise the file looks fishy
		if (header.size() < 1) {
			throw new Exception(selectedConversionFile.getAbsolutePath() + " does not have a header!");
		}
		
		// last column of the header should contain the column names
		
		if (old_contents.get(index).trim().split("\\s+").length < 14) {
			// if there are less than 14 columns, the file can be copied as-is, just like in the original python script,
			// but I will make an exact copy, including the complete header and column labels
			new_contents = old_contents;
		} else {
			new_contents = new ArrayList<>();
			// add the header, except the column headers 
			new_contents.addAll(header.subList(0, index-1));
			// add new column headers
			new_contents.add("#energy\tln(i0/it)\tln(it/iref)\tff/i0");
			// now add the data
			for (String line : old_contents.subList(index, old_contents.size())) {
				String[] splitted = line.trim().split("\\s+");
				//staying really close to the original code here...
				double en = Double.parseDouble(splitted[0]);
				double i0 = Double.parseDouble(splitted[2]);
				double it = Double.parseDouble(splitted[3]);
				double iref = Double.parseDouble(splitted[4]);
				double ff = Double.parseDouble(splitted[splitted.length-2]);
				double a = Math.log(i0/it);
				double b = Math.log(it/iref);
				double c = ff/i0;
				if (!Double.isFinite(a))
					a = 0.0;
				if (!Double.isFinite(b))
					b = 0.0;
				if (!Double.isFinite(c))
					c = 0.0;
				new_contents.add(StringUtils.join(new Double[]{en, a, b, c}, '\t'));
			}
		}
		
        if (file.exists()) {
        	file.delete();
        } else {
        	file.getParentFile().mkdirs();   	
        }
    	file.createNewFile();

    	FileUtils.write(file, new_contents, "\r\n");
	}
}
