package org.dawb.common.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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
	/**
	 * Get a full image data for a given data set and PaletteData
	 * @param set
	 * @return
	 */
	public ImageData getImageData(ImageServiceBean bean) throws Exception;

	/**
	 * Get a full image for a given data set and PaletteData
	 * @param set
	 * @return
	 */
	public Image getImage(ImageServiceBean bean) throws Exception;
}
