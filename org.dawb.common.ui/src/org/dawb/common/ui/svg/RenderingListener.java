package org.dawb.common.ui.svg;

import org.eclipse.draw2d.Graphics;

/**
 * A listener interface for receiving notification that an RenderedImage has completed 
 * rendering.
 */
public interface RenderingListener {

	/**
	 * While the rendering is occuring on a separate thread, this method is a hook to draw a temporary
	 * image onto the drawing surface.
	 * 
	 * @param g the <code>Graphics</code> object to paint the temporary image to
	 */
	public void paintFigureWhileRendering(Graphics g);
	
	/**
	 * Called when the given <code>RenderedImage</code> has completed rendering
	 * to the swt image.
	 * 
	 * @param source The <code>RenderedImage</code> that was being rendered.
	 */
	public void imageRendered(RenderedImage rndImg);
}