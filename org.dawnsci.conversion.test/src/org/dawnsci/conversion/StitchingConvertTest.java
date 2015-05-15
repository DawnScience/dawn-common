/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.conversion;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.dawb.common.util.io.FileUtils;
import org.dawnsci.boofcv.BoofCVImageStitchingProcessCreator;
import org.dawnsci.boofcv.BoofCVImageTransformCreator;
import org.dawnsci.conversion.converters.ImagesToStitchedConverter;
import org.dawnsci.conversion.converters.ImagesToStitchedConverter.ConversionStitchedBean;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext.ConversionScheme;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.Utils;
import uk.ac.diamond.scisoft.analysis.osgi.LoaderServiceImpl;

public class StitchingConvertTest {

	private File dir;
	private File output;
	private String stitchedFileName;
	private IImageStitchingProcess sticher;
	private IImageTransform transformer;
	private final int rows = 4;
	private final int columns = 4;
	private final double fieldOfView = 50;
	private final double angle = 45;
	List<double[]> translations = new ArrayList<double[]>();

	@Before
	public void before() {
		dir = new File(System.getProperty("java.io.tmpdir"), "StitchTestFolder");
		output = new File(dir.getAbsolutePath()+"/stitchedImage");
		stitchedFileName = "image.tif";
		FileUtils.createNewUniqueDir(output);
		LocalServiceManager.setLoaderService(new LoaderServiceImpl());

		sticher = BoofCVImageStitchingProcessCreator.createStitchingProcess();
		transformer = BoofCVImageTransformCreator.createTransformService();
		ImagesToStitchedConverter.setImageSticher(sticher);
		ImagesToStitchedConverter.setImageTransform(transformer);
	}

	@Test
	public void testDir() throws Exception {
		System.out.println("starting stitching image conversion test from directory with image files");
		doTestDir();
	}

	@After
	public void after() {
		FileUtils.recursiveDelete(dir);
	}

	private void doTestDir() throws Exception {
		final File sourcedir = new File("testfiles/imagesToStitch");
		org.apache.commons.io.FileUtils.copyDirectory(sourcedir, dir);

		IConversionService service = new ConversionServiceImpl();

		final IConversionContext context = service.open(dir.getAbsolutePath());

		List<File> files = listFiles(dir, new String[] { "tif" }, false);
		String[] filePaths = new String[files.size()];
		for (int i = 0; i < filePaths.length; i++) {
			filePaths[i] = files.get(i).getAbsolutePath();
		}

		List<IDataset> data = loadData(filePaths);
		context.setFilePaths(filePaths);
		// disable macro
		context.setEchoMacro(false);
		context.setOutputPath(output.getAbsolutePath() + File.separator + stitchedFileName);
		context.setConversionScheme(ConversionScheme.STITCHED_FROM_IMAGEDIR);

		ConversionStitchedBean bean = new ConversionStitchedBean();

		// region to select on the test images
		EllipticalROI roi = new EllipticalROI(234.978, 236.209, 0, 264.615, 247.385);
		// create translations
		for (int i = 0; i < data.size(); i ++)
			translations.add(new double[] {25, 25});
		// perform stitching in memory
		IDataset stitched = getStichedImage(data, roi);

		bean.setRoi(roi);
		bean.setAngle(angle);
		bean.setColumns(columns);
		bean.setRows(rows);
		bean.setFieldOfView(fieldOfView);
		bean.setFeatureAssociated(true);
		bean.setInputDatFile(false);
		bean.setTranslations(translations);

		context.setUserObject(bean);
		// process stitching and saving of stitched result
		service.process(context);

		// load stitched saved data
		IDataset stitchedSaved = loadData(new String[] {output.getAbsolutePath() + File.separator + stitchedFileName}).get(0);

		int[] stitchedShape = stitched.getShape();
		int[] stitchedSavedShape = stitchedSaved.getShape();
		if (!Arrays.equals(stitchedShape, stitchedSavedShape)) {
			fail("Shape of stitched data in memory and stitched data saved is not the same for dataset with name "
					+ stitched.getName());
		}

		if (stitched.getDouble(10, 10) != stitchedSaved.getDouble(10, 10)) {
			fail("Data is not the same for stitched dataset in memory and dataset saved.");
		}
	}

	private List<File> listFiles(File dir, String[] extensions, boolean isRecursive) {
		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(dir, extensions, isRecursive);
		List<File> listFiles = new ArrayList<File>(files);
		Collections.sort(listFiles);
		return listFiles;
	}

	private List<IDataset> loadData(String[] filePaths) {
		final List<IDataset> data = new ArrayList<IDataset>();
		try {
			Utils.loadData(data, filePaths);
		} catch (Exception e) {
			fail("Failed to load image stack:" + e);
		}
		return data;
	}

	private IDataset getStichedImage(List<IDataset> data, IROI roi) {
		try {
			if (sticher == null)
				sticher = BoofCVImageStitchingProcessCreator.createStitchingProcess();
			List<IDataset> rotatedCroppedData = new ArrayList<IDataset>();
			for (IDataset im : data) {
				IDataset rotated = transformer.rotate(im, angle);
				// crop each image given an elliptical roi
				IDataset cropped = Image.maxRectangleFromEllipticalImage(rotated, (EllipticalROI)roi);
				rotatedCroppedData.add(cropped);
			}
			IDataset shiftedImages = sticher.stitch(rotatedCroppedData, rows, columns, fieldOfView, translations, true, false);
			return shiftedImages;
		} catch (Exception e) {
			fail("An error occured while stitching images:" + e);
		}
		return null;
	}
}