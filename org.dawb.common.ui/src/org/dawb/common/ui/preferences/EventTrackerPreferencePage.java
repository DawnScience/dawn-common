package org.dawb.common.ui.preferences;

import org.dawb.common.ui.Activator;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EventTrackerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor isTrackerEnabled;

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		setPreferenceStore(store);
		setDescription("Preferences for the usage tracking in DAWN:");
	}

	@Override
	protected void createFieldEditors() {
		isTrackerEnabled = new BooleanFieldEditor(BasePlottingConstants.IS_TRACKER_ENABLED, "Enable usage tracking", getFieldEditorParent());
		addField(isTrackerEnabled);
	}
}
