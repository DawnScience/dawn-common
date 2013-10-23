package org.dawb.common.ui.views;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Information provided to GalleryDelegate when 
 * implementing a custom gallery.
 * 
 * @author fcp94556
 *
 */
public interface GalleryDelegateInfo {

	/**
	 * Size of gallery
	 * @return
	 */
	int getSize();

	/**
	 * Name of gallery
	 * @return
	 */
	String getName();

	/**
	 * Provides the data of any item in the gallery. For which
	 * a thumbnail image is constructed.
	 * @param ii
	 * @return
	 */
	IDataset getData(boolean fullData, ImageItem ii) throws Exception;

	/**
	 * Optionally implemented to return the path for the item.
	 * @param itemCount
	 * @return
	 */
	String getPath(int itemCount);

}
