package org.dawnsci.boofcv.examples.imageprocessing;

import org.dawnsci.boofcv.BoofCVImageTrackerServiceCreator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageTracker;
import org.eclipse.dawnsci.analysis.api.image.IImageTracker.TrackerType;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageTrackerTest {

	private IImageTracker tracker;

	private String dataname = "image-01";
	private IDataset data;
	private IDataset data2;

	@Before
	public void before() throws Exception {
		if (tracker == null)
			tracker = BoofCVImageTrackerServiceCreator.createImageTrackerService();
		data = LoaderFactory.getData("resources/particles01.jpg", null).getDataset(dataname);
		data2 = LoaderFactory.getData("resources/particles02.jpg", null).getDataset(dataname);
	}

	@Test
	public void testImageTrackerBasic() throws Exception {
		double[] originalLocation = new double[] { 446, 73, 491, 73, 446, 104, 491, 104 };
		// initialize tracker
		tracker.initialize(data, originalLocation, TrackerType.TLD);
		// run tracker against second image
		double[] location = tracker.track(data2);

		//TODO make the assert work
//		Assert.assertEquals("Value of first item is not the expected one", location[2], 0.5, 0);
	}
}
