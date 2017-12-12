/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.conversion;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.conversion.converters.Convert1DtoND.Convert1DInfoBean;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.dawnsci.conversion.schemes.Convert1DtoNDScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class Convert1DtoNDTest {
	private static final IConversionScheme scheme = new Convert1DtoNDScheme();
	private String testfile = "MoKedge_1_15.nxs";
	private String nonNexusTest = "HyperOut.dat";
	
	@Before
	public void before() {
		LocalServiceManager.setLoaderService(new LoaderServiceImpl());
		ServiceLoader.setNexusFileFactory(new NexusFileFactoryHDF5());
	}
	
	@Test
	public void test1DSimple() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(testfile);
		
		String[] paths = new String[]{path,path,path,path};
		
		final IConversionContext context = service.open(paths);
		
		final File tmp = File.createTempFile("testSimple", ".nxs");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(scheme);
        context.setAxisDatasetName("/entry1/counterTimer01/Energy");
        context.setDatasetName("/entry1/counterTimer01/(I0|lnI0It|It)");
        
        service.process(context);
        
        final IDataHolder   dh    = LocalServiceManager.getLoaderService().getData(tmp.getAbsolutePath(),null);
        final List<String> names = Arrays.asList("/entry1/counterTimer01/I0","/entry1/counterTimer01/lnI0It","/entry1/counterTimer01/It");
        for (String name : names) {
            ILazyDataset ds = dh.getLazyDataset(name);
            assertArrayEquals(new int[] {4,489},ds.getShape());
		}
        
        ILazyDataset ds = dh.getLazyDataset("/entry1/counterTimer01/Energy");
        assertArrayEquals(new int[] {489},ds.getShape());
   	}
	
	@Test
	public void test3DSimple() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(testfile);
		
		String[] paths = new String[]{path,path,path,path,path,path,path,path,path,path,path,path};
		
		final IConversionContext context = service.open(paths);
		
		Convert1DInfoBean bean = new Convert1DInfoBean();
		bean.fastAxis = 4;
		bean.slowAxis = 3;
		
		context.setUserObject(bean);
		
		final File tmp = File.createTempFile("testSimple3d", ".nxs");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(scheme);
        context.setAxisDatasetName("/entry1/counterTimer01/Energy");
        context.setDatasetName("/entry1/counterTimer01/(I0|lnI0It|It)");
        
        service.process(context);
        
        final IDataHolder   dh    = LocalServiceManager.getLoaderService().getData(tmp.getAbsolutePath(),null);
        final List<String> names = Arrays.asList("/entry1/counterTimer01/I0","/entry1/counterTimer01/lnI0It","/entry1/counterTimer01/It");
        for (String name : names) {
            ILazyDataset ds = dh.getLazyDataset(name);
            assertArrayEquals(new int[] {3,4,489},ds.getShape());
		}
        ILazyDataset ds = dh.getLazyDataset("/entry1/counterTimer01/Energy");
        assertArrayEquals(new int[] {489},ds.getShape());
   	}
	
	@Test
	public void test1DNotNexus() throws Exception {
		
		IConversionService service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(nonNexusTest);
		
		String[] paths = new String[]{path,path,path,path};
		
		final IConversionContext context = service.open(paths);
		
		final File tmp = File.createTempFile("testSimple", ".nxs");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(scheme);
        context.setAxisDatasetName("x");
        context.setDatasetName("(dataset_0|dataset_1)");
        
        service.process(context);
        
        final IDataHolder   dh    = LoaderFactory.getData(tmp.getAbsolutePath());
        final List<String> names = Arrays.asList("/entry1/dataset_0","/entry1/dataset_1");
        for (String name : names) {
            ILazyDataset ds = dh.getLazyDataset(name);
            assertArrayEquals(new int[] {4,1608},ds.getShape());
		}
        
        ILazyDataset ds = dh.getLazyDataset("/entry1/x");
        assertArrayEquals(new int[] {1608},ds.getShape());
   	}
	
private String getTestFilePath(String fileName) {
		
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	
	}

}
