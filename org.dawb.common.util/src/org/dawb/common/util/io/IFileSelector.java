package org.dawb.common.util.io;

import java.io.File;

/**
 * Used to mark parts as able to return selected files.
 * 
 * @author fcp94556
 *
 */
public interface IFileSelector {

	/**
	 * 
	 * @return
	 */
	public File getSelectedFile();
}
