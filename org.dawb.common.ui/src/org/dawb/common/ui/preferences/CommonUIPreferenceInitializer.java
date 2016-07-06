/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.preferences;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;

import org.dawb.common.ui.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 *
 */
public class CommonUIPreferenceInitializer extends AbstractPreferenceInitializer {

	private Collection<String> integerPrefs;
	private Collection<String> booleanPrefs;
	
	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		booleanPrefs = new HashSet<String>(7);
		store.setDefault(CommonUIPreferenceConstants.DASHBOARD_BOUNDS,       false);
		booleanPrefs.add(CommonUIPreferenceConstants.DASHBOARD_BOUNDS);
		store.setDefault(CommonUIPreferenceConstants.DASHBOARD_DESCRIPTION,  false);
		booleanPrefs.add(CommonUIPreferenceConstants.DASHBOARD_DESCRIPTION);

		store.setDefault(CommonUIPreferenceConstants.DASHBOARD_FORMAT, "#0.00");
		
		// Server ocnfiguration
		integerPrefs = new HashSet<String>(7);
		store.setDefault(CommonUIPreferenceConstants.MOCK_SESSION,   true);
		store.setDefault(CommonUIPreferenceConstants.SERVER_NAME, getTangoHostName());
		store.setDefault(CommonUIPreferenceConstants.SERVER_PORT, getTangoHostPort());
		integerPrefs.add(CommonUIPreferenceConstants.SERVER_PORT);
		store.setDefault(CommonUIPreferenceConstants.BEAMLINE_NAME, getBeamlineName());
		store.setDefault(CommonUIPreferenceConstants.SPEC_NAME,     "SPEC");
	}


	private String getTangoHostName() {
		String host = System.getenv("TANGO_HOST");
		if (host!=null) return host.split(":")[0];
		
		try {
		    return InetAddress.getLocalHost().getHostName();
		} catch (Exception ne) {
			return "<unknown>";
		}
	}


	private int getTangoHostPort() {
		String host = System.getenv("TANGO_HOST");
		if (host!=null) return Integer.parseInt(host.split(":")[1]);
		
		return 20000;
	}


	private String getBeamlineName() {
		
		String name = System.getenv("BEAMLINENAME");
		if (name!=null) return name.toLowerCase();
		return "<unknown>";		
	}


	@SuppressWarnings("unused")
	private boolean isInt(String name) {
		return integerPrefs.contains(name);
	}

	@SuppressWarnings("unused")
	private boolean isBoolean(String name) {
		return booleanPrefs.contains(name);
	}
	
}
