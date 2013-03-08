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


import org.dawb.common.services.IVariableManager;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * An interface used to specify how expressions are evaluated
 * @author fcp94556
 *
 */
public interface IExpressionPlottingManager extends IVariableManager{

	/**
	 * A data set which can be used without loading the data
	 * @param name
	 * @param monitor
	 * @return
	 */
	public ILazyDataset getLazyDataSet(String name, IMonitor monitor);

	/**
	 * Return data set for name
	 * @param name
	 * @param monitor
	 * @return
	 */
	public AbstractDataset getDataSet(final String name, final IMonitor monitor);


	/**
	 * Test if data set name.
	 * @param name
	 * @param monitor
	 * @return
	 */
	public boolean isDataSetName(String name, IMonitor monitor);

	/**
	 * May return null, if data not plotting
	 * @return
	 */
	public IPlottingSystem getPlottingSystem();

	/**
	 * Delete selected expression, if any
	 */
	public void deleteExpression();


	/**
	 * Create a plottable dataset from an expression.
	 * Normally is implemented to add an item to a table and make it editable to recieve the expression.
	 */
	public void addExpression();

	/**
	 * 
	 * @return the path to the original data if there is a file path.
	 */
	public String getFilePath();


}
