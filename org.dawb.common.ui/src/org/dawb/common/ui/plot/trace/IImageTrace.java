package org.dawb.common.ui.plot.trace;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.IImageService;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public interface IImageTrace extends ITrace {

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
	
	public enum DownsampleType {
		
		POINT("Point, top left of bin"),  // select corner point of bin
		MEAN("Mean value of bin"),   // mean average over bin
		MAXIMUM("Maximum value of bin"), // use maximum value in bin
		MINIMUM("Minimum value of bin"); // use minimum value in bin
		
		private String label;
		
		DownsampleType(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
	}

	/**
	 * Pulls a data set out of the image data for
	 * a given selection. For instance getting the bounds
	 * of a box to slice and return the data.
	 * 
	 * @param bounds
	 * @return
	 */
	AbstractDataset slice(RegionBounds bounds);

	/**
	 * Default is TOP_LEFT unlike normal plotting
	 * @return
	 */
	public ImageOrigin getImageOrigin();
	
	/**
	 * Repaints the axes and the image to the new origin.
	 * @param origin
	 */
	public void setImageOrigin(final ImageOrigin origin);
	
	/**
	 * PaletteData for creating the image from the AbstractDataset
	 * @return
	 */
	public PaletteData getPaletteData();
	
	/**
	 * Setting palette data causes the image to redraw with the new palette.
	 * @param paletteData
	 */
	public void setPaletteData(PaletteData paletteData);
	
	/**
	 * Call to set image data
	 * @param image
	 * @param axes - may be null
	 */
	public void setData(final AbstractDataset image, List<AbstractDataset> axes);

	/**
	 * The min intensity for generating the image
	 * @return
	 */
	public Number getMin();
	
	/**
	 * The min intensity for generating the image
	 * @return
	 */
	public void setMin(Number min);
	
	/**
	 * The max intensity for generating the image
	 * @return
	 */
	public Number getMax();
	
	/**
	 * The max intensity for generating the image
	 * @return
	 */
	public void setMax(Number max);
	
	/**
	 * Returns the last image service bean sent to the service for getting
	 * the image.
	 * 
	 * @return
	 */
	public IImageService.ImageServiceBean getImageServiceBean();
	
	/**
	 * Returns the getMax() value if it has been set or the last calculated
	 * value of max if not.
	 * 
	 * @return
	 */
	public Number getCalculatedMax();
	
	
	/**
	 * Returns the getMin() value if it has been set or the last calculated
	 * value of min if not.
	 * 
	 * @return
	 */
	public Number getCalculatedMin();

	/**
	 * Call to add a palette listener
	 * @param pl
	 */
	public void addPaletteListener(PaletteListener pl);
	
	
	/**
	 * Call to remove a palette listener
	 * @param pl
	 */
	public void removePaletteListener(PaletteListener pl);
	
	/**
	 * 
	 * @return the downsample type being used for plotting less data
	 * than recieved.
	 */
	public DownsampleType getDownsampleType();
	
	/**
	 * Change the downsample type, will also refresh the UI.
	 * @param type
	 */
	public void setDownsampleType(DownsampleType type);

}
