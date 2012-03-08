package org.dawb.common.services;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public interface IImageService {

	public enum ImageOrigin {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
	}
	/**
	 * Get a full image for a given data set and PaletteData
	 * @param set
	 * @return
	 */
	public Image getImage(AbstractDataset set, PaletteData colorMap, ImageOrigin origin) throws Exception;
}
