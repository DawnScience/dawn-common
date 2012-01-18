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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;


/**
 * Represents a bridge to the plotting system.
 * 
 * Currently this is the diamond hardware accelerated system
 * or the fable directly drawn one.
 * 
 * To get your plotting system use the class PlottingFactory. This
 * will return the user preferred plotting.
 * 
 * @author gerring
 *
 */
public interface IPlottingSystem {

	/**
	 * Call to create the UI component dealing with the plotting.
	 * @param right
	 */
	public void createPlotPart(Composite      parent,
			                   String         plotName,
			                   IActionBars    bars,
			                   PlotType       hint,
			                   IWorkbenchPart part);

	
	/**
	 * For 1D - x is the x axis, ys is the y traces or null if only 1 data set should be plotted.
	 * NOTE Using this option plots everything on the default x and y axes.
	 * 
	 * For 2D - x is the image, ys is the axes.
	 * 
	 * Does not have to be called in the UI thread. May be called to update as well as the entire
	 * plot contents will be switched. The plot may also change mode.
	 * 
	 * @param sum
	 * @param axes
	 * @param mode
	 * @param monitor
	 */
	public void createPlot( AbstractDataset       x, 
							List<AbstractDataset> ys,
							PlotType              mode, 
							IProgressMonitor      monitor);
	
	/**
	 * This method can be used to add a single plot data point to 
	 * an individual 1D plot already created in createPlot(...). The dataSetName
	 * argument is the same as the name of the original data set plotted,
	 * the data is added to this plot efficiently.
	 * 
	 * Example of starting a plot with nothing and then adding points:
	 * 
	 * final AbstractDataset y = new DoubleDataset(new double[]{}, 0}
	 * y.setName("y")
	 * 
	 * plottingSystem.createPlot(y, null, PlotType.PT1D, mon);
	 * 
	 * ...Update, x value indices in this case
	 * plottingSystem.add("y", y.getSize()+1, 10, mon);
	 * 
	 * Call this update method in any thread, it will do the update in the UI thread
	 * if not already called in the UI thread.
	 * 
	 * Use redrawStraightAway to not draw if updating many data sets in a loop and
	 * then later call repaint() to draw all data.
	 * 
	 * @param dataSetName
	 * @param xValue - may be null if using indices
	 * @param yValue
	 * @param monitor - often null, this will be a fast operation.
	 * @throws Exception
	 */
	public void append( final String           dataSetName, 
			            final Number           xValue,
					    final Number           yValue,
					    final IProgressMonitor monitor) throws Exception ;


	/**
	 * Call to tell the plot to plot nothing.
	 */
	public void reset();


	/**
	 * Call to mark widgets and plotting as no longer required.
	 * 
	 * This will be called when the part is disposed.
	 */
	public void dispose();


	/**
	 * Redraws all the data
	 */
	public void repaint();

}
