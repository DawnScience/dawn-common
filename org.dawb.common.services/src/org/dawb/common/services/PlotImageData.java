/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.common.services;

import org.eclipse.ui.services.IDisposable;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class PlotImageData {

	public static enum PlotImageType {
		XY_PLOT, IMAGE_ONLY, IMAGE_PLOT, SURFACE_PLOT;
	}
	
	private String   plotTitle;
	private IDataset data;
	private int      width;
	private int      height;
	private PlotImageType type=PlotImageType.IMAGE_ONLY;
	private boolean constantRange=false;
	
	private double yLower, yUpper, xLower, xUpper;
	
	/**
	 * An object which may be used to cache the plotting
	 * system when doing images in 1D or surfaces. This 
	 * cache makes drawing off screen more efficient.
	 */
	private IDisposable   disposable;
	
	public PlotImageData() {
		
	}
	
	public PlotImageData(IDataset data, int width, int height) {
		this.data   = data;
		this.width  = width;
		this.height = height;
	}
	
	public IDataset getData() {
		return data;
	}
	public void setData(IDataset data) {
		this.data = data;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}

	public PlotImageType getType() {
		return type;
	}

	public void setType(PlotImageType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result
				+ ((disposable == null) ? 0 : disposable.hashCode());
		result = prime * result + height;
		result = prime * result
				+ ((plotTitle == null) ? 0 : plotTitle.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + width;
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
		PlotImageData other = (PlotImageData) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (disposable == null) {
			if (other.disposable != null)
				return false;
		} else if (!disposable.equals(other.disposable))
			return false;
		if (height != other.height)
			return false;
		if (plotTitle == null) {
			if (other.plotTitle != null)
				return false;
		} else if (!plotTitle.equals(other.plotTitle))
			return false;
		if (type != other.type)
			return false;
		if (width != other.width)
			return false;
		return true;
	}

	public IDisposable getDisposable() {
		return disposable;
	}

	public void setDisposible(IDisposable disposable) {
		this.disposable = disposable;
	}

	public String getPlotTitle() {
		return plotTitle;
	}

	public void setPlotTitle(String plotTitle) {
		this.plotTitle = plotTitle;
	}

	public double getyLower() {
		return yLower;
	}

	public void setyLower(double yLower) {
		this.yLower = yLower;
	}

	public double getyUpper() {
		return yUpper;
	}

	public void setyUpper(double yUpper) {
		this.yUpper = yUpper;
	}

	public double getxLower() {
		return xLower;
	}

	public void setxLower(double xLower) {
		this.xLower = xLower;
	}

	public double getxUpper() {
		return xUpper;
	}

	public void setxUpper(double xUpper) {
		this.xUpper = xUpper;
	}

	public boolean isConstantRange() {
		return constantRange;
	}

	public void setConstantRange(boolean constantRange) {
		this.constantRange = constantRange;
	}
	
}
