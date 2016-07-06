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
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 *
 */
public class TangoPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public static final String ID = "org.dawb.common.ui.views.tangoPreferences";
	
	private BooleanFieldEditor mockMode;
	private StringFieldEditor  serverEd,beamlineName,specSessionName;
	private IntegerFieldEditor portEd;

	/**
	 * 
	 */
	public TangoPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for talking to a tango server.");
	}
	
	@Override
	protected void createFieldEditors() {
		
		mockMode = new BooleanFieldEditor(CommonUIPreferenceConstants.MOCK_SESSION, "Mock Connection", getFieldEditorParent());
		addField(mockMode);
		mockMode.setPreferenceName(CommonUIPreferenceConstants.MOCK_SESSION);
		
		serverEd = new StringFieldEditor(CommonUIPreferenceConstants.SERVER_NAME, "Database Name", getFieldEditorParent());
		serverEd.setEmptyStringAllowed(false);
		addField(serverEd);
				
		portEd = new IntegerFieldEditor(CommonUIPreferenceConstants.SERVER_PORT, "Database Port", getFieldEditorParent());
		portEd.setEmptyStringAllowed(false);
		addField(portEd);
		
		beamlineName = new StringFieldEditor(CommonUIPreferenceConstants.BEAMLINE_NAME, "Beamline Name", getFieldEditorParent());
		beamlineName.setEmptyStringAllowed(false);
		addField(beamlineName);

		specSessionName = new StringFieldEditor(CommonUIPreferenceConstants.SPEC_NAME, "Spec Session Name", getFieldEditorParent());
		specSessionName.setEmptyStringAllowed(false);
		addField(specSessionName);
	
		setValid(true);
		
	}
	
	protected void initialize() {
		super.initialize();
		updateMockEnabled();
		mockMode.getDescriptionControl(getFieldEditorParent()).setToolTipText("In mock mode no connection is made to tango and instead a local value repository is used. This mode applies to all tango connections from the workbench, workflow actors, spec console, monitoring etc.");
	}
	
	private void updateMockEnabled() {
		serverEd.setEnabled(!mockMode.getBooleanValue(), getFieldEditorParent());
		portEd.setEnabled(!mockMode.getBooleanValue(), getFieldEditorParent());
		beamlineName.setEnabled(!mockMode.getBooleanValue(), getFieldEditorParent());
		specSessionName.setEnabled(!mockMode.getBooleanValue(), getFieldEditorParent());
	}

	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		
		if (getFieldEditorParent()==null||getFieldEditorParent().isDisposed()) return;
		
		if (event.getSource() == mockMode) {
			updateMockEnabled();
			System.setProperty("org.dawb.test.session", new Boolean(mockMode.getBooleanValue()).toString());
		}

	}
	
	@Override
	protected void checkState() {
		
		super.checkState();
		
		try {
			//new Database(serverEd.getStringValue(), ""+portEd.getIntValue());
			
		} catch (Throwable ne) {
			//setErrorMessage("Cannot connect to "+serverEd.getStringValue()+":"+portEd.getIntValue());
			return;
		}
		
		if (beamlineName.getStringValue()==null || "".equals(beamlineName.getStringValue())) {
			setErrorMessage("Please set a beamline name to be used in the url to tango devices.");
			return;
		}
		
		if (specSessionName.getStringValue()==null || "".equals(specSessionName.getStringValue())) {
			setErrorMessage("Please set a spec session name to be used in the url to tango spec session.");
			return;
		}
		
		
		setErrorMessage(null);
		setValid(true);
		
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}
