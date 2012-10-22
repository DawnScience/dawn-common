package org.dawb.common.ui.plot.trace;

import org.dawb.common.services.ImageServiceBean;
import org.eclipse.swt.graphics.PaletteData;

public interface IPaletteTrace extends ITrace{

	
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
	 * Returns the last image service bean sent to the service for getting
	 * the image.
	 * 
	 * @return
	 */
	public ImageServiceBean getImageServiceBean();
	
	/**
	 * Call to add a palette listener
	 * @param pl
	 */
	public void addPaletteListener(IPaletteListener pl);
	
	
	/**
	 * Call to remove a palette listener
	 * @param pl
	 */
	public void removePaletteListener(IPaletteListener pl);
}
