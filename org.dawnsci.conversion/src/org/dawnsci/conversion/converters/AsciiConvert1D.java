/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

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

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * This class converts neuxs data sets to .dat file syntax, @see  uk.ac.diamond.scisoft.analysis.io.DatLoader
 * 
 * @see org.dawnsci.conversion.AsciiConvertTest
 * @author Matthew Gerring
 *
 */
public class AsciiConvert1D extends AbstractConversion {
	
	private TreeMap<String, IDataset> sortedData;

	public AsciiConvert1D(IConversionContext context) throws Exception {
		super(context);
		if (context.getSliceDimensions()!=null) throw new Exception("Data converted to ascii with AsciiConvert1D, cannot be mixed with slicing!");
		this.sortedData = new TreeMap<String, IDataset>();
	}

	private File selected = null;
	
	@Override
	protected void convert(IDataset slice) throws Exception {
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		if (selected==null || !selected.equals(context.getSelectedConversionFile())) {
			if (!sortedData.isEmpty() && selected!=null) {
				writeFile(selected, context);
			}
			
			// New sort with new input file.
			sortedData.clear();
			selected = context.getSelectedConversionFile();
		}
        sortedData.put(slice.getName(), slice.squeeze());
        if (context.getMonitor()!=null) context.getMonitor().worked(1);
	}
	
	@Override
	public void close(IConversionContext context) throws Exception {
		writeFile(selected, context);
	}
	
	private void writeFile(File selectedConversionFile, IConversionContext context) throws Exception {
		
		if (sortedData.isEmpty()) return;
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		
		final File   file;
		if (context.getFilePaths().size()>1) {
			final String name = getFileNameNoExtension(selectedConversionFile);
			file = new File(context.getOutputPath()+"/"+name+"."+getExtension());

		} else {
			final String filePath = context.getOutputPath();
			file     = new File(filePath);
			if (file.isDirectory()) throw new Exception("AsciiConvert1D must be written with an output file!");
		}
		
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
        sortedData.clear();
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


	private void writeData(final StringBuilder        contents,
							final Map<String, ? extends IDataset> sortedData,
							final int                  maxSize,
							final IConversionContext   context) {

		final ConversionInfoBean bean = (ConversionInfoBean)context.getUserObject();
		
		if (isDat()) contents.append("# ");
		for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {

			String name = it.next();
			if (bean!=null && bean.getAlernativeNames()!=null && bean.getAlernativeNames().containsKey(name)) {
				name = bean.getAlernativeNames().get(name);
			}
			if (isCsv()) contents.append("\"");
			contents.append(name);
			if (isCsv()) contents.append("\"");

			if (it.hasNext()) {
				if (isCsv()) contents.append(",");
				contents.append("\t");
			}
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
					value = (i<set.getSize()) ? set.getString(i) : "0";
				}

				
				contents.append(value);
				if (it.hasNext()) {
					if (isCsv()) contents.append(",");
					contents.append("\t");
				}

				if (context.getMonitor()!=null && i>=(maxSize-1))	context.getMonitor().worked(1);

			}
			if (context.getMonitor()!=null) context.getMonitor().worked(1);
			contents.append("\r\n"); // Intentionally windows because works on unix too.
		}
	}
	
	private String getExtension() {
		return isDat() ? "dat" : "csv";
	}
	
	protected boolean isCsv() {
		if (context.getUserObject()==null) return false;
		ConversionInfoBean bean  = (ConversionInfoBean)context.getUserObject();
		return "csv".equals(bean.getConversionType());
	}

	protected boolean isDat() {
		if (context.getUserObject()==null) return true;
		ConversionInfoBean bean  = (ConversionInfoBean)context.getUserObject();
		return "dat".equals(bean.getConversionType());
	}

	public static final class ConversionInfoBean {
	
		private String conversionType = "dat"; // dat or csv
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
			result = prime
					* result
					+ ((conversionType == null) ? 0 : conversionType.hashCode());
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
			if (conversionType == null) {
				if (other.conversionType != null)
					return false;
			} else if (!conversionType.equals(other.conversionType))
				return false;
			if (numberFormat == null) {
				if (other.numberFormat != null)
					return false;
			} else if (!numberFormat.equals(other.numberFormat))
				return false;
			return true;
		}
		public String getConversionType() {
			return conversionType;
		}
		public void setConversionType(String conversionType) {
			this.conversionType = conversionType;
		}
	}

}
