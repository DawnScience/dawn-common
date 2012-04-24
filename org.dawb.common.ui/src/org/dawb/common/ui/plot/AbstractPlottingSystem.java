/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ISystemService;
import org.dawb.common.ui.plot.annotation.IAnnotation;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.tool.IToolChangeListener;
import org.dawb.common.ui.plot.tool.IToolPage;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.tool.ToolChangeEvent;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for IPlottingSystem. NOTE some methods that should be implemented
 * throw exceptions if they are called. They should be overridden.
 * Some methods that should be implemented do nothing.
 * 
 * There are TODO tags added to provide information as to where these optional
 * methods to override are.
 * 
 * Some methods such as listeners are implemented for everyone.
 * 
 * The IToolPageSystem is implemented and populated by tools read from
 * extension point.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractPlottingSystem implements IPlottingSystem, IToolPageSystem {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlottingSystem.class);
	
	protected boolean rescale = true;

	// True if first data set should be plotted as x axis
	protected boolean xfirst = true;
	
	// Manager for actions
	protected PlottingActionBarManager actionBarManager;
	
	// Feedback for plotting, if needed
	protected Text      pointControls;
	
	// Color option for 1D plots, if needed.
	protected ColorOption colorOption=ColorOption.BY_DATA;

    protected String   rootName;
	
    /**
     * The action bars on the part using the plotting system, may be null
     */
    protected IActionBars    bars;

    
	public AbstractPlottingSystem() {
		this.actionBarManager = createActionBarManager();
		this.currentToolPageMap = new HashMap<ToolPageRole, IToolPage>(3);
	}
	
	public static enum ColorOption {
		BY_DATA, BY_NAME, NONE
	}
	

	public void setPointControls(Text pointControls) {
		this.pointControls = pointControls;
	}

 
	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	/**
	 * You may optionally implement this method to return plot
	 * color used for the IDataset
	 * @param object
	 * @return
	 */
	public Color get1DPlotColor(Object object) {
		return null;
	}

	
	public ColorOption getColorOption() {
		return colorOption;
	}

	public void setColorOption(ColorOption colorOption) {
		this.colorOption = colorOption;
	}

	/**
	 * Whether the plot should rescale when replotted.
	 * @return rescale
	 */	
	public boolean isRescale() {
		return rescale;
	}

	public void setRescale(boolean rescale) {
		this.rescale = rescale;
	}
	/**
	 * Please override to provide a  PlottingActionBarManager or a class
	 * subclassing it. This class deals with Actions to avoid this
	 * class getting more complex.
	 * 
	 * @return
	 */
	protected PlottingActionBarManager createActionBarManager() {
		return new PlottingActionBarManager(this);
	}

	public void dispose() {

		if (part!=null) {
			@SuppressWarnings("unchecked")
			final ISystemService<IPlottingSystem> service = (ISystemService<IPlottingSystem>)PlatformUI.getWorkbench().getService(ISystemService.class);
			if (service!=null) {
				service.removeSystem(part.getTitle());
				logger.debug("Plotting sytem for '"+part.getTitle()+"' removed.");
			}
		}

		actionBarManager.dispose();
		
		if (traceListeners!=null) traceListeners.clear();
		traceListeners = null;
		pointControls = null;
		
		if (selectionProvider!=null) selectionProvider.clear();
		selectionProvider = null;
		
		currentToolPageMap.clear();
		currentToolPageMap = null;
	}

	/**
	 * Override to define what should happen if the 
	 * system is notified that plot types are likely
	 * to be of a certain type.
	 * 
	 * @param image
	 */
	public void setDefaultPlotType(PlotType image) {
		//TODO
	}

	public boolean isXfirst() {
		return xfirst;
	}

	public void setXfirst(boolean xfirst) {
		this.xfirst = xfirst;
	}

	/**
	 * Call this method to retrieve what is currently plotted.
	 * See all ITraceListener.
	 * 
	 * @return
	 */
	public Collection<ITrace> getTraces() {
		return null; // TODO
	}

	private List<ITraceListener> traceListeners;
	
	/**
	 * Call to be notified of events which require the plot
	 * data to be sent again.
	 * 
	 * @param l
	 */
	public void addTraceListener(final ITraceListener l) {
		if (traceListeners==null) traceListeners = new ArrayList<ITraceListener>(7);
		traceListeners.add(l);
	}
	
	/**
	 * Call to be notified of events which require the plot
	 * data to be sent again.
	 * 
	 * @param l
	 */
	public void removeTraceListener(final ITraceListener l) {
		if (traceListeners==null) return;
		traceListeners.remove(l);
	}
	
	public void fireTracesAltered(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesAltered(evt);
		}
	}
	protected void fireTraceCreated(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceCreated(evt);
		}
	}
	protected void fireTraceAdded(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceAdded(evt);
		}
	}
	protected void fireTraceRemoved(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceRemoved(evt);
		}
	}

	protected void fireTracesCleared(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesCleared(evt);
		}
	}
	
	public void fireTracesPlotted(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesPlotted(evt);
		}
	}

	/**
	 * Implement to turn off any actions relating to data set choosing
	 * @param b
	 */
	public void setDatasetChoosingRequired(boolean b) {
		//TODO
	}
	
	/**
	 * Override this method to provide an implementation of title setting.
	 * @param title
	 */
	public void setTitle(final String title) {
		//TODO
	}
	
	/**
	 * Please override if you allow your plotter to create images
	 * @param size
	 * @return
	 */
	public Image getImage(Rectangle size) {
		return null;
	}
	
	@Override
	public void append( final String           dataSetName, 
			            final Number           xValue,
					    final Number           yValue,
					    final IProgressMonitor monitor) throws Exception {
		//TODO
		throw new Exception("updatePlot not implemented for "+getClass().getName());
	}
	
	@Override
	public void repaint() {
		//TODO
	}
	
	protected IWorkbenchPart part;
	
	/**
	 * NOTE This field is partly deprecated. It is only
	 * used for the initial plot and plots after that now
	 * have specific methods for 1D, 2D etc.
	 */
	protected PlotType       defaultPlotType;
	
	/**
	 * This simply assigns the part, subclasses should override this
	 * and call super.createPlotPart(...) to assign the part.
	 */
	@Override
	public void createPlotPart(final Composite      parent,
							   final String         plotName,
							   final IActionBars    bars,
							   final PlotType       hint,
							   final IWorkbenchPart part) {

		this.defaultPlotType = hint;
		this.part = part;
		this.bars = bars;
		
		if (part!=null) {
			@SuppressWarnings("unchecked")
			final ISystemService<IPlottingSystem> service = (ISystemService<IPlottingSystem>)PlatformUI.getWorkbench().getService(ISystemService.class);
			if (service!=null) {
				service.putSystem(part.getTitle(), this);
				logger.debug("Plotting sytem for '"+part.getTitle()+"' registered.");
			}
		}
	}
		
	@Override
	public IAxis createAxis(final String title, final boolean isYAxis, int side) {
		//TODO
		throw new RuntimeException("Cannot create an axis with "+getClass().getName());
	}
	
	/**
	 * The current y axis to plot to. Intended for 1D plotting with multiple axes.
	 * @return
	 */
	@Override
	public IAxis getSelectedYAxis(){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}
	
	/**
	 * Set the current plotting yAxis. Intended for 1D plotting with multiple axes.
	 * @param yAxis
	 */
	@Override
	public void setSelectedYAxis(IAxis yAxis){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}
	
	/**
	 * The current x axis to plot to. Intended for 1D plotting with multiple axes.
	 * @return
	 */
	@Override
	public IAxis getSelectedXAxis(){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}
	
	/**
	 * Set the current plotting xAxis. Intended for 1D plotting with multiple axes.
	 * @param xAxis
	 */
	@Override
	public void setSelectedXAxis(IAxis xAxis){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}
	
	protected PlottingSelectionProvider selectionProvider;
	
	public ISelectionProvider getSelectionProvider() {
		if (selectionProvider==null) selectionProvider = new PlottingSelectionProvider();
		return selectionProvider;
	}
	
	private Collection<IRegionListener> regionListeners;
	
	/**
	 * Creates a selection region by type. This does not create any user interface
	 * for the region. You can then call methods on the region to set color and 
	 * position for the selection. Use addRegion(...) and removeRegion(...) to control
	 * if the selection is active on the graph.
	 * 
	 * @param name
	 * @param regionType
	 * @return
	 */
	@Override
	public IRegion createRegion(final String name, final RegionType regionType)  throws Exception {
		//TODO Please implement creation of region here.
		return null;
	}
	
	protected void fireRegionCreated(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) l.regionCreated(evt);
	}
	
	/**
	 * Add a selection region to the graph.
	 * @param region
	 */
	@Override
	public void addRegion(final IRegion region) {
		fireRegionAdded(new RegionEvent(region));
	}
	protected void fireRegionAdded(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) l.regionAdded(evt);
	}
	
	
	/**
	 * Remove a selection region to the graph.
	 * @param region
	 */
	@Override
	public void removeRegion(final IRegion region) {
		fireRegionRemoved(new RegionEvent(region));
	}
	public void clearRegions() {
		//TODO
	}
	
	/**
	 * Get a region by name.
	 * @param name
	 * @return
	 */
	@Override
	public IRegion getRegion(final String name) {
		return null; // TODO
	}
	/**
	 * Get a region by name.
	 * @param name
	 * @return
	 */
	@Override
	public Collection<IRegion> getRegions() {
		return null; // TODO
	}

	protected void fireRegionRemoved(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) l.regionRemoved(evt);
	}

	/**
	 * 
	 * @param l
	 */
	public boolean addRegionListener(final IRegionListener l) {
		if (regionListeners == null) regionListeners = new HashSet<IRegionListener>(7);
		return regionListeners.add(l);
	}
	
	/**
	 * 
	 * @param l
	 */
	public boolean removeRegionListener(final IRegionListener l) {
		if (regionListeners == null) return true;
		return regionListeners.remove(l);
	}
	
	
	/**
	 * Creates an annotation. This does not create any user interface
	 * for the annotation. You can then call methods on the annoation.
	 * Use addAnnotation(...) and removeAnnotation(...) to control
	 * if the selection is active on the graph.
	 * 
	 * @param name
	 * @param regionType
	 * @return
	 * @throws Exception if name exists already.
	 */
	@Override
	public IAnnotation createAnnotation(final String name) throws Exception {
		return null;//TODO 
	}
	
	/**
	 * Add an annotation to the graph.
	 * @param region
	 */
	@Override
	public void addAnnotation(final IAnnotation region) {
		//TODO 
	}
	
	
	/**
	 * Remove an annotation to the graph.
	 * @param region
	 */
	@Override
	public void removeAnnotation(final IAnnotation region) {
		//TODO 
	}
	
	/**
	 * Get an annotation by name.
	 * @param name
	 * @return
	 */
	@Override
	public IAnnotation getAnnotation(final String name) {
		return null;
	}

	/**
	 * Remove all annotations
	 */
	@Override
	public void clearAnnotations(){
		//TODO 
	}

	private Map<ToolPageRole, IToolPage> currentToolPageMap;
	private Collection<IToolChangeListener> toolChangeListeners;

	/**
	 * Get the current tool page that the user would like to use.
	 * Fitting, profile, derivative etc. Null if no selection has been made.
	 * @return
	 */
	@Override
	public IToolPage getCurrentToolPage(ToolPageRole role) {
		
		IToolPage toolPage = currentToolPageMap.get(role);
		if (toolPage==null) {
			toolPage = getEmptyTool();
			currentToolPageMap.put(role, toolPage);
		}
		return toolPage;
	}
	
	protected void setCurrentToolPage(IToolPage page) {
		currentToolPageMap.put(page.getToolPageRole(), page);
	}
	
	/**
	 * Add a tool change listener. If the user changes preferred tool
	 * this listener will be called so that any views showing the current
	 * tool are updated.
	 * 
	 * @param l
	 */
	@Override
	public void addToolChangeListener(IToolChangeListener l) {
		if (toolChangeListeners==null) toolChangeListeners = new HashSet<IToolChangeListener>(7);
		toolChangeListeners.add(l);
	}
	
	/**
	 * Remove a tool change listener if one has been addded.
	 * @param l
	 */
	@Override
	public void removeToolChangeListener(IToolChangeListener l) {
		if (toolChangeListeners==null) return;
		toolChangeListeners.remove(l);
	}
	
	protected void fireToolChangeListeners(final ToolChangeEvent evt) {
		if (toolChangeListeners==null) return;
		
		if (evt.getOldPage()!=null) evt.getOldPage().deactivate();
		if (evt.getNewPage()!=null) evt.getNewPage().activate();
		
	    for (IToolChangeListener l : toolChangeListeners) {
			l.toolChanged(evt);
		}
	}
	
	private EmptyTool emptyTool;

	protected EmptyTool getEmptyTool() {
		
		if (emptyTool==null) {
			emptyTool = new EmptyTool();
			emptyTool.setToolSystem(this);
			emptyTool.setPlottingSystem(this);
			emptyTool.setTitle("No tool");
			emptyTool.setPart(part);
		}
		return emptyTool;
	}

	protected void clearRegionTool() {
		// TODO Implement to clear any region tool which the plotting system may be adding if createRegion(...) has been called.
	}

	/**
	 * Creates a line trace used for 1D plotting.
	 * @param traceName
	 * @return
	 */
	@Override
	public ILineTrace createLineTrace(String traceName) {
		// TODO
		return null;
	}
	/**
	 * Creates an image trace used for 1D plotting.
	 * @param traceName
	 * @return
	 */
	@Override
	public IImageTrace createImageTrace(String traceName) {
		// TODO
		return null;
	}
	
	@Override
	public ITrace getTrace(String name) {
		// TODO
		return null;
	}


	/**
	 * Adds trace, makes visible
	 * @param traceName
	 * @return
	 */
	@Override
	public void addTrace(ITrace trace) {
		// TODO
		fireTraceAdded(new TraceEvent(trace));
	}
	/**
	 * Removes a trace.
	 * @param traceName
	 * @return
	 */
	@Override
	public void removeTrace(ITrace trace) {
		// TODO
		fireTraceRemoved(new TraceEvent(trace));
	}

	protected IWorkbenchPart getPart() {
		return part;
	}

	/**
	 * 
	 * @return true if some or all of the plotted data is 2D or images.
	 */
	@Override
	public boolean is2D() {
		final Collection<ITrace> traces = getTraces();
		if (traces==null) return false;
		for (ITrace iTrace : traces) {
			if (iTrace instanceof IImageTrace) return true;
		}
		return false;
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.dawb.common.ui.plot.IAxisSystem#autoscaleAxes()
	 */
	@Override
	public void autoscaleAxes() {
		// TODO Does nothing
	}
	
	/**
	 * Call this method to retrieve what is currently plotted by trace type
	 * See all ITraceListener.
	 * 
	 * @return
	 */
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		final Collection<ITrace> traces = getTraces();
		if (traces==null) return null;
		
		final Collection<ITrace> ret= new ArrayList<ITrace>();
		for (ITrace trace : traces) {
			if (clazz.isInstance(trace)) {
				ret.add(trace);
			}
		}
		
		return ret; // may be empty
	}

	/**
	 * @return IActionBars, may be null
	 */
	public IActionBars getActionBars() {
		return bars;
	}


	public void setFocus() {
		if (getPlotComposite()!=null) getPlotComposite().setFocus();
	}
	
	public boolean  isDisposed() {
		return getPlotComposite().isDisposed();
	}
	
	public boolean setToolVisible(final String toolId, final ToolPageRole role, final String viewId) throws Exception {
		return actionBarManager.setToolVisible(toolId, role, viewId);
	}
}
