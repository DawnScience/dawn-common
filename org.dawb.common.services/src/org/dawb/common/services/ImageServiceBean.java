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

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.ImageServiceBean.HistoType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class ImageServiceBean {
	
	private static HistogramBound DEFAULT_MAXIMUM = new HistogramBound(Double.POSITIVE_INFINITY, Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
	private static HistogramBound DEFAULT_MINIMUM = new HistogramBound(Double.NEGATIVE_INFINITY, Display.getDefault().getSystemColor(SWT.COLOR_RED));
	private static HistogramBound DEFAULT_NAN     = new HistogramBound(Double.NaN, Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
	
	private HistogramBound  maximumCutBound = DEFAULT_MAXIMUM;
	private HistogramBound  minimumCutBound = DEFAULT_MINIMUM;
	private HistogramBound  nanBound     = DEFAULT_NAN;
	private AbstractDataset image;
	private PaletteData     palette;
	private ImageOrigin     origin;
	private Number          min;
	private Number          max;
	private IProgressMonitor monitor;
	private HistoType       histogramType = HistoType.MEAN;
	

	public ImageServiceBean() {
		
	}
    public ImageServiceBean(AbstractDataset slice, HistoType histoType) {
		this.image = slice;
		this.histogramType = histoType;
	}

	/**
     * Removes all references
     */
	public void dispose() {
		maximumCutBound=null;
		minimumCutBound=null;
		nanBound=null;
		image=null;
		palette=null;
		origin=null;
		min=null;
		max=null;
		monitor=null;
		histogramType=null;
	}

	
	
	public AbstractDataset getImage() {
		return image;
	}
	public void setImage(AbstractDataset image) {
		this.image = image;
	}
	public PaletteData getPalette() {
		return palette;
	}
	public void setPalette(PaletteData palette) {
		this.palette = palette;
	}
	public ImageOrigin getOrigin() {
		return origin;
	}
	public void setOrigin(ImageOrigin origin) {
		this.origin = origin;
	}
	/**
	 * The max valid value for the 
	 * @return
	 */
	public Number getMin() {
		return min;
	}
	public void setMin(Number min) {
		this.min = min;
	}
	public Number getMax() {
		return max;
	}
	public void setMax(Number max) {
		this.max = max;
	}
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	public boolean isCancelled() {
		return monitor!=null && monitor.isCanceled();
	}
	public IProgressMonitor getMonitor() {
		return monitor;
	}
	/**
	 * Default Color positive infinity with blue
	 * @return
	 */
	public HistogramBound getMaximumCutBound() {
		return maximumCutBound;
	}
	public void setMaximumCutBound(HistogramBound maximumBound) {
		this.maximumCutBound = maximumBound;
	}
	/**
	 * Default Color negative infinity with red
	 * @return
	 */
	public HistogramBound getMinimumCutBound() {
		return minimumCutBound;
	}
	public void setMinimumCutBound(HistogramBound minimumBound) {
		this.minimumCutBound = minimumBound;
	}
	/**
	 * Default Color NaN with green
	 * @return
	 */
	public HistogramBound getNanBound() {
		return nanBound;
	}
	public void setNanBound(HistogramBound nanBound) {
		this.nanBound = nanBound;
	}
	public HistoType getHistogramType() {
		return histogramType;
	}
	public void setHistogramType(HistoType histogramType) {
		this.histogramType = histogramType;
	}

	
	/**
	 * Immutable HistogramBound class. Keep immutable so that static
	 * bound defaults cannot be modified.
	 */
	public static class HistogramBound {

		private Number bound;
		private Color  color;
		public HistogramBound(Number bound, Color color) {
			this.bound = bound;
			this.color = color;
		}
		public Number getBound() {
			return bound;
		}
		public Color getColor() {
			return color;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bound == null) ? 0 : bound.hashCode());
			result = prime * result + ((color == null) ? 0 : color.hashCode());
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
			HistogramBound other = (HistogramBound) obj;
			if (bound == null) {
				if (other.bound != null)
					return false;
			} else if (!bound.equals(other.bound))
				return false;
			if (color == null) {
				if (other.color != null)
					return false;
			} else if (!color.equals(other.color))
				return false;
			return true;
		}
		
		
	}
	
	public enum HistoType {
		MEAN(0, "Mean based"), MEDIAN(1, "Median based");
		
		public final String label;
		public final int    index;
		HistoType(int index, String label) {
			this.index = index;
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		public int getIndex() {
			return index;
		}
		public static List<HistoType> histoTypes;
		static {
			histoTypes = new ArrayList<HistoType>();
			histoTypes.add(MEAN);
			histoTypes.add(MEDIAN);
		}
		public static HistoType forLabel(String label) {
			for (HistoType t : histoTypes) {
				if (t.label.equals(label)) return t;
			}
			return null;
		}
	}

	

	public enum ImageOrigin {
		TOP_LEFT("Top left"), TOP_RIGHT("Top right"), BOTTOM_LEFT("Bottom left"), BOTTOM_RIGHT("Bottom right");
		
		public static List<ImageOrigin> origins;
		static {
			origins = new ArrayList<ImageOrigin>();
			origins.add(TOP_LEFT);
			origins.add(TOP_RIGHT);
			origins.add(BOTTOM_LEFT);
			origins.add(BOTTOM_RIGHT);
		}

		private String label;
		public String getLabel() {
			return label;
		}
		
		ImageOrigin(String label) {
			this.label = label;
		}
		
		public static ImageOrigin forLabel(String label) {
			for (ImageOrigin o : origins) {
				if (o.label.equals(label)) return o;
			}
			return null;
		}
	}

}