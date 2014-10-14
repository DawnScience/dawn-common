/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import org.eclipse.dawnsci.analysis.api.metadata.PeemMetadata;

import boofcv.struct.image.ImageSingleBand;

/**
 * Class used to store an image and its metadata
 * 
 * @author wqk87977
 * @param <T>
 * 
 */
public class ImageAndMetadata {
	ImageSingleBand<?> image;
	PeemMetadata metadata;

	public ImageAndMetadata (ImageSingleBand<?> image, PeemMetadata metadata) {
		this.image = image;
		this.metadata = metadata;
	}

	public ImageSingleBand<?> getImage() {
		return image;
	}

	public void setImage(ImageSingleBand<?> image) {
		this.image = image;
	}

	public PeemMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(PeemMetadata metadata) {
		this.metadata = metadata;
	}
}