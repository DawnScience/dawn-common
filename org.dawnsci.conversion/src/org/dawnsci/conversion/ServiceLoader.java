package org.dawnsci.conversion;

import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;

public class ServiceLoader {

	private static EventTracker serviceTracker;
	private static IImageStitchingProcess stitcher;
	private static IImageTransform transformer;

	/**
	 * used by OSGI
	 */
	public ServiceLoader() {
		
	}

	public static void setEventTracker(EventTracker et) {
		serviceTracker = et;
	}

	public static EventTracker getEventTracker() {
		return serviceTracker;
	}

	public static IImageStitchingProcess getImageStitcher() {
		return stitcher;
	}

	public static void setImageStitcher(IImageStitchingProcess s) {
		stitcher = s;
	}

	public static IImageTransform getImageTransform() {
		return transformer;
	}

	public static void setImageTransform(IImageTransform its) {
		transformer = its;
	}
}