package org.dawb.common.ui.printing;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public interface IPrintImageProvider {

	/**
	 * Get an image of a certain size
	 * @param imageSizeRect
	 * @return
	 */
	Image getImage(Rectangle size);

	/**
	 * Get the natural bounds of the image.
	 * @return
	 */
	Rectangle getBounds();

}
