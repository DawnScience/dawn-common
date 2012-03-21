package org.dawb.common.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public interface IImageService {

	public static class ImageServiceBean {
		private AbstractDataset image;
		private PaletteData     palette;
		private ImageOrigin     origin;
		private Number          min;
		private Number          max;
		private IProgressMonitor monitor;
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
	}

	public enum ImageOrigin {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
	}
	/**
	 * Get a full image for a given data set and PaletteData
	 * @param set
	 * @return
	 */
	public Image getImage(ImageServiceBean bean) throws Exception;
}
