package org.dawnsci.boofcv.examples.imageprocessing;

import static org.junit.Assert.assertArrayEquals;

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
		double[] locationExpected = new double[] { 435.9968422696961, 72.9932498803572, 481.00780718826627,
				72.9932498803572, 481.00780718826627, 104.00080349092775, 435.9968422696961, 104.00080349092775 };
		assertArrayEquals(locationExpected, location, 0);
	}
}
