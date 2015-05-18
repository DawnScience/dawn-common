/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.preferences;

import org.dawb.common.ui.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class EventTrackerPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		// set enable/disable tracking, if no system property is set then it will be set to false
		String defaultValue = System.getProperty(BasePlottingConstants.IS_TRACKER_ENABLED);
		boolean def = Boolean.valueOf(defaultValue);
		store.setDefault(BasePlottingConstants.IS_TRACKER_ENABLED, def);
		System.setProperty(BasePlottingConstants.IS_TRACKER_ENABLED, String.valueOf(def));
		// set analytics tracking code
		store.setDefault(BasePlottingConstants.ANALYTICS_TRACK_CODE, "UA-48311061-3");
		System.setProperty(BasePlottingConstants.ANALYTICS_TRACK_CODE, "UA-48311061-3");
	}

}