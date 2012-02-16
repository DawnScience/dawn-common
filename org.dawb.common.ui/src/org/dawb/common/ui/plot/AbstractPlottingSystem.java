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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * The base class for IPlottingSystem. NOTE some methods that should be implemented
 * throw exceptions if they are called. They should be overriden.
 * Some methods that should be implemented do nothing.
 * 
 * There are TODO tags added to provide information as to where these optional
 * methods to override are.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractPlottingSystem implements IPlottingSystem {
	
	
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
		
		if (plotListeners!=null) plotListeners.clear();
		plotListeners = null;
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

	private List<IPlotUpdateListener> plotListeners;
	
	/**
	 * Call to be notified of events which require the plot
	 * data to be sent again.
	 * 
	 * @param l
	 */
	public void addPlotListener(final IPlotUpdateListener l) {
		if (plotListeners==null) plotListeners = new ArrayList<IPlotUpdateListener>(7);
		plotListeners.add(l);
	}
	
	protected void firePlotListeners(final PlotUpdateEvent evt) {
		if (plotListeners==null) return;
		for (IPlotUpdateListener l : plotListeners) {
			l.plotRequested(evt);
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
	
	@Override
	public void createPlot1D(AbstractDataset       x, 
							List<AbstractDataset> ys,
							IProgressMonitor      monitor) {
		createPlot(x,ys,PlotType.PT1D, monitor);
	}


	@Override
	public void createPlot2D(AbstractDataset      image, 
							List<AbstractDataset> axes,
							IProgressMonitor      monitor) {
		createPlot(image,axes,PlotType.IMAGE, monitor);
	}

	protected abstract void createPlot(final AbstractDataset       data, 
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
}
