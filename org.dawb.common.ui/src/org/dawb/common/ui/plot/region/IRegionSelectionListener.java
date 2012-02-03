package org.dawb.common.ui.plot.region;

import java.util.EventListener;

public interface IRegionSelectionListener extends EventListener {

	/**
	 * Called when region selection changed.
	 * @param evt
	 */
	public void regionSelectionPerformed(IRegionSelectionEvent evt);
}
