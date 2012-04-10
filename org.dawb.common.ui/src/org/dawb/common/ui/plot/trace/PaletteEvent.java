package org.dawb.common.ui.plot.trace;

import java.util.EventObject;

import org.eclipse.swt.graphics.PaletteData;

/**
 * Event used for palette changes, including change of Palette Data.
 * @author fcp94556
 *
 */
public class PaletteEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5574618484168809185L;

	protected IImageTrace trace;
	protected PaletteData paletteData;
	
	public PaletteEvent(Object source, PaletteData paletteData) {
		super(source);
		this.trace       = (IImageTrace)source;
		this.paletteData = paletteData;
	}

	public IImageTrace getTrace() {
		return trace;
	}

	public void setTrace(IImageTrace trace) {
		this.trace = trace;
	}

	/**
	 * May be null!
	 * @return
	 */
	public PaletteData getPaletteData() {
		return paletteData;
	}

	public void setPaletteData(PaletteData paletteData) {
		this.paletteData = paletteData;
	}

}
