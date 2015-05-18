package org.dawb.common.ui.preferences;

import org.dawb.common.ui.Activator;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EventTrackerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor isTrackerEnabled;
	private StringFieldEditor googleTrackCode;
	private IPreferenceStore store;
	private IPropertyChangeListener listener;

	public EventTrackerPreferencePage() {
		super();
		store = Activator.getDefault().getPreferenceStore();
		listener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == BasePlottingConstants.IS_TRACKER_ENABLED) {
					System.setProperty(
							BasePlottingConstants.IS_TRACKER_ENABLED,
							String.valueOf((Boolean) event.getNewValue()));
				} else if (event.getProperty() == BasePlottingConstants.ANALYTICS_TRACK_CODE) {
					System.setProperty(
							BasePlottingConstants.ANALYTICS_TRACK_CODE,
							(String) event.getNewValue());
				}
			}
		};
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(store);
		setDescription("Preferences for the usage tracking in DAWN:");
		store.addPropertyChangeListener(listener);
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

	@Override
	public void dispose() {
		store.removePropertyChangeListener(listener);
		super.dispose();
	}
}
