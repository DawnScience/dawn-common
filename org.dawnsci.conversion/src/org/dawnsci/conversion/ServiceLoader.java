package org.dawnsci.conversion;

import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.nexus.INexusFileFactory;

public class ServiceLoader {

	private static IImageStitchingProcess stitcher;
	private static IImageTransform transformer;
	private static INexusFileFactory nexusFileFactory;

	/**
	 * used by OSGI
	 */
	public ServiceLoader() {
		
	}

	public static IImageStitchingProcess getImageStitcher() {
		return stitcher;
	}

	public void setImageStitcher(IImageStitchingProcess s) {
		stitcher = s;
	}

	public static IImageTransform getImageTransform() {
		return transformer;
	}

	public void setImageTransform(IImageTransform its) {
		transformer = its;
	}

	public static INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}

	public void setNexusFileFactory(INexusFileFactory nexusFileFactory) {
		ServiceLoader.nexusFileFactory = nexusFileFactory;
	}
}