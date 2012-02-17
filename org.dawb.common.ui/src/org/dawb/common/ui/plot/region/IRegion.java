package org.dawb.common.ui.plot.region;

import org.eclipse.swt.graphics.Color;

/**
 * A selection region must conform to this interface. You can set
 * its position, color and transparency settings.
 * 
 * @author fcp94556
 *
 */
public interface IRegion {
	
	/**
	 * The name of the region
	 * @return
	 */
	public String getName();
	
	/**
	 * The name of the region
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * The Color of the region
	 * @return
	 */
	public Color getRegionColor();
	
	/**
	 * The Color of the region
	 * @return
	 */

	public void setRegionColor(Color regionColor);
	
	/**
	 * If position information should be shown in the region.
	 * @return
	 */
	public boolean isShowPosition() ;
	/**
	 * If position information should be shown in the region.
	 * @return
	 */
	public void setShowPosition(boolean showPosition) ;
	
	/**
	 * Alpha transparency 0-255, 0-transparent, 255-opaque
	 * @return
	 */
	public int getAlpha();
	/**
	 * Alpha transparency 0-255, 0-transparent, 255-opaque
	 * @return
	 */
	public void setAlpha(int alpha);
	/**
	 * Visibility
	 * @return
	 */
	public boolean isVisible() ;
	/**
	 * Visibility
	 * @return
	 */
	public void setVisible(boolean visible);
	/**
	 * Moveable or not
	 * @return
	 */
	public boolean isMotile() ;
	
	/**
	 * Moveable or not
	 * @return
	 */
	public void setMotile(boolean motile);
	
	
	/**
	 * Get the position (in coordinate frame of the axis that region is added to)
	 */
	public RegionBounds getBounds();
	
	/**
	 * Set the position (in coordinate frame of the axis that region is added to)
	 */
	public void setBounds(RegionBounds bounds);
	
}
