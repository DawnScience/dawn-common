/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

public class AppliableTableViewer extends TableViewer {

	public AppliableTableViewer(Composite parent, int style) {
		super(parent, style);
	}
	public void applyEditorValue() {
		super.applyEditorValue();
	}		
}
