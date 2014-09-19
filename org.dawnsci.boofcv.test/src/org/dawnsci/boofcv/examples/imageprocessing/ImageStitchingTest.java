package org.dawnsci.boofcv.examples.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.boofcv.BoofCVImageStitchingProcessCreator;
import org.dawnsci.boofcv.examples.util.ImageLoaderJob;
import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.junit.Test;

public class ImageStitchingTest {

	@Test
	public void testImageStitchingProcess() throws Throwable {
		List<String> filenames = new ArrayList<String>();
		String[] files = Utils.getFileNames("resources/82702_UViewImage/", true);
		Utils.getArrayAsList(files, filenames);
		List<IDataset> data = loadData(filenames);

		IImageStitchingProcess stitcher = BoofCVImageStitchingProcessCreator.createStitchingProcess();
		IDataset stitched = stitcher.stitch(data);
		ExampleImageStitching.showStitchedImage(stitched);
	}

	private List<IDataset> loadData(List<String> filenames) {
		ImageLoaderJob imageLoader = new ImageLoaderJob(filenames);
		imageLoader.schedule();
		try {
			imageLoader.join();
			return imageLoader.getData();
		} catch (InterruptedException e) {
			System.err.println("Error loading the images" + e.getMessage());
			return null;
		}
	}
}
