
package org.dawnsci.boofcv.examples.imageprocessing;

import java.util.List;

import org.dawb.common.services.IBoofCVProcessingService;
import org.dawb.common.services.ServiceManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.dawnsci.boofcv.BoofCVProcessingServiceCreator;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageFilterTest {

	private IBoofCVProcessingService service;
	private String dataname = "image-01";
	private IDataHolder holder;
	private IDataset data;

	@BeforeClass
	public static void beforeClass() {
		ServiceManager.setService(IBoofCVProcessingService.class, BoofCVProcessingServiceCreator.createPersistenceService());
	}

	@Before
	public void before() throws Exception {
		service = (IBoofCVProcessingService) ServiceManager.getService(IBoofCVProcessingService.class);
		holder = LoaderFactory.getData("resources/particles01.jpg", null);
		data = holder.getDataset(dataname);
	}

	@Test
	public void filterDerivativeSobel() {
		List<IDataset> derivatives = service.filterDerivativeSobel(data);
		Assert.assertEquals("Value of first item is not the expected one", 0, derivatives.get(0).getDouble(0), 0);
	}

	@Test
	public void filterGaussianBlur() {
		IDataset blurred = service.filterGaussianBlur(data, -1, 10);
		Assert.assertEquals("Value of first item is not the expected one", 67.10220336914062, blurred.getDouble(0), 0);
	}

	@Test
	public void filterThreshold() {
		IDataset thresholded = service.filterThreshold(data, 100, true, false);
		Assert.assertEquals("Value of first item is not the expected one", 0, thresholded.getDouble(0), 0);
	}

	@Test
	public void filterErode() {
		IDataset eroded = service.filterErode(data, false);
		Assert.assertEquals("Value of first item is not the expected one", 67.10220336914062, eroded.getDouble(0), 0);
	}

	@Test
	public void filterErodeAndDilate() {
		IDataset erodedAndDilated = service.filterErodeAndDilate(data, false);
		Assert.assertEquals("Value of first item is not the expected one", 67.10220336914062, erodedAndDilated.getDouble(0), 0);
	}


	@Test
	public void filterContour() {
		IDataset blurred = service.filterContour(data, 8, 0xFFFFFF, 0xFF2020);
		Assert.assertEquals("Value of first item is not the expected one", 67.10220336914062, blurred.getDouble(0), 0);
	}
}
