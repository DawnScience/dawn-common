/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.widgets;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * A "fake" field editor to allow a label to be added in a simple preferences page
 */
public class LabelFieldEditor extends FieldEditor {

	private static int counter = 0; 

	private Label label;
	
	/**
	 * Create the Label
     * @param labelText The text to appear in the label
     * @param parent The parent composite (normally getFieldEditorParent())
     */
    public LabelFieldEditor(String labelText, Composite parent) {
        init("LabelFieldEditor" + counter, labelText);
        counter++;
        createControl(parent);
    }

    @Override
	protected void adjustForNumColumns(int numColumns) {
        GridData gd = (GridData) label.getLayoutData();
        gd.horizontalSpan = numColumns;
     }

    @Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
    	// the label is actually created by the superclass, we just need
    	// to modify it here so that we can set numColumns later
        label = getLabelControl(parent);
        label.setLayoutData(new GridData());
    }

    @Override
	protected void doLoad() {
    }

    @Override
	protected void doLoadDefault() {
    }

    @Override
	protected void doStore() {
    }

    @Override
	public int getNumberOfControls() {
        return 1;
    }
}
