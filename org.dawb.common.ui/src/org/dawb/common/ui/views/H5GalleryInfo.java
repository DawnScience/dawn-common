/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;

public class H5GalleryInfo {

	private SliceObject slice;
	private int[]       shape;
	private int         sliceDimension;
	
	public void createDefaultSliceDimension() {
		sliceDimension = getDefaultSliceDimension();
	}
	
	public boolean isNonAxisDimension(int dim) {
		if (slice.isAxis(dim)) return false;
		return true;
	}

	/**
	 * Attempts to get a slice dimension which is the first one it encounters
	 * whose size is > 1, or if they are all 1 then the first one. 
	 * 
	 * This is very limiting but simple. Since SDA already
	 * has better slicing no plans to make better.
	 * 
	 * @return
	 */
	private int getDefaultSliceDimension() {
		for (int dim = 0; dim < shape.length; dim++) {
			if (slice.isAxis(dim)) continue;
			if (shape[dim]>1) return dim;
			continue;
		}
		for (int dim = 0; dim < shape.length; dim++) {
			if (slice.isAxis(dim)) continue;
			return dim;
		}
		return 0;
	}
	
	public List<Integer> getSliceableDimensions() {
		final List<Integer> ret = new ArrayList<Integer>(shape.length);
		for (int dim = 0; dim < shape.length; dim++) {
			if (slice.isAxis(dim)) continue;
			ret.add(dim);
		}
		return ret;
	}

	public int getSliceDimension() {
		return sliceDimension;
	}
	
	public void setSliceDimension(int dim) {
		sliceDimension = dim;
	}
	
	public int getSize() {
		return shape[getSliceDimension()];
	}

	public SliceObject getSlice() {
		return slice;
	}
	public void setSlice(SliceObject slice) {
		this.slice = slice;
	}
	public int[] getShape() {
		return shape;
	}
	public void setShape(int[] shape) {
		this.shape = shape;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(shape);
		result = prime * result + ((slice == null) ? 0 : slice.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		H5GalleryInfo other = (H5GalleryInfo) obj;
		if (!Arrays.equals(shape, other.shape))
			return false;
		if (slice == null) {
			if (other.slice != null)
				return false;
		} else if (!slice.equals(other.slice))
			return false;
		return true;
	}
	
	public int getStart(int dim) {
		if (slice.getSlicedShape().length==2 || !isNonAxisDimension(dim)) return 0;
		if (slice.getSlicedShape().length==1) return slice.getSliceStart()[dim];
		throw new RuntimeException("Can only deal with 1D and 2D slices!");
	}
	
	public int getStop(int dim) {
		if (slice.getSlicedShape().length==2 || !isNonAxisDimension(dim)) return getShape()[dim];
		if (slice.getSlicedShape().length==1) return slice.getSliceStart()[dim]+1;
		throw new RuntimeException("Can only deal with 1D and 2D slices!");
	}


	
}
