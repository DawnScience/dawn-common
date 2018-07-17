/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.conversion;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.dawb.common.util.io.FileUtils;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.dawnsci.conversion.schemes.ImagesToHDFConverterScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class ImageToH5Test {

	private static final IConversionScheme scheme = new ImagesToHDFConverterScheme();
	
	@Before
	public void before() {
		new LocalServiceManager().setLoaderService(new LoaderServiceImpl());
		new ServiceLoader().setNexusFileFactory(new NexusFileFactoryHDF5());
	}
	
	@Test
	public void testImageSimple() throws Exception {
		
		System.out.println("starting testImageSimple");
		doTest("testImageSimple", "/entry/data", new int[]{10,2048,2048});
	}
	@Test
	public void testImageLongPath() throws Exception {
		
		System.out.println("starting testImageLongPath");
		doTest("testImageLongPath", "/entry1/a/long/path/data", new int[]{20,2048,2048});
	}

	/**
	 * Test should be done @ 1000x 2k but this clobbers the test decks.
	 * We reduce to 100 x 2k for the test decks to run properly.
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testLargeData() throws Exception {
		
		System.out.println("starting testLargeData");
		doTest("testLargeData", "/entry1/data", new int[]{100,2048,2048});
	}

    private void doTest(String testname, String dPath, int[] shape) throws Exception {

		final File image = new File("testfiles/dir/ref-testscale_1_001.img");
		
		final File dir   = Files.createTempDirectory("ImageToH5Test_"+testname).toFile();
		try {
			// Copy the file a few times.
			for (int i = 0; i < shape[0]; i++) {
				final File nf = new File(dir, "copy_"+i+".img");
				nf.deleteOnExit();
				FileUtils.copyNio(image, nf);
			}
			
			IConversionService service = new ConversionServiceImpl();
			
	        final IConversionContext context = service.open(dir.getAbsolutePath()+"/copy_.*img");
	        final File output = File.createTempFile("imageStackTestOutput_"+testname, ".h5", dir);
	        output.deleteOnExit();
	        context.setOutputPath(output.getAbsolutePath());
	        context.setDatasetName(dPath); // With this conversion dataset is the OUTPUT
	        context.setConversionScheme(scheme);
			
			service.process(context);
			
			if (!output.exists()) {
				fail("Image stack was not written to "+output.getName());
			}

			final ILazyDataset set = LocalServiceManager.getLoaderService().getData(output.getAbsolutePath(), null).getLazyDataset(0);
			if (!Arrays.equals(set.getShape(), shape)) {
				fail("Dataset written with shape "+Arrays.toString(set.getShape())+", but expected shape was "+Arrays.toString(shape));
			}

		} finally {
			FileUtils.recursiveDelete(dir);
		}
		
	}
}
