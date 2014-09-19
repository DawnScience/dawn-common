/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
