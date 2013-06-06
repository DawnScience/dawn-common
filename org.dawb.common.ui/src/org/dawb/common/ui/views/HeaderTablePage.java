/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;

/**
 * This class used as a page in the Data page view.
 * @author fcp94556
 *
 */
public class HeaderTablePage extends Page implements IAdaptable{

	
	private Composite control;
	private String    filePath;
	
	public HeaderTablePage(final String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void createControl(Composite parent) {
		
		this.control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1,false));
		final HeaderTableView view = new HeaderTableView(false);
        view.createPartControl(control);
        view.updatePath(filePath);
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}
	
	public void dispose() {
		control.dispose();
		super.dispose();
	}
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
		if (type == String.class) {
			return "Metadata";
		}
		return null;
	}
}
