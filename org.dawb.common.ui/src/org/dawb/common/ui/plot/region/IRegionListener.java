package org.dawb.common.ui.plot.region;

import java.util.EventListener;

public interface IRegionListener extends EventListener {

	public class Stub implements IRegionListener {

		@Override
		public void regionCreated(RegionEvent evt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void regionAdded(RegionEvent evt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void regionRemoved(RegionEvent evt) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * Called when region created.
	 * @param evt
	 */
	void regionCreated(final RegionEvent evt);
	
	
	/**
	 * Called when region added to graph.
	 * @param evt
	 */
	void regionAdded(final RegionEvent evt);

	/**
	 * Called when region removed from graph.
	 * @param evt
	 */
	void regionRemoved(final RegionEvent evt);

}
