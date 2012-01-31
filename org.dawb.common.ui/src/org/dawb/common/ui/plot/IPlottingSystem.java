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
	 * NOTE Using this option plots everything on the current x and y axes. These are the default axes,
	 * to change axes, use createAxis(...) then setActiveAxis(...). Then subsequent plotting will plot
	 * to these active axes.
	 * 
	 * Does not have to be called in the UI thread - any thread will do. Should be called to switch the entire
	 * plot contents.
	 * 
	 * There is append for 1D plotting, @see IPlottingSystem.append(...)
	 * 
	 * @param x
	 * @param ys
	 * @param mode
	 * @param monitor
	 */
	public void createPlot1D(AbstractDataset       x, 
							 List<AbstractDataset> ys,
							 IProgressMonitor      monitor);
	
	/**
	 * For 2D - x is the image dataset, ys is the axes.
	 * 
	 * Does not have to be called in the UI thread. Should be called to switch the entire
	 * plot contents. 
	 * 
	 * @param image
	 * @param axes
	 * @param mode
	 * @param monitor
	 */
	public void createPlot2D(AbstractDataset       image, 
							 List<AbstractDataset> axes,
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
	 * plottingSystem.append("y", y.getSize()+1, 10, mon);
	 * 
	 * Call this update method in any thread, it will do the update in the UI thread
	 * if not already called in the UI thread.
	 * 
	 * @param dataSetName
	 * @param xValue - may be null if using indices
	 * @param yValue
	 * @param monitor - often null, this will be a fast operation.
	 * @throws Exception
	 */
	public void append(final String           dataSetName, 
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
	
	/**
	 * Use this method to create axes other than the default y and x axes.
	 * @param title
	 * @param isYAxis, normally it is.
	 * @return
	 */
	public IAxis createAxis(final String title, final boolean isYAxis);
	
	/**
	 * The current y axis to plot to. Intended for 1D plotting with multiple axes.
	 * @return
	 */
	public IAxis getSelectedYAxis();
	
	/**
	 * Set the current plotting yAxis. Intended for 1D plotting with multiple axes.
	 * @param yAxis
	 */
	public void setSelectedYAxis(IAxis yAxis);
	
	/**
	 * The current x axis to plot to. Intended for 1D plotting with multiple axes.
	 * @return
	 */
	public IAxis getSelectedXAxis();
	
	/**
	 * Set the current plotting xAxis. Intended for 1D plotting with multiple axes.
	 * @param xAxis
	 */
	public void setSelectedXAxis(IAxis xAxis);
	

}
