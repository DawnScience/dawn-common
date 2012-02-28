package org.dawb.common.ui.plot.region;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

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
	 * Label shown or not
	 * @return
	 */
	public boolean isShowLabel() ;
	
	/**
	 * Label shown or not
	 * @return
	 */
	public void setShowLabel(boolean label);
	
	/**
	 * Get the position (in coordinate frame of the axis that region is added to)
	 */
	public RegionBounds getRegionBounds();
	
	/**
	 * Set the position (in coordinate frame of the axis that region is added to)
	 */
	public void setRegionBounds(RegionBounds bounds);
	
	/**
	 * Add a listener which is notified when this region is resized or
	 * moved.
	 * 
	 * @param l
	 */
	public boolean addRegionBoundsListener(final IRegionBoundsListener l);
	
	/**
	 * Remove a RegionBoundsListener
	 * @param l
	 */
	public boolean removeRegionBoundsListener(final IRegionBoundsListener l);
	
	/**
	 * Will be called to remove the region and clean up resources when the 
	 * user calls the removeRegion(...) method.
	 */
	public void remove();
	
	/**
	 * Class packages types of regions, their default names, colors and indices.
	 * @author fcp94556
	 *
	 */
	public enum RegionType {
		
		LINE("Line",     0, Display.getDefault().getSystemColor(SWT.COLOR_CYAN)), 
		BOX("Box",       1, Display.getDefault().getSystemColor(SWT.COLOR_GREEN)), 
		XAXIS("X-Axis",  2, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		YAXIS("Y-Axis",  3, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		SECTOR("Sector", 4, Display.getDefault().getSystemColor(SWT.COLOR_RED)),
		XAXIS_LINE("X-Axis Line",  5, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		YAXIS_LINE("Y-Axis Line",  6, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)); 
		
		private int    index;
		private String name;
		private Color defaultColor;
		
		public static List<RegionType> ALL_TYPES;
		static {
			ALL_TYPES = new ArrayList<RegionType>(5);
			ALL_TYPES.add(LINE);
			ALL_TYPES.add(BOX);
			ALL_TYPES.add(XAXIS);
			ALL_TYPES.add(YAXIS);
			ALL_TYPES.add(SECTOR);
			ALL_TYPES.add(XAXIS_LINE);
			ALL_TYPES.add(YAXIS_LINE);
		}
	
		RegionType(String name, int index, Color defaultColor) {
			this.name  = name;
			this.index = index;
			this.defaultColor = defaultColor;
		}
		
		public int getIndex() {
			return index;
		}
		public String getName() {
			return name;
		}
		public Color getDefaultColor() {
			return defaultColor;
		}

		public static RegionType getRegion(int index) {
			for (RegionType r : ALL_TYPES) {
				if (r.getIndex()==index) return r;
			}
			return null;
		}
	}

	/**
	 * return the line width used for drawing any lines (if any are drawn, otherwise 0).
	 * @return
	 */
	public int getLineWidth();
	
	/**
	 * set the line width used for drawing any lines (if any are drawn, otherwise does nothing).
	 * @return
	 */
	public void setLineWidth(int i);

}
