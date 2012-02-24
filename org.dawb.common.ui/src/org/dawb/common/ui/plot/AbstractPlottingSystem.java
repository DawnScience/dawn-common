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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.annotation.IAnnotation;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.tool.IToolChangeListener;
import org.dawb.common.ui.plot.tool.IToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.tool.ToolChangeEvent;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * The base class for IPlottingSystem. NOTE some methods that should be implemented
 * throw exceptions if they are called. They should be overriden.
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
	
	public static enum ColorOption {
		BY_DATA, BY_NAME, NONE
	}
	
	protected boolean rescale = true;

	// True if first data set should be plotted as x axis
	protected boolean xfirst = true;
	
	// Extrac actions for 1D and image viewing
	protected List<IAction> extraImageActions;
	protected List<IAction> extra1DActions;
	
	// Feedback for plotting, if needed
	protected Text      pointControls;
	
	// Color option for 1D plots, if needed.
	protected ColorOption colorOption=ColorOption.BY_DATA;

	public void setPointControls(Text pointControls) {
		this.pointControls = pointControls;
	}

    protected String   rootName;
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
	 * Wether the plot should rescale when replotted.
	 * @return rescale
	 */	public boolean isRescale() {
		return rescale;
	}

	public List<IAction> getExtraImageActions() {
		return extraImageActions;
	}

	public void setExtraImageActions(List<IAction> extraImageActions) {
		this.extraImageActions = extraImageActions;
	}

	public List<IAction> getExtra1DActions() {
		return extra1DActions;
	}

	public void setExtra1DActions(List<IAction> extra1dActions) {
		extra1DActions = extra1dActions;
	}

	
	public void dispose() {
		
		if (extraImageActions!=null) extraImageActions.clear();
		extraImageActions = null;
		
		if (extra1DActions!=null) extra1DActions.clear();
		extra1DActions = null;
		
		if (traceListeners!=null) traceListeners.clear();
		traceListeners = null;
		pointControls = null;
		
		if (selectionProvider!=null) selectionProvider.clear();
		selectionProvider = null;
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
	
	protected void fireTracesAltered(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesAltered(evt);
		}
	}
	protected void fireTracesCleared(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesCleared(evt);
		}
	}
	protected void fireTracePlotted(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracePlotted(evt);
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
	
	public void append( final String           dataSetName, 
			            final Number           xValue,
					    final Number           yValue,
					    final IProgressMonitor monitor) throws Exception {
		//TODO
		throw new Exception("updatePlot not implemented for "+getClass().getName());
	}
	
	/**
	 * Please update to repaint the plotting.
	 */
	public void repaint() {
		//TODO
	}
	
	protected IWorkbenchPart part;
	
	/**
	 * This simply assigns the part, subclasses should override this
	 * and call super.createPlotPart(...) to assign the part.
	 */
	public void createPlotPart(final Composite      parent,
							   final String         plotName,
							   final IActionBars    bars,
							   final PlotType       hint,
							   final IWorkbenchPart part) {

		this.part = part;
	}
	
	@Override
	public List<ITrace> createPlot1D(AbstractDataset       x, 
							List<AbstractDataset> ys,
							IProgressMonitor      monitor) {
		return createPlot(x,ys,PlotType.PT1D, monitor);
	}


	@Override
	public ITrace createPlot2D(AbstractDataset      image, 
							List<AbstractDataset> axes,
							IProgressMonitor      monitor) {
		List<ITrace> traces = createPlot(image,axes,PlotType.IMAGE, monitor);
		if (traces!=null) return traces.get(0);
		return null;
	}

	protected abstract List<ITrace> createPlot(final AbstractDataset       data, 
							            final List<AbstractDataset> axes,
							            final PlotType              mode, 
							            final IProgressMonitor      monitor);
	
	
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
	public IRegion getRegion(final String name) {
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
	public IAnnotation createAnnotation(final String name) throws Exception {
		return null;//TODO 
	}
	
	/**
	 * Add an annotation to the graph.
	 * @param region
	 */
	public void addAnnotation(final IAnnotation region) {
		//TODO 
	}
	
	
	/**
	 * Remove an annotation to the graph.
	 * @param region
	 */
	public void removeAnnotation(final IAnnotation region) {
		//TODO 
	}
	
	/**
	 * Get an annotation by name.
	 * @param name
	 * @return
	 */
	public IAnnotation getAnnotation(final String name) {
		return null;
	}

	/**
	 * Remove all annotations
	 */
	public void clearAnnotations(){
		//TODO 
	}

	private IToolPage currentToolPage;
	private Collection<IToolChangeListener> toolChangeListeners;
	/**
	 * Get the current tool page that the user would like to use.
	 * Fitting, profile, derivative etc. Null if no selection has been made.
	 * @return
	 */
	public IToolPage getCurrentToolPage() {
		return currentToolPage;
	}
	
	protected void setCurrentToolPage(IToolPage page) {
		this.currentToolPage = page;
	}
	
	/**
	 * Add a tool change listener. If the user changes preferred tool
	 * this listener will be called so that any views showing the current
	 * tool are updated.
	 * 
	 * @param l
	 */
	public void addToolChangeListener(IToolChangeListener l) {
		if (toolChangeListeners==null) toolChangeListeners = new HashSet<IToolChangeListener>(7);
		toolChangeListeners.add(l);
	}
	
	/**
	 * Remove a tool change listener if one has been addded.
	 * @param l
	 */
	public void removeToolChangeListener(IToolChangeListener l) {
		if (toolChangeListeners==null) return;
		toolChangeListeners.remove(l);
	}
	
	protected void fireToolChangeListeners(final ToolChangeEvent evt) {
		if (toolChangeListeners==null) return;
	    for (IToolChangeListener l : toolChangeListeners) {
			l.toolChanged(evt);
		}
	}

	/**
	 * Return a MenuAction which can be attached to the part using the plotting system.
	 * 
	 * 
	 * @return
	 */
	protected MenuAction createToolActions() throws Exception {
		
		final MenuAction toolActions = new MenuAction("Switch plotting tool.");
		toolActions.setId("org.dawb.common.ui.plot.toolActions");
	       		
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
	    for (IConfigurationElement e : configs) {
			
	    	final String    label = e.getAttribute("label");
	    	final IToolPage page  = (IToolPage)e.createExecutableExtension("class");
	    	page.setToolSystem(this);
	    	page.setPlottingSystem(this);
	    	page.setTitle(label);
	    	
	    	final Action    action= new Action(label) {
	    		public void run() {		
	    			
	    			try {
						EclipseUtils.getActivePage().showView("org.dawb.workbench.plotting.views.ToolPageView");
					} catch (PartInitException e) {
						logger.error("Cannot find a view with id org.dawb.workbench.plotting.views.ToolPageView", e);
					}
	    			final IToolPage old = getCurrentToolPage();
	    			setCurrentToolPage(page);
	    			fireToolChangeListeners(new ToolChangeEvent(this, old, page, part));
	    			
	    			toolActions.setSelectedAction(this);
	    		}
	    	};
	    	
	    	action.setId(e.getAttribute("id"));
	    	final String   icon  = e.getAttribute("icon");
	    	if (icon!=null) {
		    	final String   id    = e.getContributor().getName();
		    	final Bundle   bundle= Platform.getBundle(id);
		    	final URL      entry = bundle.getEntry(icon);
		    	final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
		    	action.setImageDescriptor(des);
	    	}
	    	
	    	final String    tooltip = e.getAttribute("tooltip");
	    	if (tooltip!=null) action.setToolTipText(tooltip);
	    	
	    	toolActions.add(action);
		}
	
    	final Action    clear = new Action("No plotting tool") {
    		public void run() {		
    			
    			final IToolPage old = getCurrentToolPage();
       			setCurrentToolPage(null);
    			fireToolChangeListeners(new ToolChangeEvent(this, old, null, part));
     			
    			toolActions.setSelectedAction(this);
    		}
    	};
    	clear.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
    	clear.setToolTipText("No plotting tool");
	    toolActions.add(clear);
	    toolActions.setSelectedAction(clear);
	    return toolActions;
	}
}
