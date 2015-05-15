package org.dawb.common.ui.preferences;

import org.dawb.common.ui.Activator;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EventTrackerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor isTrackerEnabled;
	private StringFieldEditor googleTrackCode;

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

		googleTrackCode = new StringFieldEditor(BasePlottingConstants.ANALYTICS_TRACK_CODE, "Tracking code:", getFieldEditorParent());
		addField(googleTrackCode);
//		Text trackText = googleTrackCode.getTextControl(getFieldEditorParent());
//		trackText.setToolTipText("Google Analytics tracking code");
	}

}
