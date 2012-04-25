package org.dawb.common.ui.plot.region;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * A selection region must conform to this interface. You can set its position, colour and transparency settings.
 * 
 * @author fcp94556
 */
public interface IRegion extends IFigure {

	/**
	 * @return the name of the region
	 */
	public String getName();

	/**
	 * The name of the region
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @return the colour of the region
	 */
	public Color getRegionColor();

	/**
	 * The colour of the region
	 * 
	 * @param regionColor
	 */
	public void setRegionColor(Color regionColor);

	/**
	 * @return if true, position information should be shown in the region.
	 */
	public boolean isShowPosition();

	/**
	 * If position information should be shown in the region.
	 * 
	 * @param showPosition
	 */
	public void setShowPosition(boolean showPosition);

	/**
	 * Alpha transparency 0-255, 0-transparent, 255-opaque
	 * 
	 * @return
	 */
	public int getAlpha();

	/**
	 * Alpha transparency 0-255, 0-transparent, 255-opaque
	 * 
	 * @param alpha
	 */
	public void setAlpha(int alpha);

	/**
	 * @return true if visible
	 */
	public boolean isVisible();

	/**
	 * Visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible);

	/**
	 * @return true if moveable
	 */
	public boolean isMobile();

	/**
	 * Moveable or not
	 * 
	 * @param mobile
	 */
	public void setMobile(boolean mobile);

	/**
	 * @return true if label is shown
	 */
	public boolean isShowLabel();

	/**
	 * Label shown or not
	 * 
	 * @param label
	 */
	public void setShowLabel(boolean label);

	/**
	 * Get the region of interest (in coordinate frame of the axis that region is added to)
	 */
	public ROIBase getROI();

	/**
	 * Set the region of interest (in coordinate frame of the axis that region is added to)
	 */
	public void setROI(ROIBase roi);

	/**
	 * Add a listener which is notified when this region is resized or moved.
	 * 
	 * @param l
	 */
	public boolean addROIListener(final IROIListener l);

	/**
	 * Remove a ROIListener
	 * 
	 * @param l
	 */
	public boolean removeROIListener(final IROIListener l);

	/**
	 * Will be called to remove the region and clean up resources when the user
	 * calls the removeRegion(...) method.
	 */
	public void remove();

	/**
	 * Class packages types of regions, their default names, colours and indices.
	 * @author fcp94556
	 *
	 */
	public enum RegionType {
		
		LINE("Line",               0, Display.getDefault().getSystemColor(SWT.COLOR_CYAN)), 
		BOX("Box",                 1, Display.getDefault().getSystemColor(SWT.COLOR_GREEN)), 
		RING("Ring",               2, Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW)), 
		XAXIS("X-Axis",            3, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		YAXIS("Y-Axis",            4, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		SECTOR("Sector",           5, Display.getDefault().getSystemColor(SWT.COLOR_RED)),
		XAXIS_LINE("X-Axis Line",  6, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		YAXIS_LINE("Y-Axis Line",  7, Display.getDefault().getSystemColor(SWT.COLOR_BLUE)), 
		FREE_DRAW("Free draw",     8, Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW)),
		POINT("Point",             9, Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA)),
		POLYLINE("Polyline",       10, Display.getDefault().getSystemColor(SWT.COLOR_CYAN));

		private int    index;
		private String name;
		private Color defaultColor;
		
		public static List<RegionType> ALL_TYPES;
		static {
			ALL_TYPES = new ArrayList<RegionType>(5);
			ALL_TYPES.add(LINE);
			ALL_TYPES.add(BOX);
			ALL_TYPES.add(RING);
			ALL_TYPES.add(XAXIS);
			ALL_TYPES.add(YAXIS);
			ALL_TYPES.add(SECTOR);
			ALL_TYPES.add(XAXIS_LINE);
			ALL_TYPES.add(YAXIS_LINE);
			ALL_TYPES.add(FREE_DRAW);
			ALL_TYPES.add(POINT);
			ALL_TYPES.add(POLYLINE);
		}
	
		RegionType(String name, int index, Color defaultColor) {
			this.name = name;
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
				if (r.getIndex() == index)
					return r;
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

	/**
	 * The type of this region
	 * @return
	 */
	public RegionType getRegionType();
	
	/**
	 * return true if the mouse should be tracked. The region will mouse with this tracking.
	 * WARNING Most regions will not respond to this setting.
	 * 
	 * @return
	 */
	public boolean isTrackMouse();
	
	/**
	 * return true if the mouse should be tracked.
	 * WARNING Most regions will not respond to this setting. AxisSelection does.
     *
	 * @return
	 */
	public void setTrackMouse(boolean trackMouse);

	/**
	 * 
	 * @return true if user region. If not a user region the region has been created programmatically
	 * and has been marked as not editable to the user.
	 */
	public boolean isUserRegion();

	/**
	 *  If not a user region the region has been created programmatically
	 * and has been marked as not editable to the user.
	 * @param userRegion
	 */
	public void setUserRegion(boolean userRegion);
	
	/**
	 * Add Mouse listener to the region if it supports it and if it is a draw2d region.
	 */
	public void addMouseListener(MouseListener l);
	
	
	/**
	 * Remove Mouse listener to the region if it supports it and if it is a draw2d region.
	 */
	public void removeMouseListener(MouseListener l);

	
	/**
	 * Add Mouse motion listener to the region if it supports it and if it is a draw2d region.
	 */
	public void addMouseMotionListener(MouseMotionListener l);
	
	/**
	 * Remove Mouse motion listener to the region if it supports it and if it is a draw2d region.
	 */
	public void removeMouseMotionListener(MouseMotionListener l);
	
	/**
	 * This method will send the figure back to the start of its
	 * parents child list. This results in it being underneath the other children.
	 */
	public void toBack();

	
	/**
	 * This method will send the figure to the end of its
	 * parents child list. This results in it being above the other children.
	 */
	public void toFront();

}
