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
	String getPath(int index);

	/**
	 * Maybe implemented to define the path as already being a thumbnail.
	 * In which case the image will be loaded directly rather than rescaled.
	 * 
	 * NOTE getData(...) is called before getDirectThumbnailPath()
	 * @return
	 */
	public String getDirectThumbnailPath();

	/**
	 * The name of the image as it is read from disk.
	 * @param index
	 * @return
	 */
	String getItemName(int index, boolean attemptToShorten);
}
