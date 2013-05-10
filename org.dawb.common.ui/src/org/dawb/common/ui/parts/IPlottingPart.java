package org.dawb.common.ui.parts;

import org.dawnsci.plotting.api.IPlottingSystem;

/**
 * Any editor or view can implement this interface to allow access to its
 * embedded plotting system 
 */
public interface IPlottingPart {
	/**
	 * The plotting system embedded in the part. May be return null
	 * @return plotting system
	 */
	public IPlottingSystem getPlottingSystem();
}
