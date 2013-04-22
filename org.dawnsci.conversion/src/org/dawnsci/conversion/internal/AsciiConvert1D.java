package org.dawnsci.conversion.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.dawb.common.services.conversion.IConversionContext;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * This class converts neuxs data sets to .dat file syntax, @see  uk.ac.diamond.scisoft.analysis.io.DatLoader
 * 
 * @see org.dawnsci.conversion.AsciiConvertTest
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
		
		final String filePath = context.getOutputPath();
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
        writeData(contents, sortedData, maxSize, context);

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


	private static void writeData(final StringBuilder        contents,
										final Map<String, ? extends IDataset> sortedData,
										final int                  maxSize,
										final IConversionContext   context) {

		final ConversionInfoBean bean = (ConversionInfoBean)context.getUserObject();
		
		contents.append("# ");
		for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {

			String name = it.next();
			if (bean!=null && bean.getAlernativeNames()!=null && bean.getAlernativeNames().containsKey(name)) {
				name = bean.getAlernativeNames().get(name);
			}
			contents.append(name);
			if (it.hasNext()) contents.append("\t");
		}
		contents.append("\r\n"); // Intentionally windows.

		NumberFormat format = null;
		if (bean!=null && bean.getNumberFormat()!=null) {
			format = new DecimalFormat(bean.getNumberFormat());
		}
		for (int i = 0; i < maxSize; i++) {
			for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {

				final String name = it.next();

				final IDataset set = sortedData.get(name);
				final String     value;
				
				if (format!=null) {
					value = format.format(set.getDouble(i));
				} else {
					value = (i<set.getSize()) ? set.getString(i) : " ";
				}

				
				contents.append(value);
				if (it.hasNext()) contents.append("\t");

				if (context.getMonitor()!=null && i>=(maxSize-1))	context.getMonitor().worked(1);

			}
			contents.append("\r\n"); // Intentionally windows because works on unix too.
		}
	}


	public static final class ConversionInfoBean {
		private String numberFormat;
		private Map<String,String> alernativeNames;
		public String getNumberFormat() {
			return numberFormat;
		}
		public void setNumberFormat(String numberFormat) {
			this.numberFormat = numberFormat;
		}
		public Map<String, String> getAlernativeNames() {
			return alernativeNames;
		}
		public void setAlernativeNames(Map<String, String> alernativeNames) {
			this.alernativeNames = alernativeNames;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((alernativeNames == null) ? 0 : alernativeNames
							.hashCode());
			result = prime * result
					+ ((numberFormat == null) ? 0 : numberFormat.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConversionInfoBean other = (ConversionInfoBean) obj;
			if (alernativeNames == null) {
				if (other.alernativeNames != null)
					return false;
			} else if (!alernativeNames.equals(other.alernativeNames))
				return false;
			if (numberFormat == null) {
				if (other.numberFormat != null)
					return false;
			} else if (!numberFormat.equals(other.numberFormat))
				return false;
			return true;
		}
	}

}
