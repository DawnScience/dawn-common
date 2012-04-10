package org.dawb.common.ui.plot.trace;

import java.util.EventListener;

public interface PaletteListener extends EventListener {

	/**
	 * Called when palette data changed
	 * @param evt
	 */
	public void paletteChanged(PaletteEvent evt);
	
	/**
	 * Called when min changed.
	 * @param evt
	 */
	public void minChanged(PaletteEvent evt);
	
	/**
	 * Called when max changed.
	 * @param evt
	 */
	public void maxChanged(PaletteEvent evt);
	
	public class Stub implements PaletteListener {

		@Override
		public void paletteChanged(PaletteEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void minChanged(PaletteEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void maxChanged(PaletteEvent evt) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
