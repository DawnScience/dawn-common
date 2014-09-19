/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.preferences;

import org.dawb.common.ui.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class ViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.edna.workbench.views.preferencePage";
	
	/**
	 * @wbp.parser.constructor
	 */
	public ViewPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for image monitoring.");
	}
	
	@Override
	protected void createFieldEditors() {
		
     	final IntegerFieldEditor imageSize = new IntegerFieldEditor(ViewConstants.IMAGE_SIZE, "Thumbnail Image Size", getFieldEditorParent());
     	imageSize.setValidRange(32, 512);
		addField(imageSize);
		imageSize.getTextControl(getFieldEditorParent()).setToolTipText("The thumbnail is square and this field is the size of one side of the thumbnail generated in display pixels.");
		
     	final IntegerFieldEditor pollRate = new IntegerFieldEditor(ViewConstants.POLL_RATE, "Poll Rate (s)", getFieldEditorParent());
     	pollRate.setValidRange(1, 60);
		addField(pollRate);
		pollRate.getTextControl(getFieldEditorParent()).setToolTipText("The image folder monitored is refreshed at this interval, in seconds (the larger the value the slower the refresh). Polling cannot be done at a higher rate than 1Hz.");
	}
	


	@Override
	public void init(IWorkbench workbench) {
		
		
	}
	
}
