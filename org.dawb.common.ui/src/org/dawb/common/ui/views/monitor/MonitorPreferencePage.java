/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views.monitor;

import java.text.DecimalFormat;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 *
 */
public class MonitorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public static final String ID = "org.dawb.common.ui.views.dashboard.dashboardPreferences";
	
	private StringFieldEditor formatFieldEditor;

	/**
	 * 
	 */
	public MonitorPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for the dashboard view such as the format for the values and bounds and visibility controls.");
	}
	
	@Override
	protected void createFieldEditors() {
		formatFieldEditor = new StringFieldEditor(CommonUIPreferenceConstants.DASHBOARD_FORMAT, "Dashboard number format", getFieldEditorParent());
		addField(formatFieldEditor);
				
		final BooleanFieldEditor showBounds = new BooleanFieldEditor(CommonUIPreferenceConstants.DASHBOARD_BOUNDS, "Show bounds", getFieldEditorParent());
		addField(showBounds);
		
		final BooleanFieldEditor showDes = new BooleanFieldEditor(CommonUIPreferenceConstants.DASHBOARD_DESCRIPTION, "Show description", getFieldEditorParent());
		addField(showDes);
	}
	
	@Override
	protected void checkState() {
		super.checkState();
		
		try {
			DecimalFormat format = new DecimalFormat(formatFieldEditor.getStringValue());
			format.format(100.001);
		} catch (IllegalArgumentException ne) {
			setErrorMessage("The format '"+formatFieldEditor.getStringValue()+"' is not valid.");
			setValid(false);
			return;
		}
		
		setErrorMessage(null);
		setValid(true);
		
	}

	@Override
	public void init(IWorkbench workbench) {

	}

}
