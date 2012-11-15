package org.dawb.common.ui.plot.region;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * This is a Figure, disabled for mouse events. 
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractRegion extends Figure implements IRegion, IRegionContainer {

	private Collection<IROIListener> roiListeners;
	private boolean regionEventsActive = true;
	private boolean maskRegion         = false;
	protected String label = null;
	protected Color labelColour = null;
	protected Font labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.NORMAL);
	protected Dimension labeldim;

	@Override
	public boolean addROIListener(final IROIListener l) {
		if (roiListeners==null) roiListeners = new HashSet<IROIListener>(11);
		if (!roiListeners.contains(l)) return roiListeners.add(l);
		return false;
	}
	
	@Override
	public boolean removeROIListener(final IROIListener l) {
		if (roiListeners==null) return false;
		return roiListeners.remove(l);
	}
	
	protected void clearListeners() {
		if (roiListeners==null) return;
		roiListeners.clear();
	}
	
	protected void fireROIDragged(ROIBase roi, ROIEvent.DRAG_TYPE type) {
		if (roiListeners==null) return;
		if (!regionEventsActive) return;
		
		final ROIEvent evt = new ROIEvent(this, roi);
		evt.setDragType(type);
		for (IROIListener l : roiListeners) {
			l.roiDragged(evt);
		}
	}
	
	protected void fireROIChanged(ROIBase roi) {
		if (roiListeners==null) return;
		if (!regionEventsActive) return;
		
		final ROIEvent evt = new ROIEvent(this, roi);
		for (IROIListener l : roiListeners) {
			l.roiChanged(evt);
		}
	}

	protected ROIBase roi;

	@Override
	public ROIBase getROI() {
		return roi;
	}

	@Override
	public void setROI(ROIBase roi) {
		this.roi = roi;
		updateROI();
		fireROIChanged(roi);
	}

	/**
	 * Implement to return the region of interest
	 * @param recordResult if true this calculation changes the recorded absolute position
	 */
	protected abstract ROIBase createROI(boolean recordResult);

	/**
	 * Updates the region, usually called when items have been created and the position of the
	 * region should be updated. Does not fire events.
	 */
	protected void updateROI() {
		if (roi != null) {
			try {
				this.regionEventsActive = false;
				updateROI(roi);
			} finally {
				this.regionEventsActive = true;
			}
		}
	}
	
	/**
	 * Implement this method to redraw the figure to the axis coordinates (only).
	 * 
	 * @param roi
	 */
	protected abstract void updateROI(ROIBase roi);

	public String toString() {
		if (getName()!=null) return getName();
		return super.toString();
	}
	
	protected boolean trackMouse;

	@Override
	public boolean isTrackMouse() {
		return trackMouse;
	}

	@Override
	public void setTrackMouse(boolean trackMouse) {
		this.trackMouse = trackMouse;
	}
	
	private boolean userRegion = true; // Normally a user region.

	@Override
	public boolean isUserRegion() {
		return userRegion;
	}

	@Override
	public void setUserRegion(boolean userRegion) {
		this.userRegion = userRegion;
	}
	
	public IRegion getRegion() {
		return this;
	}

	public void setRegion(IRegion region) {
		// Does nothing
	}

	public boolean isMaskRegion() {
		return maskRegion;
	}

	public void setMaskRegion(boolean maskRegion) {
		this.maskRegion = maskRegion;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label ;
		this.labeldim = FigureUtilities.getTextExtents(label, labelFont);
	}
	
	private Object userObject;
	/**
	 * 
	 * @return last object
	 */
	public Object setUserObject(Object object) {
		Object tmp = userObject;
		userObject = object;
		return tmp;
	}
	
	/**
	 * 
	 * @return object
	 */
	public Object getUserObject() {
		return userObject;
	}

}
