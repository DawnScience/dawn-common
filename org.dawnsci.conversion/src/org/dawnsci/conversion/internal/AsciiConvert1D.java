package org.dawnsci.conversion.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.dawb.common.services.IConversionContext;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * There is no testing for this class now. TODO Add testing.
 * @author fcp94556
 *
 */
public class AsciiConvert1D extends AbstractConversion {

	private TreeMap<String, IDataset> sortedData;

	public AsciiConvert1D(IConversionContext context) throws Exception {
		super(context);
		if (context.getSliceDimensions()!=null) throw new Exception("Data converted to ascii with AsciiConvert1D, cannot be mixed with slicing!");
		this.sortedData = new TreeMap<String, IDataset>();
	}

	@Override
	protected void convert(AbstractDataset slice) {
        sortedData.put(slice.getName(), slice);
	}
	
	@Override
	public void close(IConversionContext context) throws Exception {
		
		final String filePath = context.getFilePath();
		final File   file     = new File(filePath);
		if (file.isDirectory()) throw new Exception("AsciiConvert1D must be written with an output file!");
		
	    int maxSize = Integer.MIN_VALUE;
		for (String name : sortedData.keySet()) {
			final IDataset set = sortedData.get(name);
			if (set.getShape()==null)     continue;
			if (set.getShape().length!=1) continue;
			maxSize = Math.max(maxSize, set.getSize());
		}

		final StringBuilder contents = new StringBuilder();
        get1DDataSetCVS(contents, sortedData, maxSize, context.getMonitor());

        if (file.exists()) {
        	file.delete();
        } else {
        	file.getParentFile().mkdirs();   	
        }
    	file.createNewFile();

        write(file, contents.toString(), "US-ASCII");
	}
	
	/**
	 * @param file
	 * @param text
	 * @param encoding
	 * @throws Exception
	 */
	public static void write(final File file, final String text, String encoding) throws Exception {
		BufferedWriter b = null;
		try {
			final OutputStream out = new FileOutputStream(file);
			final OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
			b = new BufferedWriter(writer);
			b.write(text.toCharArray());
		} finally {
			if (b != null) {
				b.close();
			}
		}
	}


	private static void get1DDataSetCVS(final StringBuilder        contents,
										final Map<String, ? extends IDataset> sortedData,
										final int                  maxSize,
										final IMonitor             monitor) {

		for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {

			final String name = it.next();
			contents.append("\"");
			contents.append(name);
			contents.append("\"");
			if (it.hasNext()) contents.append(",");
		}
		contents.append("\r\n"); // Intentionally windows.

		for (int i = 0; i < maxSize; i++) {
			for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {

				final String name = it.next();

				final IDataset set = sortedData.get(name);
				final String     value = (i<set.getSize()) ? ((IDataset)set).getString(i) : " ";
				contents.append(value);
				if (it.hasNext()) contents.append(",");

				if (monitor!=null && i>=(maxSize-1))	monitor.worked(1);

			}
			contents.append("\r\n"); // Intentionally windows.
		}
	}


}
