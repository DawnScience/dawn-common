package org.dawnsci.boofcv.examples.imageprocessing;

import junit.framework.Assert;

import org.dawnsci.boofcv.BoofCVImageThresholdServiceCreator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageThreshold;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageThresholdTest {

	private IImageThreshold service;
	private IDataHolder holder;
	private String dataname = "image-01";
	private IDataset data;

	@Before
	public void before() throws Exception {
		if (service == null)
			service = BoofCVImageThresholdServiceCreator.createFilterService();
		holder = LoaderFactory.getData("resources/particles01.jpg", null);
		data = holder.getDataset(dataname);
	}

	@Test
	public void testFirstValueOfThreshold() {
		IDataset thresholded = service.globalThreshold(data, 20, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}

	@Test
	public void testFirstValueOfMeanThreshold() {
		IDataset thresholded = service.globalMeanThreshold(data, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}

	@Test
	public void testFirstValueOfOtsuThreshold() {
		IDataset thresholded = service.globalOtsuThreshold(data, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}

	@Test
	public void testFirstValueOfEntropyThreshold() {
		IDataset thresholded = service.globalEntropyThreshold(data, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}

	@Test
	public void testFirstValueOfAdaptiveSquareThreshold() {
		IDataset thresholded = service.adaptiveSquareThreshold(data, 15, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}

	@Test
	public void testFirstValueOfAdaptiveGaussianThreshold() {
		IDataset thresholded = service.adaptiveGaussianThreshold(data, 15, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}

	@Test
	public void testFirstValueOfAdaptiveSauvolaThreshold() {
		IDataset thresholded = service.adaptiveSauvolaThreshold(data, 15, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0));
	}
}
