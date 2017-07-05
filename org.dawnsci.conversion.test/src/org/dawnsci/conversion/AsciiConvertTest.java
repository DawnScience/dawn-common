/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.conversion.converters.AsciiConvert1D;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.dawnsci.conversion.schemes.AsciiConvert1DScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.junit.Before;
import org.junit.Test;

import com.sun.xml.bind.v2.runtime.AssociationMap;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;


public class AsciiConvertTest {
	private static final IConversionScheme scheme = new AsciiConvert1DScheme();
	private String testfile = "MoKedge_1_15.nxs";
	
	@Before
	public void before() {
		LocalServiceManager.setLoaderService(new LoaderServiceImpl());
	}
	
	@Test
	public void testAsciiSimple() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(testfile);
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("testSimple", ".dat");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(scheme);
        context.setDatasetName("/entry1/counterTimer01/(Energy|I0|lnI0It|It)");
        
        service.process(context);
        
        final IDataHolder   dh    = LocalServiceManager.getLoaderService().getData(tmp.getAbsolutePath(),null);
        final List<String> names = Arrays.asList("/entry1/counterTimer01/Energy","/entry1/counterTimer01/I0","/entry1/counterTimer01/lnI0It","/entry1/counterTimer01/It");
        for (String name : names) {
            if (dh.getDataset(name)==null) throw new Exception("Missing dataset "+name);
		}
   	}
	
	@Test
	public void testAsciiCustomConfig() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(testfile);
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("testConfig", ".dat");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(scheme);
        context.setDatasetName("/entry1/counterTimer01/(Energy|I0|lnI0It|It)");
        
        // Set some custom data
        final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
        bean.setNumberFormat("#0.00");
        final Map<String,String> alternates = new HashMap<String,String>(4);
        alternates.put("/entry1/counterTimer01/Energy", "Energy");
        alternates.put("/entry1/counterTimer01/I0",     "I0");
        alternates.put("/entry1/counterTimer01/lnI0It", "lnI0It");
        alternates.put("/entry1/counterTimer01/It",     "It");
        bean.setAlernativeNames(alternates);
        context.setUserObject(bean);

        service.process(context);
        
        // Check rename worked
        final IDataHolder   dh    = LocalServiceManager.getLoaderService().getData(tmp.getAbsolutePath(),null);
        final List<String> names = Arrays.asList("Energy","I0","lnI0It","It");
        for (String name : names) {
            if (dh.getDataset(name)==null) throw new Exception("Missing dataset "+name);
		}
        
        // Check format worked
        final StringBuffer content = readFile(tmp);
        final String[] sa = content.toString().split("\n");
        final String firstDataLine = sa[1];
        if (!"6912.00\t134878.00\t2040284.00\t-2.72".equals(firstDataLine.trim())) {
        	throw new Exception("Unexpected format!");
        }
 	}

	@Test
	public void testAsciiCustomConfig2() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(testfile);
		
		final IConversionContext context = service.open(path);
		final File tmp = File.createTempFile("testConfig", ".csv");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(scheme);
        context.setDatasetName("/entry1/counterTimer01/(Energy|I0|lnI0It|It)");
        
        // Set some custom data
        final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
        bean.setNumberFormat("#0.00");
        bean.setConversionType("csv");
        final Map<String,String> alternates = new HashMap<String,String>(4);
        alternates.put("/entry1/counterTimer01/Energy", "Energy");
        alternates.put("/entry1/counterTimer01/I0",     "I0");
        alternates.put("/entry1/counterTimer01/lnI0It", "lnI0It");
        alternates.put("/entry1/counterTimer01/It",     "It");
        bean.setAlernativeNames(alternates);
        context.setUserObject(bean);

        service.process(context);
                
        // Check format worked
        final StringBuffer content = readFile(tmp);
        final String[] sa = content.toString().split("\n");
        final String firstDataLine = sa[1];
        if (!"6912.00,\t134878.00,\t2040284.00,\t-2.72".equals(firstDataLine.trim())) {
        	throw new Exception("Unexpected format!");
        }
 	}


	private String getTestFilePath(String fileName) {
		
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	
	}
		
	/**
	 * @param file
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final File file) throws Exception {
		return readFile(new FileInputStream(file));
	}

	/**
	 * @param in
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final InputStream in) throws Exception {

		return readFile(in, null);
	}

	/**
	 * @param in
	 * @param charsetName
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final InputStream in, final String charsetName) throws Exception {

		BufferedReader ir = null;
		try {
			if (charsetName != null) {
				ir = new BufferedReader(new InputStreamReader(in, charsetName));
			} else {
				ir = new BufferedReader(new InputStreamReader(in));
			}

			// deliberately do not remove BOM here
			int c;
			StringBuffer currentStrBuffer = new StringBuffer();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

}
